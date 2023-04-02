package com.haulmont.projectplanning.costestimation.repository.mongo.costproject.risk;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectRisk;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

public class CostProjectRiskCustomMongoRepositoryImpl implements CostProjectRiskCustomMongoRepository {

    private MongoTemplate mongoTemplate;

    public CostProjectRiskCustomMongoRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public CostProject updateCostProjectRisk(String projectId, CostProjectRisk costProjectRisk) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(update("projectRisk", costProjectRisk))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateDefaultRisk(String projectId, Double riskValue) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(update("projectRisk.defaultRisk", riskValue))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateDevRisk(String projectId, Double riskValue) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(update("projectRisk.dev", riskValue))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateQaRisk(String projectId, Double riskValue) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(update("projectRisk.qa", riskValue))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateBaRisk(String projectId, Double riskValue) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(update("projectRisk.ba", riskValue))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateDevOpsRisk(String projectId, Double riskValue) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(update("projectRisk.devOps", riskValue))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateTmRisk(String projectId, Double riskValue) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(update("projectRisk.tm", riskValue))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updatePmRisk(String projectId, Double riskValue) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(update("projectRisk.pm", riskValue))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }
}
