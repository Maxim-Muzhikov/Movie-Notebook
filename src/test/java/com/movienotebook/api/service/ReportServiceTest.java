package com.movienotebook.api.service;

import com.movienotebook.api.dto.report.ReportRequestDto;
import com.movienotebook.api.entity.Report;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
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
	
	@InjectMocks
	private ReportService reportService;
	
	@Captor
	private ArgumentCaptor<Report> reportCaptor;
	
	private CustomUserDetails currentUser;
	private User testUser;
	private Review testReview;
	private Report testReport;
	private Report expectedReport;
	
	@BeforeEach
	void setUp() {
		currentUser = new CustomUserDetails(
				1L,
				"Имя пользователя",
				"Хэш пароля",
				List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		
		testUser = ClassesExamples.getExistingUser();
		testReview = ClassesExamples.getExistingReview();
		testReport = ClassesExamples.getExistingReport();
		expectedReport = ClassesExamples.getExistingReport();
	}
	
	@Nested
	@DisplayName("Тесты метода getAll")
	class GetAllTests {
		@Nested
		@DisplayName("Тесты метода addOrUpdatedReport")
		class AddOrUpdatedReportTests {
			
			@Test
			@DisplayName("Если жалоба не существует, должен создать и сохранить новую жалобу")
			void addOrUpdatedReport_whenReportDoesNotExist_shouldCreateNewReport() {
				// Arrange
				var requestDto = new ReportRequestDto(testReview.getId(), "Оскорбление");
				
				when(reviewService.getReviewById(requestDto.reviewId())).thenReturn(testReview);
				when(reportRepository.findByReviewAndReporter(requestDto.reviewId(), currentUser.getId()))
						.thenReturn(Optional.empty());
				when(userService.getReferenceById(currentUser.getId())).thenReturn(testUser);
				
				// Expected
				Report expectedReportToSave = new Report();
				expectedReportToSave.setReporter(testUser);
				expectedReportToSave.setStatus("NEW");
				expectedReportToSave.setReason(requestDto.reason());
				expectedReportToSave.setReview(testReview);
				
				// Act
				Report returnedReport = reportService.addOrUpdatedReport(requestDto, currentUser);
				
				// Assert
				verify(reportRepository, times(1)).save(reportCaptor.capture());
				Report savedReport = reportCaptor.getValue();
				
				assertThat(returnedReport).isSameAs(savedReport);
				
				assertThat(savedReport)
						.usingRecursiveComparison()
						.isEqualTo(expectedReportToSave);
			}
			
			@Test
			@DisplayName("Если жалоба существует, должен обновить причину и вернуть без явного вызова save")
			void addOrUpdatedReport_whenReportAlreadyExists_shouldUpdateReason() {
				// Arrange
				var requestDto = new ReportRequestDto(testReview.getId(), "Новая причина: Спам");
				
				when(reviewService.getReviewById(requestDto.reviewId())).thenReturn(testReview);
				when(reportRepository.findByReviewAndReporter(requestDto.reviewId(), currentUser.getId()))
						.thenReturn(Optional.of(testReport));
				
				// Expected
				expectedReport.setReason("Новая причина: Спам");
				
				// Act
				Report returnedReport = reportService.addOrUpdatedReport(requestDto, currentUser);
				
				// Assert
				assertThat(returnedReport)
						.isSameAs(testReport)
						.usingRecursiveComparison()
						.isEqualTo(expectedReport);
				
				verify(userService, never()).getReferenceById(any());
				verify(reportRepository, never()).save(any());
			}
		}
		
		
		@Test
		@DisplayName("Должен вернуть список всех жалоб от репозитория без изменений")
		void getAll_shouldReturnListOfReports() {
			// Arrange
			when(reportRepository.findAll()).thenReturn(List.of(testReport));
			
			// Act
			List<Report> returnedReports = reportService.getAll();
			
			// Assert
			assertThat(returnedReports)
					.hasSize(1)
					.contains(testReport);
			
			assertThat(returnedReports.getFirst())
					.usingRecursiveComparison()
					.isEqualTo(expectedReport);
		}
		
		@Test
		@DisplayName("Если жалоб нет, должен вернуть пустой список")
		void getAll_whenNoReports_shouldReturnEmptyList() {
			// Arrange
			when(reportRepository.findAll()).thenReturn(Collections.emptyList());
			
			// Act
			List<Report> returnedReports = reportService.getAll();
			
			// Assert
			assertThat(returnedReports).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("Тесты метода resolve")
	class ResolveTests {
		
		@Test
		@DisplayName("Если жалобы не существует, должен выбросить ResourceNotFoundException")
		void resolve_whenReportDoesNotExist_shouldThrowException() {
			// Arrange
			Long reportId = 999L;
			when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> reportService.resolve(reportId, "DELETE_REVIEW", currentUser))
					.isInstanceOf(ResourceNotFoundException.class);
			
			verify(reportRepository, never()).delete(any());
			verify(reviewService, never()).deleteReview(any(), any());
		}
		
		@Test
		@DisplayName("Если действие DELETE_REVIEW, должен удалить жалобу и удалить связанный отзыв")
		void resolve_whenActionIsDeleteReview_shouldDeleteReportAndReview() {
			// Arrange
			Long reportId = testReport.getId();
			Long reviewId = testReview.getId();
			
			testReport.setReview(testReview);
			expectedReport.setReview(ClassesExamples.getExistingReview());
			
			when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
			
			// Act
			reportService.resolve(reportId, "DELETE_REVIEW", currentUser);
			
			// Assert
			verify(reportRepository, times(1)).delete(reportCaptor.capture());
			assertThat(reportCaptor.getValue())
					.isSameAs(testReport)
					.usingRecursiveComparison()
					.isEqualTo(expectedReport);
			
			verify(reviewService, times(1)).deleteReview(reviewId, currentUser);
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
			verify(reportRepository, times(1)).delete(reportCaptor.capture());
			assertThat(reportCaptor.getValue())
					.isSameAs(testReport)
					.usingRecursiveComparison()
					.isEqualTo(expectedReport);
			
			verify(reviewService, never()).deleteReview(any(), any());
		}
		
		@Test
		@DisplayName("Если действие неизвестно, не должен делать ничего")
		void resolve_whenActionIsUnknown_shouldDoNothing() {
			// Arrange
			Long reportId = testReport.getId();
			when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
			
			// Act
			reportService.resolve(reportId, "UNKNOWN_ACTION", currentUser);
			
			// Assert
			verify(reportRepository, never()).delete(any());
			verify(reviewService, never()).deleteReview(any(), any());
		}
	}
}
