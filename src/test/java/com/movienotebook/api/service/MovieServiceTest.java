package com.movienotebook.api.service;

import com.movienotebook.api.dto.movie.MovieResponseDto;
import com.movienotebook.api.dto.movie.SearchMovieRequestDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.integration.KinopoiskFetcher;
import com.movienotebook.api.integration.dto.KinopoiskResponseDto;
import com.movienotebook.api.integration.mapper.KinopoiskMovieMapper;
import com.movienotebook.api.mapper.MovieMapper;
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
	
	@Mock
	private MovieMapper movieMapper;
	
	@InjectMocks
	private MovieService movieService;
	
	@Captor
	private ArgumentCaptor<Movie> movieCaptor;
	
	private Movie existingMovie;
	private Movie expectedMovieState;
	private MovieResponseDto movieResponseDto;
	
	@BeforeEach
	void setUp() {
		existingMovie = ClassesExamples.getExistingMovie();
		expectedMovieState = ClassesExamples.getExistingMovie();
		movieResponseDto = new MovieResponseDto(
				existingMovie.getId(),
				existingMovie.getExternalId(),
				existingMovie.getTitle(),
				existingMovie.getOriginalTitle(),
				existingMovie.getDescription(),
				existingMovie.getReleaseYear(),
				existingMovie.getPosterUrl(),
				existingMovie.getAverageRating()
		);
	}
	
	@Nested
	@DisplayName("Тесты метода searchMovies")
	class SearchMoviesTests {
		
		@Test
		@DisplayName("Если deepSearch=false, должен вернуть результат поиска только из локальной БД")
		void searchMovies_whenDeepSearchIsFalse_shouldReturnFromLocalDatabase() {
			// Arrange
			String query = "Матрица";
			var request = new SearchMovieRequestDto(query, 1, 10, false);
			Page<Movie> expectedPage = new PageImpl<>(List.of(existingMovie));
			
			when(movieRepository.findAllByTitleContainingIgnoreCaseOrOriginalTitleContainingIgnoreCase(
					eq(query), eq(query), any(Pageable.class)
			)).thenReturn(expectedPage);
			when(movieMapper.toDto(existingMovie)).thenReturn(movieResponseDto);
			
			// Act
			Page<MovieResponseDto> resultPage = movieService.searchMovies(request);
			
			// Assert
			assertThat(resultPage.getContent().getFirst()).isSameAs(movieResponseDto);
		}
		
		@Test
		@DisplayName("Если deepSearch=true и Fetcher ничего не нашел, должен вернуть пустую страницу")
		void searchMovies_whenDeepSearchIsTrueAndNothingFound_shouldReturnEmptyPage() {
			// Arrange
			String query = "Неизвестный фильм";
			var request = new SearchMovieRequestDto(query, 1, 10, true);
			var expectedMapping = PaginationCalculator.calculate(request.page(), request.size());
			var emptyResult = new KinopoiskFetcher.SearchResult(Collections.emptyList(), 0);
			
			when(kinopoiskFetcher.fetchPages(query, expectedMapping)).thenReturn(emptyResult);
			
			// Act
			Page<MovieResponseDto> result = movieService.searchMovies(request);
			
			// Assert
			assertThat(result.isEmpty()).isTrue();
		}
		
		@Test
		@DisplayName("Если deepSearch=true и фильмы найдены, должен вернуть Page с заготовленными DTO")
		void searchMovies_whenDeepSearchIsTrueAndFoundMovies_shouldProcessAndReturnPage() {
			// Arrange
			String query = "Матрица";
			var request = new SearchMovieRequestDto(query, 1, 1, true);
			var expectedMapping = PaginationCalculator.calculate(request.page(), request.size());
			
			var kinoDto = new KinopoiskResponseDto(1L, "Матрица", "The Matrix", "desc", 1999, "url");
			var fetcherResult = new KinopoiskFetcher.SearchResult(List.of(kinoDto), 150);
			
			when(kinopoiskFetcher.fetchPages(query, expectedMapping)).thenReturn(fetcherResult);
			when(kinopoiskMovieMapper.toEntity(kinoDto)).thenReturn(existingMovie);
			when(movieSyncService.syncAndSave(List.of(existingMovie))).thenReturn(List.of(existingMovie));
			when(movieMapper.toDto(existingMovie)).thenReturn(movieResponseDto);
			
			// Act
			Page<MovieResponseDto> result = movieService.searchMovies(request);
			
			// Assert
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().getFirst()).isSameAs(movieResponseDto);
			assertThat(result.getTotalElements()).isEqualTo(150);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getById")
	class GetByIdTests {
		
		@Test
		@DisplayName("Если фильм существует, должен вернуть DTO существующего фильма")
		void getById_whenMovieExists_shouldReturnExistingMovieDto() {
			// Arrange
			Long movieId = existingMovie.getId();
			when(movieRepository.findById(movieId)).thenReturn(Optional.of(existingMovie));
			when(movieMapper.toDto(existingMovie)).thenReturn(movieResponseDto);
			
			// Act
			MovieResponseDto result = movieService.getById(movieId);
			
			// Assert
			assertThat(result).isSameAs(movieResponseDto);
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
		@DisplayName("Если фильм существует, должен вернуть DTO фильма")
		void getByExternalId_whenMovieExists_shouldReturnExistingMovieDto() {
			// Arrange
			Long externalId = existingMovie.getExternalId();
			when(movieRepository.findByExternalId(externalId)).thenReturn(Optional.of(existingMovie));
			when(movieMapper.toDto(existingMovie)).thenReturn(movieResponseDto);
			
			// Act
			MovieResponseDto result = movieService.getByExternalId(externalId);
			
			// Assert
			assertThat(result).isSameAs(movieResponseDto);
		}
		
		@Test
		@DisplayName("Если фильма не существует, должен выбросить исключение ResourceNotFoundException")
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
	@DisplayName("Тесты метода getEntityById")
	class GetEntityByIdTests {
		
		@Test
		@DisplayName("Если сущность существует, должен вернуть её ссылку")
		void getEntityById_whenMovieExists_shouldReturnExistingMovieEntity() {
			// Arrange
			Long movieId = existingMovie.getId();
			when(movieRepository.findById(movieId)).thenReturn(Optional.of(existingMovie));
			
			// Act
			Movie result = movieService.getEntityById(movieId);
			
			// Assert
			assertThat(result).isSameAs(existingMovie);
		}
		
		@Test
		@DisplayName("Если сущности не существует, должен выбросить ResourceNotFoundException")
		void getEntityById_whenMovieDoesNotExist_shouldThrowException() {
			// Arrange
			Long movieId = 999L;
			when(movieRepository.findById(movieId)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> movieService.getEntityById(movieId))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getEntityByExternalId")
	class GetEntityByExternalIdTests {
		
		@Test
		@DisplayName("Если сущность существует, должен вернуть её ссылку")
		void getEntityByExternalId_whenMovieExists_shouldReturnExistingMovieEntity() {
			// Arrange
			Long externalId = existingMovie.getExternalId();
			when(movieRepository.findByExternalId(externalId)).thenReturn(Optional.of(existingMovie));
			
			// Act
			Movie result = movieService.getEntityByExternalId(externalId);
			
			// Assert
			assertThat(result).isSameAs(existingMovie);
		}
		
		@Test
		@DisplayName("Если сущности не существует, должен выбросить ResourceNotFoundException")
		void getEntityByExternalId_whenMovieDoesNotExist_shouldThrowException() {
			// Arrange
			Long externalId = 999L;
			when(movieRepository.findByExternalId(externalId)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> movieService.getEntityByExternalId(externalId))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода findAllEntitiesByTitle")
	class FindAllEntitiesByTitleTests {
		
		@Test
		@DisplayName("Должен вернуть список фильмов от репозитория (поиск по названию)")
		void findAllEntitiesByTitle_shouldReturnListOfMovies() {
			// Arrange
			String title = existingMovie.getTitle();
			when(movieRepository.findAllByTitleContainingIgnoreCase(title)).thenReturn(List.of(existingMovie));
			
			// Act
			List<Movie> returnedMovies = movieService.findAllEntitiesByTitle(title);
			
			// Assert
			assertThat(returnedMovies).hasSize(1);
			assertThat(returnedMovies.getFirst()).isSameAs(existingMovie);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода findAllEntitiesByOriginalTitle")
	class FindAllEntitiesByOriginalTitleTests {
		
		@Test
		@DisplayName("Должен вернуть список фильмов от репозитория (поиск по оригинальному названию)")
		void findAllEntitiesByOriginalTitle_shouldReturnListOfMovies() {
			// Arrange
			String originalTitle = existingMovie.getOriginalTitle();
			when(movieRepository.findAllByOriginalTitle(originalTitle)).thenReturn(List.of(existingMovie));
			
			// Act
			List<Movie> returnedMovies = movieService.findAllEntitiesByOriginalTitle(originalTitle);
			
			// Assert
			assertThat(returnedMovies).hasSize(1);
			assertThat(returnedMovies.getFirst()).isSameAs(existingMovie);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода save")
	class SaveTests {
		
		@Test
		@DisplayName("Должен сохранить переданный фильм")
		void save_shouldSaveMovie() {
			// Arrange
			Movie movieToSave = ClassesExamples.getMovieToSave();
			
			when(movieRepository.save(movieToSave)).thenReturn(existingMovie);
			
			// Expected
			Movie expectedSavedMovie = ClassesExamples.getMovieToSave();
			
			Movie expectedReturnedMovie = ClassesExamples.getExistingMovie();
			
			// Act
			Movie returnedMovie = movieService.save(movieToSave);
			
			// Assert
			verify(movieRepository, times(1)).save(movieCaptor.capture());
			Movie savedMovie = movieCaptor.getValue();
			
			assertThat(savedMovie)
					.isSameAs(movieToSave)
					.usingRecursiveComparison()
					.isEqualTo(expectedSavedMovie);
			
			assertThat(returnedMovie)
					.usingRecursiveComparison()
					.isEqualTo(expectedReturnedMovie);
		}
		
	}
}