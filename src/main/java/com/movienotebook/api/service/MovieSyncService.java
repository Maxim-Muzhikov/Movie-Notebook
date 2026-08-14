package com.movienotebook.api.service;

import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.mapper.MovieMapper;
import com.movienotebook.api.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieSyncService {
	private final MovieRepository movieRepository;
	private final MovieMapper movieMapper;
	
	// TODO Вынести в конфигурацию проекта
	//@Value("${app.movies.stale-threshold}")
	private final Duration movieStaleThreshold = Duration.ofHours(24);
	
	List<Movie> syncAndSave(List<Movie> fetchedMovies) {
		List<Long> externalIds = fetchedMovies
				.stream()
				.map(Movie::getExternalId)
				.toList();
		
		Map<Long, Movie> existingMoviesMap = movieRepository.findAllByExternalIdIn(externalIds)
				.stream()
				.collect(Collectors.toMap(Movie::getExternalId, m -> m));
		
		List<Movie> moviesToUpsert = new ArrayList<>();
		OffsetDateTime threshold = OffsetDateTime.now().minus(movieStaleThreshold);
		
		for (Movie fetchedMovie : fetchedMovies) {
			Movie existing = existingMoviesMap.get(fetchedMovie.getExternalId());
			
			if (existing == null) {
				moviesToUpsert.add(fetchedMovie);
			} else if (existing.getLastUpdate().isBefore(threshold)) {
				movieMapper.updateMovieFromExtracted(existing, fetchedMovie);
				moviesToUpsert.add(existing);
			} else {
				moviesToUpsert.add(existing);
			}
		}
		
		return movieRepository.saveAll(moviesToUpsert);
	}
}