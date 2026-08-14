package com.movienotebook.api.controller;

import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	
	@GetMapping("/me")
	public ResponseEntity<UserResponseDto> getCurrentUserProfile(
			@AuthenticationPrincipal CustomUserDetails userDetails){
		
		String username = userDetails.getUsername();
		return ResponseEntity.ok(userService.getByUsername(username));
	}
}