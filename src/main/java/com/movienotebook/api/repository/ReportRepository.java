package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Report;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
	
	@Query("SELECT r FROM Report r WHERE r.review.id = :reviewId AND r.reporter.id = :reporterId")
	Optional<Report> findByReviewAndReporter(
			@Param("reviewId") Long reviewId,
			@Param("reporterId") Long reporterId
	);
	
	@Query("SELECT r FROM Report r")
	List<Report> findAll();
	
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
