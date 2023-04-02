package com.haulmont.projectplanning.costestimation.service.costproject.internal;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectGlobalOrder;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.exception.AppropriateGlobalOrderItemNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.haulmont.projectplanning.costestimation.tool.CostProjectGlobalOrderingTools.findGoByProjectItemId;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectGlobalOrderingTools.findGoChildrenByProjectItemId;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectGlobalOrderingTools.findGoPreviousByItemId;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectGlobalOrderingTools.indexGoByProjectItemIdOf;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectGlobalOrderingTools.lastIndexGoByItemIdIncludeHierarchy;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectGlobalOrderingTools.replaceGoWithParents;
import static org.apache.commons.collections4.ListUtils.union;

@Component
public class InternalCostProjectOrderingService {

    private CostProjectMongoRepository costProjectMongoRepository;

    public InternalCostProjectOrderingService(CostProjectMongoRepository costProjectMongoRepository) {
        this.costProjectMongoRepository = costProjectMongoRepository;
    }

    public CostProject addCostItemAsLastOnTopLevel(String projectId, String costItemId) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        var globalOrderToSave = new CostProjectGlobalOrder(costItemId, List.of(costProject.rootItemId()));

        return costProjectMongoRepository.addGlobalOrderToTheEnd(projectId, globalOrderToSave);
    }


    public CostProject addCostItemAsLastOnConcreteParent(String projectId, String parentItemId,
                                                         String costItemId) {

        //noinspection OptionalGetWithoutIsPresent
        var costProject = costProjectMongoRepository.findById(projectId).get();

        var lastIndexOfParentId = io.vavr.collection.Stream
                .ofAll(costProject.globalOrder()).zipWithIndex()
                .foldLeft(- 1, (acc, go) -> Math.max(acc, parentItemId.equals(go._1.projectItemId())
                        || go._1.parentItemIds().contains(parentItemId) ? go._2 : - 1));

        if (lastIndexOfParentId == - 1) {
            throw new AppropriateGlobalOrderItemNotFoundException(
                    String.format("Global order item for %s not found", parentItemId));
        }

        //noinspection OptionalGetWithoutIsPresent
        var parentItemGlobalOrder = costProject.globalOrder().stream()
                .filter(go -> go.projectItemId().equals(parentItemId))
                .findAny().get();

        var costItemParentIds = Stream.of(Stream.of(parentItemGlobalOrder.projectItemId()),
                        parentItemGlobalOrder.parentItemIds().stream())
                .flatMap(Function.identity()).toList();

        var costItemGlobalOrder = new CostProjectGlobalOrder(costItemId, costItemParentIds);

        return costProjectMongoRepository.addGlobalOrderToTheConcretePosition(
                projectId, costItemGlobalOrder, lastIndexOfParentId + 1);
    }

    public CostProject addCostItemAfterConcreteItem(String projectId, String afterItemId,
                                                    String costItemId) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        var goByAfterItemId = findGoByProjectItemId(costProject, afterItemId).orElseThrow();
        var goToSave = new CostProjectGlobalOrder(costItemId, goByAfterItemId.parentItemIds());

        var positionToAdd = lastIndexGoByItemIdIncludeHierarchy(costProject, afterItemId);

        return costProjectMongoRepository.addGlobalOrderToTheConcretePosition(
                projectId, goToSave, positionToAdd + 1);
    }

    public CostProject addCostItemBeforeConcreteItem(String projectId, String afterItemId,
                                                     String costItemId) {

        //noinspection OptionalGetWithoutIsPresent
        var costProject = costProjectMongoRepository.findById(projectId).get();

        var indexOfAfterItemId = costProject.globalOrder().stream()
                .map(CostProjectGlobalOrder::projectItemId)
                .toList().indexOf(afterItemId);

        var afterItemGlobalOrder = costProject.globalOrder().get(indexOfAfterItemId);

        var globalOrderToSave = new CostProjectGlobalOrder(
                costItemId, afterItemGlobalOrder.parentItemIds());

        return costProjectMongoRepository.addGlobalOrderToTheConcretePosition(
                projectId, globalOrderToSave, indexOfAfterItemId);
    }

    @Transactional
    public CostProject moveCostItemAfterAnotherItem(String projectId, String afterItemId,
                                                    String costItemId) {
        return moveCostItemToConcretePosition(projectId, afterItemId,
                costItemId, acp -> lastIndexGoByItemIdIncludeHierarchy(acp, afterItemId) + 1);
    }
    @Transactional
    public CostProject moveCostItemBeforeAnotherItem(String projectId, String beforeItemId,
                                                     String costItemId) {
        return moveCostItemToConcretePosition(projectId, beforeItemId,
                costItemId, acp -> indexGoByProjectItemIdOf(acp, beforeItemId));
    }

    /**
     * @param indexFn calculate place to insert. Arg is actual CostProject
     */
    private CostProject moveCostItemToConcretePosition(String projectId, String anchorItemId,
                                                       String costItemId, Function<CostProject, Integer> indexFn) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();
        var anchorItemGo = findGoByProjectItemId(costProject, anchorItemId).orElseThrow();

        var costItemGo = findGoByProjectItemId(costProject, costItemId).orElseThrow();
        var childrenCostItems = findGoChildrenByProjectItemId(costProject, costItemId);

        // support hierarchy changing
        if (! anchorItemGo.parentItemIds().equals(costItemGo.parentItemIds())) {

            replaceGoWithParents(costItemGo, anchorItemGo.parentItemIds(),
                    union(childrenCostItems, List.of(costItemGo))); // IMPORTANT!!!: replace makes side effect therefore List.of(costItemGo) must be the last

        }

        // remove moving elements
        var actualCostProject = costProjectMongoRepository
                .deleteGlobalOrderByProjectItemId(projectId, costItemId);
        actualCostProject = costProjectMongoRepository
                .deleteAllGlobalOrdersByConcreteParentIn(projectId, costItemId);

        var insertIndex = indexFn.apply(actualCostProject);

        actualCostProject = costProjectMongoRepository.addAllGlobalOrdersToTheConcretePosition(
                projectId, union(List.of(costItemGo), childrenCostItems), insertIndex);

        return actualCostProject;
    }

    @Transactional
    public CostProject moveCostItemAsSubItem(String projectId, String parentItemId, String costItemId) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();
        var parentItem = findGoByProjectItemId(costProject, parentItemId).orElseThrow();

        var costItemGo = findGoByProjectItemId(costProject, costItemId).orElseThrow();
        var childrenCostItems = findGoChildrenByProjectItemId(costProject, costItemId);

        // replace parent tail for children
        replaceGoWithParents(costItemGo,
                union(List.of(parentItemId), parentItem.parentItemIds()),
                union(childrenCostItems, List.of(costItemGo))); // IMPORTANT!!!: replace makes side effect therefore List.of(costItemGo) must be the last

        // remove moving elements
        var actualCostProject = costProjectMongoRepository
                .deleteGlobalOrderByProjectItemId(projectId, costItemId);
        actualCostProject = costProjectMongoRepository
                .deleteAllGlobalOrdersByConcreteParentIn(projectId, costItemId);

        // calculate index of parent item in global order
        var parentIndex = lastIndexGoByItemIdIncludeHierarchy(actualCostProject, parentItemId);

        actualCostProject = costProjectMongoRepository.addAllGlobalOrdersToTheConcretePosition(
                projectId, union(List.of(costItemGo), childrenCostItems), parentIndex + 1);

        return actualCostProject;

    }

    @Transactional
    public CostProject moveCostItemOnLevelDown(String projectId, String costItemId) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();
        var previousItemGo = findGoPreviousByItemId(costProject, costItemId);

        return moveCostItemAsSubItem(projectId, previousItemGo.projectItemId(), costItemId);
    }

    @Transactional
    public CostProject moveCostItemOnLevelUp(String projectId, String costItemId) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();
        var costItemGo = findGoByProjectItemId(costProject, costItemId).orElseThrow();

        return moveCostItemAfterAnotherItem(projectId, costItemGo.parentItemIds().get(0), costItemId);
    }
}