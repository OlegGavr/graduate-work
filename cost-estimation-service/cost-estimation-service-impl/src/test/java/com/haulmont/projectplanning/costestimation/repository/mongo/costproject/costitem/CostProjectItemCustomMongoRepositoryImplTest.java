package com.haulmont.projectplanning.costestimation.repository.mongo.costproject.costitem;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("UnusedAssignment")
@SpringBootTest
class CostProjectItemCustomMongoRepositoryImplTest {

    @Autowired
    CostProjectMongoRepository costProjectMongoRepository;

    @Test
    void checkThatChangingCostItemNameWorksWell() {
        // given
        var costProject = new CostProject(new ObjectId().toString(), "Test project");
        var savedCostProject = costProjectMongoRepository.save(costProject);

        var costProjectItem = new CostProjectItem(new ObjectId().toString(), "Test item");
        var modifiedCostProject = costProjectMongoRepository
                .createCostItem(savedCostProject.id(), costProjectItem);

        var newCostItemName = new ObjectId().toString();

        // when
        modifiedCostProject = costProjectMongoRepository.updateCostItemName(
                modifiedCostProject.id(), costProjectItem.id(), newCostItemName);

        // then
        assertEquals(newCostItemName, modifiedCostProject.projectItems().get(0).name());
    }

    @Test
    void checkThatChangingCostItemCommentWorksWell() {
        // given
        var costProject = new CostProject(new ObjectId().toString(), "Test project");
        var savedCostProject = costProjectMongoRepository.save(costProject);

        var costProjectItem = new CostProjectItem(new ObjectId().toString(), "Test item");
        var modifiedCostProject = costProjectMongoRepository
                .createCostItem(savedCostProject.id(), costProjectItem);

        var comment = new ObjectId().toString();

        // when
        modifiedCostProject = costProjectMongoRepository.updateCostItemComment(
                modifiedCostProject.id(), costProjectItem.id(), comment);

        // then
        assertEquals(comment, modifiedCostProject.projectItems().get(0).comment());
    }

    @Test
    void checkThatRemovingCostItemsByIdsWorksWell() {
        // given
        var costProject = new CostProject(new ObjectId().toString(), "Test project");
        var savedCostProject = costProjectMongoRepository.save(costProject);

        var costProjectItem = new CostProjectItem(new ObjectId().toString(), "Test item");
        var modifiedCostProject = costProjectMongoRepository
                .createCostItem(savedCostProject.id(), costProjectItem);

        var costProjectItem2 = new CostProjectItem(new ObjectId().toString(), "Test item");
        modifiedCostProject = costProjectMongoRepository
                .createCostItem(savedCostProject.id(), costProjectItem2);

        var costProjectItem3 = new CostProjectItem(new ObjectId().toString(), "Test item");
        modifiedCostProject = costProjectMongoRepository
                .createCostItem(savedCostProject.id(), costProjectItem3);

        // when
        modifiedCostProject = costProjectMongoRepository.deleteAllCostItemsById(
                modifiedCostProject.id(), List.of(costProjectItem2.id(), costProjectItem3.id()));

        // then
        assertEquals(List.of(costProjectItem), modifiedCostProject.projectItems());
    }
}