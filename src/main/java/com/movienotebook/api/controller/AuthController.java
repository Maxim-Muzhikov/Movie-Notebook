package com.movienotebook.api.controller;

import com.movienotebook.api.dto.auth.LoginRequestDto;
import com.movienotebook.api.dto.auth.LoginResponseDto;
import com.movienotebook.api.dto.auth.RegisterRequestDto;
import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.integration.KinopoiskIntegrationService;
import com.movienotebook.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final AuthService authService;
	
	@PostMapping("/register")
	public ResponseEntity<UserResponseDto> register(
			@Valid @RequestBody RegisterRequestDto request) {
		
		UserResponseDto response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(
			@Valid @RequestBody LoginRequestDto request) {
		
		LoginResponseDto response = authService.login(request);
		return ResponseEntity.ok(response);
	}
}