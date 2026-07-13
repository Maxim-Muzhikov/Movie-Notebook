package com.movienotebook.api.integration;

import com.movienotebook.api.integration.dto.KinopoiskSearchResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KinopoiskIntegrationService {
	
	private final RestClient restClient;
	
	public KinopoiskIntegrationService(
			@Value("${kinopoisk.api.url}") String baseUrl,
			@Value("${kinopoisk.api.token}") String token
	) {
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader("X-API-KEY", token)
				.defaultHeader("Content-Type", "application/json")
				.build();
	}
	
	@Cacheable(value = "kinopoiskMovies", key = "#query + '-' + #page")
	public KinopoiskSearchResponseDto searchMovie(String query, Integer page) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/api/v2.1/films/search-by-keyword")
						.queryParam("keyword", query)
						.queryParam("page", page)
						.build())
				.retrieve()
				.body(KinopoiskSearchResponseDto.class);
	}
}