package com.haulmont.projectplanning.costestimation.mongo.dto;

import java.util.List;

public record CostProjectGlobalOrder(
        String projectItemId,
        List<String> parentItemIds
) {
}
