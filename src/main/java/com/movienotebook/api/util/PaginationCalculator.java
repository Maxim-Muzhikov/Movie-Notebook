package com.movienotebook.api.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PaginationCalculator {
	
	private final int EXTERNAL_PAGE_SIZE = 20;
	
	public record PaginationMapping(
			List<Integer> externalPagesToFetch,
			int relativeStartIndex,
			int relativeEndIndex
	) {}
	
	public PaginationMapping calculate(int requestedPage, int requestedSize) {
		if (requestedPage < 1 || requestedSize < 1) {
			throw new IllegalArgumentException("Страница и размер должны быть больше 0");
		}
		
		int absoluteStartIndex = (requestedPage - 1) * requestedSize;
		int absoluteEndIndex = absoluteStartIndex + requestedSize - 1;
		
		int startExternalPage = (absoluteStartIndex / EXTERNAL_PAGE_SIZE) + 1;
		int endExternalPage = (absoluteEndIndex / EXTERNAL_PAGE_SIZE) + 1;
		
		List<Integer> pagesToFetch = new ArrayList<>();
		for (int i = startExternalPage; i <= endExternalPage; i++) {
			pagesToFetch.add(i);
		}
		
		int firstFetchedItemAbsoluteIndex = (startExternalPage - 1) * EXTERNAL_PAGE_SIZE;
		int relativeStartIndex = absoluteStartIndex - firstFetchedItemAbsoluteIndex;
		int relativeEndIndex = relativeStartIndex + requestedSize;
		
		return new PaginationMapping(pagesToFetch, relativeStartIndex, relativeEndIndex);
	}
}