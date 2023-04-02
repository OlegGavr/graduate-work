package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType.MANUAL;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemMeasureTools.findCostMeasureByCostItemId;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemMeasureTools.findCostMeasureByCostItemIdAndMeasureId;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CostProjectItemMeasureServiceTest {

    @Autowired
    CostProjectMongoRepository costProjectMongoRepository;

    @Autowired
    CostProjectService costProjectService;

    @Autowired
    CostProjectItemService costProjectItemService;

    @Autowired
    CostProjectMeasureService costProjectItemMeasureService;

    @Test
    void checkThatCreatingEmptyAutoMeasuresWorksAsWell() {
        // given
        var actualCostProject = costProjectService.create("Test project", false);

        var costItem = new CostProjectItem(new ObjectId().toString(), "Test item");
        actualCostProject = costProjectItemService.createCostItem(actualCostProject.id(), costItem);
        var actualMeasures = findCostMeasureByCostItemId(actualCostProject, costItem.id());
        var actualMeasuresCount = actualMeasures.size();

        // when
        actualCostProject = costProjectItemMeasureService
                .createNumberOfAutoMeasures(
                        actualCostProject.id(), costItem.id(), 6);

        actualMeasures = findCostMeasureByCostItemId(actualCostProject, costItem.id());

        //then
        assertEquals(actualMeasuresCount + 6, actualMeasures.size());
    }

    @Test
    void checkThatDefiningManualMeasureWorksAsWell() {
        // given
        var actualCostProject = costProjectService.create("Test project", false);

        var costItem = new CostProjectItem(new ObjectId().toString(), "Test item");
        actualCostProject = costProjectItemService.createCostItem(actualCostProject.id(), costItem);

        var costItemMeasure = new CostProjectItemMeasure(new ObjectId().toString());
        actualCostProject = costProjectMongoRepository
                .createCostItemMeasure(actualCostProject.id(),
                        costItem.id(), costItemMeasure);

        // when
        actualCostProject = costProjectItemMeasureService
                .defineManualMeasure(actualCostProject.id(),
                        costItem.id(), costItemMeasure.id(), 10.0);

        var manualCostItemMeasure = findCostMeasureByCostItemIdAndMeasureId(
                actualCostProject, costItem.id(), costItemMeasure.id()).orElseThrow();

        // then
        assertEquals(10.0, manualCostItemMeasure.value());
        assertEquals(MANUAL, manualCostItemMeasure.type());
    }
}