package com.movienotebook.api.controller;

import com.movienotebook.api.dto.movie.MovieResponseDto;
import com.movienotebook.api.dto.movie.SearchMovieRequestDto;
import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.dto.rating.RatingResponseDto;
import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.security.CustomUserDetailsService;
import com.movienotebook.api.security.JwtService;
import com.movienotebook.api.service.MovieService;
import com.movienotebook.api.service.RatingService;
import com.movienotebook.api.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
@AutoConfigureJsonTesters
class MovieControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private JacksonTester<ReviewRequestDto> reviewRequestJsonTester;
	
	@Autowired
	private JacksonTester<RatingRequestDto> ratingRequestJsonTester;
	
	@MockitoBean
	private JwtService jwtService;
	
	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;
	
	@MockitoBean
	private MovieService movieService;
	
	@MockitoBean
	private ReviewService reviewService;
	
	@MockitoBean
	private RatingService ratingService;
	
	@Nested
	@DisplayName("GET /api/v1/movies - Поиск фильмов")
	class SearchMoviesTests {
		
		@Test
		@DisplayName("Успешный поиск фильмов (200)")
		void searchMovies_ValidRequest_Returns200() throws Exception {
			// Arrange
			var movieDto = new MovieResponseDto(
					1L, 258687L, "Интерстеллар", "Interstellar",
					"Описание фильма", 2014, "poster.jpg", BigDecimal.valueOf(8.65)
			);
			var page = new PageImpl<>(List.of(movieDto));
			
			when(movieService.searchMovies(any(SearchMovieRequestDto.class))).thenReturn(page);
			
			// Act & Assert
			mockMvc.perform(get("/api/v1/movies")
							.param("query", "Интерстеллар")
							.param("page", "1")
							.param("size", "20")
							.param("deepSearch", "false")
							.with(user("testuser")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content[0].id").value(1L))
					.andExpect(jsonPath("$.content[0].title").value("Интерстеллар"))
					.andExpect(jsonPath("$.content[0].releaseYear").value(2014));
			
			verify(movieService).searchMovies(any(SearchMovieRequestDto.class));
		}
		
		@Test
		@DisplayName("Ошибка валидации параметров (отсутствует query) (400)")
		void searchMovies_MissingQuery_Returns400() throws Exception {
			// Act & Assert
			mockMvc.perform(get("/api/v1/movies")
							.with(user("testuser")))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(movieService);
		}
	}
	
	@Nested
	@DisplayName("GET /api/v1/movies/{movieId}/reviews - Получение отзывов")
	class GetReviewsTests {
		
		@Test
		@DisplayName("Успешное получение списка отзывов (200)")
		void getReviews_ValidId_Returns200() throws Exception {
			// Arrange
			Long movieId = 1L;
			var review = new ReviewResponseDto(1L, "Потрясающий фильм!", OffsetDateTime.now(), "testuser");
			
			when(reviewService.findAllByMovieId(movieId)).thenReturn(List.of(review));
			
			// Act & Assert
			mockMvc.perform(get("/api/v1/movies/{movieId}/reviews", movieId)
							.with(user("testuser")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$[0].id").value(1L))
					.andExpect(jsonPath("$[0].content").value("Потрясающий фильм!"))
					.andExpect(jsonPath("$[0].authorUsername").value("testuser"));
			
			verify(reviewService).findAllByMovieId(movieId);
		}
	}
	
	@Nested
	@DisplayName("GET /api/v1/movies/{movieId} - Детали фильма")
	class GetMovieTests {
		
		@Test
		@DisplayName("Успешное получение детальной информации о фильме (200)")
		void getMovie_ValidId_Returns200() throws Exception {
			// Arrange
			Long movieId = 1L;
			var movieDto = new MovieResponseDto(
					movieId, 258687L, "Интерстеллар", "Interstellar",
					"Описание...", 2014, "poster.jpg", BigDecimal.valueOf(8.65)
			);
			
			when(movieService.getById(movieId)).thenReturn(movieDto);
			
			// Act & Assert
			mockMvc.perform(get("/api/v1/movies/{movieId}", movieId)
							.with(user("testuser")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(movieId))
					.andExpect(jsonPath("$.title").value("Интерстеллар"))
					.andExpect(jsonPath("$.averageRating").value(8.65));
			
			verify(movieService).getById(movieId);
		}
	}
	
	@Nested
	@DisplayName("POST /api/v1/movies/{movieId}/reviews - Добавление отзыва")
	class CreateReviewTests {
		
		@Test
		@DisplayName("Успешное создание отзыва (201)")
		void createReview_ValidRequest_Returns201() throws Exception {
			// Arrange
			Long movieId = 1L;
			String content = "Отличный фильм, рекомендую к просмотру!";
			var request = new ReviewRequestDto(content);
			
			var response = new ReviewResponseDto(
					1L,
					content,
					OffsetDateTime.parse("2026-09-01T10:00:00Z"),
					"testuser"
			);
			
			var userDetails = new CustomUserDetails(1L, "testuser", "pass", List.of());
			
			when(reviewService.save(eq(movieId), any(ReviewRequestDto.class), any()))
					.thenReturn(response);
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/movies/{movieId}/reviews", movieId)
							.with(user(userDetails))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(reviewRequestJsonTester.write(request).getJson()))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.id").value(1L))
					.andExpect(jsonPath("$.content").value(content))
					.andExpect(jsonPath("$.authorUsername").value("testuser"));
			
			verify(reviewService).save(eq(movieId), any(ReviewRequestDto.class), any());
		}
		
		@Test
		@DisplayName("Ошибка валидации пустого тела (400)")
		void createReview_InvalidBody_Returns400() throws Exception {
			// Arrange
			Long movieId = 1L;
			var invalidRequest = new ReviewRequestDto("");
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/movies/{movieId}/reviews", movieId)
							.with(user("testuser"))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(reviewRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(reviewService);
		}
	}
	
	@Nested
	@DisplayName("PUT /api/v1/movies/{movieId}/ratings - Оценка фильма")
	class RateMovieTests {
		
		@Test
		@DisplayName("Успешная установка или обновление оценки (200)")
		void rateMovie_ValidRequest_Returns200() throws Exception {
			// Arrange
			Long movieId = 1L;
			var request = new RatingRequestDto(9);
			var response = new RatingResponseDto(BigDecimal.valueOf(8.75));
			
			var userDetails = new CustomUserDetails(1L, "testuser", "pass", List.of());
			
			when(ratingService.save(eq(movieId), any(RatingRequestDto.class), any()))
					.thenReturn(response);
			
			// Act & Assert
			mockMvc.perform(put("/api/v1/movies/{movieId}/ratings", movieId)
							.with(user(userDetails))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(ratingRequestJsonTester.write(request).getJson()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.newAverageRating").value(8.75));
			
			verify(ratingService).save(eq(movieId), any(RatingRequestDto.class), any());
		}
		
		@Test
		@DisplayName("Ошибка валидации недопустимого значения оценки (400)")
		void rateMovie_InvalidScore_Returns400() throws Exception {
			// Arrange
			Long movieId = 1L;
			var invalidRequest = new RatingRequestDto(11);
			
			// Act & Assert
			mockMvc.perform(put("/api/v1/movies/{movieId}/ratings", movieId)
							.with(user("testuser"))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(ratingRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(ratingService);
		}
	}
}