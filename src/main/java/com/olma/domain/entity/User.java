package com.olma.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public void changePassword(String newPasswordHash) {
        this.password = newPasswordHash;
    }

    public void increaseTokenVersion() {
        this.tokenVersion++;
    }
}
