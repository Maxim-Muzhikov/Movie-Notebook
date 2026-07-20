package com.movienotebook.api.controller;

import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.dto.rating.RatingResponseDto;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.RatingService;
import com.movienotebook.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {
	
	private final UserService userService;
	private final RatingService ratingService;
	
	@PutMapping
	public ResponseEntity<RatingResponseDto> rateMovie(
			@Valid @RequestBody RatingRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		BigDecimal newAvg = ratingService.addOrUpdateRating(request, userDetails);
		
		return ResponseEntity.ok(new RatingResponseDto(newAvg));
	}
}
