package com.skillswap.controller;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.RequestDTO;
import com.skillswap.service.RequestService;
import com.skillswap.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController extends BaseController {

    private final RequestService requestService;

    public RequestController(UserService userService, RequestService requestService) {
        super(userService);
        this.requestService = requestService;
    }

    /** POST /api/requests — send a session request */
    @PostMapping
    public ResponseEntity<ApiResponse.Success<RequestDTO.RequestResponse>> send(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RequestDTO.SendRequest req) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(requestService.send(currentUser(principal), req)));
    }

    /** GET /api/requests/incoming — requests where I am teacher */
    @GetMapping("/incoming")
    public ResponseEntity<ApiResponse.Success<List<RequestDTO.RequestResponse>>> incoming(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(requestService.incoming(currentUser(principal))));
    }

    /** GET /api/requests/outgoing — requests I sent */
    @GetMapping("/outgoing")
    public ResponseEntity<ApiResponse.Success<List<RequestDTO.RequestResponse>>> outgoing(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(requestService.outgoing(currentUser(principal))));
    }

    /** PATCH /api/requests/{id}/respond — accept or decline */
    @PatchMapping("/{id}/respond")
    public ResponseEntity<ApiResponse.Success<RequestDTO.RequestResponse>> respond(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody RequestDTO.RespondRequest req) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(requestService.respond(id, currentUser(principal), req)));
    }

    /** PATCH /api/requests/{id}/withdraw — cancel own pending request */
    @PatchMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse.Success<RequestDTO.RequestResponse>> withdraw(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(requestService.withdraw(id, currentUser(principal))));
    }
}