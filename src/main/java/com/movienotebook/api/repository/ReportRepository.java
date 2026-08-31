package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Report;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
	
	Optional<Report> findByReviewIdAndReporterId(Long reviewId, Long reporterId);
	
	List<Report> findAllByReason(String reason);
	
	List<Report> findAllByReasonContainingIgnoreCase(String reason);
	
	List<Report> findAllByStatus(String status);
	
	List<Report> findAllByReview(Review review);
	
	List<Report> findAllByReviewId(Long reviewId);
	
	List<Report> findAllByReporter(User user);
	
	List<Report> findAllByReporterId(Long userId);
	
	List<Report> findAllByReview_User(User targetUser);
	
	List<Report> findAllByReview_User_Id(Long targetUserId);
	
}
