package com.haulmont.projectplanning.costestimation.repository.mongo.costproject;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CostProjectCustomMongoRepositoryImplTest {

    @Autowired
    CostProjectMongoRepository costProjectMongoRepository;

    @Test
    void checkThatChangingProjectNameWorksWell() {
        // given
        var costProject = new CostProject(new ObjectId().toString(), "Test project");
        var savedCostProject = costProjectMongoRepository.save(costProject);

        var newProjectName = new ObjectId().toString();

        // when
        var updatedCostProject = costProjectMongoRepository
                .updateName(savedCostProject.id(), newProjectName);

        // then
        assertEquals(newProjectName, updatedCostProject.name());
    }

    @Test
    void checkThatChangingMoneyPerHourWorksWell() {
        // given
        var costProject = new CostProject(new ObjectId().toString(), "Test project");
        var savedCostProject = costProjectMongoRepository.save(costProject);

        var newMoneyPerHour = ThreadLocalRandom.current().nextInt();

        // when
        var updatedCostProject = costProjectMongoRepository
                .updateMoneyPerHour(savedCostProject.id(), newMoneyPerHour);

        // then
        assertEquals(newMoneyPerHour, updatedCostProject.moneyPerHour());
    }
}