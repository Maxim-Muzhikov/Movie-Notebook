package com.movienotebook.api.dto.rating;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Ответ с обновленным средним рейтингом фильма")
public record RatingResponseDto(
		@Schema(description = "Новый средний рейтинг фильма (округлен до 2 знаков после запятой)", example = "8.65")
		BigDecimal newAverageRating
) {
}