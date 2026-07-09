package com.movienotebook.api.dto.movie;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SearchMovieRequestDto (
		@NotNull(message = "Содержание поискового запроса не может быть пустым")
		String query,
		
		@Builder.Default()
		@Min(value = 1, message = "Номер страницы не может быть меньше 1")
		@Max(value = 100, message = "Номер страницы не может быть больше 100")
		Integer page,
		
		@Min(value = 1, message = "Размер страницы не может быть меньше 1")
		@Max(value = 100, message = "Размер страницы не может быть больше 100")
		Integer size,
		
		Boolean deepSearch
) {
		public SearchMovieRequestDto {
		if (page == null) {
			page = 1;
		}
		if (size == null) {
			size = 20;
		}
		if (deepSearch == null) {
			deepSearch = false;
		}
	}
}
