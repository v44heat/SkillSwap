package com.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class RequestDTO {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SendRequest {
        @NotNull  private Long   skillListingId;
        @NotBlank private String proposedDateTime;   // ISO-8601 from frontend
        private String duration;
        @NotBlank private String focusMessage;
        private String meetingFormat;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RespondRequest {
        @NotBlank private String action;             // "ACCEPTED" or "DECLINED"
        private String declineReason;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RequestResponse {
        private Long   id;
        private Long   skillListingId;
        private String skillTitle;
        private String category;
        private Long   requesterId;
        private String requesterName;
        private String requesterDept;
        private Long   teacherId;
        private String teacherName;
        private String teacherDept;
        private BigDecimal teacherRating;
        private String status;
        private OffsetDateTime proposedDatetime;
        private String duration;
        private String focusMessage;
        private String meetingFormat;
        private String declineReason;
        private OffsetDateTime createdAt;
    }
}