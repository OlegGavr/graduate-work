package com.haulmont.projectplanning.costestimation.repository.mongo.costproject.globalorder;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectGlobalOrder;

import java.util.List;

public interface CostProjectGlobalOrderCustomMongoRepository {

    CostProject addGlobalOrderToTheEnd(String projectId, CostProjectGlobalOrder globalOrderItem);

    CostProject addGlobalOrderToTheConcretePosition(String projectId,
                                                    CostProjectGlobalOrder globalOrderItem,
                                                    Integer position);

    CostProject addAllGlobalOrdersToTheConcretePosition(String projectId,
                                                        List<CostProjectGlobalOrder> globalOrderItems,
                                                        Integer position);

    CostProject updateGlobalOrder(String projectId, List<CostProjectGlobalOrder> costItemsGlobalOrderIds);

    CostProject deleteGlobalOrderByProjectItemId(String projectId, String projectItemId);

    CostProject deleteAllGlobalOrdersByConcreteParentIn(String projectId, String parentId);

    CostProject deleteAllGlobalOrdersByProjectItemId(String projectId, List<String> projectItemIds);

    CostProject deleteAllGlobalOrders(String projectId);
}
