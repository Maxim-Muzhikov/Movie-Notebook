package com.movienotebook.api.dto.collection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на создание или редактирование коллекции фильмов")
public record CollectionRequestDto(
		@Schema(description = "Название коллекции", example = "Любимая научная фантастика")
		@NotBlank(message = "Имя коллекции не может быть пустым")
		String name,
		
		@Schema(description = "Описание коллекции", example = "Подборка лучших научно-фантастических фильмов всех времен", nullable = true)
		String description,
		
		@Schema(description = "Флаг публичности коллекции (видна ли другим пользователям)", example = "true", defaultValue = "false")
		Boolean isPublic
) {
	public CollectionRequestDto {
		if (isPublic == null) {
			isPublic = false;
		}
	}
}