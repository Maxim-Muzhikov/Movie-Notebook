package com.movienotebook.api.controller;

import com.movienotebook.api.dto.movie.MovieResponseDto;
import com.movienotebook.api.dto.movie.SearchMovieRequestDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.mapper.MovieMapper;
import com.movienotebook.api.mapper.ReviewMapper;
import com.movienotebook.api.service.MovieService;
import com.movienotebook.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {
	
	private final MovieService movieService;
	private final ReviewService reviewService;
	private final MovieMapper movieMapper;
	private final ReviewMapper reviewMapper;
	
	// NOTE @ModelAttribute вместо @RequestBody
	// LEARN @ModelAttribute
	@GetMapping("/search")
	public ResponseEntity<Page<MovieResponseDto>> searchMovies(
			@Valid @ModelAttribute SearchMovieRequestDto request) {
		
		Page<MovieResponseDto> page = movieService.searchMovie(request)
				.map(movieMapper::toDto);
		
		return ResponseEntity.ok(page);
	}
	
	@GetMapping("/{id}/reviews")
	public ResponseEntity<List<ReviewResponseDto>> getReviews(
			@PathVariable Long id) {
		
		return ResponseEntity.ok(reviewService.getReviewsByMovieId(id)
				.stream()
				.map(reviewMapper::toDto)
				.toList());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<MovieResponseDto> getMovie(
			@PathVariable Long id) {
		
		return ResponseEntity.ok(movieMapper.toDto(movieService.getById(id)));
	}
}

