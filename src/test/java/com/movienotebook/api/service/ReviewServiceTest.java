package com.movienotebook.api.service;

import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.repository.ReviewRepository;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.util.ClassesExamples;
import io.jsonwebtoken.lang.Classes;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
	
	@Mock
	private ReviewRepository reviewRepository;
	
	@Mock
	private MovieService movieService;
	
	@Mock
	private UserService userService;
	
	@InjectMocks
	private ReviewService reviewService;
	
	@Captor
	private ArgumentCaptor<Review> reviewCaptor;
	
	private CustomUserDetails currentUser;
	private Movie testMovie;
	private User testUser;
	private Review testReview;
	
	@BeforeEach
	void setUp() {
		currentUser = new CustomUserDetails(
				1L,
				"Имя пользователя",
				"Хэш пароля",
				List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		
		testMovie = ClassesExamples.getExistingMovie();
		testUser = ClassesExamples.getExistingUser();
		testReview = ClassesExamples.getExistingReview();
		
		testReview.setUser(testUser);
	}
	
	@Nested
	@DisplayName("Тесты метода addOrUpdateReview")
	class AddOrUpdateReviewTests {
		
		@Test
		@DisplayName("Если отзыв не существует, должен создать новый отзыв")
		void addOrUpdateReview_whenReviewDoesNotExist_shouldCreateNewReview() {
			// Arrange
			var requestDto = new ReviewRequestDto(testMovie.getId(), "Отличный фильм!");
			
			when(movieService.getById(testMovie.getId())).thenReturn(testMovie);
			when(reviewRepository.findByMovieIdAndUserId(testMovie.getId(), currentUser.getId()))
					.thenReturn(Optional.empty());
			when(userService.getReferenceById(currentUser.getId())).thenReturn(testUser);
			
			// Expected
			var expectedReview = new Review();
			expectedReview.setContent(requestDto.content());
			expectedReview.setMovie(testMovie);
			expectedReview.setUser(testUser);
			expectedReview.setIsDeleted(false);
			
			// Act
			Review returnedReview = reviewService.addOrUpdateReview(requestDto, currentUser);
			
			// Assert
			verify(reviewRepository, times(1)).save(reviewCaptor.capture());
			Review savedReview = reviewCaptor.getValue();
			
			assertThat(returnedReview)
					.isNotNull()
					.isSameAs(savedReview);
			
			assertThat(savedReview)
					.usingRecursiveComparison()
					.isEqualTo(expectedReview);
		}
		
		
		@Test
		@DisplayName("Если отзыв существует, должен обновить существующий отзыв")
		void addOrUpdateReview_whenReviewAlreadyExists_shouldUpdateExistingReview() {
			// Arrange
			var requestDto = new ReviewRequestDto(testMovie.getId(), "Обновленный текст отзыва");
			
			when(movieService.getById(testMovie.getId())).thenReturn(testMovie);
			when(reviewRepository.findByMovieIdAndUserId(testMovie.getId(), currentUser.getId()))
					.thenReturn(Optional.of(testReview));
			
			// Expected
			Review expectedReview = ClassesExamples.getExistingReview();
			expectedReview.setUser(ClassesExamples.getExistingUser());
			expectedReview.setContent(requestDto.content());
			
			// Act
			Review result = reviewService.addOrUpdateReview(requestDto, currentUser);
			
			// Assert
			verify(reviewRepository, times(1)).save(reviewCaptor.capture());
			verify(userService, never()).getReferenceById(any());
			Review updatedReview = reviewCaptor.getValue();
			
			assertThat(result)
					.isNotNull()
					.isSameAs(updatedReview);
			
			assertThat(updatedReview)
					.usingRecursiveComparison()
					.isEqualTo(expectedReview);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getReviewsByMovieId")
	class GetReviewsByMovieIdTests {
		@Test
		@DisplayName("Если отзывы есть, должен без изменений вернуть список отзывов")
		void getReviewsByMovieId_whenReviewsExist_shouldReturnExistingReviews() {
			// Arrange
			Long movieId = testMovie.getId();
			
			when(reviewRepository.findAllByMovieId(movieId)).thenReturn(List.of(testReview));
			
			// Expected
			Review expectedReview = ClassesExamples.getExistingReview();
			expectedReview.setUser(ClassesExamples.getExistingUser());
			
			// Act
			List<Review> returnedReviews = reviewService.getReviewsByMovieId(movieId);
			
			// Assert
			assertThat(returnedReviews)
					.usingRecursiveComparison()
					.isEqualTo(List.of(expectedReview));
		}
		
		@Test
		@DisplayName("Если отзывов нет, должен вернуть пустой список отзывов")
		void getReviewsByMovieId_whenReviewsDoesNotExist_shouldReturnEmptyList() {
			// Arrange
			Long movieId = testMovie.getId();
			
			when(reviewRepository.findAllByMovieId(movieId)).thenReturn(Collections.emptyList());
			
			// Expected
			var expectedReviews = Collections.emptyList();
			
			// Act
			List<Review> returnedReviews = reviewService.getReviewsByMovieId(movieId);
			
			// Assert
			assertThat(returnedReviews)
					.usingRecursiveComparison()
					.isEqualTo(expectedReviews);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода deleteReview")
	class DeleteReviewTests {
		@Test
		@DisplayName("Если отзыв существует и пользователь его автор, должен удалить существующий отзыв, согласно его номеру")
		void deleteReview_whenReviewExistAndUserIsAuthor_shouldDeleteExistingReview() {
			// Arrange
			Long reviewId = testReview.getId();
			
			when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
			
			// Expected
			Review expectedReview = ClassesExamples.getExistingReview();
			expectedReview.setUser(ClassesExamples.getExistingUser());
			
			// Act
			reviewService.deleteReview(reviewId, currentUser);
			
			// Assert
			verify(reviewRepository, times(1)).delete(reviewCaptor.capture());
			
			Review deletedReview = reviewCaptor.getValue();
			
			assertThat(deletedReview)
					.usingRecursiveComparison()
					.isEqualTo(expectedReview);
		}
		
		@Test
		@DisplayName("Если отзыв существует и пользователь администратор, должен удалить существующий отзыв, согласно его номеру")
		void deleteReview_whenReviewExistAndUserIsAdmin_shouldDeleteExistingReview() {
			// Arrange
			Long reviewId = testReview.getId();
			currentUser = new CustomUserDetails(
					2L,
					"Администратор",
					"Хэш пароля",
					List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
			);
			
			when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
			
			// Expected
			Review expectedReview = ClassesExamples.getExistingReview();
			expectedReview.setUser(ClassesExamples.getExistingUser());
			
			// Act
			reviewService.deleteReview(reviewId, currentUser);
			
			// Assert
			verify(reviewRepository, times(1)).delete(reviewCaptor.capture());
			
			Review deletedReview = reviewCaptor.getValue();
			
			assertThat(deletedReview)
					.usingRecursiveComparison()
					.isEqualTo(expectedReview);
		}
		
		@Test
		@DisplayName("Если отзыва не существует, должен выбросить ошибку ResourceNotFoundException")
		void deleteReview_whenReviewDoesNotExist_shouldThrowException() {
			// Arrange
			Long reviewId = testReview.getId();
			
			when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> reviewService.deleteReview(reviewId, currentUser))
					.isInstanceOf(ResourceNotFoundException.class);
			
			// Assert
			verify(reviewRepository, never()).delete(any());
		}
		
		@Test
		@DisplayName("Если пользователь ни автор существующего отзыва, ни администратор, то должен выбросить ошибку AccessDenied")
		void deleteReview_whenUserIsNotAuthorOrAdmin_shouldThrowException() {
			// Arrange
			Long reviewId = testReview.getId();
			Long hackerId = testUser.getId() + 999L;
			currentUser = new CustomUserDetails(
					hackerId,
					"Другой пользователь",
					"Хэш пароля",
					List.of(new SimpleGrantedAuthority("ROLE_USER"))
			);
			
			when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
			
			// Act & Assert
			assertThatThrownBy(() -> reviewService.deleteReview(reviewId, currentUser))
					.isInstanceOf(AccessDeniedException.class);
			
			// Assert
			verify(reviewRepository, never()).delete(any());
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getReviewById")
	class GetReviewByIdTest {
		@Test
		@DisplayName("Если отзыв существует, должен вернуть существующий отзыв по его номеру без изменений")
		void getReviewsById_whenReviewExist_shouldReturnExistingReview() {
			// Arrange
			Long reviewId = testReview.getId();
			
			when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
			
			// Expected
			Review expectedReview = ClassesExamples.getExistingReview();
			expectedReview.setUser(ClassesExamples.getExistingUser());
			
			// Act
			Review returnedReview = reviewService.getReviewById(reviewId);
			
			// Assert
			assertThat(returnedReview)
					.usingRecursiveComparison()
					.isEqualTo(expectedReview);
		}
		
		@Test
		@DisplayName("Если отзыва не существует, должен выбросить исключение ResourceNotFound")
		void getReviewsById_whenReviewDoesNotExist_shouldThrowException() {
			// Arrange
			Long reviewId = testReview.getId();
			
			when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> reviewService.getReviewById(reviewId))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
}