package com.movienotebook.api.dto.movie;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Параметры поиска и пагинации фильмов")
public record SearchMovieRequestDto(
		@Schema(description = "Поисковый запрос по названию фильма", example = "Интерстеллар", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull(message = "Содержание поискового запроса не может быть пустым")
		String query,
		
		@Schema(description = "Номер страницы (от 1 до 100)", example = "1", defaultValue = "1")
		@Min(value = 1, message = "Номер страницы не может быть меньше 1")
		@Max(value = 100, message = "Номер страницы не может быть больше 100")
		Integer page,
		
		@Schema(description = "Размер страницы (от 1 до 100)", example = "20", defaultValue = "20")
		@Min(value = 1, message = "Размер страницы не может быть меньше 1")
		@Max(value = 100, message = "Размер страницы не может быть больше 100")
		Integer size,
		
		@Schema(description = "Флаг глубокого поиска во внешнем API Кинопоиска", example = "false", defaultValue = "false")
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