package com.movienotebook.api.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MovieKinopoiskResponseDto(
		Long filmId,
		String nameRu,
		String nameEn,
		String description,
		Integer year,
		String posterUrl
) {
}
