package com.olma.domain.enums;

public enum EstimateAddon {
    DESIGN_SYSTEM(20),
    PROTOTYPING(10),
    SOURCE_TRANSFER(20);

    private final int percent;

    EstimateAddon(int percent) {
        this.percent = percent;
    }

    public int getPercent() {
        return percent;
    }
}
