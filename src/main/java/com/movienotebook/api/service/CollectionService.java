package com.movienotebook.api.service;

import com.movienotebook.api.dto.collection.CollectionRequestDto;
import com.movienotebook.api.dto.collection.CollectionResponseDto;
import com.movienotebook.api.dto.collection.CollectionWithMoviesResponseDto;
import com.movienotebook.api.entity.Collection;
import com.movienotebook.api.entity.CollectionMovie;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.CollectionAlreadyExistsException;
import com.movienotebook.api.exception.MovieAlreadyInCollectionException;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.mapper.CollectionMapper;
import com.movienotebook.api.repository.CollectionMovieRepository;
import com.movienotebook.api.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

// TODO Повесить на все сервисы @Transactional
// NOTE Маппинг перенесен в сервис для добавления @Transactional
// LEARN @Transactional
@Service

// NOTE @RequiredArgsConstructor вместо @AllArgsConstructor, чтобы избежать ошибок компиляции при добавлении не-final поля
@RequiredArgsConstructor
public class CollectionService {
	
	private final UserService userService;
	private final MovieService movieService;
	private final CollectionRepository collectionRepository;
	private final CollectionMovieRepository collectionMovieRepository;
	private final CollectionMapper collectionMapper;
	
	private Collection securedGetCollection(Long collectionId, String currentUser) {
		User user = userService.getByUsername(currentUser);
		
		Collection collection = collectionRepository.findById(collectionId)
				.orElseThrow(() -> new ResourceNotFoundException("Коллекции с номером " + collectionId + " не найдено"));
		
		User authorOfCollection = collection.getUser();
		
		boolean accessGranted = Objects.equals(user.getId(), authorOfCollection.getId());
		
		if (!accessGranted) {
			throw new AccessDeniedException("Пользователь  " + user.getUsername() + " не имеет права изменять коллекции пользователя " + authorOfCollection.getUsername());
		}
		return collection;
	}
	
	private static Collection mapToCollectionEntity(CollectionRequestDto request, User user) {
		
		Collection newCollection = new Collection();
		
		newCollection.setUser(user);
		newCollection.setDescription(request.description());
		newCollection.setName(request.name());
		newCollection.setPublic(request.isPublic());
		
		return newCollection;
	}
	
	private CollectionMovie mapToCollectionMovieEntity (Collection collection, Movie movie) {
		CollectionMovie newCollectionMovie = new CollectionMovie();
		
		newCollectionMovie.setCollection(collection);
		newCollectionMovie.setMovie(movie);
		
		return newCollectionMovie;
	}
	
	@Transactional(readOnly = true)
	public CollectionResponseDto getById(Long collectionId, String currentUser) {
		Collection collection = securedGetCollection(collectionId, currentUser);
		return collectionMapper.toDto(collection);
	}
	
	@Transactional(readOnly = true)
	public CollectionWithMoviesResponseDto getWithMoviesById(Long collectionId, String currentUser) {
		Collection collection = securedGetCollection(collectionId, currentUser);
		return collectionMapper.toWithMoviesDto(collection);
	}
	
	@Transactional(readOnly = true)
	public List<CollectionResponseDto> getUserCollections(String currentUser) {
		
		// TODO Вынести поле id в пользовательский UserService
		// TODO Или настроить контекст безопасности и брать напрямую оттуда id
		Long userId = userService.getByUsername(currentUser).getId();
		
		// TODO Сделать возврат только ПУБЛИЧНЫХ коллекций без проверки на доступ
		return collectionRepository.findAllByUserId(userId)
				.stream()
				.map(collectionMapper::toDto)
				.toList();
	}
	
	@Transactional
	public CollectionResponseDto createCollection(CollectionRequestDto request, String currentUser) {
		
		User user = userService.getByUsername(currentUser);
		boolean isExist = collectionRepository.existsByNameAndUserId(request.name(), user.getId());
		
		if (isExist) {
			throw new CollectionAlreadyExistsException("Коллекция с таким именем уже существует");
		}
		
		Collection newCollection = mapToCollectionEntity(request, user);
		return collectionMapper.toDto(collectionRepository.save(newCollection));
	}
	
	@Transactional
	public void deleteCollection(Long collectionId, String currentUser) {
		
		Collection collection = securedGetCollection(collectionId, currentUser);
		collectionRepository.delete(collection);
	}
	
	@Transactional
	public void addMovieToTheCollection(Long collectionId, Long movieId, String currentUser) {
		
		Collection collection = securedGetCollection(collectionId, currentUser);
		Movie movie = movieService.getById(movieId);
		
		boolean alreadyExists = collectionMovieRepository.existsByCollectionIdAndMovieId(collectionId, movieId);
		if (alreadyExists) {
			throw new MovieAlreadyInCollectionException("Фильм " + movie.getTitle() + " уже добавлен в коллекцию " + collection.getName());
		}
		
		// NOTE Используется метод-helper addMovie()
		collection.addMovie(mapToCollectionMovieEntity(collection, movie));
	}
	
	@Transactional
	public void removeMovieFromCollection(Long collectionId, Long movieId, String currentUser) {
		
		Collection collection = securedGetCollection(collectionId, currentUser);
		
		CollectionMovie link = collectionMovieRepository.findByCollectionIdAndMovieId(collectionId, movieId)
				.orElseThrow(() -> new ResourceNotFoundException("Фильм с id = " + movieId + " не найден в коллекции \"" + collection.getName() + "\""));
		
		// NOTE Используется метод-helper removeMovie()
		collection.removeMovie(link);
	}
	
}
