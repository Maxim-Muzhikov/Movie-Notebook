package com.movienotebook.api.service;

import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.integration.KinopoiskIntegrationService;
import com.movienotebook.api.integration.dto.KinopoiskSearchResponseDto;
import com.movienotebook.api.integration.dto.MovieKinopoiskResponseDto;
import com.movienotebook.api.repository.MovieRepository;
import com.movienotebook.api.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
	
	private final KinopoiskIntegrationService kinopoiskIntegrationService;
	private final MovieRepository movieRepository;
	
	// TODO При НЕ глубоком поиске, выводятся результаты без пагинации
	public List<Movie> searchMovie(String query, Integer page, Boolean deepSearch) {
		List<Movie> searchResponse;
		
		// Если поиск локальный, ищем в своей БД
		if (!deepSearch) {
			searchResponse = movieRepository.findAllByTitleContainingIgnoreCaseOrOriginalTitleContainingIgnoreCase(query, query);
		} else {
			searchResponse = new ArrayList<Movie>();
		}
		
		// Если нужен глубокий поиск ИЛИ локальный поиск ничего не нашел
		if (deepSearch || searchResponse.isEmpty()) {
			KinopoiskSearchResponseDto kinopoiskSearchResponseDto = kinopoiskIntegrationService.searchMovie(query, page);
			
			List<Movie> newMoviesToSave = new ArrayList<>();
			
			for (MovieKinopoiskResponseDto filmDto : kinopoiskSearchResponseDto.films()) {
				movieRepository.findByExternalId(filmDto.filmId()).ifPresentOrElse(
						searchResponse::add,
						() -> newMoviesToSave.add(mapMovie(filmDto))
				);
			}
			
			if (!newMoviesToSave.isEmpty()) {
				List<Movie> savedMovies = movieRepository.saveAll(newMoviesToSave);
				searchResponse.addAll(savedMovies);
			}
		}
		
		return searchResponse;
	}
	
	private Movie mapMovie(MovieKinopoiskResponseDto film) {
		Movie movie = new Movie();
		movie.setExternalId(film.filmId());
		movie.setTitle(film.nameRu());
		movie.setOriginalTitle(film.nameEn());
		movie.setDescription(film.description());
		movie.setReleaseYear(film.year());
		movie.setPosterUrl(film.posterUrl());
		return movie;
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
	public Movie save(Movie movie) {
		return movieRepository.save(movie);
	}
	
	
}