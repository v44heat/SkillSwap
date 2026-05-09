package com.skillswap.controller;

import com.skillswap.dto.ApiResponse;
import com.skillswap.dto.UserDTO;
import com.skillswap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {

    public UserController(UserService userService) {
        super(userService);
    }

    /** GET /api/users/me — current user's full profile */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse.Success<UserDTO.ProfileResponse>> me(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(userService.toProfile(currentUser(principal))));
    }

    /** PUT /api/users/me — update own profile */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse.Success<UserDTO.ProfileResponse>> update(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody UserDTO.UpdateRequest req) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(userService.updateProfile(principal.getUsername(), req)));
    }

    /** GET /api/users/{id} — public profile of any user */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse.Success<UserDTO.ProfileResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.Success.of(userService.toProfile(userService.getById(id))));
    }
}