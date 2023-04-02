package com.haulmont.projectplanning.costestimation.mongo.dto;

import org.springframework.data.annotation.PersistenceConstructor;

public record CostProjectItemDetail(
        String analyseCost,
        String backendCost,
        String frontendCost,
        String devCost,
        String qaCost,
        String devOpsCost,
        String tmCost,
        String pmCost,
        String otherCost,
        String fullCost) {

    @PersistenceConstructor
    public CostProjectItemDetail {
    }
}
//    ProjectCostItemMeasure analyseCost,
//    ProjectCostItemMeasure backendCost,
//    ProjectCostItemMeasure frontendCost,
//    ProjectCostItemMeasure qaCost,
//    ProjectCostItemMeasure devOpsCost,
//    ProjectCostItemMeasure otherCost
