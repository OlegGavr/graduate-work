package com.haulmont.projectplanning.costestimation.mongo.dto;

import org.springframework.data.annotation.PersistenceConstructor;

public record CostProjectRisk(
    Double defaultRisk,
    Double dev,
    Double qa,
    Double ba,
    Double devOps,
    Double tm,
    Double pm) {
    public static Double DEFAULT_RISK = 0.3;

    public CostProjectRisk() {
        this(DEFAULT_RISK, null, null, null, null, null, null);
    }

    @PersistenceConstructor
    public CostProjectRisk {
    }
}
