package com.movienotebook.api.dto.movie;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Детальная информация о фильме")
public record MovieResponseDto(
		@Schema(description = "Уникальный идентификатор фильма в локальной БД", example = "1")
		Long id,
		
		@Schema(description = "Идентификатор фильма во внешнем API Кинопоиска", example = "258687", nullable = true)
		Long externalId,
		
		@Schema(description = "Название фильма на русском языке", example = "Интерстеллар")
		String title,
		
		@Schema(description = "Оригинальное название фильма", example = "Interstellar", nullable = true)
		String originalTitle,
		
		@Schema(description = "Описание сюжета фильма", example = "Когда засуха, пыльные бури и вымирание растений приводят человечество к продовольственному кризису, коллектив исследователей отправляется сквозь червоточину...", nullable = true)
		String description,
		
		@Schema(description = "Год выхода фильма", example = "2014", nullable = true)
		Integer releaseYear,
		
		@Schema(description = "URL постера фильма", example = "https://avatars.mds.yandex.net/get-kinopoisk-image/1600147/43018274-c276-464a-912b-6563ee9a92a5/orig", nullable = true)
		String posterUrl,
		
		@Schema(description = "Средний пользовательский рейтинг (округлен до сотых)", example = "8.65", nullable = true)
		BigDecimal averageRating
) {
}