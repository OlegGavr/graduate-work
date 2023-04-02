package com.haulmont.projectplanning.costestimation.mongo.dto;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

import java.util.List;

public record CostProject(
        @Id
        String id,
        String name,
        String rootItemId,
        Integer moneyPerHour,
        CostProjectRisk projectRisk,
        CostProjectAggregateItems aggregateItems,
        List<CostProjectItem> projectItems,
        List<CostProjectGlobalOrder> globalOrder,
        String sharePointLink) {

    public CostProject(String id, String name) {
        this(id, name, new ObjectId().toString(), 2500,
                new CostProjectRisk(), null, List.of(), List.of(), null);
    }

    public CostProject(String id, String name, String rootItemId,
                       CostProjectRisk projectRisk,
                       CostProjectAggregateItems aggregateItems,
                       List<CostProjectItem> projectItems) {

        this(id, name, rootItemId, 2500, projectRisk,
                aggregateItems, projectItems, List.of(), null);
    }

    public CostProject(String id, String rootItemId, CostProjectRisk projectRisk,
                       CostProjectAggregateItems aggregateItems,
                       List<CostProjectItem> projectItems,
                       List<CostProjectGlobalOrder> globalOrder) {

        this(id, null, rootItemId, 2500, projectRisk,
                aggregateItems, projectItems, globalOrder, null);
    }

    @PersistenceConstructor
    public CostProject {
    }
}
