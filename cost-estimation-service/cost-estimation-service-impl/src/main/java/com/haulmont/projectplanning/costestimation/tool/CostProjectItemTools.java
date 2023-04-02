package com.haulmont.projectplanning.costestimation.tool;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CostProjectItemTools {

    private static Logger logger = LoggerFactory.getLogger(CostProjectItemTools.class);

    public static Optional<CostProjectItem> findCostItemById(CostProject costProject,
                                                             String costItemId) {

        logger.debug("Find CostItem {} in project {}", costItemId, costProject.id());

        return costProject.projectItems().stream()
                .filter(ci -> costItemId.equals(ci.id()))
                .findAny();

    }

    public static Optional<CostProjectItem> findCostItemByName(CostProject costProject,
                                                               String name) {

        logger.debug("Find CostItem by name {} in project {}", name, costProject.id());

        return costProject.projectItems().stream()
                .filter(ci -> name.equals(ci.name()))
                .findAny();

    }

    public static Optional<CostProjectItem> findParentCostItemById(CostProject costProject,
                                                                   String costItemId) {

        logger.debug("Find parent CostItem by item id {} in project {}", costItemId, costProject.id());

        return findCostItemById(costProject, costItemId)
                .flatMap(item -> findCostItemById(costProject, item.parentItemId()));
    }
}
