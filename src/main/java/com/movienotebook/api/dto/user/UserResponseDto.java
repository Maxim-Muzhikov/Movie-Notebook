package com.movienotebook.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Данные профиля пользователя")
public record UserResponseDto(
		@Schema(description = "Уникальный идентификатор пользователя", example = "1")
		Long id,
		
		@Schema(description = "Имя пользователя", example = "john_doe")
		String username,
		
		@Schema(description = "Адрес электронной почты", example = "john.doe@example.com")
		String email,
		
		@Schema(description = "Роль пользователя в системе", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_ADMIN"})
		String role,
		
		@Schema(description = "Дата и время регистрации в формате ISO 8601", example = "2024-03-30T15:30:00Z")
		OffsetDateTime createdAt
) {
}