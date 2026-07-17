package com.olma.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.olma.domain.enums.UserDraftStatus;
import com.olma.domain.enums.UserDraftType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "user_drafts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_drafts_user_type",
                columnNames = {"user_id", "draft_type"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "draft_type", nullable = false, length = 30)
    private UserDraftType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserDraftStatus status = UserDraftStatus.IN_PROGRESS;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode state = JsonNodeFactory.instance.objectNode();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Builder
    public UserDraft(User user, UserDraftType type, JsonNode state) {
        this.user = user;
        this.type = type;
        this.state = state != null ? state : JsonNodeFactory.instance.objectNode();
    }

    public void saveProgress(JsonNode state) {
        this.status = UserDraftStatus.IN_PROGRESS;
        this.state = state != null ? state : JsonNodeFactory.instance.objectNode();
        this.updatedAt = OffsetDateTime.now();
        this.completedAt = null;
    }

    public void complete(JsonNode state) {
        if (state != null) {
            this.state = state;
        }
        OffsetDateTime now = OffsetDateTime.now();
        this.status = UserDraftStatus.COMPLETED;
        this.updatedAt = now;
        this.completedAt = now;
    }
}
