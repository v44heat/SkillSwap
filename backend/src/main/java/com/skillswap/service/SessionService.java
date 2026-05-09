package com.skillswap.service;

import com.skillswap.dto.SessionDTO;
import com.skillswap.exception.AppException;
import com.skillswap.model.Session;
import com.skillswap.model.User;
import com.skillswap.repository.FeedbackRepository;
import com.skillswap.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository  sessionRepo;
    private final FeedbackRepository feedbackRepo;

    public List<SessionDTO.SessionResponse> allForUser(User user) {
        return sessionRepo.findAllForUser(user)
            .stream().map(s -> toResponse(s, user)).collect(Collectors.toList());
    }

    public List<SessionDTO.SessionResponse> upcomingForUser(User user) {
        return sessionRepo.findUpcomingForUser(user)
            .stream().map(s -> toResponse(s, user)).collect(Collectors.toList());
    }

    public List<SessionDTO.SessionResponse> historyForUser(User user) {
        return sessionRepo.findHistoryForUser(user)
            .stream().map(s -> toResponse(s, user)).collect(Collectors.toList());
    }

    public SessionDTO.SessionResponse getById(Long id, User user) {
        Session session = findById(id);
        assertParticipant(session, user);
        return toResponse(session, user);
    }

    @Transactional
    public SessionDTO.SessionResponse cancel(Long id, User user) {
        Session session = findById(id);
        assertParticipant(session, user);
        if (session.getStatus() != Session.SessionStatus.CONFIRMED)
            throw AppException.badRequest("Only confirmed sessions can be cancelled");
        session.setStatus(Session.SessionStatus.CANCELLED);
        return toResponse(sessionRepo.save(session), user);
    }

    @Transactional
    public SessionDTO.SessionResponse complete(Long id, User user) {
        Session session = findById(id);
        assertParticipant(session, user);
        if (session.getStatus() != Session.SessionStatus.CONFIRMED)
            throw AppException.badRequest("Only confirmed sessions can be marked complete");
        session.setStatus(Session.SessionStatus.COMPLETED);
        session.setCompletedAt(OffsetDateTime.now());
        return toResponse(sessionRepo.save(session), user);
    }

    // ── Admin view — no user context needed ──────────────────────────
    public SessionDTO.SessionResponse toAdminResponse(Session s) {
        return SessionDTO.SessionResponse.builder()
            .id(s.getId())
            .skillListingId(s.getSkillListing().getId())
            .skillTitle(s.getSkillListing().getTitle())
            .category(s.getSkillListing().getCategory().name())
            .teacherId(s.getTeacher().getId())
            .teacherName(s.getTeacher().getFullName())
            .learnerId(s.getLearner().getId())
            .learnerName(s.getLearner().getFullName())
            .status(s.getStatus().name())
            .scheduledAt(s.getScheduledAt())
            .duration(s.getDuration())
            .meetingFormat(s.getMeetingFormat().name())
            .cancelReason(s.getCancelReason())
            .completedAt(s.getCompletedAt())
            .createdAt(s.getCreatedAt())
            .myRole(null)
            .feedbackGiven(false)
            .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────
    public Session findById(Long id) {
        return sessionRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Session"));
    }

    private void assertParticipant(Session session, User user) {
        boolean isTeacher = session.getTeacher().getId().equals(user.getId());
        boolean isLearner = session.getLearner().getId().equals(user.getId());
        if (!isTeacher && !isLearner)
            throw AppException.forbidden("You are not a participant of this session");
    }

    public SessionDTO.SessionResponse toResponse(Session s, User viewer) {
        boolean isTeacher    = s.getTeacher().getId().equals(viewer.getId());
        boolean feedbackGiven = feedbackRepo.existsBySessionAndReviewer(s, viewer);

        return SessionDTO.SessionResponse.builder()
            .id(s.getId())
            .skillListingId(s.getSkillListing().getId())
            .skillTitle(s.getSkillListing().getTitle())
            .category(s.getSkillListing().getCategory().name())
            .teacherId(s.getTeacher().getId())
            .teacherName(s.getTeacher().getFullName())
            .learnerId(s.getLearner().getId())
            .learnerName(s.getLearner().getFullName())
            .status(s.getStatus().name())
            .scheduledAt(s.getScheduledAt())
            .duration(s.getDuration())
            .meetingFormat(s.getMeetingFormat().name())
            .cancelReason(s.getCancelReason())
            .completedAt(s.getCompletedAt())
            .createdAt(s.getCreatedAt())
            .myRole(isTeacher ? "TEACHER" : "LEARNER")
            .feedbackGiven(feedbackGiven)
            .build();
    }
}
