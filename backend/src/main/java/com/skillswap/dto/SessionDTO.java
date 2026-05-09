package com.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

public class SessionDTO {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SessionResponse {
        private Long   id;
        private Long   skillListingId;
        private String skillTitle;
        private String category;
        private Long   teacherId;
        private String teacherName;
        private Long   learnerId;
        private String learnerName;
        private String status;
        private OffsetDateTime scheduledAt;
        private String duration;
        private String meetingFormat;
        private String cancelReason;
        private OffsetDateTime completedAt;
        private OffsetDateTime createdAt;
        private String  myRole;          // "TEACHER" or "LEARNER"
        private Boolean feedbackGiven;
    }
}