package com.haulmont.projectplanning.costestimation.service.costproject.internal;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType.AUTO;

@SuppressWarnings("UnnecessaryLocalVariable")
@Component
public class InternalCostProjectMeasureService {

    private CostProjectMongoRepository costProjectMongoRepository;

    // avoid recursion
    public InternalCostProjectMeasureService(CostProjectMongoRepository costProjectMongoRepository) {
        this.costProjectMongoRepository = costProjectMongoRepository;
    }

    public CostProject createMeasure(String projectId, String itemId,
                                     CostProjectItemMeasure template) {

        throw new UnsupportedOperationException("Not Implemented");
    }

    public CostProject createMeasures(String projectId, String itemId,
                                      List<CostProjectItemMeasure> templateMeasures) {

        throw new UnsupportedOperationException("Not Implemented");
    }

    public CostProject createAutoMeasure(String projectId, String itemId) {
        return createNumberOfAutoMeasures(projectId, itemId, 1);
    }

    public CostProject createNumberOfAutoMeasures(String projectId, String itemId, Integer count) {

        var measures = new ArrayList<CostProjectItemMeasure>(count);
        for (int i = 0; i < count; i++) {
            measures.add(new CostProjectItemMeasure(new ObjectId().toString(), AUTO));
        }

        var actualCostProject = costProjectMongoRepository
                .createAllCostItemMeasures(projectId, itemId, measures);


        return actualCostProject;
    }

    public CostProject defineManualMeasure(String projectId, String costItemId,
                                           String measureId, Double value) {

        var manualMeasure = new CostProjectItemMeasure(measureId,
                value, CostProjectItemDetailMeasureType.MANUAL);

        var actualCostProject = costProjectMongoRepository.updateMeasure(
                projectId, costItemId, measureId, manualMeasure);

        return actualCostProject;
    }

    public CostProject defineAutoMeasure(String projectId, String costItemId,
                                         String measureId, Double value) {

        var manualMeasure = new CostProjectItemMeasure(measureId,
                value, CostProjectItemDetailMeasureType.AUTO);

        return costProjectMongoRepository.updateMeasure(
                projectId, costItemId, measureId, manualMeasure);
    }

    public CostProject clearMeasure(String projectId, String costItemId,
                                    String measureId) {

        return this.defineAutoMeasure(projectId, costItemId, measureId, null);
    }
}
