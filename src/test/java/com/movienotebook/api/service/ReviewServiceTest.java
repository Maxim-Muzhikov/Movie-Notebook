package com.movienotebook.api.service;

import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.mapper.ReviewMapper;
import com.movienotebook.api.repository.ReportRepository;
import com.movienotebook.api.repository.ReviewRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
	
	@Mock
	private ReviewRepository reviewRepository;
	
	@Mock
	private MovieService movieService;
	
	@Mock
	private UserService userService;
	
	@Mock
	private ReviewMapper reviewMapper;
	
	@Mock
	private ReportRepository reportRepository; // Инжектится в сервис, но не используется в методах напрямую
	
	@InjectMocks
	private ReviewService reviewService;
	
	@Captor
	private ArgumentCaptor<Review> reviewCaptor;
	
	private User testUser;
	private Movie testMovie;
	private Review testReview;
	private CustomUserDetails currentUser;
	private ReviewResponseDto mockReviewResponseDto;
	
	@BeforeEach
	void setUp() {
		testUser = ClassesExamples.getExistingUser();
		testMovie = ClassesExamples.getExistingMovie();
		testReview = ClassesExamples.getExistingReview();
		testReview.setUser(testUser);
		testReview.setMovie(testMovie);
		
		currentUser = new CustomUserDetails(
				testUser.getId(),
				testUser.getUsername(),
				testUser.getPasswordHash(),
				List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		
		mockReviewResponseDto = new ReviewResponseDto(
				testReview.getId(),
				testReview.getContent(),
				OffsetDateTime.now(),
				testUser.getUsername()
		);
	}
	
	@Nested
	@DisplayName("Тесты метода save")
	class SaveTests {
		
		@Test
		@DisplayName("Если отзыв пользователя на фильм уже существует, должен обновить контент и вернуть DTO")
		void save_whenReviewExists_shouldUpdateContentAndReturnDto() {
			// Arrange
			String newContent = "Обновленный контент";
			ReviewRequestDto request = new ReviewRequestDto(testMovie.getId(), newContent);
			
			when(movieService.getEntityById(testMovie.getId())).thenReturn(testMovie);
			when(reviewRepository.findByMovieIdAndUserId(testMovie.getId(), currentUser.getId()))
					.thenReturn(Optional.of(testReview));
			when(reviewMapper.toDto(testReview)).thenReturn(mockReviewResponseDto);
			
			// Act
			ReviewResponseDto result = reviewService.save(request, currentUser);
			
			// Assert
			verify(reviewRepository).save(reviewCaptor.capture());
			Review savedReview = reviewCaptor.getValue();
			
			assertThat(savedReview)
					.isSameAs(testReview);
			
			assertThat(savedReview.getContent())
					.isEqualTo(newContent);
			
			assertThat(result)
					.isSameAs(mockReviewResponseDto);
		}
		
		@Test
		@DisplayName("Если отзыва пользователя на фильм нет, должен создать новый, сохранить и вернуть DTO")
		void save_whenReviewDoesNotExist_shouldCreateNewReviewAndReturnDto() {
			// Arrange
			String newContent = "Новый отличный отзыв";
			ReviewRequestDto request = new ReviewRequestDto(testMovie.getId(), newContent);
			
			when(movieService.getEntityById(testMovie.getId())).thenReturn(testMovie);
			when(reviewRepository.findByMovieIdAndUserId(testMovie.getId(), currentUser.getId()))
					.thenReturn(Optional.empty());
			when(userService.getReferenceById(currentUser.getId())).thenReturn(testUser);
			when(reviewMapper.toDto(any(Review.class))).thenReturn(mockReviewResponseDto);
			
			// Expected
			Review expectedState = new Review();
			expectedState.setMovie(testMovie);
			expectedState.setUser(testUser);
			expectedState.setContent(newContent);
			
			// Act
			ReviewResponseDto result = reviewService.save(request, currentUser);
			
			// Assert
			verify(reviewRepository).save(reviewCaptor.capture());
			Review savedReview = reviewCaptor.getValue();
			
			assertThat(savedReview)
					.usingRecursiveComparison()
					.ignoringFields("id", "isDeleted", "updatedAt", "createdAt") // Поля, устанавливаемые БД или Hibernate
					.isEqualTo(expectedState);
			
			assertThat(result)
					.isSameAs(mockReviewResponseDto);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода delete")
	class DeleteTests {
		
		@Test
		@DisplayName("Если отзыв существует и пользователь является автором, должен удалить отзыв")
		void delete_whenReviewExistsAndUserIsAuthor_shouldDeleteReview() {
			// Arrange
			Long reviewId = testReview.getId();
			when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
			
			// Act
			reviewService.delete(reviewId, currentUser);
			
			// Assert
			verify(reviewRepository).delete(testReview);
		}
		
		@Test
		@DisplayName("Если отзыв существует и пользователь администратор, должен удалить отзыв")
		void delete_whenReviewExistsAndUserIsAdmin_shouldDeleteReview() {
			// Arrange
			Long reviewId = testReview.getId();
			User author = ClassesExamples.getExistingUser();
			author.setId(999L);
			testReview.setUser(author);
			
			CustomUserDetails adminUser = new CustomUserDetails(
					1L, "admin", "hash", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
			);
			
			when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
			
			// Act
			reviewService.delete(reviewId, adminUser);
			
			// Assert
			verify(reviewRepository).delete(testReview);
		}
		
		@Test
		@DisplayName("Если отзыва не существует, должен выбросить исключение ResourceNotFoundException")
		void delete_whenReviewDoesNotExist_shouldThrowException() {
			// Arrange
			Long reviewId = 999L;
			when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> reviewService.delete(reviewId, currentUser))
					.isInstanceOf(ResourceNotFoundException.class);
		}
		
		@Test
		@DisplayName("Если пользователь не автор и не администратор, должен выбросить исключение AccessDeniedException")
		void delete_whenUserIsNotAuthorOrAdmin_shouldThrowException() {
			// Arrange
			Long reviewId = testReview.getId();
			User author = ClassesExamples.getExistingUser();
			author.setId(999L); // ID отличается от currentUser
			testReview.setUser(author);
			
			when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
			
			// Act & Assert
			assertThatThrownBy(() -> reviewService.delete(reviewId, currentUser))
					.isInstanceOf(AccessDeniedException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода findAllByMovieId")
	class FindAllByMovieIdTests {
		
		@Test
		@DisplayName("Если отзывы существуют, должен вернуть список DTO")
		void findAllByMovieId_whenReviewsExist_shouldReturnListOfDtos() {
			// Arrange
			Long movieId = testMovie.getId();
			when(reviewRepository.findAllByMovieId(movieId)).thenReturn(List.of(testReview));
			when(reviewMapper.toDto(testReview)).thenReturn(mockReviewResponseDto);
			
			// Act
			List<ReviewResponseDto> result = reviewService.findAllByMovieId(movieId);
			
			// Assert
			assertThat(result)
					.hasSize(1)
					.element(0).isSameAs(mockReviewResponseDto);
		}
		
		@Test
		@DisplayName("Если отзывов нет, должен вернуть пустой список")
		void findAllByMovieId_whenNoReviewsExist_shouldReturnEmptyList() {
			// Arrange
			Long movieId = testMovie.getId();
			when(reviewRepository.findAllByMovieId(movieId)).thenReturn(Collections.emptyList());
			
			// Act
			List<ReviewResponseDto> result = reviewService.findAllByMovieId(movieId);
			
			// Assert
			assertThat(result).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getById")
	class GetByIdTests {
		
		@Test
		@DisplayName("Если отзыв существует, должен вернуть его DTO")
		void getById_whenReviewExists_shouldReturnDto() {
			// Arrange
			Long id = testReview.getId();
			when(reviewRepository.findById(id)).thenReturn(Optional.of(testReview));
			when(reviewMapper.toDto(testReview)).thenReturn(mockReviewResponseDto);
			
			// Act
			ReviewResponseDto result = reviewService.getById(id);
			
			// Assert
			assertThat(result).isSameAs(mockReviewResponseDto);
		}
		
		@Test
		@DisplayName("Если отзыва не существует, должен выбросить исключение ResourceNotFoundException")
		void getById_whenReviewDoesNotExist_shouldThrowException() {
			// Arrange
			Long id = 999L;
			when(reviewRepository.findById(id)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> reviewService.getById(id))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода findAllEntityByMovieId")
	class FindAllEntityByMovieIdTests {
		
		@Test
		@DisplayName("Всегда возвращает результат из репозитория по ссылке")
		void findAllEntityByMovieId_always_shouldReturnListFromRepository() {
			// Arrange
			Long movieId = testMovie.getId();
			List<Review> expectedList = List.of(testReview);
			when(reviewRepository.findAllByMovieId(movieId)).thenReturn(expectedList);
			
			// Act
			List<Review> result = reviewService.findAllEntityByMovieId(movieId);
			
			// Assert
			assertThat(result).isSameAs(expectedList);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getEntityById")
	class GetEntityByIdTests {
		
		@Test
		@DisplayName("Если отзыв существует, должен вернуть его сущность")
		void getEntityById_whenReviewExists_shouldReturnEntity() {
			// Arrange
			Long id = testReview.getId();
			when(reviewRepository.findById(id)).thenReturn(Optional.of(testReview));
			
			// Act
			Review result = reviewService.getEntityById(id);
			
			// Assert
			assertThat(result).isSameAs(testReview);
		}
		
		@Test
		@DisplayName("Если отзыва не существует, должен выбросить исключение ResourceNotFoundException")
		void getEntityById_whenReviewDoesNotExist_shouldThrowException() {
			// Arrange
			Long id = 999L;
			when(reviewRepository.findById(id)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> reviewService.getEntityById(id))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
}