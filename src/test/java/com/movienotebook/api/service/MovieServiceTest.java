package com.movienotebook.api.service;

import com.movienotebook.api.dto.movie.SearchMovieRequestDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.integration.KinopoiskFetcher;
import com.movienotebook.api.integration.dto.KinopoiskResponseDto;
import com.movienotebook.api.integration.mapper.KinopoiskMovieMapper;
import com.movienotebook.api.repository.MovieRepository;
import com.movienotebook.api.util.ClassesExamples;
import com.movienotebook.api.util.PaginationCalculator;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {
	
	@Mock
	private MovieRepository movieRepository;
	
	@Mock
	private KinopoiskFetcher kinopoiskFetcher;
	
	@Mock
	private MovieSyncService movieSyncService;
	
	@Mock
	private KinopoiskMovieMapper kinopoiskMovieMapper;
	
	@InjectMocks
	private MovieService movieService;
	
	@Captor
	private ArgumentCaptor<Movie> movieCaptor;
	
	private Movie testMovie;
	private Movie expectedMovie;
	@BeforeEach
	void setUp() {
		testMovie = ClassesExamples.getExistingMovie();
		expectedMovie = ClassesExamples.getExistingMovie();
	}
	
	@Nested
	@DisplayName("Тесты метода searchMovie")
	class SearchMovieTests {
		
		@Test
		@DisplayName("Если deepSearch=false, должен вернуть результат поиска только из локальной БД")
		void searchMovie_whenDeepSearchIsFalse_shouldReturnFromLocalDatabase() {
			// Arrange
			String query = "Матрица";
			var request = new SearchMovieRequestDto(query, 1, 10, false);
			Page<Movie> expectedPage = new PageImpl<>(List.of(testMovie));
			
			when(movieRepository.findAllByTitleContainingIgnoreCaseOrOriginalTitleContainingIgnoreCase(
					eq(query), eq(query), any(Pageable.class)
			)).thenReturn(expectedPage);
			
			// Act
			Page<Movie> resultPage = movieService.searchMovie(request);
			
			// Assert
			assertThat(resultPage)
					.isSameAs(expectedPage);
			
			assertThat(resultPage.getContent().getFirst())
					.usingRecursiveComparison()
					.isEqualTo(expectedMovie);
			
			verifyNoInteractions(kinopoiskFetcher, movieSyncService, kinopoiskMovieMapper);
		}
		
		@Test
		@DisplayName("Если deepSearch=true и Fetcher ничего не нашел, должен вернуть пустую страницу")
		void searchMovie_whenDeepSearchIsTrueAndNothingFound_shouldReturnEmptyPage() {
			// Arrange
			String query = "Неизвестный фильм";
			var request = new SearchMovieRequestDto(query, 1, 10, true);
			
			var expectedMapping = PaginationCalculator.calculate(request.page(), request.size());
			
			var emptyResult = new KinopoiskFetcher.SearchResult(Collections.emptyList(), 0);
			when(kinopoiskFetcher.fetchPages(query, expectedMapping)).thenReturn(emptyResult);
			
			// Act
			Page<Movie> result = movieService.searchMovie(request);
			
			// Assert
			assertThat(result.isEmpty()).isTrue();
			
			verifyNoInteractions(movieSyncService, kinopoiskMovieMapper);
		}
		
		@Test
		@DisplayName("Если deepSearch=true и фильмы найдены, должен смаппить, сохранить, обрезать и вернуть Page")
		void searchMovie_whenDeepSearchIsTrueAndFoundMovies_shouldProcessAndReturnPage() {
			// Arrange
			String query = "Матрица";
			var request = new SearchMovieRequestDto(query, 1, 2, true);
			var expectedMapping = PaginationCalculator.calculate(request.page(), request.size());
			
			var dto1 = new KinopoiskResponseDto(1L, "Фильм 1", "M1", "desc", 2000, "url");
			var dto2 = new KinopoiskResponseDto(2L, "Фильм 2", "M2", "desc", 2001, "url");
			var fetcherResult = new KinopoiskFetcher.SearchResult(List.of(dto1, dto2), 150);
			
			when(kinopoiskFetcher.fetchPages(query, expectedMapping)).thenReturn(fetcherResult);
			
			Movie rawMovie1 = new Movie(); rawMovie1.setTitle("Сырой Фильм 1");
			Movie rawMovie2 = new Movie(); rawMovie2.setTitle("Сырой Фильм 2");
			
			when(kinopoiskMovieMapper.toEntity(dto1)).thenReturn(rawMovie1);
			when(kinopoiskMovieMapper.toEntity(dto2)).thenReturn(rawMovie2);
			
			Movie savedMovie1 = new Movie(); savedMovie1.setId(10L);
			Movie savedMovie2 = new Movie(); savedMovie2.setId(20L);
			
			when(movieSyncService.syncAndSave(List.of(rawMovie1, rawMovie2)))
					.thenReturn(List.of(savedMovie1, savedMovie2));
			
			// Act
			Page<Movie> result = movieService.searchMovie(request);
			
			// Assert
			// Проверяем данные страницы
			assertThat(result.getContent())
					.hasSize(2)
					.containsExactly(savedMovie1, savedMovie2);
			
			assertThat(result.getTotalElements()).isEqualTo(150);
			assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
			assertThat(result.getPageable().getPageSize()).isEqualTo(2);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getById")
	class GetByIdTests {
		
		@Test
		@DisplayName("Если фильм существует, должен вернуть существующий фильм")
		void getById_whenMovieExists_shouldReturnExistingMovie() {
			// Arrange
			Long movieId = testMovie.getId();
			when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
			
			// Act
			Movie returnedMovie = movieService.getById(movieId);
			
			// Assert
			assertThat(returnedMovie)
					.isSameAs(testMovie)
					.usingRecursiveComparison()
					.isEqualTo(expectedMovie);
		}
		
		@Test
		@DisplayName("Если фильма не существует, должен выбросить исключение ResourceNotFoundException")
		void getById_whenMovieDoesNotExist_shouldThrowException() {
			// Arrange
			Long movieId = 999L;
			when(movieRepository.findById(movieId)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> movieService.getById(movieId))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getByExternalId")
	class GetByExternalIdTests {
		
		@Test
		@DisplayName("Если фильм с внешним ID существует, должен вернуть фильм")
		void getByExternalId_whenMovieExists_shouldReturnExistingMovie() {
			// Arrange
			Long externalId = testMovie.getExternalId();
			when(movieRepository.findByExternalId(externalId)).thenReturn(Optional.of(testMovie));
			
			// Act
			Movie returnedMovie = movieService.getByExternalId(externalId);
			
			// Assert
			assertThat(returnedMovie)
					.isSameAs(testMovie)
					.usingRecursiveComparison()
					.isEqualTo(expectedMovie);
		}
		
		@Test
		@DisplayName("Если фильм с внешним ID не существует, должен выбросить исключение ResourceNotFoundException")
		void getByExternalId_whenMovieDoesNotExist_shouldThrowException() {
			// Arrange
			Long externalId = 999L;
			when(movieRepository.findByExternalId(externalId)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> movieService.getByExternalId(externalId))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты поиска по названию (getByTitle и getByOriginalTitle)")
	class GetByTitleTests {
		
		@Test
		@DisplayName("getByTitle: должен вернуть список фильмов от репозитория")
		void getByTitle_shouldReturnListOfMovies() {
			// Arrange
			String title = testMovie.getTitle();
			when(movieRepository.findAllByTitleContainingIgnoreCase(title)).thenReturn(List.of(testMovie));
			
			// Act
			List<Movie> returnedMovies = movieService.getByTitle(title);
			
			// Assert
			assertThat(returnedMovies)
					.hasSize(1)
					.contains(testMovie);
			
			assertThat(returnedMovies.getFirst())
					.usingRecursiveComparison()
					.isEqualTo(expectedMovie);
		}
		
		@Test
		@DisplayName("getByOriginalTitle: должен вернуть список фильмов от репозитория")
		void getByOriginalTitle_shouldReturnListOfMovies() {
			// Arrange
			String originalTitle = testMovie.getOriginalTitle();
			when(movieRepository.findAllByOriginalTitle(originalTitle)).thenReturn(List.of(testMovie));
			
			// Act
			List<Movie> returnedMovies = movieService.getByOriginalTitle(originalTitle);
			
			// Assert
			assertThat(returnedMovies)
					.hasSize(1)
					.contains(testMovie);
			
			assertThat(returnedMovies.getFirst())
					.usingRecursiveComparison()
					.isEqualTo(expectedMovie);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода save")
	class SaveTests {
		
		@Test
		@DisplayName("Должен передать сущность в репозиторий для сохранения")
		void save_shouldCallRepositorySave() {
			// Act
			movieService.save(testMovie);
			
			// Assert
			verify(movieRepository, times(1)).save(movieCaptor.capture());
			Movie savedMovie = movieCaptor.getValue();
			
			assertThat(savedMovie)
					.isSameAs(testMovie)
					.usingRecursiveComparison()
					.isEqualTo(expectedMovie);
		}
	}
}