package com.movienotebook.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
		@NotBlank(message = "Имя пользователя не может быть пустым")
		@Size(min = 3, max = 50, message = "Имя пользователя содержит от 3 до 50 символов")
		String username,
		
		@NotBlank(message = "Пароль не может быть пустым")
		@Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
		String password
) {
}
