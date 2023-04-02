package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectAggregateItems;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.internal.InternalCostProjectItemService;
import com.haulmont.projectplanning.costestimation.tool.Records;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class CostProjectService {

    public static final String EXAMPLE_COST_ITEM_NAME = "Cost item";
    public static final String EXAMPLE_SUB_COST_ITEM_NAME = "Sub cost item";

    private InternalCostProjectItemService internalCostProjectItemService;

    private CostProjectMongoRepository costProjectMongoRepository;

    private Calculation calculation;

    public CostProjectService(InternalCostProjectItemService internalCostProjectItemService,
                              CostProjectMongoRepository costProjectMongoRepository,
                              Calculation calculation) {
        this.internalCostProjectItemService = internalCostProjectItemService;
        this.costProjectMongoRepository = costProjectMongoRepository;
        this.calculation = calculation;
    }

    @Transactional
    public CostProject create(Boolean withExampleItems) {
        var costProject = new CostProject(new ObjectId().toString(), "Untitled");

        return create(costProject, withExampleItems);
    }

    @Transactional
    public CostProject create(String name, Boolean withExampleItems) {
        var costProject = new CostProject(new ObjectId().toString(), name);

        return create(costProject, withExampleItems);
    }

    @SuppressWarnings("UnusedAssignment")
    @Transactional
    public CostProject create(CostProject template, Boolean withExampleItems) {
        var rootProjectItem = createRootItemTemplate();
        var aggregateItemsResult = createAggregationItemsTemplate();
//        var projectRisk = new CostProjectRisk();

        var costProjectId = template.id() != null ? template.id() : new ObjectId().toString();
        var costProjectToSave = Records.clone(template,
                Map.of("id", costProjectId,
                        "name", template.name().isBlank() ? "Untitled" : template.name(),
                        "rootItemId", rootProjectItem.id(),
                        "aggregateItems", aggregateItemsResult.costProjectAggregateItems,
                        "globalOrder", List.of()));

        costProjectMongoRepository.save(costProjectToSave);

        var updatedCostProject = internalCostProjectItemService
                .createRootItem(costProjectId, rootProjectItem);

        // create aggregation items
        updatedCostProject = internalCostProjectItemService
                .createAggregationItem(costProjectId, rootProjectItem.id(),
                        aggregateItemsResult.aggregatedHoursItem);
        updatedCostProject = internalCostProjectItemService
                .createAggregationItem(costProjectId, rootProjectItem.id(),
                        aggregateItemsResult.aggregatedMoneyWithoutNdsItem);
        updatedCostProject = internalCostProjectItemService
                .createAggregationItem(costProjectId, rootProjectItem.id(),
                        aggregateItemsResult.aggregatedMoneyWithNds20CostItem);

        // add example lines
        if (withExampleItems) {
            var exampleCostItem = new CostProjectItem(
                    new ObjectId().toString(), EXAMPLE_COST_ITEM_NAME);

            updatedCostProject = internalCostProjectItemService
                    .createCostItem(updatedCostProject.id(), exampleCostItem);

            updatedCostProject = internalCostProjectItemService
                    .createCostSubItem(updatedCostProject.id(), exampleCostItem.id(),
                            new CostProjectItem(new ObjectId().toString(), EXAMPLE_SUB_COST_ITEM_NAME));
        }
        return updatedCostProject;
    }

    private CostProjectItem createRootItemTemplate() {
        return new CostProjectItem(new ObjectId().toString(), "Root item");
    }

    private CreateAggregationItemsResult createAggregationItemsTemplate() {
        var aggregatedHoursItem = new CostProjectItem(new ObjectId().toString(), "Aggregate hours");
        var aggregatedMoneyWithoutNdsItem = new CostProjectItem(new ObjectId().toString(), "Aggregate money without NDS 20%");
        var aggregatedMoneyWithNds20CostItem = new CostProjectItem(new ObjectId().toString(), "Aggregate money with NDS 20%");

        return new CreateAggregationItemsResult(aggregatedHoursItem,
                aggregatedMoneyWithoutNdsItem, aggregatedMoneyWithNds20CostItem,
                new CostProjectAggregateItems(aggregatedHoursItem.id(),
                        aggregatedMoneyWithoutNdsItem.id(), aggregatedMoneyWithNds20CostItem.id()));
    }

    @Transactional
    public CostProject defineMoneyPerHour(String id, Integer moneyPerHour) {
        return this.defineMoneyPerHour(id, moneyPerHour, true);
    }

    @Transactional
    public CostProject defineMoneyPerHour(String id, Integer moneyPerHour, Boolean recalculate) {
        var actualCostProject = costProjectMongoRepository
                .updateMoneyPerHour(id, moneyPerHour);

        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    public CostProject defineSharePointLink(String projectId, String sharePointLink) {
        return costProjectMongoRepository.updateSharePointLink(projectId, sharePointLink);
    }

    private record CreateAggregationItemsResult(
            CostProjectItem aggregatedHoursItem,
            CostProjectItem aggregatedMoneyWithoutNdsItem,
            CostProjectItem aggregatedMoneyWithNds20CostItem,
            CostProjectAggregateItems costProjectAggregateItems) {}
}
