package com.skillswap.repository;

import com.skillswap.model.SessionRequest;
import com.skillswap.model.SkillListing;
import com.skillswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRequestRepository extends JpaRepository<SessionRequest, Long> {

    // Requests where current user is the learner (sent outgoing)
    List<SessionRequest> findByRequesterOrderByCreatedAtDesc(User requester);

    // Requests where current user is the teacher (incoming)
    List<SessionRequest> findByTeacherOrderByCreatedAtDesc(User teacher);

    // Check for duplicate pending request
    boolean existsBySkillListingAndRequesterAndStatus(
            SkillListing skillListing, User requester, SessionRequest.RequestStatus status
    );

    long countByStatus(SessionRequest.RequestStatus status);
}