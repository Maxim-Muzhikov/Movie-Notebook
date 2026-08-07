package com.movienotebook.api.integration;

import com.movienotebook.api.integration.dto.KinopoiskResponseDto;
import com.movienotebook.api.integration.dto.KinopoiskSearchResponseDto;
import com.movienotebook.api.util.PaginationCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KinopoiskFetcher {
	public static final int EXTERNAL_PAGE_SIZE = 20;
	
	private final KinopoiskIntegrationService integrationService;
	
	public record SearchResult(
			List<KinopoiskResponseDto> fetchedMovies,
			int totalCount) {}
	
	public SearchResult fetchPages(String query, PaginationCalculator.KinopoiskPaginationMapping mapping) {
		List<KinopoiskResponseDto> allFetchedMovies = new ArrayList<>();
		
		int searchFilmsCountResult = 0;
		
		for (Integer page : mapping.externalPagesToFetch()) {
			KinopoiskSearchResponseDto kinopoiskResponse = integrationService.searchMovie(query, page);
			
			if (kinopoiskResponse.films() != null && !kinopoiskResponse.films().isEmpty()) {
				allFetchedMovies.addAll(kinopoiskResponse.films());
			}
			
			if (kinopoiskResponse.films() == null || kinopoiskResponse.films().size() < EXTERNAL_PAGE_SIZE) {
				break;
			}
			
			if (searchFilmsCountResult == 0) {
				searchFilmsCountResult = kinopoiskResponse.searchFilmsCountResult();
			}
		}
		
		if (mapping.relativeStartIndex() >= allFetchedMovies.size()) {
			return new SearchResult(Collections.emptyList(), 0);
		}
		
		return new SearchResult(allFetchedMovies, searchFilmsCountResult);
	}
}