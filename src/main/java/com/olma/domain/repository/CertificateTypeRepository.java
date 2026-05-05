package com.olma.domain.repository;

import com.olma.domain.entity.CertificateType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateTypeRepository extends JpaRepository<CertificateType, Long> {
}
