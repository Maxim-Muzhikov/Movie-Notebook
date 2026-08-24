package com.movienotebook.api.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequestDto(
		@NotBlank(message = "Содержание отзыва не может быть пустым")
		@Size(min = 8, max = 1000, message = "Длина отзыва должна быть не менее 8 и не более 1000 символов")
		String content
) {
}