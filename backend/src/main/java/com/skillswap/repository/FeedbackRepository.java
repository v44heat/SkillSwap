package com.skillswap.repository;

import com.skillswap.model.Feedback;
import com.skillswap.model.Session;
import com.skillswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByRevieweeOrderByCreatedAtDesc(User reviewee);

    List<Feedback> findByReviewerOrderByCreatedAtDesc(User reviewer);

    List<Feedback> findByIsReportedTrueOrderByCreatedAtDesc();

    boolean existsBySessionAndReviewer(Session session, User reviewer);

    long countByIsReportedTrue();
}