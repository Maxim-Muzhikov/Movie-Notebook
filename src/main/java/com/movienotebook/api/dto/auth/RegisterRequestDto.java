package com.movienotebook.api.dto.auth;

import jakarta.validation.constraints.*;

public record RegisterRequestDto (
		@NotBlank(message = "Имя пользователя не может быть пустым")
		@Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
		String username,
		
		@NotBlank(message = "Email не может быть пустым")
		@Email(message = "Некорректный формат email")
		String email,
		
		@NotBlank(message = "Пароль не может быть пустым")
		@Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
		String password,
		
		@NotNull(message = "Необходимо передать статус согласия с политикой")
		@AssertTrue(message = "Вы должны принять пользовательское соглашение (152-ФЗ)")
		Boolean agreementAccepted
){

}
