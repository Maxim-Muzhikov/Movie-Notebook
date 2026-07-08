package com.movienotebook.api.dto.rating;

import java.math.BigDecimal;

public record RatingResponseDto(
		BigDecimal newAverageRating
) {
}
