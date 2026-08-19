package com.movienotebook.api.service;

import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.dto.rating.RatingResponseDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Rating;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.mapper.RatingMapper;
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
	
	@Mock
	private RatingMapper ratingMapper;
	
	@InjectMocks
	private RatingService ratingService;
	
	@Captor
	private ArgumentCaptor<Rating> ratingCaptor;
	
	@Captor
	private ArgumentCaptor<Movie> movieCaptor;
	
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
			var requestDto = new RatingRequestDto(existingMovie.getId(), 8);
			Double rawCalculatedAverage = 8.0;
			BigDecimal newAverageRating = new BigDecimal("8.00");
			RatingResponseDto expectedDto = new RatingResponseDto(newAverageRating);
			
			when(movieService.getEntityById(requestDto.movieId())).thenReturn(existingMovie);
			when(ratingRepository.findByMovieIdAndUserId(existingMovie.getId(), currentUser.getId()))
					.thenReturn(Optional.empty());
			when(userService.getReferenceById(currentUser.getId())).thenReturn(existingUser);
			when(ratingRepository.calculateAverageScoreByMovieId(existingMovie.getId())).thenReturn(rawCalculatedAverage);
			when(ratingMapper.toDto(newAverageRating)).thenReturn(expectedDto);
			
			// Expected
			Rating expectedNewRating = new Rating();
			expectedNewRating.setMovie(existingMovie);
			expectedNewRating.setUser(existingUser);
			expectedNewRating.setScore(8);
			
			Movie expectedMovie = ClassesExamples.getExistingMovie();
			expectedMovie.setId(existingMovie.getId());
			expectedMovie.setAverageRating(newAverageRating);
			
			// Act
			RatingResponseDto actualDto = ratingService.save(requestDto, currentUser);
			
			// Assert
			assertThat(actualDto).isSameAs(expectedDto);
			
			verify(ratingRepository).save(ratingCaptor.capture());
			assertThat(ratingCaptor.getValue())
					.usingRecursiveComparison()
					.ignoringFields("id")
					.isEqualTo(expectedNewRating);
			
			verify(movieService).save(movieCaptor.capture());
			assertThat(movieCaptor.getValue())
					.isSameAs(existingMovie) // Фильм мутировал оригинал
					.usingRecursiveComparison()
					.isEqualTo(expectedMovie);
		}
		
		@Test
		@DisplayName("Если оценка существует, должен обновить её, округлить средний рейтинг (HALF_UP) и вернуть DTO")
		void save_whenRatingAlreadyExists_shouldUpdateExistingRecalculateAverageAndReturnDto() {
			// Arrange
			var requestDto = new RatingRequestDto(existingMovie.getId(), 9);
			Double rawCalculatedAverage = 8.456;
			BigDecimal newAverageRating = new BigDecimal("8.46");
			RatingResponseDto expectedDto = new RatingResponseDto(newAverageRating);
			
			when(movieService.getEntityById(requestDto.movieId())).thenReturn(existingMovie);
			when(ratingRepository.findByMovieIdAndUserId(existingMovie.getId(), currentUser.getId()))
					.thenReturn(Optional.of(existingRating));
			when(ratingRepository.calculateAverageScoreByMovieId(existingMovie.getId())).thenReturn(rawCalculatedAverage);
			when(ratingMapper.toDto(newAverageRating)).thenReturn(expectedDto);
			
			// Expected
			Rating expectedUpdatedRating = ClassesExamples.getExistingRating();
			expectedUpdatedRating.setScore(9);
			
			Movie expectedMovie = ClassesExamples.getExistingMovie();
			expectedMovie.setId(existingMovie.getId());
			expectedMovie.setAverageRating(newAverageRating);
			
			// Act
			RatingResponseDto actualDto = ratingService.save(requestDto, currentUser);
			
			// Assert
			assertThat(actualDto).isSameAs(expectedDto);
			
			verify(userService, never()).getReferenceById(any());
			
			verify(ratingRepository).save(ratingCaptor.capture());
			assertThat(ratingCaptor.getValue())
					.isSameAs(existingRating)
					.usingRecursiveComparison()
					.isEqualTo(expectedUpdatedRating);
			
			verify(movieService).save(movieCaptor.capture());
			assertThat(movieCaptor.getValue())
					.isSameAs(existingMovie)
					.usingRecursiveComparison()
					.isEqualTo(expectedMovie);
		}
	}
}