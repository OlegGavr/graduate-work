package com.haulmont.projectplanning.costestimation.repository.mongo;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class CostProjectMongoRepositoryTest {

    @Autowired
    CostProjectMongoRepository projectMongoRepository;

    @Test
    void checkSavingProjectWorksAsExpected() {

        var cap = 2;
        List<CostProjectItem> projectCostItems = new ArrayList<>(cap);
        for (int i = 0; i < cap; i++) {
            var projectCostItem = new CostProjectItem(null, null, null, null, null, null, null, null);
//            projectCostItem.setId(new ObjectId().toString());
//            projectCostItem.setName("Super name: " + projectCostItem.getId());

            projectCostItems.add(projectCostItem);
        }

        var projectCost = new CostProject(null, "Test project", null, 0, null,  null, projectCostItems, null, null);
        var savedProjectCost = projectMongoRepository.save(projectCost);
        System.out.println(savedProjectCost);
        var fetchedById = projectMongoRepository.findById(savedProjectCost.id());

        var projectCostItem = projectCostItems.get(0);
//        projectCostItem.setName("Test");
        projectMongoRepository.updateCostItem(fetchedById.orElseThrow().id(), projectCostItem);

        System.out.println(fetchedById);
    }
}