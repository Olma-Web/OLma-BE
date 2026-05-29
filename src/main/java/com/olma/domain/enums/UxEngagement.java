package com.olma.domain.enums;

import java.math.BigDecimal;

public enum UxEngagement {
    GUI_ONLY(new BigDecimal("1.0")),
    WIREFRAME_PLUS(new BigDecimal("1.3")),
    FULL_PLANNING(new BigDecimal("1.8"));

    private final BigDecimal multiplier;

    UxEngagement(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }
}
