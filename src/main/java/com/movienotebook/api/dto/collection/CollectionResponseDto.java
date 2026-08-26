package com.movienotebook.api.dto.collection;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Информация о коллекции фильмов")
public record CollectionResponseDto(
		@Schema(description = "Уникальный идентификатор коллекции", example = "1")
		Long id,
		
		@Schema(description = "Название коллекции", example = "Любимая научная фантастика")
		String name,
		
		@Schema(description = "Описание коллекции", example = "Подборка лучших научно-фантастических фильмов всех времен", nullable = true)
		String description,
		
		@Schema(description = "Флаг публичности коллекции", example = "true")
		Boolean isPublic,
		
		@Schema(description = "Дата и время создания коллекции в формате ISO 8601", example = "2024-03-30T15:30:00Z")
		OffsetDateTime createdAt,
		
		@Schema(description = "Дата и время последнего обновления коллекции в формате ISO 8601", example = "2024-03-30T16:00:00Z")
		OffsetDateTime updatedAt
) {
}