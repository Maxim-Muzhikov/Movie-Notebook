package com.movienotebook.api.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KinopoiskSearchResponseDto(
		String keyword,
		Integer pagesCount,
		Integer searchFilmsCountResult,
		List<MovieKinopoiskResponseDto> films
) {
}
