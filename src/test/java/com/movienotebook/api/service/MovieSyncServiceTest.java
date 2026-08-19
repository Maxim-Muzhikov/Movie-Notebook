package com.movienotebook.api.service;

import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.mapper.MovieMapper;
import com.movienotebook.api.repository.MovieRepository;
import com.movienotebook.api.util.ClassesExamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieSyncServiceTest {
	
	@Mock
	private MovieRepository movieRepository;
	
	@Mock
	private MovieMapper movieMapper;
	
	@InjectMocks
	private MovieSyncService movieSyncService;
	
	@Captor
	private ArgumentCaptor<List<Movie>> movieListCaptor;
	
	private Movie fetchedMovie;
	private Movie existingMovie;
	
	@BeforeEach
	void setUp() {
		fetchedMovie = ClassesExamples.getMovieToSave();
		fetchedMovie.setExternalId(100L);
		fetchedMovie.setTitle("Новое название");
		
		existingMovie = ClassesExamples.getExistingMovie();
		existingMovie.setExternalId(100L);
	}
	
	@Nested
	@DisplayName("Тесты метода syncAndSave")
	class SyncAndSaveTests {
		
		@Test
		@DisplayName("Если фильма нет в БД, должен добавить новый фильм в список на сохранение")
		void syncAndSave_whenMovieIsNew_shouldSaveFetchedMovie() {
			// Arrange
			List<Movie> fetchedMovies = List.of(fetchedMovie);
			List<Long> externalIds = List.of(fetchedMovie.getExternalId());
			
			when(movieRepository.findAllByExternalIdIn(externalIds)).thenReturn(Collections.emptyList());
			when(movieRepository.saveAll(any())).thenReturn(fetchedMovies);
			
			// Act
			List<Movie> result = movieSyncService.syncAndSave(fetchedMovies);
			
			// Assert
			verify(movieRepository).saveAll(movieListCaptor.capture());
			List<Movie> savedMovies = movieListCaptor.getValue();
			
			assertThat(savedMovies)
					.usingRecursiveComparison()
					.isEqualTo(fetchedMovies);
			
			assertThat(result).isSameAs(fetchedMovies);
			verifyNoInteractions(movieMapper);
		}
		
		@Test
		@DisplayName("Если фильм есть в БД и он устарел (stale), должен обновить его маппером и сохранить")
		void syncAndSave_whenMovieIsStale_shouldUpdateAndSaveExistingMovie() {
			// Arrange
			existingMovie.setLastUpdate(OffsetDateTime.now().minusHours(25));
			
			List<Movie> fetchedMovies = List.of(fetchedMovie);
			List<Long> externalIds = List.of(fetchedMovie.getExternalId());
			List<Movie> expectedDbMovies = List.of(existingMovie);
			
			when(movieRepository.findAllByExternalIdIn(externalIds)).thenReturn(expectedDbMovies);
			when(movieRepository.saveAll(any())).thenReturn(expectedDbMovies);
			
			// Act
			List<Movie> result = movieSyncService.syncAndSave(fetchedMovies);
			
			// Assert
			verify(movieMapper).updateMovieFromExtracted(existingMovie, fetchedMovie);
			
			verify(movieRepository).saveAll(movieListCaptor.capture());
			List<Movie> savedMovies = movieListCaptor.getValue();
			
			assertThat(savedMovies)
					.hasSize(1)
					.element(0).isSameAs(existingMovie);
			
			assertThat(result).isSameAs(expectedDbMovies);
		}
		
		@Test
		@DisplayName("Если фильм есть в БД и он актуален (fresh), должен сохранить без обновления маппером")
		void syncAndSave_whenMovieIsFresh_shouldSaveExistingMovieWithoutUpdate() {
			// Arrange
			existingMovie.setLastUpdate(OffsetDateTime.now().minusHours(1));
			
			List<Movie> fetchedMovies = List.of(fetchedMovie);
			List<Long> externalIds = List.of(fetchedMovie.getExternalId());
			List<Movie> expectedDbMovies = List.of(existingMovie);
			
			when(movieRepository.findAllByExternalIdIn(externalIds)).thenReturn(expectedDbMovies);
			when(movieRepository.saveAll(any())).thenReturn(expectedDbMovies);
			
			// Act
			List<Movie> result = movieSyncService.syncAndSave(fetchedMovies);
			
			// Assert
			verify(movieMapper, never()).updateMovieFromExtracted(any(), any());
			
			verify(movieRepository).saveAll(movieListCaptor.capture());
			List<Movie> savedMovies = movieListCaptor.getValue();
			
			assertThat(savedMovies)
					.hasSize(1)
					.element(0).isSameAs(existingMovie);
			
			assertThat(result).isSameAs(expectedDbMovies);
		}
		
		@Test
		@DisplayName("Смешанный сценарий: должен корректно обработать новый, устаревший и актуальный фильмы вместе")
		void syncAndSave_whenMixedMovies_shouldProcessEachMovieCorrectly() {
			// Arrange
			// 1. Новый фильм
			Movie newFetchedMovie = ClassesExamples.getMovieToSave();
			newFetchedMovie.setExternalId(200L);
			
			// 2. Устаревший фильм
			Movie staleFetchedMovie = ClassesExamples.getMovieToSave();
			staleFetchedMovie.setExternalId(300L);
			Movie staleDbMovie = ClassesExamples.getExistingMovie();
			staleDbMovie.setExternalId(300L);
			staleDbMovie.setLastUpdate(OffsetDateTime.now().minusHours(48));
			
			// 3. Актуальный фильм
			Movie freshFetchedMovie = ClassesExamples.getMovieToSave();
			freshFetchedMovie.setExternalId(400L);
			Movie freshDbMovie = ClassesExamples.getExistingMovie();
			freshDbMovie.setExternalId(400L);
			freshDbMovie.setLastUpdate(OffsetDateTime.now().minusHours(5));
			
			List<Movie> fetchedMovies = List.of(newFetchedMovie, staleFetchedMovie, freshFetchedMovie);
			List<Long> externalIds = List.of(200L, 300L, 400L);
			
			// В БД найдены только 2 и 3
			List<Movie> dbMovies = List.of(staleDbMovie, freshDbMovie);
			
			// Ожидаемый список на сохранение: новый, существующий (устаревший, обновлен), существующий (актуальный)
			List<Movie> expectedToSave = List.of(newFetchedMovie, staleDbMovie, freshDbMovie);
			
			when(movieRepository.findAllByExternalIdIn(externalIds)).thenReturn(dbMovies);
			when(movieRepository.saveAll(any())).thenReturn(expectedToSave);
			
			// Act
			List<Movie> result = movieSyncService.syncAndSave(fetchedMovies);
			
			// Assert
			// Маппер должен быть вызван только для устаревшего фильма
			verify(movieMapper).updateMovieFromExtracted(staleDbMovie, staleFetchedMovie);
			verify(movieMapper, never()).updateMovieFromExtracted(freshDbMovie, freshFetchedMovie);
			
			verify(movieRepository).saveAll(movieListCaptor.capture());
			List<Movie> savedMovies = movieListCaptor.getValue();
			
			assertThat(savedMovies)
					.hasSize(3)
					.containsExactlyElementsOf(expectedToSave);
			
			assertThat(result).isSameAs(expectedToSave);
		}
	}
}