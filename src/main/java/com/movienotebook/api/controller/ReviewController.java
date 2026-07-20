package com.movienotebook.api.controller;

import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.mapper.ReviewMapper;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.ReviewService;
import com.movienotebook.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
	
	private final ReviewService reviewService;
	private final UserService userService;
	private final ReviewMapper mapper;
	
	@PostMapping
	public ResponseEntity<ReviewResponseDto> reviewMovie(
			@Valid @RequestBody ReviewRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		ReviewResponseDto reviewResponseDto = mapper.toDto(reviewService.addOrUpdateReview(request, userDetails));
		
		return ResponseEntity.ok(reviewResponseDto);
		
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteReview (
			@PathVariable Long id,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		reviewService.deleteReview(id, userDetails);
		
		return ResponseEntity.ok().build();
	}
	
}
