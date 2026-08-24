package com.movienotebook.api.controller;

import com.movienotebook.api.dto.collection.CollectionResponseDto;
import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.CollectionService;
import com.movienotebook.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	private final CollectionService collectionService;
	
	@GetMapping("/me")
	public ResponseEntity<UserResponseDto> getCurrentUserProfile(
			@AuthenticationPrincipal CustomUserDetails userDetails){
		
		String username = userDetails.getUsername();
		UserResponseDto response = userService.getByUsername(username);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{userId}/collections")
	public ResponseEntity<List<CollectionResponseDto>> getUsersCollections(
			@PathVariable Long userId) {
		
		List<CollectionResponseDto> response = collectionService.getPublic(userId);
		return ResponseEntity.ok(response);
	}
}