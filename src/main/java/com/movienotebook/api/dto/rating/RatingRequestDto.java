package com.movienotebook.api.dto.rating;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на выставление или обновление оценки фильму")
public record RatingRequestDto(
		@Schema(description = "Пользовательская оценка фильма от 1 до 10", example = "9", minimum = "1", maximum = "10")
		@NotNull(message = "Оценка не может быть пустой")
		@Min(value = 1, message = "Оценка должна быть не меньше 1")
		@Max(value = 10, message = "Оценка должна быть не больше 10")
		Integer score
) {
}