package com.skillswap.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "session_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_listing_id", nullable = false)
    private SkillListing skillListing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "proposed_datetime", nullable = false)
    private OffsetDateTime proposedDatetime;

    @Column(length = 50)
    private String duration;

    @Column(name = "focus_message", nullable = false, columnDefinition = "TEXT")
    private String focusMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_format", nullable = false)
    private MeetingFormat meetingFormat = MeetingFormat.FLEXIBLE;

    @Column(name = "decline_reason", columnDefinition = "TEXT")
    private String declineReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public enum RequestStatus { PENDING, ACCEPTED, DECLINED, WITHDRAWN }
    public enum MeetingFormat  { IN_PERSON, ONLINE_GOOGLE_MEET, ONLINE_ZOOM, FLEXIBLE }
}