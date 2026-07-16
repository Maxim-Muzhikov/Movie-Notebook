package com.movienotebook.api.dto.collection;

import com.movienotebook.api.dto.movie.MovieShortResponseDto;

import java.time.OffsetDateTime;
import java.util.List;

public record CollectionWithMoviesResponseDto(
		Long id,
		String name,
		String description,
		Boolean isPublic,
		String authorUsername,
		List<MovieShortResponseDto> movies,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
