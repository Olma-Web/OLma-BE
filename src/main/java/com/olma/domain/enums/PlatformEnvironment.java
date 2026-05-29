package com.olma.domain.enums;

import java.math.BigDecimal;

public enum PlatformEnvironment {
    MOBILE_APP(new BigDecimal("1.0")),
    PC_WEB(new BigDecimal("1.0")),
    RESPONSIVE_WEB(new BigDecimal("1.5"));

    private final BigDecimal multiplier;

    PlatformEnvironment(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }
}
