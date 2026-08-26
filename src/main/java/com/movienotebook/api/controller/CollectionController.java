package com.movienotebook.api.controller;

import com.movienotebook.api.dto.collection.CollectionRequestDto;
import com.movienotebook.api.dto.collection.CollectionResponseDto;
import com.movienotebook.api.dto.collection.CollectionWithMoviesResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
@Tag(name = "Коллекции", description = "Управление пользовательскими подборками (коллекциями) фильмов")
public class CollectionController {
	
	private final CollectionService collectionService;
	
	@Operation(
			summary = "Редактирование коллекции",
			description = "Обновляет данные существующей коллекции (название, описание, публичность). Изменять коллекцию может только ее автор."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Коллекция успешно обновлена"),
			@ApiResponse(responseCode = "400", description = "Ошибка валидации переданных данных"),
			@ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка редактировать чужую коллекцию)"),
			@ApiResponse(responseCode = "404", description = "Коллекция с указанным ID не найдена")
	})
	@PutMapping("/{collectionId}")
	public ResponseEntity<CollectionResponseDto> editCollection (
			@PathVariable Long collectionId,
			@Valid @RequestBody CollectionRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		CollectionResponseDto response = collectionService.update(collectionId, request, userDetails);
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "Получение коллекции по ID",
			description = "Возвращает детали коллекции вместе со списком добавленных в нее фильмов. Доступно автору или любому пользователю, если коллекция публичная (isPublic = true)."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Данные коллекции успешно получены"),
			@ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка просмотреть чужую приватную коллекцию)"),
			@ApiResponse(responseCode = "404", description = "Коллекция с указанным ID не найдена")
	})
	@GetMapping("/{collectionId}")
	public ResponseEntity<CollectionWithMoviesResponseDto> getCollection (
			@PathVariable Long collectionId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		CollectionWithMoviesResponseDto response = collectionService.getWithMoviesById(collectionId, userDetails);
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "Получение списка своих коллекций",
			description = "Возвращает список всех коллекций, созданных текущим авторизованным пользователем."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Список коллекций успешно получен")
	})
	@GetMapping
	public ResponseEntity<List<CollectionResponseDto>> getMyCollections (
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		List<CollectionResponseDto> response = collectionService.getByCurrentUser(userDetails);
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "Создание новой коллекции",
			description = "Создает новую подборку фильмов для текущего авторизованного пользователя. По умолчанию коллекция является приватной, если не указано иное."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Коллекция успешно создана"),
			@ApiResponse(responseCode = "400", description = "Ошибка валидации переданных данных (например, пустое имя коллекции)")
	})
	@PostMapping
	public ResponseEntity<CollectionResponseDto> createCollection (
			@Valid @RequestBody CollectionRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		CollectionResponseDto response = collectionService.create(request, userDetails);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@Operation(
			summary = "Удаление коллекции",
			description = "Полностью удаляет коллекцию по ее ID. Удалить коллекцию может только ее автор."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Коллекция успешно удалена"),
			@ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка удалить чужую коллекцию)"),
			@ApiResponse(responseCode = "404", description = "Коллекция с указанным ID не найдена")
	})
	@DeleteMapping("/{collectionId}")
	public ResponseEntity<Void> deleteCollection (
			@PathVariable Long collectionId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		collectionService.delete(collectionId, userDetails);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	@Operation(
			summary = "Добавление фильма в коллекцию",
			description = "Добавляет существующий фильм в указанную коллекцию. Добавлять фильмы может только автор коллекции."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Фильм успешно добавлен в коллекцию"),
			@ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка добавить фильм в чужую коллекцию)"),
			@ApiResponse(responseCode = "404", description = "Коллекция или фильм не найдены"),
			@ApiResponse(responseCode = "409", description = "Конфликт (фильм уже добавлен в эту коллекцию)")
	})
	@PostMapping("/{collectionId}/movies/{movieId}")
	public ResponseEntity<Void> addMovieToCollection (
			@PathVariable Long collectionId,
			@PathVariable Long movieId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		collectionService.addMovie(collectionId, movieId, userDetails);
		return ResponseEntity.ok().build();
	}
	
	@Operation(
			summary = "Удаление фильма из коллекции",
			description = "Удаляет фильм из указанной коллекции. Удалять фильмы может только автор коллекции."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Фильм успешно удален из коллекции"),
			@ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка удалить фильм из чужой коллекции)"),
			@ApiResponse(responseCode = "404", description = "Коллекция или фильм не найдены")
	})
	@DeleteMapping("/{collectionId}/movies/{movieId}")
	public ResponseEntity<Void> removeMovieFromCollection (
			@PathVariable Long collectionId,
			@PathVariable Long movieId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		collectionService.removeMovie(collectionId, movieId, userDetails);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}