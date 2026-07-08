package com.movienotebook.api.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RatingRequestDto(
		@NotNull(message = "ID фильма не может быть пустым")
		Long movieId,
		
		@NotNull(message = "Оценка не может быть пустой")
		@Min(value = 1, message = "Оценка должна быть не меньше 1")
		@Max(value = 10, message = "Оценка должна быть не больше 10")
		Integer score
) {
}