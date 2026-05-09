package com.skillswap.repository;

import com.skillswap.model.Session;
import com.skillswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("SELECT s FROM Session s WHERE s.teacher = :user OR s.learner = :user ORDER BY s.scheduledAt DESC")
    List<Session> findAllForUser(@Param("user") User user);

    @Query("SELECT s FROM Session s WHERE (s.teacher = :user OR s.learner = :user) " +
            "AND s.status = 'CONFIRMED' AND s.scheduledAt >= CURRENT_TIMESTAMP ORDER BY s.scheduledAt ASC")
    List<Session> findUpcomingForUser(@Param("user") User user);

    @Query("SELECT s FROM Session s WHERE (s.teacher = :user OR s.learner = :user) " +
            "AND s.status = 'COMPLETED' ORDER BY s.scheduledAt DESC")
    List<Session> findHistoryForUser(@Param("user") User user);

    long countByStatus(Session.SessionStatus status);
}