package com.olma.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_certificates")
@IdClass(UserCertificateId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCertificate {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_type_id")
    private CertificateType certificateType;
}
