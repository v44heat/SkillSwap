package com.skillswap.service;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.FeedbackDTO;
import com.skillswap.dto.SessionDTO;
import com.skillswap.dto.SkillDTO;
import com.skillswap.dto.UserDTO;
import com.skillswap.exception.AppException;
import com.skillswap.model.Session;
import com.skillswap.model.SessionRequest;
import com.skillswap.model.SkillListing;
import com.skillswap.model.User;
import com.skillswap.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository           userRepo;
    private final SkillListingRepository   listingRepo;
    private final SessionRepository        sessionRepo;
    private final SessionRequestRepository requestRepo;
    private final FeedbackRepository       feedbackRepo;
    private final UserService              userService;
    private final SkillService             skillService;
    private final FeedbackService          feedbackService;
    private final SessionService           sessionService;
    private final PasswordEncoder          passwordEncoder;

    // ── Stats ────────────────────────────────────────────────────────
    public ApiResponse.AdminStats stats() {
        long total     = userRepo.count();
        long active    = userRepo.findAll().stream()
            .filter(u -> u.getStatus() == User.UserStatus.ACTIVE).count();
        long suspended = total - active;
        long listings  = listingRepo.count();
        long activeLst = listingRepo.countByIsActiveTrue();
        long flagged   = listingRepo.findByIsFlaggedTrue().size();
        long sessions  = sessionRepo.count();
        long completed = sessionRepo.countByStatus(Session.SessionStatus.COMPLETED);
        long pending   = requestRepo.countByStatus(SessionRequest.RequestStatus.PENDING);
        long reported  = feedbackRepo.countByIsReportedTrue();

        return ApiResponse.AdminStats.builder()
            .totalUsers(total)
            .activeUsers(active)
            .suspendedUsers(suspended)
            .totalListings(listings)
            .activeListings(activeLst)
            .flaggedListings(flagged)
            .totalSessions(sessions)
            .completedSessions(completed)
            .pendingRequests(pending)
            .reportedFeedback(reported)
            .build();
    }

    // ── Users ────────────────────────────────────────────────────────
    public List<UserDTO.ProfileResponse> allUsers() {
        return userRepo.findAll().stream()
            .map(userService::toProfile).collect(Collectors.toList());
    }

    public List<UserDTO.ProfileResponse> searchUsers(String query) {
        return userRepo.searchUsers(query).stream()
            .map(userService::toProfile).collect(Collectors.toList());
    }

    public UserDTO.ProfileResponse getUser(Long id) {
        return userService.toProfile(userService.getById(id));
    }

    @Transactional
    public UserDTO.ProfileResponse suspendUser(Long id) {
        User user = userService.getById(id);
        if (user.getRole() == User.UserRole.ADMIN)
            throw AppException.forbidden("Cannot suspend an admin account");
        user.setStatus(User.UserStatus.SUSPENDED);
        return userService.toProfile(userRepo.save(user));
    }

    @Transactional
    public UserDTO.ProfileResponse activateUser(Long id) {
        User user = userService.getById(id);
        user.setStatus(User.UserStatus.ACTIVE);
        return userService.toProfile(userRepo.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userService.getById(id);
        if (user.getRole() == User.UserRole.ADMIN)
            throw AppException.forbidden("Cannot delete an admin account");
        userRepo.delete(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userService.getById(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    // ── Listings ─────────────────────────────────────────────────────
    public List<SkillDTO.ListingResponse> allListings() {
        return listingRepo.findAll().stream()
            .map(skillService::toResponse).collect(Collectors.toList());
    }

    public List<SkillDTO.ListingResponse> flaggedListings() {
        return listingRepo.findByIsFlaggedTrue().stream()
            .map(skillService::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public SkillDTO.ListingResponse clearFlag(Long id) {
        SkillListing listing = skillService.findById(id);
        listing.setIsFlagged(false);
        listing.setFlagReason(null);
        return skillService.toResponse(listingRepo.save(listing));
    }

    @Transactional
    public void removeListing(Long id) {
        listingRepo.delete(skillService.findById(id));
    }

    // ── Sessions ─────────────────────────────────────────────────────
    /** All sessions on the platform — for admin dashboard table */
    public List<SessionDTO.SessionResponse> allSessions() {
        // Pass a null user — admin sees everything, myRole will be null
        return sessionRepo.findAll().stream()
            .map(s -> sessionService.toAdminResponse(s))
            .collect(Collectors.toList());
    }

    // ── Feedback ─────────────────────────────────────────────────────
    public List<FeedbackDTO.FeedbackResponse> reportedFeedback() {
        return feedbackRepo.findByIsReportedTrueOrderByCreatedAtDesc().stream()
            .map(feedbackService::toResponse).collect(Collectors.toList());
    }
}
