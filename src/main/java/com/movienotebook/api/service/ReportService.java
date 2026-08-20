package com.movienotebook.api.service;

import com.movienotebook.api.dto.report.ReportRequestDto;
import com.movienotebook.api.dto.report.ReportResponseDto;
import com.movienotebook.api.entity.Report;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.mapper.ReportMapper;
import com.movienotebook.api.repository.ReportRepository;
import com.movienotebook.api.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
	
	private final ReportRepository reportRepository;
	private final ReviewService reviewService;
	private final UserService userService;
	private final ReportMapper reportMapper;
	
	@Transactional
	public ReportResponseDto save(ReportRequestDto request, CustomUserDetails currentUser) {
		
		Review review = reviewService.getEntityById(request.reviewId());
		
		Optional<Report> existingReport = reportRepository.findByReviewAndReporter(request.reviewId(), currentUser.getId());
		
		if (existingReport.isPresent()) {
			existingReport.get().setReason(request.reason());
			return reportMapper.toDto(existingReport.get());
		} else {
			User user = userService.getReferenceById(currentUser.getId());
			Report newReport = new Report();
			newReport.setReporter(user);
			newReport.setReview(review);
			newReport.setReason(request.reason());
			// TODO Создать Status Enum
			newReport.setStatus("NEW");
			reportRepository.save(newReport);
			return reportMapper.toDto(newReport);
		}
	}
	
	@Transactional
	public void resolve(Long reportId, String action, CustomUserDetails currentUser) {
		
		Report report = reportRepository.findById(reportId)
				.orElseThrow(() -> new ResourceNotFoundException("Жалоба с номером " + reportId + " не найдена"));
		
		if ("DELETE_REVIEW".equals(action)) {
			reportRepository.delete(report);
			reviewService.delete(report.getReview().getId(), currentUser);
		} else if ("REJECT_REPORT".equals(action)) {
			reportRepository.delete(report);
		}
	}
	
	public List<ReportResponseDto> getAll() {
		return reportRepository.findAll().stream().map(reportMapper::toDto).toList();
	}
	
	public List<Report> getAllEntity() {
		return reportRepository.findAll();
	}
}
