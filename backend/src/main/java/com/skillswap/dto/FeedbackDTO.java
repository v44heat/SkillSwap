package com.skillswap.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

public class FeedbackDTO {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LeaveRequest {
        @NotNull  private Long    sessionId;
        @NotNull  private Integer overallRating;
        private String reviewText;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FeedbackResponse {
        private Long   id;
        private Long   sessionId;
        private Long   skillListingId;
        private String skillTitle;
        private Long   reviewerId;
        private String reviewerName;
        private Long   revieweeId;
        private String revieweeName;
        private Integer overallRating;
        private String  reviewText;
        private String  teacherReply;
        private Boolean isReported;
        private OffsetDateTime createdAt;
    }
}