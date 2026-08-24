package com.movienotebook.api.controller;

import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
	
	private final ReviewService reviewService;
	
	@PutMapping("/{reviewId}")
	public ResponseEntity<ReviewResponseDto> updateReview (
			@PathVariable Long reviewId,
			@Valid @RequestBody ReviewRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		ReviewResponseDto response = reviewService.update(reviewId, request, userDetails);
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> deleteReview (
			@PathVariable Long reviewId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		reviewService.delete(reviewId, userDetails);
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
}
