package com.haulmont.projectplanning.costestimation.service.costproject.internal;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectGlobalOrder;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectOrderingService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findCostItemById;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"UnusedAssignment", "OptionalGetWithoutIsPresent"})
@SpringBootTest
class InternalCostProjectOrderingServiceTest {

    @Autowired
    CostProjectService costProjectService;

    @Autowired
    CostProjectOrderingService costProjectOrderingService;

    @Autowired
    InternalCostProjectItemService internalCostProjectItemService;

    @Autowired
    InternalCostProjectOrderingService internalCostProjectOrderingService;

    @Autowired
    CostProjectMongoRepository costProjectMongoRepository;


    @Test
    void checkMovingItemAfterWithoutChangingParentWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");


        var modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), firstLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), firstLevel2CostItem.id(), firstLevel3CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel1CostItem.id(), secondLevel2CostItem);


        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemAfterAnotherItem(costProject.id(),
                        secondLevel1CostItem.id(), firstLevel1CostItem.id());

        // then
        // check that global order has correct state
        assertEquals(List.of(secondLevel1CostItem.id(), secondLevel2CostItem.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id(), firstLevel3CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkMovingItemAfterWithChangingParentWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), firstLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), firstLevel2CostItem.id(), firstLevel3CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel1CostItem.id(), secondLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel2CostItem.id(), secondLevel3CostItem);

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemAfterAnotherItem(costProject.id(),
                        secondLevel3CostItem.id(), firstLevel2CostItem.id());

        // then
        // check that moved items have correct parents
        assertEquals(secondLevel2CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(firstLevel2CostItem.id())).findAny().get().parentItemId());
        assertEquals(firstLevel2CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(firstLevel3CostItem.id())).findAny().get().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), secondLevel1CostItem.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id(), firstLevel2CostItem.id(),
                        firstLevel3CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkMovingItemBeforeWithoutChangingParentWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");


        var modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), firstLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), firstLevel2CostItem.id(), firstLevel3CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel1CostItem.id(), secondLevel2CostItem);


        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemBeforeAnotherItem(costProject.id(),
                        firstLevel1CostItem.id(), secondLevel1CostItem.id());

        // then
        // check that global order has correct state
        assertEquals(List.of(secondLevel1CostItem.id(), secondLevel2CostItem.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id(), firstLevel3CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkMovingItemBeforeWithChangingParentWorksAsWell() {
        // given
        var costProject = costProjectService.create(
                "Test project for checkMovingItemBeforeWithChangingParentWorksAsWell", false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "firstLevel1CostItem");
        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "firstLevel2CostItem");
        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "firstLevel3CostItem");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "secondLevel1CostItem");
        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "secondLevel2CostItem");
        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "secondLevel3CostItem");

        var modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), firstLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), firstLevel2CostItem.id(), firstLevel3CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel1CostItem.id(), secondLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel2CostItem.id(), secondLevel3CostItem);

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemBeforeAnotherItem(costProject.id(),
                        secondLevel3CostItem.id(), firstLevel2CostItem.id());

        // then
        // check that moved items have correct parents
        assertEquals(secondLevel2CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(firstLevel2CostItem.id())).findAny().get().parentItemId());
        assertEquals(firstLevel2CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(firstLevel3CostItem.id())).findAny().get().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), secondLevel1CostItem.id(),
                        secondLevel2CostItem.id(), firstLevel2CostItem.id(),
                        firstLevel3CostItem.id(), secondLevel3CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkMovingItemAsSubItemWithChangingParentWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), firstLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), firstLevel2CostItem.id(), firstLevel3CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel1CostItem.id(), secondLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel2CostItem.id(), secondLevel3CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), thirdLevel1CostItem);

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemAsSubItem(costProject.id(),
                        secondLevel2CostItem.id(), firstLevel2CostItem.id());

        // then
        // check that moved items have correct parents
        assertEquals(secondLevel2CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(firstLevel2CostItem.id())).findAny().get().parentItemId());
        assertEquals(firstLevel2CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(firstLevel3CostItem.id())).findAny().get().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), secondLevel1CostItem.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id(),
                        firstLevel2CostItem.id(), firstLevel3CostItem.id(), thirdLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkThatMovingItemDownAddItemAndSubItemsAsChildrenOfPreviousItem() {

        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel1CostItem.id(), secondLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel2CostItem.id(), secondLevel3CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), thirdLevel1CostItem);

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemOnLevelDown(costProject.id(), secondLevel1CostItem.id());

        // then
        // check that moved items have correct parents
        assertEquals(firstLevel1CostItem.id(), findCostItemById(modifiedCostProject,
                secondLevel1CostItem.id()).orElseThrow().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), secondLevel1CostItem.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id(), thirdLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkThatMovingItemUpAddItemAndSubItemsAsNextOfTheParent() {

        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel1CostItem.id(), secondLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel2CostItem.id(), secondLevel3CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), thirdLevel1CostItem);

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemOnLevelUp(costProject.id(), secondLevel2CostItem.id());

        // then
        // check that moved items have correct parents
        assertEquals(costProject.rootItemId(), findCostItemById(modifiedCostProject,
                secondLevel2CostItem.id()).orElseThrow().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), secondLevel1CostItem.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id(), thirdLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkThatMovingItemUpSupportManyElementsOnTheSameLevel() {

        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel2_1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel1CostItem.id(), secondLevel2CostItem);

        modifiedCostProject = internalCostProjectItemService.createCostSubItem(
                costProject.id(), secondLevel1CostItem.id(), secondLevel2_1CostItem);

        modifiedCostProject = internalCostProjectItemService
                .createCostItem(costProject.id(), thirdLevel1CostItem);

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemOnLevelUp(costProject.id(), secondLevel2CostItem.id());

        // then
        // check that moved items have correct parents
        assertEquals(costProject.rootItemId(), findCostItemById(modifiedCostProject,
                secondLevel2CostItem.id()).orElseThrow().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), secondLevel1CostItem.id(),
                        secondLevel2_1CostItem.id(), secondLevel2CostItem.id(), thirdLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkThatMovingCostItemAfterAnotherItemOnTheSameHierarchyLevelWolksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id());

        // before check state
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemAfterAnotherItem(costProject.id(),
                        secondLevel1CostItem.id(), firstLevel1CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

    }

    @Test
    void checkThatMovingCostItemAfterAnotherItemSupportHierarchyChanging() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem.id(), firstLevel3CostItem.id());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemAfterAnotherItem(costProject.id(),
                        firstLevel2CostItem.id(), secondLevel2CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

    }

    @Test
    void checkThatMovingCostItemAfterAnotherItemSupportHierarchyChanging2() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem2 = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel3CostItem2 = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel2CostItem.id(), firstLevel3CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem2.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem2.id(), secondLevel3CostItem2.id());

        // before check state
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel3CostItem.id(), List.of(firstLevel2CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem2.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem2.id(), List.of(secondLevel2CostItem2.id(), secondLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemAfterAnotherItem(costProject.id(),
                        secondLevel2CostItem.id(), firstLevel2CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel3CostItem.id(), List.of(firstLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem2.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem2.id(), List.of(secondLevel2CostItem2.id(), secondLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());
    }

    @Test
    void checkThatMovingCostItemBeforeAnotherItemOnTheSameHierarchyLevelWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem.id(), firstLevel3CostItem.id());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemBeforeAnotherItem(costProject.id(),
                        firstLevel1CostItem.id(), secondLevel1CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

    }

    @Test
    void checkThatMovingCostItemBeforeAnotherItemSupportHierarchyChanging() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id());

        // before check state
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemBeforeAnotherItem(costProject.id(),
                        firstLevel2CostItem.id(), secondLevel2CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

    }

    @Test
    void checkThatMovingCostItemBeforeAnotherItemSupportHierarchyChanging2() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel2CostItem.id(), firstLevel3CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id());

        // before check state
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel3CostItem.id(), List.of(firstLevel2CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemBeforeAnotherItem(costProject.id(),
                        secondLevel3CostItem.id(), firstLevel2CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel3CostItem.id(), List.of(firstLevel2CostItem.id(), secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

    }

    @Test
    void checkThatMovingCostItemAsSubItemAnotherItemWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id());

        // before check state
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemAsSubItem(costProject.id(),
                        firstLevel2CostItem.id(), secondLevel2CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(firstLevel2CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), firstLevel2CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId()))),
                modifiedCostProject.globalOrder());
    }

    @Test
    void checkThatMovingCostItemAsSubItemAnotherItemWorksAsWellWithHierarchy() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel2CostItem.id(), firstLevel3CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id());

        // before check state
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel3CostItem.id(), List.of(firstLevel2CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemAsSubItem(costProject.id(),
                        firstLevel2CostItem.id(), secondLevel2CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel3CostItem.id(), List.of(firstLevel2CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(firstLevel2CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), firstLevel2CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

    }

    @Test
    void checkThatMovingCostItemOnLevelDownWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), thirdLevel1CostItem.id());

        // before check state
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(thirdLevel1CostItem.id(), List.of(costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemOnLevelDown(costProject.id(), secondLevel1CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(thirdLevel1CostItem.id(), List.of(costProject.rootItemId()))),
                modifiedCostProject.globalOrder());
    }

    @Test
    void checkThatMovingCostItemOnLevelDownWorksAsWellIfPreviousItemIsParent() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), thirdLevel1CostItem.id());

        // before check state
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(thirdLevel1CostItem.id(), List.of(costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemOnLevelDown(costProject.id(), secondLevel1CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(thirdLevel1CostItem.id(), List.of(costProject.rootItemId()))),
                modifiedCostProject.globalOrder());
    }

    @Test
    void checkThatMovingCostItemOnLevelUpWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), secondLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel1CostItem.id(), secondLevel2CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        secondLevel2CostItem.id(), secondLevel3CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), thirdLevel1CostItem.id());

        // before check state
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), firstLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(thirdLevel1CostItem.id(), List.of(costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

        // when
        modifiedCostProject = internalCostProjectOrderingService
                .moveCostItemOnLevelUp(costProject.id(), secondLevel1CostItem.id());

        // then
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel2CostItem.id(), List.of(secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(secondLevel3CostItem.id(), List.of(secondLevel2CostItem.id(), secondLevel1CostItem.id(), costProject.rootItemId())),
                        new CostProjectGlobalOrder(thirdLevel1CostItem.id(), List.of(costProject.rootItemId()))),
                modifiedCostProject.globalOrder());
    }


    @Test
    void checkThatExceptionalMovingDoesNotLooseItems() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnTopLevel(costProject.id(), firstLevel1CostItem.id());

        modifiedCostProject = internalCostProjectOrderingService
                .addCostItemAsLastOnConcreteParent(costProject.id(),
                        firstLevel1CostItem.id(), firstLevel2CostItem.id());

        // before check state
        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

        // when | then
        assertThrows(RuntimeException.class, () -> internalCostProjectOrderingService
                .moveCostItemAfterAnotherItem(costProject.id(),
                        firstLevel2CostItem.id(), firstLevel2CostItem.id()));

        modifiedCostProject = costProjectMongoRepository.findById(costProject.id()).orElseThrow();

        assertEquals(List.of(new CostProjectGlobalOrder(firstLevel1CostItem.id(), List.of(costProject.rootItemId())),
                        new CostProjectGlobalOrder(firstLevel2CostItem.id(), List.of(firstLevel1CostItem.id(), costProject.rootItemId()))),
                modifiedCostProject.globalOrder());

    }
}