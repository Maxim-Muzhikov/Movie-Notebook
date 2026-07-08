package com.movienotebook.api.controller;

import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.dto.rating.RatingResponseDto;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.service.RatingService;
import com.movienotebook.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {
	
	private final UserService userService;
	private final RatingService ratingService;
	
	@PostMapping("/set")
	public ResponseEntity<RatingResponseDto> rateMovie(
			@Valid @RequestBody RatingRequestDto request,
			@AuthenticationPrincipal UserDetails userDetails) {
		
		User user = userService.getByUsername(userDetails.getUsername());
		
		BigDecimal newAvg = ratingService.addOrUpdateRating(request, user);
		
		return ResponseEntity.ok(new RatingResponseDto(newAvg));
	}
}
