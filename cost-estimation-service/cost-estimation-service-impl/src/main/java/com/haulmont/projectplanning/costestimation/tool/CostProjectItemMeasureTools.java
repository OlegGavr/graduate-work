package com.haulmont.projectplanning.costestimation.tool;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;

import java.util.List;
import java.util.Optional;

public class CostProjectItemMeasureTools {

    public static Optional<CostProjectItemMeasure> findCostMeasureByCostItemIdAndMeasureId(
            CostProject costProject, String costItemId, String measureId) {

        return costProject.projectItems().stream()
                .filter(ci -> costItemId.equals(ci.id())).findAny().stream()
                .flatMap(ci -> ci.measures().stream())
                .filter(m -> measureId.equals(m.id())).findAny();
    }

    public static List<CostProjectItemMeasure> findCostMeasureByCostItemId(
            CostProject costProject, String costItemId) {

        return costProject.projectItems().stream()
                .filter(ci -> costItemId.equals(ci.id())).findAny().stream()
                .flatMap(ci -> ci.measures().stream()).toList();
    }
}
