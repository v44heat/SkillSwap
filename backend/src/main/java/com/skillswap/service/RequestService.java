package com.skillswap.service;

import com.skillswap.dto.RequestDTO;
import com.skillswap.exception.AppException;
import com.skillswap.model.Session;
import com.skillswap.model.SessionRequest;
import com.skillswap.model.SkillListing;
import com.skillswap.model.User;
import com.skillswap.repository.SessionRepository;
import com.skillswap.repository.SessionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final SessionRequestRepository requestRepo;
    private final SessionRepository        sessionRepo;
    private final SkillService             skillService;

    // ── Send a request ───────────────────────────────────────────────
    @Transactional
    public RequestDTO.RequestResponse send(User requester, RequestDTO.SendRequest req) {
        SkillListing listing = skillService.findById(req.getSkillListingId());

        // Cannot request your own listing
        if (listing.getUser().getId().equals(requester.getId())) {
            throw AppException.badRequest("You cannot request your own skill listing");
        }
        // No duplicate pending requests
        if (requestRepo.existsBySkillListingAndRequesterAndStatus(
                listing, requester, SessionRequest.RequestStatus.PENDING)) {
            throw AppException.conflict("You already have a pending request for this listing");
        }

        SessionRequest request = SessionRequest.builder()
                .skillListing(listing)
                .requester(requester)
                .teacher(listing.getUser())
                .status(SessionRequest.RequestStatus.PENDING)
                .proposedDatetime(OffsetDateTime.parse(req.getProposedDateTime()))
                .duration(req.getDuration())
                .focusMessage(req.getFocusMessage())
                .meetingFormat(parseMeetingFormat(req.getMeetingFormat()))
                .build();

        return toResponse(requestRepo.save(request));
    }

    // ── Incoming (where I am the teacher) ───────────────────────────
    public List<RequestDTO.RequestResponse> incoming(User teacher) {
        return requestRepo.findByTeacherOrderByCreatedAtDesc(teacher)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Outgoing (where I am the requester) ─────────────────────────
    public List<RequestDTO.RequestResponse> outgoing(User requester) {
        return requestRepo.findByRequesterOrderByCreatedAtDesc(requester)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Accept / Decline ─────────────────────────────────────────────
    @Transactional
    public RequestDTO.RequestResponse respond(Long id, User teacher, RequestDTO.RespondRequest req) {
        SessionRequest request = findById(id);

        if (!request.getTeacher().getId().equals(teacher.getId())) {
            throw AppException.forbidden("This request is not addressed to you");
        }
        if (request.getStatus() != SessionRequest.RequestStatus.PENDING) {
            throw AppException.badRequest("Request is no longer pending");
        }

        String action = req.getAction().toUpperCase();
        if ("ACCEPTED".equals(action)) {
            request.setStatus(SessionRequest.RequestStatus.ACCEPTED);

            // Auto-create session
            Session session = Session.builder()
                    .request(request)
                    .skillListing(request.getSkillListing())
                    .teacher(request.getTeacher())
                    .learner(request.getRequester())
                    .scheduledAt(request.getProposedDatetime())
                    .duration(request.getDuration())
                    .meetingFormat(request.getMeetingFormat())
                    .status(Session.SessionStatus.CONFIRMED)
                    .build();
            sessionRepo.save(session);

        } else if ("DECLINED".equals(action)) {
            request.setStatus(SessionRequest.RequestStatus.DECLINED);
            request.setDeclineReason(req.getDeclineReason());
        } else {
            throw AppException.badRequest("action must be ACCEPTED or DECLINED");
        }

        return toResponse(requestRepo.save(request));
    }

    // ── Withdraw ─────────────────────────────────────────────────────
    @Transactional
    public RequestDTO.RequestResponse withdraw(Long id, User requester) {
        SessionRequest request = findById(id);

        if (!request.getRequester().getId().equals(requester.getId())) {
            throw AppException.forbidden("This is not your request");
        }
        if (request.getStatus() != SessionRequest.RequestStatus.PENDING) {
            throw AppException.badRequest("Only pending requests can be withdrawn");
        }
        request.setStatus(SessionRequest.RequestStatus.WITHDRAWN);
        return toResponse(requestRepo.save(request));
    }

    // ── Helpers ──────────────────────────────────────────────────────
    public SessionRequest findById(Long id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Request"));
    }

    private SessionRequest.MeetingFormat parseMeetingFormat(String fmt) {
        if (fmt == null) return SessionRequest.MeetingFormat.FLEXIBLE;
        try { return SessionRequest.MeetingFormat.valueOf(fmt.toUpperCase()); }
        catch (Exception e) { return SessionRequest.MeetingFormat.FLEXIBLE; }
    }

    public RequestDTO.RequestResponse toResponse(SessionRequest r) {
        SkillListing listing = r.getSkillListing();
        User teacher  = r.getTeacher();
        User requester = r.getRequester();
        return RequestDTO.RequestResponse.builder()
                .id(r.getId())
                .skillListingId(listing.getId())
                .skillTitle(listing.getTitle())
                .category(listing.getCategory().name())
                .requesterId(requester.getId())
                .requesterName(requester.getFullName())
                .requesterDept(requester.getDepartment())
                .teacherId(teacher.getId())
                .teacherName(teacher.getFullName())
                .teacherDept(teacher.getDepartment())
                .teacherRating(teacher.getAverageRating())
                .status(r.getStatus().name())
                .proposedDatetime(r.getProposedDatetime())
                .duration(r.getDuration())
                .focusMessage(r.getFocusMessage())
                .meetingFormat(r.getMeetingFormat().name())
                .declineReason(r.getDeclineReason())
                .createdAt(r.getCreatedAt())
                .build();
    }
}