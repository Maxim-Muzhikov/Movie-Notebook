package com.movienotebook.api.service;

import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Rating;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.repository.RatingRepository;
import com.movienotebook.api.security.CustomUserDetails;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
	
	@Mock
	private RatingRepository ratingRepository;
	
	@Mock
	private MovieService movieService;
	
	@Mock
	private UserService userService;
	
	@InjectMocks
	private RatingService ratingService;
	
	@Captor
	private ArgumentCaptor<Rating> ratingCaptor;
	
	@Captor
	private ArgumentCaptor<Movie> movieCaptor;
	
	private CustomUserDetails currentUser;
	private User testUser;
	private Movie testMovie;
	private Rating testRating;
	
	@BeforeEach
	void setUp() {
		currentUser = new CustomUserDetails(
				1L,
				"ratingUser",
				"hash",
				List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		
		testUser = ClassesExamples.getExistingUser();
		
		testMovie = new Movie();
		testMovie.setId(10L);
		testMovie.setTitle("Начало");
		testMovie.setAverageRating(new BigDecimal("5.00"));
		
		testRating = new Rating();
		testRating.setId(100L);
		testRating.setScore(5);
		testRating.setMovie(testMovie);
		testRating.setUser(testUser);
	}
	
	@Nested
	@DisplayName("Тесты метода addOrUpdateRating")
	class AddOrUpdateRatingTests {
		
		@Test
		@DisplayName("Если оценка не существует, должен создать новую, обновить рейтинг фильма и вернуть его")
		void addOrUpdateRating_whenRatingDoesNotExist_shouldCreateNewAndRecalculateAverage() {
			// Arrange
			var requestDto = new RatingRequestDto(testMovie.getId(), 8);
			Double rawCalculatedAverage = 8.0;
			
			when(movieService.getById(requestDto.movieId())).thenReturn(testMovie);
			when(ratingRepository.findByMovieIdAndUserId(testMovie.getId(), currentUser.getId()))
					.thenReturn(Optional.empty());
			when(userService.getReferenceById(currentUser.getId())).thenReturn(testUser);
			when(ratingRepository.calculateAverageScoreByMovieId(testMovie.getId())).thenReturn(rawCalculatedAverage);
			
			// Expected
			Rating expectedNewRating = new Rating();
			expectedNewRating.setMovie(testMovie);
			expectedNewRating.setUser(testUser);
			expectedNewRating.setScore(8);
			
			Movie expectedMovie = new Movie();
			expectedMovie.setId(testMovie.getId());
			expectedMovie.setTitle(testMovie.getTitle());
			expectedMovie.setAverageRating(new BigDecimal("8.00")); // Ожидаем масштаб (scale) 2
			
			// Act
			BigDecimal returnedAverage = ratingService.addOrUpdateRating(requestDto, currentUser);
			
			// Assert
			assertThat(returnedAverage).isEqualByComparingTo(new BigDecimal("8.00"));
			
			// Assert
			verify(ratingRepository, times(1)).save(ratingCaptor.capture());
			Rating savedRating = ratingCaptor.getValue();
			assertThat(savedRating)
					.usingRecursiveComparison()
					.isEqualTo(expectedNewRating);
			
			verify(movieService, times(1)).save(movieCaptor.capture());
			Movie savedMovie = movieCaptor.getValue();
			assertThat(savedMovie)
					.isSameAs(testMovie) // Фильм не должен был пересоздаваться
					.usingRecursiveComparison()
					.isEqualTo(expectedMovie);
		}
		
		@Test
		@DisplayName("Если оценка существует, должен обновить её, округлить средний рейтинг фильма (HALF_UP) и вернуть")
		void addOrUpdateRating_whenRatingAlreadyExists_shouldUpdateExistingAndRecalculateAverage() {
			// Arrange
			var requestDto = new RatingRequestDto(testMovie.getId(), 9);

			Double rawCalculatedAverage = 8.456;
			
			when(movieService.getById(requestDto.movieId())).thenReturn(testMovie);
			when(ratingRepository.findByMovieIdAndUserId(testMovie.getId(), currentUser.getId()))
					.thenReturn(Optional.of(testRating));
			when(ratingRepository.calculateAverageScoreByMovieId(testMovie.getId())).thenReturn(rawCalculatedAverage);
			
			// Expected
			Rating expectedUpdatedRating = new Rating();
			expectedUpdatedRating.setId(testRating.getId());
			expectedUpdatedRating.setMovie(testMovie);
			expectedUpdatedRating.setUser(testUser);
			expectedUpdatedRating.setScore(9);
			
			Movie expectedMovie = new Movie();
			expectedMovie.setId(testMovie.getId());
			expectedMovie.setTitle(testMovie.getTitle());
			expectedMovie.setAverageRating(new BigDecimal("8.46"));
			
			// Act
			BigDecimal returnedAverage = ratingService.addOrUpdateRating(requestDto, currentUser);
			
			// Assert
			assertThat(returnedAverage).isEqualByComparingTo(new BigDecimal("8.46"));
			
			// Assert
			verify(userService, never()).getReferenceById(any());
			
			// Assert
			verify(ratingRepository, times(1)).save(ratingCaptor.capture());
			Rating savedRating = ratingCaptor.getValue();
			assertThat(savedRating)
					.isSameAs(testRating)
					.usingRecursiveComparison()
					.isEqualTo(expectedUpdatedRating);
			
			// Assert
			verify(movieService, times(1)).save(movieCaptor.capture());
			Movie savedMovie = movieCaptor.getValue();
			assertThat(savedMovie)
					.isSameAs(testMovie)
					.usingRecursiveComparison()
					.isEqualTo(expectedMovie);
		}
	}
}