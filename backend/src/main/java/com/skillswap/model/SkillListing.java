package com.skillswap.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skill_listings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private SkillCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private SkillLevel level = SkillLevel.INTERMEDIATE;

    @Column(name = "session_duration", length = 50)
    private String sessionDuration;

    @Column(length = 100)
    private String availability;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_flagged", nullable = false)
    private Boolean isFlagged = false;

    @Column(name = "flag_reason", columnDefinition = "TEXT")
    private String flagReason;

    @Column(name = "total_sessions")
    private Integer totalSessions = 0;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "skillListing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SessionRequest> requests = new ArrayList<>();

    public enum SkillCategory {
        PROGRAMMING, DESIGN, MATHEMATICS, LANGUAGES, BUSINESS, SCIENCE, ARTS, OTHER
    }

    public enum SkillLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}