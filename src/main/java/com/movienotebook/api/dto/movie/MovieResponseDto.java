package com.movienotebook.api.dto.movie;

import java.math.BigDecimal;

public record MovieResponseDto(
		Long id,
		Long externalId,
		String title,
		String originalTitle,
		String description,
		Integer releaseYear,
		String posterUrl,
		BigDecimal averageRating
) {
}
