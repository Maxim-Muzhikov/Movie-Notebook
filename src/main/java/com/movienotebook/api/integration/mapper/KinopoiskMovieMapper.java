package com.movienotebook.api.integration.mapper;

import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.integration.dto.KinopoiskResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KinopoiskMovieMapper {
	
	@Mapping(target = "externalId", source = "filmId")
	@Mapping(target = "originalTitle", source = "nameEn")
	@Mapping(target = "releaseYear", source = "year")
	@Mapping(target = "title", expression = "java(resolveTitle(dto.nameRu(), dto.nameEn()))")
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "averageRating", ignore = true)
	@Mapping(target = "lastUpdate", ignore = true)
	@Mapping(target = "reviews", ignore = true)
	@Mapping(target = "ratings", ignore = true)
	Movie toEntity(KinopoiskResponseDto dto);
	
	default String resolveTitle(String nameRu, String nameEn) {
		if (nameRu != null && !nameRu.isBlank()) {
			return nameRu;
		}
		if (nameEn != null && !nameEn.isBlank()) {
			return nameEn;
		}
		return "Без названия";
	}
}