package com.skillswap.controller;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.FeedbackDTO;
import com.skillswap.service.FeedbackService;
import com.skillswap.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController extends BaseController {

    private final FeedbackService feedbackService;

    public FeedbackController(UserService userService, FeedbackService feedbackService) {
        super(userService);
        this.feedbackService = feedbackService;
    }

    /** POST /api/feedback — leave feedback for a completed session */
    @PostMapping
    public ResponseEntity<ApiResponse.Success<FeedbackDTO.FeedbackResponse>> leave(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody FeedbackDTO.LeaveRequest req) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(feedbackService.leave(currentUser(principal), req)));
    }

    /** GET /api/feedback/received */
    @GetMapping("/received")
    public ResponseEntity<ApiResponse.Success<List<FeedbackDTO.FeedbackResponse>>> received(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(feedbackService.received(currentUser(principal))));
    }

    /** GET /api/feedback/given */
    @GetMapping("/given")
    public ResponseEntity<ApiResponse.Success<List<FeedbackDTO.FeedbackResponse>>> given(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(feedbackService.given(currentUser(principal))));
    }

    /** GET /api/feedback/user/{id} — public feedback for a user (their profile) */
    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse.Success<List<FeedbackDTO.FeedbackResponse>>> forUser(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(feedbackService.received(userService.getById(id))));
    }

    /** PATCH /api/feedback/{id}/reply?text= — teacher replies to a review */
    @PatchMapping("/{id}/reply")
    public ResponseEntity<ApiResponse.Success<FeedbackDTO.FeedbackResponse>> reply(
            @PathVariable Long id,
            @RequestParam String text,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(feedbackService.reply(id, currentUser(principal), text)));
    }

    /** PATCH /api/feedback/{id}/report — report inappropriate feedback */
    @PatchMapping("/{id}/report")
    public ResponseEntity<ApiResponse.Success<FeedbackDTO.FeedbackResponse>> report(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(feedbackService.report(id, currentUser(principal))));
    }
}