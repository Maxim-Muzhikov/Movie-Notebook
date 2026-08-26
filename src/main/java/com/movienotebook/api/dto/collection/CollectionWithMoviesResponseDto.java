package com.movienotebook.api.dto.collection;

import com.movienotebook.api.dto.movie.MovieShortResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Детальная информация о коллекции со списком входящих в нее фильмов")
public record CollectionWithMoviesResponseDto(
		@Schema(description = "Уникальный идентификатор коллекции", example = "1")
		Long id,
		
		@Schema(description = "Название коллекции", example = "Любимая научная фантастика")
		String name,
		
		@Schema(description = "Описание коллекции", example = "Подборка лучших научно-фантастических фильмов всех времен", nullable = true)
		String description,
		
		@Schema(description = "Флаг публичности коллекции", example = "true")
		Boolean isPublic,
		
		@Schema(description = "Имя пользователя автора коллекции", example = "john_doe")
		String authorUsername,
		
		@Schema(description = "Список фильмов в коллекции")
		List<MovieShortResponseDto> movies,
		
		@Schema(description = "Дата и время создания коллекции в формате ISO 8601", example = "2024-03-30T15:30:00Z")
		OffsetDateTime createdAt,
		
		@Schema(description = "Дата и время последнего обновления коллекции в формате ISO 8601", example = "2024-03-30T16:00:00Z")
		OffsetDateTime updatedAt
) {
}