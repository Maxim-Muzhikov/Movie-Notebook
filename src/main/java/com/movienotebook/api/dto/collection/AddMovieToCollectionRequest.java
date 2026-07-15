package com.movienotebook.api.dto.collection;

import jakarta.validation.constraints.NotNull;

public record AddMovieToCollectionRequest(
		@NotNull(message = "Идентификатор фильма не может быть пустым")
		Long movieId
) {
}
