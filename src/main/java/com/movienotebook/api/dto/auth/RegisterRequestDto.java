package com.movienotebook.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Запрос на регистрацию нового пользователя")
public record RegisterRequestDto(
		@Schema(description = "Уникальное имя пользователя (от 3 до 50 символов)", example = "john_doe")
		@NotBlank(message = "Имя пользователя не может быть пустым")
		@Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
		String username,
		
		@Schema(description = "Адрес электронной почты", example = "john.doe@example.com")
		@NotBlank(message = "Email не может быть пустым")
		@Email(message = "Некорректный формат email")
		String email,
		
		@Schema(description = "Пароль учетной записи (от 8 до 50 символов)", example = "P@ssword123")
		@NotBlank(message = "Пароль не может быть пустым")
		@Size(min = 8, max = 50, message = "Пароль должен содержать от 8 до 50 символов")
		String password,
		
		@Schema(description = "Согласие с пользовательским соглашением и политикой сервиса", example = "true")
		@NotNull(message = "Необходимо передать статус согласия с политикой")
		@AssertTrue(message = "Вы должны принять пользовательское соглашение")
		Boolean agreementAccepted
) {
}