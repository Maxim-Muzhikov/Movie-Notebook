package com.movienotebook.api.controller;

import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.security.CustomUserDetailsService;
import com.movienotebook.api.security.JwtService;
import com.movienotebook.api.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureJsonTesters
class ReviewControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private JacksonTester<ReviewRequestDto> reviewRequestJsonTester;
	
	@MockitoBean
	private JwtService jwtService;
	
	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;
	
	@MockitoBean
	private ReviewService reviewService;
	
	@Nested
	@DisplayName("PUT /api/v1/reviews/{reviewId} - Обновление отзыва")
	class UpdateReviewTests {
		
		@Test
		@DisplayName("Успешное обновление отзыва (200)")
		void updateReview_ValidRequest_Returns200() throws Exception {
			// Arrange
			Long reviewId = 1L;
			String updatedContent = "Это обновленный текст отзыва, он должен быть достаточно длинным.";
			var request = new ReviewRequestDto(updatedContent);
			
			var response = new ReviewResponseDto(
					reviewId,
					updatedContent,
					OffsetDateTime.parse("2026-09-01T12:00:00Z"),
					"testuser"
			);
			
			var userDetails = new CustomUserDetails(1L, "testuser", "pass", List.of());
			
			when(reviewService.update(eq(reviewId), any(ReviewRequestDto.class), any()))
					.thenReturn(response);
			
			// Act & Assert
			mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId)
							.with(user(userDetails))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(reviewRequestJsonTester.write(request).getJson()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(reviewId))
					.andExpect(jsonPath("$.content").value(updatedContent))
					.andExpect(jsonPath("$.authorUsername").value("testuser"));
			
			verify(reviewService).update(eq(reviewId), any(ReviewRequestDto.class), any());
		}
		
		@Test
		@DisplayName("Ошибка валидации слишком короткого текста (400)")
		void updateReview_InvalidBody_Returns400() throws Exception {
			// Arrange
			Long reviewId = 1L;
			var invalidRequest = new ReviewRequestDto("Коротко");
			
			// Act & Assert
			mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId)
							.with(user("testuser"))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(reviewRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(reviewService);
		}
	}
	
	@Nested
	@DisplayName("DELETE /api/v1/reviews/{reviewId} - Удаление отзыва")
	class DeleteReviewTests {
		
		@Test
		@DisplayName("Успешное удаление отзыва (204)")
		void deleteReview_ValidId_Returns204() throws Exception {
			// Arrange
			Long reviewId = 1L;
			var userDetails = new CustomUserDetails(1L, "testuser", "pass", List.of());
			
			// Act & Assert
			mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId)
							.with(user(userDetails))
							.with(csrf()))
					.andExpect(status().isNoContent()); // 204 No Content
			
			verify(reviewService).delete(eq(reviewId), any(CustomUserDetails.class));
		}
	}
}