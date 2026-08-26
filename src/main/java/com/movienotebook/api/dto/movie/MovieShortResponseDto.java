package com.movienotebook.api.dto.movie;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Краткая информация о фильме для списков и коллекций")
public record MovieShortResponseDto(
		@Schema(description = "Уникальный идентификатор фильма", example = "1")
		Long id,
		
		@Schema(description = "Название фильма", example = "Интерстеллар")
		String title,
		
		@Schema(description = "Год выхода фильма", example = "2014", nullable = true)
		Integer releaseYear,
		
		@Schema(description = "URL постера фильма", example = "https://avatars.mds.yandex.net/get-kinopoisk-image/1600147/43018274-c276-464a-912b-6563ee9a92a5/orig", nullable = true)
		String posterUrl,
		
		@Schema(description = "Средний пользовательский рейтинг", example = "8.65", nullable = true)
		BigDecimal averageRating
) {
}