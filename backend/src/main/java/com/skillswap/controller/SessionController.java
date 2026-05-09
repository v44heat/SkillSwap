package com.skillswap.controller;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.SessionDTO;
import com.skillswap.service.SessionService;
import com.skillswap.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController extends BaseController {

    private final SessionService sessionService;

    public SessionController(UserService userService, SessionService sessionService) {
        super(userService);
        this.sessionService = sessionService;
    }

    /** GET /api/sessions — all sessions for current user */
    @GetMapping
    public ResponseEntity<ApiResponse.Success<List<SessionDTO.SessionResponse>>> all(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(sessionService.allForUser(currentUser(principal))));
    }

    /** GET /api/sessions/upcoming */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse.Success<List<SessionDTO.SessionResponse>>> upcoming(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(sessionService.upcomingForUser(currentUser(principal))));
    }

    /** GET /api/sessions/history */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse.Success<List<SessionDTO.SessionResponse>>> history(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(sessionService.historyForUser(currentUser(principal))));
    }

    /** GET /api/sessions/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse.Success<SessionDTO.SessionResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(sessionService.getById(id, currentUser(principal))));
    }

    /** PATCH /api/sessions/{id}/cancel */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse.Success<SessionDTO.SessionResponse>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(sessionService.cancel(id, currentUser(principal))));
    }

    /** PATCH /api/sessions/{id}/complete */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse.Success<SessionDTO.SessionResponse>> complete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(sessionService.complete(id, currentUser(principal))));
    }
}