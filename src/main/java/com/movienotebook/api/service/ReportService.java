package com.movienotebook.api.service;

import com.movienotebook.api.dto.report.ReportRequestDto;
import com.movienotebook.api.entity.Report;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.repository.ReportRepository;
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
	public Report addOrUpdatedReport(ReportRequestDto request, String currentUser) {
		
		User reporter = userService.getByUsername(currentUser);
		Review review = reviewService.getReviewById(request.reviewId());
		
		Optional<Report> existingReport = reportRepository.findByReviewAndReporter(request.reviewId(), reporter.getId());
		
		if (existingReport.isPresent()) {
			existingReport.get().setReason(request.reason());
			return existingReport.get();
		} else {
			Report newReport = new Report();
			newReport.setUser(reporter);
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
	public void resolve(Long id, String action, String username) {
		
		Report report = reportRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Жалоба с номером " + id + " не найдена"));
		
		if ("DELETE_REVIEW".equals(action)) {
			reportRepository.delete(report);
			reviewService.deleteReview(report.getReview().getId(), username);
		} else if ("REJECT_REPORT".equals(action)) {
			reportRepository.delete(report);
		}
	}
}
