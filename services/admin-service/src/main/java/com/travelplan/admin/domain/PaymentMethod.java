package com.travelplan.admin.domain;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment_methods")
public class PaymentMethod extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 32)
    private PaymentProvider provider;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private PaymentMethodType type;

    @Size(max = 255)
    @Column(name = "provider_token", length = 255)
    private String providerToken;

    @Size(max = 4)
    @Column(name = "last_four", length = 4)
    private String lastFour;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentMethodStatus status = PaymentMethodStatus.ACTIVE;

    @jakarta.persistence.PrePersist
    void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
