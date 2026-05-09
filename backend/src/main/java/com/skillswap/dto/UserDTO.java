package com.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class UserDTO {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProfileResponse {
        private Long   id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String department;
        private String yearOfStudy;
        private String studentId;
        private String bio;
        private String role;
        private String status;
        private BigDecimal averageRating;
        private Integer    totalSessions;
        private OffsetDateTime createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String department;
        private String yearOfStudy;
        private String bio;
    }

    /** Compact user ref used inside nested objects */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserRef {
        private Long   id;
        private String fullName;
        private String department;
        private BigDecimal averageRating;
    }
}