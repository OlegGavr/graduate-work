package com.haulmont.projectplanning.costestimation.repository.mongo.costproject.costitem.measure;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class CostProjectItemMeasureCustomMongoRepositoryImpl
        implements CostProjectItemMeasureCustomMongoRepository {

    private MongoTemplate mongoTemplate;

    public CostProjectItemMeasureCustomMongoRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public CostProject createCostItemMeasure(String projectId, String itemId,
                                             CostProjectItemMeasure measure) {

        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)
                        .and("projectItems").elemMatch(where("id").is(itemId))))
                .apply(new Update().push("projectItems.$.measures", measure))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject createAllCostItemMeasures(String projectId, String itemId,
                                                 List<CostProjectItemMeasure> measures) {

        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)
                        .and("projectItems").elemMatch(where("id").is(itemId))))
                .apply(new Update().push("projectItems.$.measures")
                        .each(measures.toArray()))
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateMeasure(String projectId, String itemId,
                                     String measureId, CostProjectItemMeasure measure) {

        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(new Update()
                        .set("projectItems.$[pi].measures.$[m]", measure)
                        .filterArray("pi._id", new ObjectId(itemId))
                        .filterArray("m._id", new ObjectId(measureId))
                )
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    @Override
    public CostProject updateMeasures(String projectId, String itemId, List<CostProjectItemMeasure> measures) {
        return mongoTemplate.update(CostProject.class)
                .matching(query(where("id").is(projectId)))
                .apply(new Update()
                        .set("projectItems.$[pi].measures", measures)
                        .filterArray("pi._id", new ObjectId(itemId))
                )
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }
}
