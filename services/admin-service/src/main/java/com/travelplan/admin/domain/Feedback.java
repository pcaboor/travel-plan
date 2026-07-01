package com.travelplan.admin.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feedback", uniqueConstraints = @UniqueConstraint(
        name = "uq_feedback_author_travel", columnNames = {"author_user_id", "travel_ref_id"}))
public class Feedback extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @NotNull
    @Column(name = "travel_ref_id", nullable = false)
    private UUID travelRefId;

    @Min(1)
    @Max(5)
    @Column(name = "rating", nullable = false)
    private int rating;

    @Size(max = 2000)
    @Column(name = "comment", length = 2000)
    private String comment;

    /** Denormalized id of the manager who owns the reviewed travel, for per-manager stats. */
    @Column(name = "manager_id")
    private UUID managerId;

    @PrePersist
    void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
