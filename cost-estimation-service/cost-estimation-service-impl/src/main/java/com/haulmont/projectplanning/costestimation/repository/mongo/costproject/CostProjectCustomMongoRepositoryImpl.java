package com.haulmont.projectplanning.costestimation.repository.mongo.costproject;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

public class CostProjectCustomMongoRepositoryImpl implements CostProjectCustomMongoRepository {

    private MongoTemplate mongoTemplate;

    public CostProjectCustomMongoRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public CostProject updateName(String id, String projectName) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(id)))
                .apply(update("name", projectName))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateMoneyPerHour(String id, Integer moneyPerHour) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(id)))
                .apply(update("moneyPerHour", moneyPerHour))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateSharePointLink(String projectId, String sharePointLink) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(update("sharePointLink", sharePointLink))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }
}
