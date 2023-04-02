package com.haulmont.projectplanning.costestimation.mongo.dto;

import org.springframework.data.annotation.PersistenceConstructor;

import java.util.List;

public record CostProjectItem(
        String id,
        String name,
        List<CostProjectItemMeasure> measures,
        CostProjectItemDetail original,
        CostProjectItemDetail multipliedByKWithRound,
        CostProjectItemDetail multipliedByKWithRound5,
        String comment,

        // for the structure
        String parentItemId) {

    public CostProjectItem(String id, String name) {
        this(
                id,
                name,
                List.of(),
                null,
                null,
                null,
                null,
                null
        );
    }

    public CostProjectItem(String id, String name,
                           List<CostProjectItemMeasure> measures,
                           CostProjectItemDetail original,
                           CostProjectItemDetail multipliedByKWithRound,
                           CostProjectItemDetail multipliedByKWithRound5,
                           String parentItemId) {
        this(id, name, measures, original, multipliedByKWithRound, multipliedByKWithRound5, null, parentItemId);
    }

    @PersistenceConstructor
    public CostProjectItem {
    }
}
