package com.movienotebook.api.dto.collection;

import jakarta.validation.constraints.NotBlank;

public record CollectionRequest(
		@NotBlank(message = "Имя коллекции не может быть пустым")
		String name,
		
		String description,
		
		Boolean isPublic
) {
	public CollectionRequest {
		if (isPublic == null) {
			isPublic = false;
		}
	}
}
