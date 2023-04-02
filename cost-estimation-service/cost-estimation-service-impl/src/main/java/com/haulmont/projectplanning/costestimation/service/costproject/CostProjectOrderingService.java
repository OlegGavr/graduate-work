package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.internal.InternalCostProjectOrderingService;
import io.vavr.Function3;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.haulmont.projectplanning.costestimation.tool.CostProjectGlobalOrderingTools.findGoPreviousByItemId;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findCostItemById;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findParentCostItemById;

@Component
public class CostProjectOrderingService {

    private Calculation calculation;

    private CostProjectMongoRepository costProjectMongoRepository;

    private InternalCostProjectOrderingService internalCostProjectOrderingService;

    public CostProjectOrderingService(Calculation calculation,
                                      CostProjectMongoRepository costProjectMongoRepository,
                                      InternalCostProjectOrderingService internalCostProjectOrderingService) {

        this.calculation = calculation;
        this.costProjectMongoRepository = costProjectMongoRepository;
        this.internalCostProjectOrderingService = internalCostProjectOrderingService;
    }

    @Transactional
    public CostProject moveCostItemAfterAnotherItem(String projectId, String afterItemId,
                                                    String costItemId) {

        return this.moveCostItemAfterAnotherItem(projectId, afterItemId, costItemId, true);
    }

    @Transactional
    public CostProject moveCostItemAfterAnotherItem(String projectId, String afterItemId,
                                                    String costItemId, Boolean recalculate) {

        var actualCostProject = moveCostItemOperation(projectId, afterItemId, costItemId,
                internalCostProjectOrderingService::moveCostItemAfterAnotherItem);


        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject moveCostItemBeforeAnotherItem(String projectId, String beforeItemId,
                                                     String costItemId) {

        return this.moveCostItemBeforeAnotherItem(projectId, beforeItemId, costItemId, true);
    }

    @Transactional
    public CostProject moveCostItemBeforeAnotherItem(String projectId, String beforeItemId,
                                                     String costItemId, Boolean recalculate) {

        var actualCostProject = moveCostItemOperation(projectId, beforeItemId, costItemId,
                internalCostProjectOrderingService::moveCostItemBeforeAnotherItem);

        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;

    }

    private CostProject moveCostItemOperation(String projectId, String anchorItemId, String costItemId,
                                              Function3<String, String, String, CostProject> operation) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        var anchorCostItem = findCostItemById(costProject, anchorItemId).orElseThrow();

        var costItem = findCostItemById(costProject, costItemId).orElseThrow();

        var actualCostProject = operation.apply(projectId, anchorItemId, costItemId);

        // change parent
        if (! Objects.equals(anchorCostItem.parentItemId(), costItem.parentItemId())) {
            actualCostProject = costProjectMongoRepository.updateCostItemParentId(projectId,
                    anchorCostItem.parentItemId(), costItemId);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject moveCostItemAsSubItem(String projectId, String parentItemId,
                                             String costItemId) {

        return this.moveCostItemAsSubItem(projectId, parentItemId, costItemId, true);
    }

    @Transactional
    public CostProject moveCostItemAsSubItem(String projectId, String parentItemId,
                                             String costItemId, Boolean recalculate) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();
        var anchorCostItem = findCostItemById(costProject, parentItemId).orElseThrow();

        var actualCostProject = internalCostProjectOrderingService
                .moveCostItemAsSubItem(projectId, parentItemId, costItemId);

        // change parent
        actualCostProject = costProjectMongoRepository
                .updateCostItemParentId(projectId,
                        anchorCostItem.id(), costItemId);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject moveCostItemOnLevelDown(String projectId, String costItemId) {

        return this.moveCostItemOnLevelDown(projectId, costItemId, true);
    }

    @Transactional
    public CostProject moveCostItemOnLevelDown(String projectId, String costItemId, Boolean recalculate) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();
        var goPrevious = findGoPreviousByItemId(costProject, costItemId);

        var actualCostProject = internalCostProjectOrderingService
                .moveCostItemOnLevelDown(projectId, costItemId);

        // change parent
        actualCostProject = costProjectMongoRepository
                .updateCostItemParentId(projectId,
                        goPrevious.projectItemId(), costItemId);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject moveCostItemOnLevelUp(String projectId, String costItemId) {

        return this.moveCostItemOnLevelUp(projectId, costItemId, true);
    }

    @Transactional
    public CostProject moveCostItemOnLevelUp(String projectId, String costItemId, Boolean recalculate) {
        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();
        var parentCostItem = findParentCostItemById(costProject, costItemId).orElseThrow();

        var actualCostProject = internalCostProjectOrderingService
                .moveCostItemOnLevelUp(projectId, costItemId);

        // change parent
        actualCostProject = costProjectMongoRepository
                .updateCostItemParentId(projectId,
                        parentCostItem.parentItemId(), costItemId);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }
}