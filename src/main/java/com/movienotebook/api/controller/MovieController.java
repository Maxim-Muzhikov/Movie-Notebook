package com.movienotebook.api.controller;

import com.movienotebook.api.dto.movie.MovieResponseDto;
import com.movienotebook.api.dto.movie.SearchMovieRequestDto;
import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.dto.rating.RatingResponseDto;
import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.MovieService;
import com.movienotebook.api.service.RatingService;
import com.movienotebook.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {
	
	private final MovieService movieService;
	private final ReviewService reviewService;
	private final RatingService ratingService;
	
	@GetMapping()
	public ResponseEntity<Page<MovieResponseDto>> searchMovies(
			@Valid @ModelAttribute SearchMovieRequestDto request) {
			
		Page<MovieResponseDto> response = movieService.searchMovies(request);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{movieId}/reviews")
	public ResponseEntity<List<ReviewResponseDto>> getReviews(
			@PathVariable Long movieId) {
		
		List<ReviewResponseDto> response = reviewService.findAllByMovieId(movieId);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{movieId}")
	public ResponseEntity<MovieResponseDto> getMovie(
			@PathVariable Long movieId) {
		
		MovieResponseDto response = movieService.getById(movieId);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/{movieId}/reviews")
	public ResponseEntity<ReviewResponseDto> reviewMovie(
			@PathVariable Long movieId,
			@Valid @RequestBody ReviewRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		ReviewResponseDto response = reviewService.save(movieId, request, userDetails);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@PutMapping("/{movieId}/ratings")
	public ResponseEntity<RatingResponseDto> rateMovie(
			@PathVariable Long movieId,
			@Valid @RequestBody RatingRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		RatingResponseDto response = ratingService.save(movieId, request, userDetails);
		return ResponseEntity.ok(response);
	}
}

