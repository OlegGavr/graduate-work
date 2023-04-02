package com.haulmont.projectplanning.costestimation.repository.mongo.costproject.costitem;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetail;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public interface CostProjectItemCustomMongoRepository {

    CostProject createCostItem(String id, CostProjectItem projectCostItem);

    CostProject createCostItemDetailOriginal(String id, String itemId, CostProjectItemDetail costProjectItemDetail);

    CostProject createCostItemDetailMultipliedByKWithRound(String id, String itemId, CostProjectItemDetail costProjectItemDetail);

    CostProject createCostItemDetailMultipliedByKWithRound5(String id, String itemId, CostProjectItemDetail costProjectItemDetail);

    CostProject updateCostItem(String id, CostProjectItem projectCostItem);

    CostProject updateCostItemParentId(String projectId, String parentItemId, String costItemId);

    CostProject updateCostItemName(String id, String itemId, String costItemName);

    CostProject updateCostItemComment(String id, String itemId, String comment);

    CostProject deleteAllCostItemsById(String id, List<String> itemIds);
}
