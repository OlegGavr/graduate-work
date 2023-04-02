package com.haulmont.projectplanning.costestimation.repository.mongo;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectVersion;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectVersionId;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static java.util.Collections.emptyList;

@SpringBootTest
class CostProjectVersionMongoRepositoryTest {

    @Autowired
    CostProjectMongoRepository projectMongoRepository;

    @Autowired
    CostProjectVersionMongoRepository projectCostVersionMongoRepository;

    @Test
    void checkThatKeyCanContainsIdAndVersion() {
        var projectCost = new CostProject(new ObjectId().toString(), "Test project",
                new ObjectId().toString(), null, null, emptyList());
        var savedProjectCost = projectMongoRepository.save(projectCost);
        var projectCostVersionId = new CostProjectVersionId(savedProjectCost.id(), 1);
        var projectCostVersion = new CostProjectVersion(null, null);
//        projectCostVersion.setId(projectCostVersionId);
//        projectCostVersion.setProjectCost(projectCost);
//        var savedProjectCostVersion = projectCostVersionMongoRepository.save(projectCostVersion);
        var all = projectCostVersionMongoRepository.findAll();
        System.out.println(all);

    }
}