package com.movienotebook.api.service;

import com.movienotebook.api.dto.movie.SearchMovieRequestDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.integration.KinopoiskIntegrationService;
import com.movienotebook.api.integration.dto.KinopoiskSearchResponseDto;
import com.movienotebook.api.integration.dto.MovieKinopoiskResponseDto;
import com.movienotebook.api.mapper.MovieMapper;
import com.movienotebook.api.repository.MovieRepository;
import com.movienotebook.api.util.PaginationCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
	
	private final KinopoiskIntegrationService kinopoiskIntegrationService;
	private final MovieRepository movieRepository;
	private final MovieMapper movieMapper;
	
	// TODO Вынести в конфигурацию проекта
	//@Value("${app.movies.stale-threshold}")
	private final Duration movieStaleThreshold = Duration.ofHours(24);
	
	private record SearchKinopoiskResult (
			List<MovieKinopoiskResponseDto> allFetchedMovies,
			int searchFilmsCountResult) {}
	
	public Page<Movie> searchMovie(SearchMovieRequestDto request) {
		if (request.deepSearch()) {
			return fetchAndCacheFromIntegration(request.query(), request.page(), request.size());
		} else {
			return getMovies(request);
		}
	}
	
	private Page<Movie> fetchAndCacheFromIntegration(String query, int page, int pageSize) {
		
		PaginationCalculator.PaginationMapping mapping = PaginationCalculator.calculate(page, pageSize);
		
		SearchKinopoiskResult kinopoiskResult = searchKinopoisk(query, mapping);
		
		if (kinopoiskResult.allFetchedMovies().isEmpty()) {
			return Page.empty();
		}
		
		List<Movie> rawMovies = kinopoiskResult.allFetchedMovies()
				.stream()
				.map(this::mapMovie)
				.toList();
		
		List<Movie> savedMovies = saveFetchedMoviesBatch(rawMovies);
		
		int actualEndIndex = Math.min(mapping.relativeEndIndex(), savedMovies.size());
		List<Movie> pageContent = savedMovies.subList(mapping.relativeStartIndex(), actualEndIndex);
		
		PageRequest pageable = PageRequest.of(page - 1, pageSize);
		return new PageImpl<>(pageContent, pageable, kinopoiskResult.searchFilmsCountResult());
	}
	
	private List<Movie> saveFetchedMoviesBatch(List<Movie> moviesToSave) {
		List<Long> externalIds = moviesToSave
				.stream()
				.map(Movie::getExternalId)
				.toList();
		
		// LEARN .stream().collect(...toMap())
		Map<Long, Movie> existingMoviesMap = movieRepository.findAllByExternalIdIn(externalIds)
				.stream()
				.collect(Collectors.toMap(Movie::getExternalId, m -> m));
		
		List<Movie> moviesToUpsert = new ArrayList<>();
		OffsetDateTime threshold = OffsetDateTime.now().minus(movieStaleThreshold);
		
		for (Movie fetchedMovie : moviesToSave) {
			Movie existing = existingMoviesMap.get(fetchedMovie.getExternalId());
			
			if (existing == null) {
				moviesToUpsert.add(fetchedMovie);
			} else if (existing.getLastUpdate().isBefore(threshold)) {
				movieMapper.updateMovieFromExtracted(existing, fetchedMovie);
				moviesToUpsert.add(existing);
			} else {
				moviesToUpsert.add(existing);
			}
		}
		
		return movieRepository.saveAll(moviesToUpsert);
	}
	
	private SearchKinopoiskResult searchKinopoisk(String query, PaginationCalculator.PaginationMapping paginationMapping) {
		
		List<MovieKinopoiskResponseDto> allFetchedMovies = new ArrayList<>();
		
		int searchFilmsCountResult = 0;
		
		for (Integer page : paginationMapping.externalPagesToFetch()) {
			KinopoiskSearchResponseDto kinopoiskResponse = kinopoiskIntegrationService.searchMovie(query, page);
			
			if (kinopoiskResponse.films() != null && !kinopoiskResponse.films().isEmpty()) {
				allFetchedMovies.addAll(kinopoiskResponse.films());
			}
			
			if (kinopoiskResponse.films() == null || kinopoiskResponse.films().size() < 20) {
				break;
			}
			
			if (searchFilmsCountResult == 0) {
				searchFilmsCountResult = kinopoiskResponse.searchFilmsCountResult();
			}
		}
		
		if (paginationMapping.relativeStartIndex() >= allFetchedMovies.size()) {
			return new SearchKinopoiskResult(Collections.emptyList(), 0);
		}
		
		return new SearchKinopoiskResult(allFetchedMovies, searchFilmsCountResult);
	}
	
	private Page<Movie> getMovies(SearchMovieRequestDto request) {
		Sort sort = Sort.by(
				Sort.Order.desc("averageRating"),
				Sort.Order.asc("id")
		);
		
		Pageable pageable = PageRequest.of(
				request.page() - 1,
				request.size(),
				sort);
		
		return movieRepository.findAllByTitleContainingIgnoreCaseOrOriginalTitleContainingIgnoreCase(
				request.query(),
				request.query(),
				pageable);
	}
	
	private Movie mapMovie(MovieKinopoiskResponseDto film) {
		Movie movie = new Movie();
		movie.setExternalId(film.filmId());
		movie.setTitle(resolveTitle(film.nameRu(), film.nameEn()));
		movie.setOriginalTitle(film.nameEn());
		movie.setDescription(film.description());
		movie.setReleaseYear(film.year());
		movie.setPosterUrl(film.posterUrl());
		return movie;
	}
	
	private String resolveTitle(String nameRu, String nameEn) {
		if (nameRu != null && !nameRu.isBlank()) {
			return nameRu;
		}
		if (nameEn != null && !nameEn.isBlank()) {
			return nameEn;
		}
		return "Без названия";
	}
	
	public Movie getById(Long id) {
		return movieRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Фильм с идентификатором " + id + "не найден"));
	}
	
	public Movie getByExternalId(Long id) {
		return movieRepository.findByExternalId(id)
				.orElseThrow(() -> new ResourceNotFoundException("Фильм с внешним идентификатором " + id + " не найден"));
	}
	
	public List<Movie> getByTitle(String title) {
		return movieRepository.findAllByTitleContainingIgnoreCase(title);
	}
	
	public List<Movie> getByOriginalTitle(String title) {
		return movieRepository.findAllByOriginalTitle(title);
	}
	
	@Transactional
	public void save(Movie movie) {
		movieRepository.save(movie);
	}
	
	
}