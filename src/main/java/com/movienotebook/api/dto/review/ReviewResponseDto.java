package com.movienotebook.api.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Информация об отзыве к фильму")
public record ReviewResponseDto(
		@Schema(description = "Уникальный идентификатор отзыва", example = "1")
		Long id,
		
		@Schema(description = "Текст отзыва", example = "Потрясающий фильм с невероятным визуальным рядом и глубоким эмоциональным сюжетом.")
		String content,
		
		@Schema(description = "Дата и время публикации отзыва в формате ISO 8601", example = "2024-03-30T15:30:00Z")
		OffsetDateTime createdAt,
		
		@Schema(description = "Имя пользователя автора отзыва", example = "john_doe")
		String authorUsername
) {
}