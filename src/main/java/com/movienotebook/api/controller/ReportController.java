package com.movienotebook.api.controller;

import com.movienotebook.api.dto.report.ReportRequestDto;
import com.movienotebook.api.dto.report.ReportResponseDto;
import com.movienotebook.api.dto.report.ResolveReportRequestDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.ReportService;
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
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Жалобы", description = "Управление жалобами на отзывы пользователей")
public class ReportController {
	
	private final ReportService reportService;
	
	@Operation(
			summary = "Отправка жалобы на отзыв",
			description = "Позволяет авторизованному пользователю создать жалобу на определенный отзыв, указав причину."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Жалоба успешно создана"),
			@ApiResponse(responseCode = "400", description = "Ошибка валидации данных (например, не указан ID отзыва или пустая причина)"),
			@ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
			@ApiResponse(responseCode = "404", description = "Отзыв с указанным ID не найден")
	})
	@PostMapping()
	public ResponseEntity<Void> reporting(
			@Valid @RequestBody ReportRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		reportService.save(request, userDetails);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@Operation(
			summary = "Получение списка всех жалоб",
			description = "Возвращает список всех зарегистрированных жалоб вместе со статусом и текстом отзыва. (Обычно доступно только администраторам или модераторам)."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Список жалоб успешно получен"),
			@ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
			@ApiResponse(responseCode = "403", description = "Доступ запрещен (недостаточно прав для просмотра жалоб)")
	})
	@GetMapping
	public ResponseEntity<List<ReportResponseDto>> getAllReports() {
		
		List<ReportResponseDto> response = reportService.getAll();
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "Обработка (разрешение) жалобы",
			description = "Принимает решение по конкретной жалобе (например: удалить отзыв, отклонить жалобу и т.д.) и меняет ее статус. Требует прав администратора/модератора."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Жалоба успешно обработана"),
			@ApiResponse(responseCode = "400", description = "Ошибка валидации переданных данных (например, не указано действие)"),
			@ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
			@ApiResponse(responseCode = "403", description = "Доступ запрещен (недостаточно прав для обработки жалобы)"),
			@ApiResponse(responseCode = "404", description = "Жалоба с указанным ID не найдена")
	})
	@PostMapping("/{id}/resolve")
	public ResponseEntity<Void> resolveReport(
			@PathVariable Long id,
			@Valid @RequestBody ResolveReportRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		reportService.resolve(id, request, userDetails);
		return ResponseEntity.ok().build();
	}
}