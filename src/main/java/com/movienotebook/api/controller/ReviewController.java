package com.movienotebook.api.controller;

import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Отзывы", description = "Редактирование и удаление существующих отзывов к фильмам")
public class ReviewController {
	
	private final ReviewService reviewService;
	
	@Operation(
			summary = "Обновление отзыва",
			description = "Позволяет изменить текст уже существующего отзыва. Редактировать отзыв может только его автор (либо администратор/модератор, если это предусмотрено правами)."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Отзыв успешно обновлен"),
			@ApiResponse(responseCode = "400", description = "Ошибка валидации переданных данных (например, текст отзыва слишком короткий или длинный)"),
			@ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
			@ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка редактировать чужой отзыв)"),
			@ApiResponse(responseCode = "404", description = "Отзыв с указанным ID не найден")
	})
	@PutMapping("/{reviewId}")
	public ResponseEntity<ReviewResponseDto> updateReview (
			@PathVariable Long reviewId,
			@Valid @RequestBody ReviewRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		ReviewResponseDto response = reviewService.update(reviewId, request, userDetails);
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "Удаление отзыва",
			description = "Полностью удаляет отзыв по его ID. Удалить отзыв может только его автор (или администратор/модератор)."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Отзыв успешно удален"),
			@ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
			@ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка удалить чужой отзыв)"),
			@ApiResponse(responseCode = "404", description = "Отзыв с указанным ID не найден")
	})
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> deleteReview (
			@PathVariable Long reviewId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		reviewService.delete(reviewId, userDetails);
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
}