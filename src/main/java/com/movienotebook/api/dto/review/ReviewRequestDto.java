package com.movienotebook.api.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на написание или редактирование отзыва к фильму")
public record ReviewRequestDto(
		@Schema(description = "Текст отзыва (от 8 до 1000 символов)", example = "Потрясающий фильм с невероятным визуальным рядом и глубоким эмоциональным сюжетом.")
		@NotBlank(message = "Содержание отзыва не может быть пустым")
		@Size(min = 8, max = 1000, message = "Длина отзыва должна быть не менее 8 и не более 1000 символов")
		String content
) {
}