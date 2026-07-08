package com.movienotebook.api.dto.user;

import java.time.OffsetDateTime;

public record UserResponseDto(
		Long id,
		String username,
		String email,
		String role,
		OffsetDateTime createdAt
) {
}
