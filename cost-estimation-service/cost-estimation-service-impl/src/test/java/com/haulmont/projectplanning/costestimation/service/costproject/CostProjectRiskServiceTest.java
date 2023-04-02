package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CostProjectRiskServiceTest {

    @Autowired
    CostProjectService costProjectService;

    @Autowired
    CostProjectRiskService costProjectRiskService;

    @Test
    void checkThatDefiningBaRiskWorksAsWell() {
        // given
        var template = new CostProject(null, "Test project");
        var createdProject = costProjectService.create(template, false);

        // when
        var updatedCostProject = costProjectRiskService
                .defineDevRisk(createdProject.id(), 0.8);

        // then
        assertEquals(0.3, updatedCostProject.projectRisk().defaultRisk());
        assertEquals(0.8, updatedCostProject.projectRisk().dev());
    }
}