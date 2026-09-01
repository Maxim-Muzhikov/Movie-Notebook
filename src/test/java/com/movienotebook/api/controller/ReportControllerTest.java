package com.movienotebook.api.controller;

import com.movienotebook.api.dto.report.ReportRequestDto;
import com.movienotebook.api.dto.report.ReportResponseDto;
import com.movienotebook.api.dto.report.ResolveReportRequestDto;
import com.movienotebook.api.entity.enums.ReportAction;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.security.CustomUserDetailsService;
import com.movienotebook.api.security.JwtService;
import com.movienotebook.api.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@AutoConfigureJsonTesters
class ReportControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private JacksonTester<ReportRequestDto> reportRequestJsonTester;
	
	@Autowired
	private JacksonTester<ResolveReportRequestDto> resolveReportRequestJsonTester;
	
	@MockitoBean
	private JwtService jwtService;
	
	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;
	
	@MockitoBean
	private ReportService reportService;
	
	@Nested
	@DisplayName("POST /api/v1/reports - Отправка жалобы на отзыв")
	class CreateReportTests {
		
		@Test
		@DisplayName("Успешное создание жалобы (201)")
		void reporting_ValidRequest_Returns201() throws Exception {
			// Arrange
			Long reviewId = 10L;
			String reason = "Нецензурная лексика и спам в отзыве";
			var request = new ReportRequestDto(reviewId, reason);
			
			var userDetails = new CustomUserDetails(1L, "testuser", "pass", List.of());
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/reports")
							.with(user(userDetails))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(reportRequestJsonTester.write(request).getJson()))
					.andExpect(status().isCreated());
			
			verify(reportService).save(any(ReportRequestDto.class), any(CustomUserDetails.class));
		}
		
		@Test
		@DisplayName("Ошибка валидации жалобы с пустой причиной (400)")
		void reporting_BlankReason_Returns400() throws Exception {
			// Arrange
			var invalidRequest = new ReportRequestDto(10L, "");
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/reports")
							.with(user("testuser"))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(reportRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(reportService);
		}
		
		@Test
		@DisplayName("Ошибка валидации жалобы без reviewId (400)")
		void reporting_NullReviewId_Returns400() throws Exception {
			// Arrange
			var invalidRequest = new ReportRequestDto(null, "Спам");
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/reports")
							.with(user("testuser"))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(reportRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(reportService);
		}
	}
	
	@Nested
	@DisplayName("GET /api/v1/reports - Получение списка всех жалоб")
	class GetAllReportsTests {
		
		@Test
		@DisplayName("Успешное получение списка жалоб (200)")
		void getAllReports_Returns200() throws Exception {
			// Arrange
			var reportDto = new ReportResponseDto(
					1L,
					"Оскорбления",
					"Текст плохого отзыва...",
					"reporter_user",
					"NEW"
			);
			
			when(reportService.getAll()).thenReturn(List.of(reportDto));
			
			// Act & Assert
			mockMvc.perform(get("/api/v1/reports")
							.with(user("admin").roles("ADMIN")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$[0].id").value(1L))
					.andExpect(jsonPath("$[0].reason").value("Оскорбления"))
					.andExpect(jsonPath("$[0].reviewContent").value("Текст плохого отзыва..."))
					.andExpect(jsonPath("$[0].reporterUsername").value("reporter_user"))
					.andExpect(jsonPath("$[0].status").value("NEW"));
			
			verify(reportService).getAll();
		}
	}
	
	@Nested
	@DisplayName("POST /api/v1/reports/{id}/resolve - Обработка решения по жалобе")
	class ResolveReportTests {
		
		@Test
		@DisplayName("Успешная обработка жалобы (200)")
		void resolveReport_ValidRequest_Returns200() throws Exception {
			// Arrange
			Long reportId = 1L;
			var request = new ResolveReportRequestDto(ReportAction.DELETE_REVIEW);
			var userDetails = new CustomUserDetails(1L, "admin", "pass", List.of());
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/reports/{id}/resolve", reportId)
							.with(user(userDetails))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(resolveReportRequestJsonTester.write(request).getJson()))
					.andExpect(status().isOk());
			
			verify(reportService).resolve(eq(reportId), any(ResolveReportRequestDto.class), any());
		}
		
		@Test
		@DisplayName("Ошибка валидации при отсутствии действия (400)")
		void resolveReport_NullAction_Returns400() throws Exception {
			// Arrange
			Long reportId = 1L;
			var invalidRequest = new ResolveReportRequestDto(null);
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/reports/{id}/resolve", reportId)
							.with(user("admin"))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(resolveReportRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(reportService);
		}
	}
}