package com.movienotebook.api.service;

import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.dto.rating.RatingResponseDto;
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
	
	private CustomUserDetails currentUser;
	private User existingUser;
	private Movie existingMovie;
	private Rating existingRating;
	
	@BeforeEach
	void setUp() {
		currentUser = new CustomUserDetails(
				1L,
				"Текущий пользователь",
				"Хэш пароля",
				List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		
		existingUser = ClassesExamples.getExistingUser();
		existingUser.setId(currentUser.getId());
		existingMovie = ClassesExamples.getExistingMovie();
		existingRating = ClassesExamples.getExistingRating();
	}
	
	@Nested
	@DisplayName("Тесты метода save")
	class SaveTests {
		
		@Test
		@DisplayName("Если оценка не существует, должен создать новую, обновить рейтинг фильма и вернуть DTO")
		void save_whenRatingDoesNotExist_shouldCreateNewRecalculateAverageAndReturnDto() {
			// Arrange
			Long movieId = existingMovie.getId();
			var request = new RatingRequestDto(5);
			
			when(ratingRepository.findByMovieIdAndUserId(movieId, existingUser.getId()))
					.thenReturn(Optional.empty());
			
			when(movieService.getReferenceById(movieId)).thenReturn(existingMovie);
			when(userService.getReferenceById(existingUser.getId())).thenReturn(existingUser);
			when(movieService.updateAndGetAverageRating(movieId)).thenReturn(BigDecimal.valueOf(4.5));
			
			// Expected
			var expectedResponse = new RatingResponseDto(BigDecimal.valueOf(4.5));
			
			var expectedRatingToSave = new Rating();
			expectedRatingToSave.setMovie(existingMovie);
			expectedRatingToSave.setUser(existingUser);
			expectedRatingToSave.setScore(5);
			
			// Act
			RatingResponseDto response = ratingService.save(movieId, request, currentUser);
			
			// Assert
			verify(ratingRepository).saveAndFlush(ratingCaptor.capture());
			Rating ratingToSave = ratingCaptor.getValue();
			
			assertThat(response)
					.usingRecursiveComparison()
					.isEqualTo(expectedResponse);
			
			assertThat(ratingToSave)
					.usingRecursiveComparison()
					.isEqualTo(expectedRatingToSave);
		}
		
		@Test
		@DisplayName("Если оценка существует, должен обновить счет, сохранить и вернуть DTO")
		void save_whenRatingAlreadyExists_shouldUpdateExistingRecalculateAverageAndReturnDto() {
			// Arrange
			Long movieId = existingMovie.getId();
			var request = new RatingRequestDto(5);
			existingRating.setScore(1);
			
			when(ratingRepository.findByMovieIdAndUserId(movieId, existingUser.getId()))
					.thenReturn(Optional.of(existingRating));
			
			when(movieService.updateAndGetAverageRating(movieId)).thenReturn(BigDecimal.valueOf(4.50));
			
			// Act
			RatingResponseDto response = ratingService.save(movieId, request, currentUser);
			
			// Assert
			verify(ratingRepository).saveAndFlush(ratingCaptor.capture());
			Rating ratingToSave = ratingCaptor.getValue();
			
			verify(movieService, never()).getReferenceById(any());
			verify(userService, never()).getReferenceById(any());
			
			assertThat(ratingToSave.getScore()).isEqualTo(5);
			assertThat(ratingToSave).isSameAs(existingRating);
			assertThat(response.newAverageRating()).isEqualByComparingTo(BigDecimal.valueOf(4.50));
		}
	}
}