package com.movienotebook.api.controller;

import com.movienotebook.api.dto.collection.CollectionResponseDto;
import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.CollectionService;
import com.movienotebook.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Пользователи", description = "Получение данных о профиле пользователя и просмотр публичной информации других пользователей")
public class UserController {
	
	private final UserService userService;
	private final CollectionService collectionService;
	
	@Operation(
			summary = "Получение профиля текущего пользователя",
			description = "Возвращает подробные данные профиля авторизованного пользователя (ID, username, email, роль, дату создания аккаунта)."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Данные профиля успешно получены"),
			@ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
			@ApiResponse(responseCode = "404", description = "Пользователь не найден")
	})
	@GetMapping("/me")
	public ResponseEntity<UserResponseDto> getCurrentUserProfile(
			@AuthenticationPrincipal CustomUserDetails userDetails){
		
		String username = userDetails.getUsername();
		UserResponseDto response = userService.getByUsername(username);
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "Получение публичных коллекций пользователя",
			description = "Возвращает список всех публичных коллекций (подборок фильмов), созданных пользователем с указанным ID."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Список публичных коллекций успешно получен"),
			@ApiResponse(responseCode = "404", description = "Пользователь с указанным ID не найден")
	})
	@GetMapping("/{userId}/collections")
	public ResponseEntity<List<CollectionResponseDto>> getUsersCollections(
			@PathVariable Long userId) {
		
		List<CollectionResponseDto> response = collectionService.getPublic(userId);
		return ResponseEntity.ok(response);
	}
}