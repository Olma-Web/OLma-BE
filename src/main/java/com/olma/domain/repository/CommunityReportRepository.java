package com.olma.domain.repository;

import com.olma.domain.entity.CommunityReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityReportRepository extends JpaRepository<CommunityReport, Long> {

    boolean existsByReporter_IdAndPost_Id(Long reporterId, Long postId);

    boolean existsByReporter_IdAndComment_Id(Long reporterId, Long commentId);
}
