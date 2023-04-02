package com.haulmont.projectplanning.costestimation.repository.mongo.costproject.costitem;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetail;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

public class CostProjectItemCustomMongoRepositoryImpl implements CostProjectItemCustomMongoRepository {

    private MongoTemplate mongoTemplate;

    public CostProjectItemCustomMongoRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public CostProject createCostItem(String id, CostProjectItem projectCostItem) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(id)))
                .apply(new Update().push("projectItems", projectCostItem))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject createCostItemDetailOriginal(String id, String itemId,
                                                    CostProjectItemDetail costProjectItemDetail) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(id)
                        .and("projectItems").elemMatch(where("id").is(itemId))))
                .apply(update("projectItems.$.original", costProjectItemDetail))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject createCostItemDetailMultipliedByKWithRound(String id, String itemId,
                                                                  CostProjectItemDetail costProjectItemDetail) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(id)
                        .and("projectItems").elemMatch(where("id").is(itemId))))
                .apply(update("projectItems.$.multipliedByKWithRound", costProjectItemDetail))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject createCostItemDetailMultipliedByKWithRound5(String id, String itemId,
                                                                   CostProjectItemDetail costProjectItemDetail) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(id)
                        .and("projectItems").elemMatch(where("id").is(itemId))))
                .apply(update("projectItems.$.multipliedByKWithRound5", costProjectItemDetail))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateCostItem(String id, CostProjectItem projectCostItem) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(id)
                        .and("projectItems").elemMatch(where("id").is(projectCostItem.id()))))
                .apply(update("projectItems.$", projectCostItem))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateCostItemParentId(String projectId, String parentItemId,
                                              String costItemId) {

        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)
                        .and("projectItems").elemMatch(where("id").is(costItemId))))
                .apply(update("projectItems.$.parentItemId", parentItemId))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateCostItemName(String id, String itemId, String costItemName) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(id)
                        .and("projectItems").elemMatch(where("id").is(itemId))))
                .apply(update("projectItems.$.name", costItemName))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateCostItemComment(String id, String itemId, String comment) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(id)
                        .and("projectItems").elemMatch(where("id").is(itemId))))
                .apply(update("projectItems.$.comment", comment))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject deleteAllCostItemsById(String id, List<String> itemIds) {

        var itemObjectIds = itemIds.stream().map(ObjectId::new).toList();

        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(id)))
                .apply(new Update().pull("projectItems", query(where("id").in(itemObjectIds))))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }
}
