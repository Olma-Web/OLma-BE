package com.olma.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.olma.domain.enums.ProfileSpecStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "agreement_at", nullable = false, updatable = false)
    private OffsetDateTime agreementAt = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_level_id")
    private ExperienceLevel experienceLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_category_id")
    private JobCategory jobCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_spec_status", nullable = false, length = 20)
    private ProfileSpecStatus profileSpecStatus = ProfileSpecStatus.NOT_STARTED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "profile_spec_state", nullable = false, columnDefinition = "jsonb")
    private JsonNode profileSpecState = JsonNodeFactory.instance.objectNode();

    @Column(name = "profile_spec_started_at")
    private OffsetDateTime profileSpecStartedAt;

    @Column(name = "profile_spec_updated_at")
    private OffsetDateTime profileSpecUpdatedAt;

    @Column(name = "profile_spec_completed_at")
    private OffsetDateTime profileSpecCompletedAt;

    // 발급된 토큰의 세대. 로그아웃 시 증가시켜 이전 세대 토큰을 무효화한다.
    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @Builder
    public User(String email, String password, String nickname,
                ExperienceLevel experienceLevel, JobCategory jobCategory) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.experienceLevel = experienceLevel;
        this.jobCategory = jobCategory;
    }

    public void updateProfile(ExperienceLevel experienceLevel, JobCategory jobCategory) {
        this.experienceLevel = experienceLevel;
        this.jobCategory = jobCategory;
    }

    public void updateProfileSpecProgress(JsonNode state) {
        OffsetDateTime now = OffsetDateTime.now();
        if (profileSpecStatus != ProfileSpecStatus.IN_PROGRESS) {
            profileSpecStartedAt = now;
            profileSpecCompletedAt = null;
        }
        profileSpecStatus = ProfileSpecStatus.IN_PROGRESS;
        profileSpecState = state != null ? state : JsonNodeFactory.instance.objectNode();
        profileSpecUpdatedAt = now;
    }

    public void completeProfileSpec(JsonNode state) {
        OffsetDateTime now = OffsetDateTime.now();
        if (profileSpecStartedAt == null) {
            profileSpecStartedAt = now;
        }
        profileSpecStatus = ProfileSpecStatus.COMPLETED;
        profileSpecState = state != null ? state : JsonNodeFactory.instance.objectNode();
        profileSpecUpdatedAt = now;
        profileSpecCompletedAt = now;
    }

    public void changePassword(String newPasswordHash) {
        this.password = newPasswordHash;
    }

    public void increaseTokenVersion() {
        this.tokenVersion++;
    }
}
