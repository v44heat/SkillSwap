package com.skillswap.service;

import com.skillswap.dto.FeedbackDTO;
import com.skillswap.exception.AppException;
import com.skillswap.model.Feedback;
import com.skillswap.model.Session;
import com.skillswap.model.User;
import com.skillswap.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepo;
    private final SessionService     sessionService;

    @Transactional
    public FeedbackDTO.FeedbackResponse leave(User reviewer, FeedbackDTO.LeaveRequest req) {
        Session session = sessionService.findById(req.getSessionId());

        if (session.getStatus() != Session.SessionStatus.COMPLETED) {
            throw AppException.badRequest("Feedback can only be left for completed sessions");
        }

        boolean isTeacher = session.getTeacher().getId().equals(reviewer.getId());
        boolean isLearner = session.getLearner().getId().equals(reviewer.getId());
        if (!isTeacher && !isLearner) {
            throw AppException.forbidden("You are not a participant of this session");
        }

        // Cannot review twice
        if (feedbackRepo.existsBySessionAndReviewer(session, reviewer)) {
            throw AppException.conflict("You have already submitted feedback for this session");
        }

        // Reviewee is the other participant
        User reviewee = isTeacher ? session.getLearner() : session.getTeacher();

        if (req.getOverallRating() < 1 || req.getOverallRating() > 5) {
            throw AppException.badRequest("Rating must be between 1 and 5");
        }

        Feedback feedback = Feedback.builder()
                .session(session)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .skillListing(session.getSkillListing())
                .overallRating(req.getOverallRating())
                .reviewText(req.getReviewText())
                .isReported(false)
                .build();

        // DB trigger recalculates average_rating on skill_listings and users
        return toResponse(feedbackRepo.save(feedback));
    }

    public List<FeedbackDTO.FeedbackResponse> received(User user) {
        return feedbackRepo.findByRevieweeOrderByCreatedAtDesc(user)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<FeedbackDTO.FeedbackResponse> given(User user) {
        return feedbackRepo.findByReviewerOrderByCreatedAtDesc(user)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public FeedbackDTO.FeedbackResponse reply(Long id, User teacher, String text) {
        Feedback feedback = findById(id);
        if (!feedback.getReviewee().getId().equals(teacher.getId())) {
            throw AppException.forbidden("Only the reviewee can reply to feedback");
        }
        feedback.setTeacherReply(text);
        return toResponse(feedbackRepo.save(feedback));
    }

    @Transactional
    public FeedbackDTO.FeedbackResponse report(Long id, User reporter) {
        Feedback feedback = findById(id);
        // Only the reviewee can report feedback about themselves
        if (!feedback.getReviewee().getId().equals(reporter.getId())) {
            throw AppException.forbidden("Only the reviewee can report this feedback");
        }
        feedback.setIsReported(true);
        return toResponse(feedbackRepo.save(feedback));
    }

    // ── Helpers ──────────────────────────────────────────────────────
    public Feedback findById(Long id) {
        return feedbackRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Feedback"));
    }

    public FeedbackDTO.FeedbackResponse toResponse(Feedback f) {
        return FeedbackDTO.FeedbackResponse.builder()
                .id(f.getId())
                .sessionId(f.getSession().getId())
                .skillListingId(f.getSkillListing().getId())
                .skillTitle(f.getSkillListing().getTitle())
                .reviewerId(f.getReviewer().getId())
                .reviewerName(f.getReviewer().getFullName())
                .revieweeId(f.getReviewee().getId())
                .revieweeName(f.getReviewee().getFullName())
                .overallRating(f.getOverallRating())
                .reviewText(f.getReviewText())
                .teacherReply(f.getTeacherReply())
                .isReported(f.getIsReported())
                .createdAt(f.getCreatedAt())
                .build();
    }
}