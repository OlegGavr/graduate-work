package com.haulmont.projectplanning.costestimation.mongo.dto;

public record CostProjectAggregateItems(
        String aggregatedHoursCostItemId,
        String aggregatedMoneyWithoutNdsCostItemId,
        String aggregatedMoneyWithNds20CostItemId) {
}
