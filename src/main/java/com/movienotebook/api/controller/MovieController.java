package com.movienotebook.api.controller;

import com.movienotebook.api.dto.movie.MovieResponseDto;
import com.movienotebook.api.dto.movie.SearchMovieRequestDto;
import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.dto.rating.RatingResponseDto;
import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.MovieService;
import com.movienotebook.api.service.RatingService;
import com.movienotebook.api.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Tag(name = "Фильмы", description = "Поиск фильмов, просмотр информации, а также управление отзывами и оценками пользователей")
public class MovieController {
	
	private final MovieService movieService;
	private final ReviewService reviewService;
	private final RatingService ratingService;
	
	@Operation(
			summary = "Поиск фильмов",
			description = "Выполняет поиск фильмов по текстовому запросу. Поддерживает пагинацию. При необходимости можно активировать глубокий поиск (deepSearch)."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены (возвращается страница с фильмами)"),
			@ApiResponse(responseCode = "400", description = "Ошибка валидации параметров запроса (например, пустой поисковый запрос или неверный номер страницы)")
	})
	@GetMapping()
	public ResponseEntity<Page<MovieResponseDto>> searchMovies(
			@Valid @ModelAttribute SearchMovieRequestDto request) {
		
		Page<MovieResponseDto> response = movieService.searchMovies(request);
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "Получение отзывов к фильму",
			description = "Возвращает список всех отзывов, оставленных пользователями для указанного фильма."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Список отзывов успешно получен"),
			@ApiResponse(responseCode = "404", description = "Фильм с указанным ID не найден")
	})
	@GetMapping("/{movieId}/reviews")
	public ResponseEntity<List<ReviewResponseDto>> getReviews(
			@PathVariable Long movieId) {
		
		List<ReviewResponseDto> response = reviewService.findAllByMovieId(movieId);
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "Получение детальной информации о фильме",
			description = "Возвращает подробные данные фильма по его ID (включая описание, средний рейтинг, постер и т.д.)."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Информация о фильме успешно получена"),
			@ApiResponse(responseCode = "404", description = "Фильм с указанным ID не найден")
	})
	@GetMapping("/{movieId}")
	public ResponseEntity<MovieResponseDto> getMovie(
			@PathVariable Long movieId) {
		
		MovieResponseDto response = movieService.getById(movieId);
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "Добавление отзыва",
			description = "Оставляет текстовый отзыв к указанному фильму от лица текущего авторизованного пользователя."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Отзыв успешно добавлен"),
			@ApiResponse(responseCode = "400", description = "Ошибка валидации данных (например, слишком короткий/длинный текст отзыва)"),
			@ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
			@ApiResponse(responseCode = "404", description = "Фильм с указанным ID не найден")
	})
	@PostMapping("/{movieId}/reviews")
	public ResponseEntity<ReviewResponseDto> createReview(
			@PathVariable Long movieId,
			@Valid @RequestBody ReviewRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		ReviewResponseDto response = reviewService.save(movieId, request, userDetails);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@Operation(
			summary = "Оценка фильма",
			description = "Добавляет новую или обновляет существующую оценку для фильма от лица текущего авторизованного пользователя. (Используется метод PUT, так как операция идемпотентна)."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Оценка успешно сохранена/обновлена"),
			@ApiResponse(responseCode = "400", description = "Ошибка валидации данных (например, значение оценки вне допустимого диапазона)"),
			@ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
			@ApiResponse(responseCode = "404", description = "Фильм с указанным ID не найден")
	})
	@PutMapping("/{movieId}/ratings")
	public ResponseEntity<RatingResponseDto> rateMovie(
			@PathVariable Long movieId,
			@Valid @RequestBody RatingRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		RatingResponseDto response = ratingService.save(movieId, request, userDetails);
		return ResponseEntity.ok(response);
	}
}