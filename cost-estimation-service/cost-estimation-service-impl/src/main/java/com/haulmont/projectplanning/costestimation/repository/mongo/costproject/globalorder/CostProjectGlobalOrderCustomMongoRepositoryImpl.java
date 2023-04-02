package com.haulmont.projectplanning.costestimation.repository.mongo.costproject.globalorder;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectGlobalOrder;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

public class CostProjectGlobalOrderCustomMongoRepositoryImpl
        implements CostProjectGlobalOrderCustomMongoRepository {

    private MongoTemplate mongoTemplate;

    public CostProjectGlobalOrderCustomMongoRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public CostProject addGlobalOrderToTheEnd(String projectId, CostProjectGlobalOrder globalOrderItem) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(new Update().push("globalOrder", globalOrderItem))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject addGlobalOrderToTheConcretePosition(String projectId,
                                                           CostProjectGlobalOrder globalOrderItem,
                                                           Integer position) {

        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(new Update().push("globalOrder")
                        .atPosition(position).value(globalOrderItem))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject addAllGlobalOrdersToTheConcretePosition(String projectId,
                                                               List<CostProjectGlobalOrder> globalOrderItems,
                                                               Integer position) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(new Update().push("globalOrder")
                        .atPosition(position)
                        .each(globalOrderItems.toArray()))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject deleteGlobalOrderByProjectItemId(String projectId, String projectItemId) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(new Update().pull("globalOrder", query(
                        where("projectItemId").is(projectItemId))))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject deleteAllGlobalOrdersByConcreteParentIn(String projectId, String parentId) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(new Update().pull("globalOrder", query(
                        where("parentItemIds").is(parentId))))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject deleteAllGlobalOrdersByProjectItemId(String projectId, List<String> projectItemIds) {

        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(new Update().pull("globalOrder", query(
                        where("projectItemId").in(projectItemIds))))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject deleteAllGlobalOrders(String projectId) {

        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(update("globalOrder", emptyList()))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateGlobalOrder(String projectId, List<CostProjectGlobalOrder> costItemsGlobalOrderIds) {
        throw new UnsupportedOperationException("Will be implemented later");
    }
}
