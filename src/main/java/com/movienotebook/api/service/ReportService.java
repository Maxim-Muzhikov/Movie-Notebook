package com.movienotebook.api.service;

import com.movienotebook.api.dto.report.ReportRequestDto;
import com.movienotebook.api.entity.Report;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.repository.ReportRepository;
import com.movienotebook.api.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {
	
	private final ReportRepository reportRepository;
	private final ReviewService reviewService;
	private final UserService userService;
	
	@Transactional
	public Report addOrUpdatedReport(ReportRequestDto request, CustomUserDetails currentUser) {
		
		Review review = reviewService.getReviewById(request.reviewId());
		
		Optional<Report> existingReport = reportRepository.findByReviewAndReporter(request.reviewId(), currentUser.getId());
		
		if (existingReport.isPresent()) {
			existingReport.get().setReason(request.reason());
			return existingReport.get();
		} else {
			User user = userService.getReferenceById(currentUser.getId());
			Report newReport = new Report();
			newReport.setReporter(user);
			newReport.setReview(review);
			newReport.setReason(request.reason());
			// TODO Создать Status Enum
			newReport.setStatus("NEW");
			reportRepository.save(newReport);
			return newReport;
		}
	}
	
	public List<Report> getAll() {
		return reportRepository.findAll();
	}
	
	@Transactional
	public void resolve(Long id, String action, CustomUserDetails currentUser) {
		
		Report report = reportRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Жалоба с номером " + id + " не найдена"));
		
		if ("DELETE_REVIEW".equals(action)) {
			reportRepository.delete(report);
			reviewService.deleteReview(report.getReview().getId(), currentUser);
		} else if ("REJECT_REPORT".equals(action)) {
			reportRepository.delete(report);
		}
	}
}
