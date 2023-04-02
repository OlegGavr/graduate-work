package com.haulmont.projectplanning.costestimation.tool;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectGlobalOrder;
import com.haulmont.projectplanning.exception.AppropriateGlobalOrderItemNotFoundException;

import java.util.List;
import java.util.Optional;

public class CostProjectGlobalOrderingTools {

    public static Optional<CostProjectGlobalOrder> findGoByProjectItemId(CostProject costProject,
                                                                         String costProjectItemId) {
        return costProject.globalOrder().stream()
                .filter(go -> costProjectItemId.equals(go.projectItemId()))
                .findAny();

    }

    public static List<CostProjectGlobalOrder> findGoChildrenByProjectItemId(CostProject costProject,
                                                                             String costItemId) {
        return costProject.globalOrder().stream()
                .filter(go -> go.parentItemIds().contains(costItemId))
                .toList();

    }

    public static CostProjectGlobalOrder findGoPreviousByItemId(CostProject costProject, String costItemId) {
        var costItemIdx = indexGoByProjectItemIdOf(costProject, costItemId);
        return costProject.globalOrder().get(costItemIdx - 1);
    }

    public static int indexGoByProjectItemIdOf(CostProject costProject, String projectItemId) {
        var index = costProject.globalOrder().stream()
                .map(CostProjectGlobalOrder::projectItemId)
                .toList().indexOf(projectItemId);

        if (index == - 1) {
            throw new AppropriateGlobalOrderItemNotFoundException(
                    String.format("Global order item for %s not found", projectItemId));
        }

        return index;
    }

    public static int lastIndexGoByItemIdIncludeHierarchy(CostProject costProject, String projectItemId) {

        var index = io.vavr.collection.Stream
                .ofAll(costProject.globalOrder()).zipWithIndex()
                .foldLeft(- 1, (acc, go) -> Math.max(acc, projectItemId.equals(go._1.projectItemId())
                        || go._1.parentItemIds().contains(projectItemId) ? go._2 : - 1));

        if (index == - 1) {
            throw new AppropriateGlobalOrderItemNotFoundException(
                    String.format("Global order item for %s not found", projectItemId));
        }

        return index;
    }

    public static void replaceGoWithParents(CostProjectGlobalOrder root,
                                            List<String> newParentsChain,
                                            List<CostProjectGlobalOrder> globalOrders) {

        globalOrders.forEach(ci -> {
            ci.parentItemIds().removeAll(root.parentItemIds());
            ci.parentItemIds().addAll(newParentsChain);
        });
    }
}
