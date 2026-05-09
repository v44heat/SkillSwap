package com.skillswap.controller;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.SkillDTO;
import com.skillswap.service.SkillService;
import com.skillswap.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController extends BaseController {

    private final SkillService skillService;

    public SkillController(UserService userService, SkillService skillService) {
        super(userService);
        this.skillService = skillService;
    }

    /** GET /api/skills/listings?category=&search= — browse all active listings */
    @GetMapping("/listings")
    public ResponseEntity<ApiResponse.Success<List<SkillDTO.ListingResponse>>> browse(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.Success.of(skillService.browse(category, search)));
    }

    /** GET /api/skills/listings/{id} */
    @GetMapping("/listings/{id}")
    public ResponseEntity<ApiResponse.Success<SkillDTO.ListingResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.of(skillService.getById(id)));
    }

    /** GET /api/skills/my — current user's listings */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse.Success<List<SkillDTO.ListingResponse>>> myListings(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(skillService.myListings(currentUser(principal))));
    }

    /** POST /api/skills — create a listing */
    @PostMapping
    public ResponseEntity<ApiResponse.Success<SkillDTO.ListingResponse>> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody SkillDTO.CreateRequest req) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(skillService.create(currentUser(principal), req)));
    }

    /** PUT /api/skills/{id} — update listing */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse.Success<SkillDTO.ListingResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody SkillDTO.UpdateRequest req) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(skillService.update(id, currentUser(principal), req)));
    }

    /** DELETE /api/skills/{id} — delete listing */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse.Success<String>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        skillService.delete(id, currentUser(principal));
        return ResponseEntity.ok(ApiResponse.Success.of("Listing deleted"));
    }

    /** PATCH /api/skills/{id}/toggle — pause / unpause listing */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse.Success<SkillDTO.ListingResponse>> toggle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(skillService.toggle(id, currentUser(principal))));
    }

    /** POST /api/skills/{id}/flag?reason= — flag a listing */
    @PostMapping("/{id}/flag")
    public ResponseEntity<ApiResponse.Success<String>> flag(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails principal) {
        skillService.flag(id, currentUser(principal), reason);
        return ResponseEntity.ok(ApiResponse.Success.of("Listing flagged for review"));
    }
}