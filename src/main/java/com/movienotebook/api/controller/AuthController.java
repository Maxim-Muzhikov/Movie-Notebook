package com.movienotebook.api.controller;

import com.movienotebook.api.dto.auth.LoginRequestDto;
import com.movienotebook.api.dto.auth.LoginResponseDto;
import com.movienotebook.api.dto.auth.RegisterRequestDto;
import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Эндпоинты для регистрации и авторизации пользователей")
public class AuthController {
	
	private final AuthService authService;
	
	@Operation(
			summary = "Регистрация нового пользователя",
			description = "Создает новую учетную запись пользователя на основе переданных данных (username, email, пароль). Также требует подтверждения пользовательского соглашения."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
			@ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
			@ApiResponse(responseCode = "409", description = "Конфликт: Пользователь с таким email или именем пользователя уже существует")
	})
	@PostMapping("/register")
	public ResponseEntity<UserResponseDto> register(
			@Valid @RequestBody RegisterRequestDto request) {
		
		UserResponseDto response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@Operation(
			summary = "Авторизация пользователя",
			description = "Проверяет учетные данные пользователя (логин и пароль) и, в случае успеха, возвращает JWT токен для доступа к защищенным эндпоинтам."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Успешная авторизация, токен доступа возвращен"),
			@ApiResponse(responseCode = "400", description = "Ошибка валидации переданных данных"),
			@ApiResponse(responseCode = "401", description = "Неавторизован: Неверное имя пользователя или пароль")
	})
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(
			@Valid @RequestBody LoginRequestDto request) {
		
		LoginResponseDto response = authService.login(request);
		return ResponseEntity.ok(response);
	}
}