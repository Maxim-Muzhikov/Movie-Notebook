package com.movienotebook.api.dto.collection;

import java.time.OffsetDateTime;

public record CollectionResponseDto(
		Long id,
		String name,
		String description,
		Boolean isPublic,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
