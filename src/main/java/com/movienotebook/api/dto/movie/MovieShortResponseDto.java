package com.movienotebook.api.dto.movie;

import java.math.BigDecimal;

public record MovieShortResponseDto(
		Long id,
		String title,
		Integer releaseYear,
		String posterUrl,
		BigDecimal averageRating
) {
}
