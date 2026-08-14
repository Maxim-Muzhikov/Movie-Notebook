package com.movienotebook.api.service;

import com.movienotebook.api.dto.movie.MovieResponseDto;
import com.movienotebook.api.dto.movie.SearchMovieRequestDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.integration.KinopoiskFetcher;
import com.movienotebook.api.integration.mapper.KinopoiskMovieMapper;
import com.movienotebook.api.mapper.MovieMapper;
import com.movienotebook.api.repository.MovieRepository;
import com.movienotebook.api.util.PaginationCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
	
	private final MovieRepository movieRepository;
	private final KinopoiskFetcher kinopoiskFetcher;
	private final MovieSyncService movieSyncService;
	private final KinopoiskMovieMapper kinopoiskMovieMapper;
	private final MovieMapper movieMapper;
	
	public Page<MovieResponseDto> searchMovies(SearchMovieRequestDto request) {
		if (!request.deepSearch()) {
			return getMoviesFromLocalDb(request);
		}
		
		PaginationCalculator.KinopoiskPaginationMapping mapping = PaginationCalculator.calculate(request.page(), request.size());
		KinopoiskFetcher.SearchResult kinopoiskSearchResult = kinopoiskFetcher.fetchPages(request.query(), mapping);
		
		if (kinopoiskSearchResult.fetchedMovies().isEmpty()) {
			return Page.empty();
		}
		
		List<Movie> rawMovies = kinopoiskSearchResult.fetchedMovies().stream().map(kinopoiskMovieMapper::toEntity).toList();
		List<Movie> savedMovies = movieSyncService.syncAndSave(rawMovies);
		
		int actualEndIndex = Math.min(mapping.relativeEndIndex(), savedMovies.size());
		List<Movie> pageContent = savedMovies.subList(mapping.relativeStartIndex(), actualEndIndex);
		
		return makePage(pageContent, request, kinopoiskSearchResult.totalCount());
	}
	
	private Page<MovieResponseDto> getMoviesFromLocalDb(SearchMovieRequestDto request) {
		Sort sort = Sort.by(
				Sort.Order.desc("averageRating"),
				Sort.Order.asc("id")
		);
		
		Pageable pageable = PageRequest.of(
				request.page() - 1,
				request.size(),
				sort);
		
		Page<Movie> page = movieRepository.findAllByTitleContainingIgnoreCaseOrOriginalTitleContainingIgnoreCase(
				request.query(),
				request.query(),
				pageable);
		
		return page.map(movieMapper::toDto);
	}
	
	private Page<MovieResponseDto> makePage(
			List<Movie> listMovies,
			SearchMovieRequestDto request,
			int totalCount) {
		
		Sort sort = Sort.by(
				Sort.Order.desc("averageRating"),
				Sort.Order.asc("id")
		);
		
		Pageable pageable = PageRequest.of(
				request.page() - 1,
				request.size(),
				sort);
		
		var page = new PageImpl<>(
				listMovies,
				pageable,
				totalCount);
		
		return page.map(movieMapper::toDto);
	}
	
	public MovieResponseDto getById(Long id) {
		return movieMapper.toDto(getEntityById(id));
	}
	
	public MovieResponseDto getByExternalId(Long externalId) {
		return  movieMapper.toDto(getEntityByExternalId(externalId));
	}
	
	Movie getEntityById(Long id) {
		return movieRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Фильм с идентификатором " + id + " не найден"));
	}
	
	Movie getEntityByExternalId(Long externalId) {
		return movieRepository.findByExternalId(externalId)
				.orElseThrow(() -> new ResourceNotFoundException("Фильм с внешним идентификатором " + externalId + " не найден"));
	}
	
	List<Movie> findAllEntitiesByTitle(String title) {
		return movieRepository.findAllByTitleContainingIgnoreCase(title);
	}
	
	List<Movie> findAllEntitiesByOriginalTitle(String title) {
		return movieRepository.findAllByOriginalTitle(title);
	}
	
	@Transactional
	Movie save(Movie movie) {
		return movieRepository.save(movie);
	}
}