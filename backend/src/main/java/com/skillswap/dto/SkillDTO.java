package com.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class SkillDTO {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ListingResponse {
        private Long   id;
        private Long   userId;
        private String teacherName;
        private String teacherDept;
        private BigDecimal teacherRating;
        private String title;
        private String description;
        private String category;
        private String level;
        private String sessionDuration;
        private String availability;
        private Boolean isActive;
        private Boolean isFlagged;
        private String  flagReason;
        private Integer totalSessions;
        private BigDecimal averageRating;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank private String title;
        @NotBlank private String description;
        @NotNull  private String category;
        private String level;
        private String sessionDuration;
        private String availability;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRequest {
        private String title;
        private String description;
        private String category;
        private String level;
        private String sessionDuration;
        private String availability;
    }
}