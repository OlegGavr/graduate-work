package com.haulmont.projectplanning.costestimation.repository.mongo.costproject.costitem.measure;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public interface CostProjectItemMeasureCustomMongoRepository {

    CostProject createCostItemMeasure(String projectId, String itemId,
                                      CostProjectItemMeasure measure);

    CostProject createAllCostItemMeasures(String projectId, String itemId,
                                          List<CostProjectItemMeasure> measures);

    CostProject updateMeasure(String projectId, String itemId, String measureId, CostProjectItemMeasure measure);

    CostProject updateMeasures(String projectId, String itemId, List<CostProjectItemMeasure> measures);
}
