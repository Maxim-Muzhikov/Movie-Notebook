package com.movienotebook.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на аутентификацию (вход в систему)")
public record LoginRequestDto(
		@Schema(description = "Имя пользователя", example = "john_doe")
		@NotBlank(message = "Имя пользователя не может быть пустым")
		@Size(min = 3, max = 50, message = "Имя пользователя содержит от 3 до 50 символов")
		String username,
		
		@Schema(description = "Пароль пользователя", example = "P@ssword123")
		@NotBlank(message = "Пароль не может быть пустым")
		@Size(min = 8, max = 50, message = "Пароль должен содержать от 8 до 50 символов")
		String password
) {
}