package com.movienotebook.api.dto.review;

import java.time.OffsetDateTime;

public record ReviewResponseDto(
		Long id,
		String content,
		OffsetDateTime createdAt,
		String authorUsername
) {
}
