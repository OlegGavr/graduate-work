package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectGlobalOrder;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findCostItemById;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "UnusedAssignment"})
@SpringBootTest
class CostProjectItemServiceTest {

    @Autowired
    CostProjectService costProjectService;

    @Autowired
    CostProjectItemService costProjectItemService;

    @Test
    void checkCreatingItemsAddsItToProjectAsChildForRootItem() {
        // given
        var costProject = costProjectService.create(false);
        var templateCostItem = new CostProjectItem(new ObjectId().toString(), "Test item");

        // when
        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), templateCostItem);

        var createdCostItem = modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(templateCostItem.id()))
                .findAny().orElse(null);

        // then
        assertNotNull(createdCostItem);
        assertEquals(costProject.rootItemId(), createdCostItem.parentItemId());
    }

    @Test
    void checkCreatingItemsAddsItToTheEndOfGlobalOrdering() {
        // given
        var costProject = costProjectService.create(false);
        var templateCostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        // when
        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), templateCostItem);

        // then
        assertEquals(templateCostItem.id(), modifiedCostProject.globalOrder()
                .get(modifiedCostProject.globalOrder().size() - 1).projectItemId());
    }

    @Test
    void checkCreatingSubItemAddsItToTheEndOfConcreteParent() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        //noinspection UnusedAssignment
        modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        var level2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        // when
        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), level2CostItem);

        // then
        // check all items are presented in projectItems
        var allCostProjectItemIds = modifiedCostProject.projectItems()
                .stream().map(CostProjectItem::id).collect(toSet());

        var expectedCostProjectItemsIds = List.of(firstLevel1CostItem.id(),
                secondLevel1CostItem.id(), level2CostItem.id());

        assertTrue(allCostProjectItemIds.containsAll(expectedCostProjectItemsIds));

        // check added in a when block item has right parent
        //noinspection OptionalGetWithoutIsPresent
        assertEquals(firstLevel1CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(level2CostItem.id())).findAny().get().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), level2CostItem.id(), secondLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkCreatingSubItemAddsItToTheEndOfConcreteParentWhenChildrenExists() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        //noinspection UnusedAssignment
        modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        // when
        //noinspection UnusedAssignment
        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), firstLevel2CostItem);

        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), secondLevel2CostItem);

        // then
        // check all items are presented in projectItems
        var allCostProjectItemIds = modifiedCostProject.projectItems()
                .stream().map(CostProjectItem::id).collect(toSet());

        var expectedCostProjectItemsIds = List.of(firstLevel1CostItem.id(),
                secondLevel1CostItem.id(), firstLevel2CostItem.id(), secondLevel2CostItem.id());

        assertTrue(allCostProjectItemIds.containsAll(expectedCostProjectItemsIds));

        // check added in a when block item has right parent
        //noinspection OptionalGetWithoutIsPresent
        assertEquals(firstLevel1CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(firstLevel2CostItem.id())).findAny().get().parentItemId());

        //noinspection OptionalGetWithoutIsPresent
        assertEquals(firstLevel1CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(secondLevel2CostItem.id())).findAny().get().parentItemId());


        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), firstLevel2CostItem.id(),
                        secondLevel2CostItem.id(), secondLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream()
                        .map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkCreatingSubItemAddsItToTheEndOfConcreteParentWhenTwoLevelChildrenExists() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel3CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        // when
        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), firstLevel2CostItem);

        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), firstLevel2CostItem.id(), firstLevel3CostItem);

        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), secondLevel2CostItem);

        // then
        // check all items are presented in projectItems
        var allCostProjectItemIds = modifiedCostProject.projectItems()
                .stream().map(CostProjectItem::id).collect(toSet());

        var expectedCostProjectItemsIds = List.of(firstLevel1CostItem.id(),
                secondLevel1CostItem.id(), firstLevel2CostItem.id(),
                secondLevel2CostItem.id(), firstLevel3CostItem.id());

        assertTrue(allCostProjectItemIds.containsAll(expectedCostProjectItemsIds));

        // check added in a when block item has right parent
        assertEquals(firstLevel1CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(firstLevel2CostItem.id())).findAny().get().parentItemId());

        assertEquals(firstLevel2CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(firstLevel3CostItem.id())).findAny().get().parentItemId());

        assertEquals(firstLevel1CostItem.id(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(secondLevel2CostItem.id())).findAny().get().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), firstLevel2CostItem.id(),
                        firstLevel3CostItem.id(), secondLevel2CostItem.id(), secondLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkThatCreatingCostItemAfterAnotherAddsItExactlyAfter() {
        // given
        var costProject = costProjectService.create(false);

        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        //noinspection UnusedAssignment
        modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        // when
        modifiedCostProject = costProjectItemService.createCostItemAfter(
                costProject.id(), firstLevel1CostItem.id(), thirdLevel1CostItem);

        // then
        // check all items are presented in projectItems
        var allProjectItemIds = modifiedCostProject.projectItems()
                .stream().map(CostProjectItem::id).collect(toSet());

        var expectedProjectItemsIds = List.of(firstLevel1CostItem.id(),
                secondLevel1CostItem.id(), thirdLevel1CostItem.id());

        assertTrue(allProjectItemIds.containsAll(expectedProjectItemsIds));

        // check added in a when block item has right parent
        //noinspection OptionalGetWithoutIsPresent
        assertEquals(costProject.rootItemId(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(thirdLevel1CostItem.id())).findAny().get().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), thirdLevel1CostItem.id(), secondLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkThatCreatingCostItemBeforeAnotherAddsItExactlyBefore() {
        // given
        var costProject = costProjectService.create(false);

        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        //noinspection UnusedAssignment
        modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        // when
        modifiedCostProject = costProjectItemService.createCostItemBefore(
                costProject.id(), secondLevel1CostItem.id(), thirdLevel1CostItem);

        // then
        // check all items are presented in projectItems
        var allCostProjectItemIds = modifiedCostProject.projectItems()
                .stream().map(CostProjectItem::id).collect(toSet());

        var expectedCostProjectItemsIds = List.of(firstLevel1CostItem.id(),
                secondLevel1CostItem.id(), thirdLevel1CostItem.id());

        assertTrue(allCostProjectItemIds.containsAll(expectedCostProjectItemsIds));

        // check added in a when block item has right parent
        //noinspection OptionalGetWithoutIsPresent
        assertEquals(costProject.rootItemId(), modifiedCostProject.projectItems().stream()
                .filter(pi -> pi.id().equals(thirdLevel1CostItem.id())).findAny().get().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), thirdLevel1CostItem.id(), secondLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkThatCreatingAfterItemOnTheSameLevelWithExistedSubItemWorksWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), firstLevel2CostItem);

        // when
        modifiedCostProject = costProjectItemService
                .createCostItemAfter(costProject.id(), firstLevel1CostItem.id(), secondLevel1CostItem);

        // then
        // check that moved items have correct parents
        assertEquals(modifiedCostProject.rootItemId(), findCostItemById(modifiedCostProject,
                secondLevel1CostItem.id()).orElseThrow().parentItemId());

        // check that global order has correct state
        assertEquals(List.of(firstLevel1CostItem.id(), firstLevel2CostItem.id(), secondLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());

    }

    @Test
    void checkThatCreatingAfterItemOnTheSameLevelAddItemExactlyAfter() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");
        var firstLevel2CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), firstLevel2CostItem);

        // when
        modifiedCostProject = costProjectItemService
                .createCostItemAfter(costProject.id(), firstLevel1CostItem.id(), secondLevel1CostItem);

        modifiedCostProject = costProjectItemService
                .createCostItemAfter(costProject.id(), firstLevel1CostItem.id(), thirdLevel1CostItem);

        // then
        // check that moved items have correct parents
        assertEquals(modifiedCostProject.rootItemId(), findCostItemById(modifiedCostProject,
                secondLevel1CostItem.id()).orElseThrow().parentItemId());

        assertEquals(modifiedCostProject.rootItemId(), findCostItemById(modifiedCostProject,
                thirdLevel1CostItem.id()).orElseThrow().parentItemId());

        // check that global order has correct state
        assertEquals(
                List.of(firstLevel1CostItem.id(), firstLevel2CostItem.id(), thirdLevel1CostItem.id(), secondLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream().map(CostProjectGlobalOrder::projectItemId).toList());

    }

    @Test
    void checkThatRemovingItemsWithInARowWorksWell() {
        // given
        var costProject = costProjectService.create(false);
        var firstLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var secondLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");

        var thirdLevel1CostItem = new CostProjectItem(
                new ObjectId().toString(), "Test item");



        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), thirdLevel1CostItem);

        // when
        modifiedCostProject = costProjectItemService
                .deleteAllCostItemsById(costProject.id(), List.of(
                        firstLevel1CostItem.id(), secondLevel1CostItem.id()), false);

        // then
        assertTrue(modifiedCostProject.projectItems().stream()
                .map(CostProjectItem::id).collect(Collectors.toSet())
                .contains(thirdLevel1CostItem.id()));

        assertFalse(modifiedCostProject.projectItems().stream()
                .map(CostProjectItem::id).collect(Collectors.toSet())
                .contains(firstLevel1CostItem.id()));

        assertFalse(modifiedCostProject.projectItems().stream()
                .map(CostProjectItem::id).collect(Collectors.toSet())
                .contains(secondLevel1CostItem.id()));

        // check that global order has correct state
        assertEquals(List.of(thirdLevel1CostItem.id()),
                modifiedCostProject.globalOrder().stream()
                        .map(CostProjectGlobalOrder::projectItemId).toList());
    }

    @Test
    void checkThatRemovingItemsWithHierarchyWorksWell() {
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


        var modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), firstLevel1CostItem);

        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), firstLevel1CostItem.id(), firstLevel2CostItem);

        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), firstLevel2CostItem.id(), firstLevel3CostItem);

        modifiedCostProject = costProjectItemService
                .createCostItem(costProject.id(), secondLevel1CostItem);

        modifiedCostProject = costProjectItemService.createCostSubItem(
                costProject.id(), secondLevel1CostItem.id(), secondLevel2CostItem);

        // when
        modifiedCostProject = costProjectItemService.deleteAllCostItemsById(costProject.id(),
                List.of(firstLevel1CostItem.id(), firstLevel3CostItem.id()));

        // then
        assertTrue(modifiedCostProject.projectItems().stream()
                .map(CostProjectItem::id).collect(Collectors.toSet())
                .containsAll(List.of(secondLevel1CostItem.id(), secondLevel2CostItem.id())));

        assertFalse(modifiedCostProject.projectItems().stream()
                .map(CostProjectItem::id).collect(Collectors.toSet())
                .contains(firstLevel1CostItem.id()));

        assertFalse(modifiedCostProject.projectItems().stream()
                .map(CostProjectItem::id).collect(Collectors.toSet())
                .contains(firstLevel2CostItem.id()));

        assertFalse(modifiedCostProject.projectItems().stream()
                .map(CostProjectItem::id).collect(Collectors.toSet())
                .contains(firstLevel3CostItem.id()));

        // check that global order has correct state
        assertEquals(List.of(secondLevel1CostItem.id(), secondLevel2CostItem.id()),
                modifiedCostProject.globalOrder().stream()
                        .map(CostProjectGlobalOrder::projectItemId).toList());
    }

}