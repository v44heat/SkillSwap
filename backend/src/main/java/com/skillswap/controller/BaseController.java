package com.skillswap.controller;

import com.skillswap.model.User;
import com.skillswap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Shared helper — subclasses call currentUser(principal) to resolve the
 * full User entity from the JWT-authenticated principal.
 */
@RequiredArgsConstructor
public abstract class BaseController {

    protected final UserService userService;

    protected User currentUser(UserDetails principal) {
        return userService.getByEmail(principal.getUsername());
    }
}