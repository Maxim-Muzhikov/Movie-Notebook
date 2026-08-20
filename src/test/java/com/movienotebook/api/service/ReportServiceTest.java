package com.movienotebook.api.service;

import com.movienotebook.api.dto.report.ReportRequestDto;
import com.movienotebook.api.dto.report.ReportResponseDto;
import com.movienotebook.api.entity.Report;
import com.movienotebook.api.entity.ReportStatus;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.NoSuchReportActionAvailableException;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.mapper.ReportMapper;
import com.movienotebook.api.repository.ReportRepository;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.util.ClassesExamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
	
	@Mock
	private ReportRepository reportRepository;
	
	@Mock
	private ReviewService reviewService;
	
	@Mock
	private UserService userService;
	
	@Mock
	private ReportMapper reportMapper;
	
	@InjectMocks
	private ReportService reportService;
	
	@Captor
	private ArgumentCaptor<Report> reportCaptor;
	
	private User testUser;
	private Review testReview;
	private Report testReport;
	private CustomUserDetails currentUser;
	private ReportResponseDto mockReportResponseDto;
	
	@BeforeEach
	void setUp() {
		testUser = ClassesExamples.getExistingUser();
		testReview = ClassesExamples.getExistingReview();
		
		testReport = ClassesExamples.getExistingReport();
		testReport.setReview(testReview);
		testReport.setReporter(testUser);
		
		currentUser = new CustomUserDetails(
				testUser.getId(),
				testUser.getUsername(),
				testUser.getPasswordHash(),
				List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		
		mockReportResponseDto = new ReportResponseDto(
				testReport.getId(),
				testReport.getReason(),
				testReview.getContent(),
				testUser.getUsername(),
				testReport.getStatus().toString()
		);
	}
	
	@Nested
	@DisplayName("Тесты метода save")
	class SaveTests {
		
		@Test
		@DisplayName("Если жалоба существует, должен обновить причину и вернуть DTO")
		void save_whenReportExists_shouldUpdateReasonAndReturnDto() {
			// Arrange
			String newReason = "Обновленная причина жалобы";
			ReportRequestDto request = new ReportRequestDto(testReview.getId(), newReason);
			
			when(reviewService.getEntityById(testReview.getId())).thenReturn(testReview);
			when(reportRepository.findByReviewAndReporter(testReview.getId(), currentUser.getId()))
					.thenReturn(Optional.of(testReport));
			when(reportMapper.toDto(testReport)).thenReturn(mockReportResponseDto);
			
			// Act
			ReportResponseDto result = reportService.save(request, currentUser);
			
			// Assert
			assertThat(testReport.getReason()).isEqualTo(newReason);
			assertThat(result).isSameAs(mockReportResponseDto);
			
			verify(reportRepository, never()).save(any());
			verify(userService, never()).getReferenceById(any());
		}
		
		@Test
		@DisplayName("Если жалоба не существует, должен создать, сохранить и вернуть DTO")
		void save_whenReportDoesNotExist_shouldCreateSaveAndReturnDto() {
			// Arrange
			String newReason = "Новая жалоба на спам";
			ReportRequestDto request = new ReportRequestDto(testReview.getId(), newReason);
			
			when(reviewService.getEntityById(testReview.getId())).thenReturn(testReview);
			when(reportRepository.findByReviewAndReporter(testReview.getId(), currentUser.getId()))
					.thenReturn(Optional.empty());
			when(userService.getReferenceById(currentUser.getId())).thenReturn(testUser);
			when(reportMapper.toDto(any(Report.class))).thenReturn(mockReportResponseDto);
			
			// Expected
			Report expectedReport = new Report();
			expectedReport.setReporter(testUser);
			expectedReport.setReview(testReview);
			expectedReport.setReason(newReason);
			expectedReport.setStatus(ReportStatus.NEW);
			
			// Act
			ReportResponseDto result = reportService.save(request, currentUser);
			
			// Assert
			verify(reportRepository).save(reportCaptor.capture());
			Report savedReport = reportCaptor.getValue();
			
			assertThat(savedReport)
					.usingRecursiveComparison()
					.isEqualTo(expectedReport);
			
			assertThat(result).isSameAs(mockReportResponseDto);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода resolve")
	class ResolveTests {
		
		@Test
		@DisplayName("Если жалоба не найдена, должен выбросить исключение ResourceNotFoundException")
		void resolve_whenReportDoesNotExist_shouldThrowException() {
			// Arrange
			Long reportId = 999L;
			when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> reportService.resolve(reportId, "DELETE_REVIEW", currentUser))
					.isInstanceOf(ResourceNotFoundException.class);
			
			verify(reportRepository, never()).delete(any());
		}
		
		@Test
		@DisplayName("Если действие некорректно, должен выбросить исключение NoSuchReportActionAvailableException")
		void resolve_whenActionInvalid_shouldThrowException() {
			// Arrange
			Long reportId = testReport.getId();
			
			when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
			
			// Act & Assert
			assertThatThrownBy(() -> reportService.resolve(reportId, "INVALID_ACTION", currentUser))
					.isInstanceOf(NoSuchReportActionAvailableException.class);
			
			// Assert
			verify(reportRepository, never()).delete(any());
			verify(reviewService, never()).delete(any(), any());
		}
		
		@Test
		@DisplayName("Если действие DELETE_REVIEW, должен удалить жалобу и связанный с ней отзыв")
		void resolve_whenActionIsDeleteReview_shouldDeleteReportAndReview() {
			// Arrange
			Long reportId = testReport.getId();
			when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
			
			// Act
			reportService.resolve(reportId, "DELETE_REVIEW", currentUser);
			
			// Assert
			verify(reportRepository, never()).delete(testReport);
			verify(reviewService).delete(testReview.getId(), currentUser);
			
			assertThat(testReport.getStatus())
					.isEqualTo(ReportStatus.RESOLVED);
		}
		
		@Test
		@DisplayName("Если действие REJECT_REPORT, должен только удалить жалобу")
		void resolve_whenActionIsRejectReport_shouldOnlyDeleteReport() {
			// Arrange
			Long reportId = testReport.getId();
			when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
			
			// Act
			reportService.resolve(reportId, "REJECT_REPORT", currentUser);
			
			// Assert
			verify(reportRepository, never()).delete(testReport);
			verify(reviewService, never()).delete(any(), any());
			
			assertThat(testReport.getStatus())
					.isEqualTo(ReportStatus.REJECTED);
		}
		
		@Test
		@DisplayName("Если действие CLAIM_REPORT, должен установить нужный статус")
		void resolve_whenActionIsClaimReport_shouldOnlyDeleteReport() {
			// Arrange
			Long reportId = testReport.getId();
			when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
			
			// Act
			reportService.resolve(reportId, "CLAIM_REPORT", currentUser);
			
			// Assert
			verify(reportRepository, never()).delete(testReport);
			verify(reviewService, never()).delete(any(), any());
			
			assertThat(testReport.getStatus())
					.isEqualTo(ReportStatus.IN_PROGRESS);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getAll")
	class GetAllTests {
		
		@Test
		@DisplayName("Если жалобы существуют, должен вернуть список DTO")
		void getAll_whenReportsExist_shouldReturnListOfDtos() {
			// Arrange
			when(reportRepository.findAll()).thenReturn(List.of(testReport));
			when(reportMapper.toDto(testReport)).thenReturn(mockReportResponseDto);
			
			// Act
			List<ReportResponseDto> result = reportService.getAll();
			
			// Assert
			assertThat(result)
					.hasSize(1)
					.element(0).isSameAs(mockReportResponseDto);
		}
		
		@Test
		@DisplayName("Если жалоб нет, должен вернуть пустой список")
		void getAll_whenNoReports_shouldReturnEmptyList() {
			// Arrange
			when(reportRepository.findAll()).thenReturn(Collections.emptyList());
			
			// Act
			List<ReportResponseDto> result = reportService.getAll();
			
			// Assert
			assertThat(result).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getAllEntity")
	class GetAllEntityTests {
		
		@Test
		@DisplayName("Всегда возвращает результат из репозитория по ссылке")
		void getAllEntity_always_shouldReturnListFromRepository() {
			// Arrange
			List<Report> expectedList = List.of(testReport);
			when(reportRepository.findAll()).thenReturn(expectedList);
			
			// Act
			List<Report> result = reportService.getAllEntity();
			
			// Assert
			assertThat(result).isSameAs(expectedList);
		}
	}
}