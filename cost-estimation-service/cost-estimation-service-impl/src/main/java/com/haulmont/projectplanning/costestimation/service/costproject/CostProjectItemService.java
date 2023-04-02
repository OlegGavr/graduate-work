package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.service.costproject.internal.InternalCostProjectItemService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CostProjectItemService {

    private InternalCostProjectItemService internalCostProjectItemService;

    private Calculation calculation;

    public CostProjectItemService(InternalCostProjectItemService internalCostProjectItemService,
                                  Calculation calculation) {
        this.internalCostProjectItemService = internalCostProjectItemService;
        this.calculation = calculation;
    }

    @Transactional
    public CostProject createCostItem(String projectId, CostProjectItem template) {
        return this.createCostItem(projectId, template, true);
    }

    /**
     * Adds costItem as last of the root
     *
     * @return modified project
     */
    @Transactional
    public CostProject createCostItem(String projectId, CostProjectItem template, Boolean recalculate) {

        var actualCostProject = internalCostProjectItemService
                .createCostItem(projectId, template);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject createCostItemAfter(String projectId, String afterItemId, CostProjectItem template) {
        return this.createCostItemAfter(projectId, afterItemId, template, true);
    }

    @Transactional
    public CostProject createCostItemAfter(String projectId, String afterItemId, CostProjectItem template, Boolean recalculate) {

        var actualCostProject = internalCostProjectItemService
                .createCostItemAfter(projectId, afterItemId, template);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject createCostItemBefore(String projectId, String beforeItemId, CostProjectItem template) {
        return this.createCostItemBefore(projectId, beforeItemId, template, true);
    }

    public CostProject createCostItemBefore(String projectId, String beforeItemId,
                                            CostProjectItem template, Boolean recalculate) {

        var actualCostProject = internalCostProjectItemService
                .createCostItemBefore(projectId, beforeItemId, template);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject createCostSubItem(String projectId, String parentItemId, CostProjectItem template) {
        return this.createCostSubItem(projectId, parentItemId, template, true);
    }

    @Transactional
    public CostProject createCostSubItem(String projectId, String parentItemId,
                                         CostProjectItem template, Boolean recalculate) {

        var actualCostProject = internalCostProjectItemService
                .createCostSubItem(projectId, parentItemId, template);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    /**
     * Delete only user meaningful items
     *
     * @param projectId
     * @return
     */
    public CostProject deleteAllCostItems(String projectId) {
        return this.deleteAllCostItems(projectId, true);
    }

    /**
     * Delete only user meaningful items
     *
     * @param projectId
     * @param recalculate
     * @return
     */
    public CostProject deleteAllCostItems(String projectId, Boolean recalculate) {
        var actualCostProject = internalCostProjectItemService
                .deleteAllCostItems(projectId);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    public CostProject deleteAllCostItemsById(String projectId, List<String> itemsToDelete) {
        return this.deleteAllCostItemsById(projectId, itemsToDelete, true);
    }

    public CostProject deleteAllCostItemsById(String projectId, List<String> itemsToDelete, Boolean recalculate) {
        var actualCostProject = internalCostProjectItemService
                .deleteAllCostItemsById(projectId, itemsToDelete);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }
}
