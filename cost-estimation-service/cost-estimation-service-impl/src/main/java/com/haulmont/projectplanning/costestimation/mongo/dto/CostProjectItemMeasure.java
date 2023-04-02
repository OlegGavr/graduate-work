package com.haulmont.projectplanning.costestimation.mongo.dto;

import org.springframework.data.annotation.PersistenceConstructor;

import static com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType.AUTO;
import static com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType.MANUAL;

public record CostProjectItemMeasure(
        String id,
        Double value,
        CostProjectItemDetailMeasureType type
) {
    public CostProjectItemMeasure(String id) {
        this(id, null, AUTO);
    }

    public CostProjectItemMeasure(String id, CostProjectItemDetailMeasureType type) {
        this(id, null, type);
    }

    public CostProjectItemMeasure(String id, Integer value) {
        this(id, Double.valueOf(value), MANUAL);
    }

    @PersistenceConstructor
    public CostProjectItemMeasure {
    }

}
