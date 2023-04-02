package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;
import com.haulmont.projectplanning.costestimation.service.costproject.internal.InternalCostProjectMeasureService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CostProjectMeasureService {

    private InternalCostProjectMeasureService internalCostProjectMeasureService;

    private Calculation calculation;

    // avoid recursion
    public CostProjectMeasureService(InternalCostProjectMeasureService internalCostProjectMeasureService,
                                     @Lazy Calculation calculation) {
        this.internalCostProjectMeasureService = internalCostProjectMeasureService;
        this.calculation = calculation;
    }

    @Transactional
    public CostProject createMeasure(String projectId, String itemId,
                                     CostProjectItemMeasure template) {

        return this.createMeasure(projectId, itemId, template, true);
    }

    @Transactional
    public CostProject createMeasure(String projectId, String itemId,
                                     CostProjectItemMeasure template,
                                     Boolean recalculate) {

        throw new UnsupportedOperationException("Not Implemented");
    }

    @Transactional
    public CostProject createMeasures(String projectId, String itemId,
                                      List<CostProjectItemMeasure> templateMeasures) {

        return this.createMeasures(projectId, itemId, templateMeasures, true);
    }

    @Transactional
    public CostProject createMeasures(String projectId, String itemId,
                                      List<CostProjectItemMeasure> templateMeasures,
                                      Boolean recalculate) {

        throw new UnsupportedOperationException("Not Implemented");
    }

    @Transactional
    public CostProject createAutoMeasure(String projectId, String itemId) {
        return this.createAutoMeasure(projectId, itemId, true);
    }

    @Transactional
    public CostProject createAutoMeasure(String projectId, String itemId,
                                         Boolean recalculate) {
        return this.createNumberOfAutoMeasures(projectId, itemId, 1, recalculate);
    }

    @Transactional
    public CostProject createNumberOfAutoMeasures(String projectId, String itemId, Integer count) {
        return this.createNumberOfAutoMeasures(projectId, itemId, count, true);
    }

    @Transactional
    public CostProject createNumberOfAutoMeasures(String projectId, String itemId,
                                                  Integer count, Boolean recalculate) {

        var actualCostProject = internalCostProjectMeasureService
                .createNumberOfAutoMeasures(projectId, itemId, count);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject defineManualMeasure(String projectId, String costItemId,
                                           String measureId, Double value) {

        return this.defineManualMeasure(projectId, costItemId, measureId, value, true);
    }

    @Transactional
    public CostProject defineManualMeasure(String projectId, String costItemId,
                                           String measureId, Double value, Boolean recalculate) {


        var actualCostProject = internalCostProjectMeasureService
                .defineManualMeasure(projectId, costItemId, measureId, value);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject defineAutoMeasure(String projectId, String costItemId,
                                         String measureId, Double value) {

        return internalCostProjectMeasureService
                .defineAutoMeasure(projectId, costItemId, measureId, value);
    }

    @Transactional
    public CostProject clearMeasure(String projectId, String costItemId,
                                    String measureId, Boolean recalculate) {
        var actualCostProject = internalCostProjectMeasureService
                .clearMeasure(projectId, costItemId, measureId);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }
}
