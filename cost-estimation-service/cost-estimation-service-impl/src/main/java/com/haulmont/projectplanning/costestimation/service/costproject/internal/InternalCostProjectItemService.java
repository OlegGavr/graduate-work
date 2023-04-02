package com.haulmont.projectplanning.costestimation.service.costproject.internal;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectGlobalOrder;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetail;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.tool.Records;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findCostItemById;
import static org.apache.commons.collections4.CollectionUtils.containsAny;

/**
 * Purpose, only control state of CostItem. It should not
 * execute another internal services
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "UnusedReturnValue"})
@Component
public class InternalCostProjectItemService {


    private CostProjectMongoRepository costProjectMongoRepository;

    private InternalCostProjectMeasureService internalCostProjectMeasureService;

    private InternalCostProjectOrderingService internalCostProjectOrderingService;


    public InternalCostProjectItemService(CostProjectMongoRepository costProjectMongoRepository,
                                          InternalCostProjectMeasureService internalCostProjectMeasureService,
                                          InternalCostProjectOrderingService internalCostProjectOrderingService) {
        this.costProjectMongoRepository = costProjectMongoRepository;
        this.internalCostProjectMeasureService = internalCostProjectMeasureService;
        this.internalCostProjectOrderingService = internalCostProjectOrderingService;
    }

    /**
     * Adds costItem as last of the root
     *
     * @return modified project
     */
    public CostProject createCostItem(String projectId, CostProjectItem template) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        // create and save item
        var itemWithProject = internalCreateSubItem(projectId, costProject.rootItemId(), template);
        var createdCostItem = itemWithProject._1;

        // manage ordering
        var actualCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(projectId, createdCostItem.id());

        return actualCostProject;
    }

    public CostProject createCostItemAfter(String projectId, String afterItemId, CostProjectItem template) {
        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        var afterCostItem = costProject.projectItems().stream()
                .filter(pi -> Objects.equals(pi.id(), afterItemId))
                .findAny().orElseThrow();

        var itemWithProject = internalCreateSubItem(projectId, afterCostItem.parentItemId(), template);
        var createdCostItem = itemWithProject._1;

        var actualCostProject = internalCostProjectOrderingService
                .addCostItemAfterConcreteItem(projectId, afterItemId, createdCostItem.id());

        return actualCostProject;
    }

    public CostProject createCostItemBefore(String projectId, String beforeItemId, CostProjectItem template) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        var beforeCostItem = costProject.projectItems().stream()
                .filter(pi -> Objects.equals(pi.id(), beforeItemId))
                .findAny().orElseThrow();

        var itemWithProject = internalCreateSubItem(projectId, beforeCostItem.parentItemId(), template);
        var createdCostItem = itemWithProject._1;

        var actualCostProject = internalCostProjectOrderingService
                .addCostItemBeforeConcreteItem(projectId, beforeItemId, createdCostItem.id());

        return actualCostProject;
    }

    public CostProject createCostSubItem(String projectId, String parentItemId, CostProjectItem template) {

        var itemWithProject = internalCreateSubItem(projectId, parentItemId, template);
        var createdCostItem = itemWithProject._1;

        var actualCostProject = internalCostProjectOrderingService.addCostItemAsLastOnConcreteParent(
                projectId, parentItemId, createdCostItem.id());

        return actualCostProject;
    }

    public CostProject createRootItem(String projectId, CostProjectItem template) {
        return internalCreateSubItem(projectId, null, template)._2;
    }

    public CostProject createAggregationItem(String projectId, String rootItemId, CostProjectItem template) {
        return internalCreateSubItem(projectId, null, template)._2;
    }


    @SuppressWarnings("UnusedAssignment")
    protected Tuple2<CostProjectItem, CostProject> internalCreateSubItem(
            String projectId, @Nullable String parentItemId, CostProjectItem template) {

        var costItemId = template.id() != null ? template.id() : new ObjectId().toString();
        var overrideFields = new HashMap<String, Object>();
        overrideFields.put("id", costItemId);
        overrideFields.put("parentItemId", parentItemId);

        var costItemToSave = Records.clone(template, overrideFields);

        var updatedCostProject = costProjectMongoRepository
                .createCostItem(projectId, costItemToSave);

        // create measures
        updatedCostProject = internalCostProjectMeasureService
                .createNumberOfAutoMeasures(projectId, costItemId, 30); // 10 * 3 = 30

        var createdCostItem = findCostItemById(updatedCostProject,
                costItemToSave.id()).orElseThrow();

        // map measures on blocks
        var original = internalCreateItemDetail(createdCostItem.measures().subList(0, 10));
        updatedCostProject = costProjectMongoRepository
                .createCostItemDetailOriginal(projectId, costItemId, original);

        var multipliedByKWithRound = internalCreateItemDetail(createdCostItem.measures().subList(10, 20));
        updatedCostProject = costProjectMongoRepository
                .createCostItemDetailMultipliedByKWithRound(
                        projectId, costItemId, multipliedByKWithRound);

        var multipliedByKWithRound5 = internalCreateItemDetail(createdCostItem.measures().subList(20, 30));
        updatedCostProject = costProjectMongoRepository
                .createCostItemDetailMultipliedByKWithRound5(
                        projectId, costItemId, multipliedByKWithRound5);

        return Tuple.of(createdCostItem, updatedCostProject);
    }

    private CostProjectItemDetail internalCreateItemDetail(List<CostProjectItemMeasure> measures) {

        var measureIds = measures.stream()
                .map(CostProjectItemMeasure::id)
                .toList();

        return new CostProjectItemDetail(
                measureIds.get(0), measureIds.get(1), measureIds.get(2),
                measureIds.get(3), measureIds.get(4), measureIds.get(5),
                measureIds.get(6), measureIds.get(7), measureIds.get(8), measureIds.get(9));
    }

    public CostProject deleteAllCostItemsById(String projectId, List<String> itemsToDelete) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        // delete items
        var itemsToDeleteSet = new HashSet<>(itemsToDelete);
        var costItemIdsToDelete = costProject.globalOrder().stream()
                .filter(go -> itemsToDeleteSet.contains(go.projectItemId())
                        || containsAny(go.parentItemIds(), itemsToDelete))
                .map(CostProjectGlobalOrder::projectItemId)
                .toList();

        var updatedCostProject = costProjectMongoRepository
                .deleteAllCostItemsById(projectId, costItemIdsToDelete);

        // delete ordering
        updatedCostProject = costProjectMongoRepository
                .deleteAllGlobalOrdersByProjectItemId(projectId, costItemIdsToDelete);

        return updatedCostProject;
    }
    public CostProject deleteAllCostItems(String projectId) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        var userSpecifiedCostItems = costProject.globalOrder()
                .stream().map(CostProjectGlobalOrder::projectItemId).toList();

        var updatedCostProject = costProjectMongoRepository
                .deleteAllCostItemsById(projectId, userSpecifiedCostItems);

        // delete ordering
        updatedCostProject = costProjectMongoRepository.deleteAllGlobalOrders(projectId);

        return updatedCostProject;
    }
}
