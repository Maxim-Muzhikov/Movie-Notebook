package com.movienotebook.api.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequestDto(
		@NotNull(message = "ID фильма не может быть пустым")
		Long movieId,
		
		@NotBlank(message = "Содержание отзыва не может быть пустым")
		String content
) {
}