package com.olma.domain.repository;

import com.olma.domain.entity.UserCertificate;
import com.olma.domain.entity.UserCertificateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCertificateRepository extends JpaRepository<UserCertificate, UserCertificateId> {
    List<UserCertificate> findAllByUser_Id(Long userId);
    void deleteAllByUser_Id(Long userId);
}
