package com.skillswap.controller;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.FeedbackDTO;
import com.skillswap.dto.SessionDTO;
import com.skillswap.dto.SkillDTO;
import com.skillswap.dto.UserDTO;
import com.skillswap.service.AdminService;
import com.skillswap.service.UserService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController extends BaseController {

    private final AdminService adminService;

    public AdminController(UserService userService, AdminService adminService) {
        super(userService);
        this.adminService = adminService;
    }

    // ── Dashboard stats ──────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse.Success<ApiResponse.AdminStats>> stats() {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.stats()));
    }

    // ── Users ────────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<ApiResponse.Success<List<UserDTO.ProfileResponse>>> users() {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.allUsers()));
    }

    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse.Success<List<UserDTO.ProfileResponse>>> searchUsers(
            @RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.searchUsers(query)));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse.Success<UserDTO.ProfileResponse>> getUser(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.getUser(id)));
    }

    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse.Success<UserDTO.ProfileResponse>> suspendUser(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.suspendUser(id)));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse.Success<UserDTO.ProfileResponse>> activateUser(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.activateUser(id)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse.Success<String>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.Success.of("User deleted"));
    }

    @PatchMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse.Success<String>> resetPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordRequest req) {
        adminService.resetPassword(id, req.getNewPassword());
        return ResponseEntity.ok(ApiResponse.Success.of("Password reset successfully"));
    }

    // ── Listings ─────────────────────────────────────────────────────
    @GetMapping("/listings")
    public ResponseEntity<ApiResponse.Success<List<SkillDTO.ListingResponse>>> listings() {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.allListings()));
    }

    @GetMapping("/listings/flagged")
    public ResponseEntity<ApiResponse.Success<List<SkillDTO.ListingResponse>>> flagged() {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.flaggedListings()));
    }

    @PatchMapping("/listings/{id}/clear-flag")
    public ResponseEntity<ApiResponse.Success<SkillDTO.ListingResponse>> clearFlag(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.clearFlag(id)));
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<ApiResponse.Success<String>> removeListing(@PathVariable Long id) {
        adminService.removeListing(id);
        return ResponseEntity.ok(ApiResponse.Success.of("Listing removed"));
    }

    // ── Sessions ─────────────────────────────────────────────────────
    /** GET /api/admin/sessions — all sessions on the platform */
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse.Success<List<SessionDTO.SessionResponse>>> sessions() {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.allSessions()));
    }

    // ── Feedback ─────────────────────────────────────────────────────
    @GetMapping("/feedback/reported")
    public ResponseEntity<ApiResponse.Success<List<FeedbackDTO.FeedbackResponse>>> reported() {
        return ResponseEntity.ok(ApiResponse.Success.of(adminService.reportedFeedback()));
    }

    // ── Inner request DTO ─────────────────────────────────────────────
    @Data @NoArgsConstructor
    public static class ResetPasswordRequest {
        private String newPassword;
    }
}
