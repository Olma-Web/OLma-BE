package com.olma.service;

import com.olma.domain.repository.RateSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutlierMarkingService {

    private static final int MIN_GROUP_SIZE = 20;

    private final RateSubmissionRepository rateSubmissionRepository;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    @Transactional
    public void markOutliersDaily() {
        log.info("outlier marking started");
        try {
            int affected = rateSubmissionRepository.markOutliers(MIN_GROUP_SIZE);
            log.info("outlier marking completed rowsTouched={}", affected);
        } catch (Exception ex) {
            log.error("outlier marking failed", ex);
            throw ex;
        }
    }
}
