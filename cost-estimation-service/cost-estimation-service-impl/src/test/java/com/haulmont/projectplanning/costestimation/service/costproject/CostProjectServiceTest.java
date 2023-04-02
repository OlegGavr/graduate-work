package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService.EXAMPLE_COST_ITEM_NAME;
import static com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService.EXAMPLE_SUB_COST_ITEM_NAME;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findCostItemByName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CostProjectServiceTest {

    @Autowired
    CostProjectService costProjectService;

    @Test
    void checkThatCreatingByTemplateCostProjectWorksWell() {
        var template = new CostProject(null, "Test project");
        var createdProject = costProjectService.create(template, false);

        assertEquals(template.name(), createdProject.name());
        assertNotNull(createdProject.rootItemId());
        assertEquals(createdProject.rootItemId(), createdProject.projectItems().get(0).id());
    }

    @Test
    void checkThatCreationEmptyCostProjectWorksWell() {
        var emptyProject = costProjectService.create(false);

        assertNotNull(emptyProject.rootItemId());
        assertEquals(emptyProject.rootItemId(), emptyProject.projectItems().get(0).id());
    }

    @Test
    void checkThatCreatingEmptyCostProjectWithExampleWorksWell() {
        // when
        var emptyCostProject = costProjectService.create(true);

        // then
        var costItem = findCostItemByName(emptyCostProject, EXAMPLE_COST_ITEM_NAME).orElse(null);
        assertNotNull(costItem);

        var subCostItem = findCostItemByName(emptyCostProject, EXAMPLE_SUB_COST_ITEM_NAME).orElse(null);
        assertNotNull(subCostItem);

        assertEquals(costItem.id(), subCostItem.parentItemId());
    }
}