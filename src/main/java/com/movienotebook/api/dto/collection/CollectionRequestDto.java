package com.movienotebook.api.dto.collection;

import jakarta.validation.constraints.NotBlank;

public record CollectionRequestDto(
		@NotBlank(message = "Имя коллекции не может быть пустым")
		String name,
		
		String description,
		
		Boolean isPublic
) {
	public CollectionRequestDto {
		if (isPublic == null) {
			isPublic = false;
		}
	}
}
