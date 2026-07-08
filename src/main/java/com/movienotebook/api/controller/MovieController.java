package com.movienotebook.api.controller;

import com.movienotebook.api.dto.movie.MovieResponseDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.mapper.MovieMapper;
import com.movienotebook.api.mapper.ReviewMapper;
import com.movienotebook.api.service.MovieService;
import com.movienotebook.api.service.ReviewService;
import lombok.RequiredArgsConstructor;
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
	
	@GetMapping("/search")
	public ResponseEntity<List<MovieResponseDto>> searchMovies(
			@RequestParam String query,
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "false") Boolean deepSearch) {
		
		return ResponseEntity.ok(movieService.searchMovie(query, page, deepSearch)
				.stream()
				.map(movieMapper::toDto)
				.toList());
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

