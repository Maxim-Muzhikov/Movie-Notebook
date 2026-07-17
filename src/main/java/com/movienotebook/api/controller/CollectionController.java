package com.movienotebook.api.controller;

import com.movienotebook.api.dto.collection.CollectionRequestDto;
import com.movienotebook.api.dto.collection.CollectionResponseDto;
import com.movienotebook.api.dto.collection.CollectionWithMoviesResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class CollectionController {

	private final CollectionService collectionService;
	
	@GetMapping("/{collectionId}")
	public ResponseEntity<CollectionResponseDto> getCollection (
			@PathVariable Long collectionId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(collectionService.getById(collectionId, userDetails));
	}
	
	@GetMapping("/{collectionId}/movies")
	public ResponseEntity<CollectionWithMoviesResponseDto> getCollectionWithMovies (
			@PathVariable Long collectionId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(collectionService.getWithMoviesById(collectionId, userDetails));
	}
	
	@GetMapping
	public ResponseEntity<List<CollectionResponseDto>> getMyCollections (
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(collectionService.getUserCollections(userDetails));
	}
	
	@PostMapping
	public ResponseEntity<CollectionResponseDto> createCollection (
			@Valid @RequestBody CollectionRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.status(HttpStatus.CREATED).body(collectionService.createCollection(request, userDetails));
	}
	
	@DeleteMapping("/{collectionId}")
	public ResponseEntity<Void> deleteCollection (
			@PathVariable Long collectionId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		collectionService.deleteCollection(collectionId, userDetails);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{collectionId}/movies/{movieId}")
	public ResponseEntity<Void> addMovieToCollection (
			@PathVariable Long collectionId,
			@PathVariable Long movieId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		collectionService.addMovieToTheCollection(collectionId, movieId, userDetails);
		return ResponseEntity.ok().build();
	}
	
	@DeleteMapping("/{collectionId}/movies/{movieId}")
	public ResponseEntity<Void> removeMovieFromCollection (
			@PathVariable Long collectionId,
			@PathVariable Long movieId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		collectionService.removeMovieFromCollection(collectionId, movieId, userDetails);
		return ResponseEntity.ok().build();
	}
}
