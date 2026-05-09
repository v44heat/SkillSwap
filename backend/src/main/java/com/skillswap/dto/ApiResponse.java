package com.skillswap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ApiResponse {

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Success<T> {
        private T      data;
        private String message;

        public static <T> Success<T> of(T data) {
            return Success.<T>builder().data(data).build();
        }
        public static <T> Success<T> of(T data, String message) {
            return Success.<T>builder().data(data).message(message).build();
        }
    }

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Error {
        private String message;
        private int    status;

        public static Error of(String message, int status) {
            return Error.builder().message(message).status(status).build();
        }
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AdminStats {
        private long totalUsers;
        private long activeUsers;
        private long suspendedUsers;
        private long totalListings;
        private long activeListings;
        private long flaggedListings;
        private long totalSessions;
        private long completedSessions;
        private long pendingRequests;
        private long reportedFeedback;
    }
}