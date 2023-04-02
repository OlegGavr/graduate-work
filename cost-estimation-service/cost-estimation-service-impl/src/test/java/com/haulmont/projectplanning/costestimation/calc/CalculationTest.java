package com.haulmont.projectplanning.costestimation.calc;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectItemService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectMeasureService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class CalculationTest {

    @Autowired
    Calculation calculation;
    @Autowired
    CostProjectService projectService;
    @Autowired
    CostProjectItemService projectItemService;
    @Autowired
    CostProjectMongoRepository projectMongoRepository;
    @Autowired
    CostProjectMeasureService measureService;


    @Test
    void calculateWithAllAutoMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with all auto measures"), false);
        var projectId = project.id();

        var mainPage = new CostProjectItem(genId(), "Main page");
        projectItemService.createCostItem(projectId, mainPage);

        var filterOnMainPageId = genId();
        var filterOnMainPage = new CostProjectItem(filterOnMainPageId, "Filter on main page");
        project = projectItemService.createCostSubItem(projectId, mainPage.id(), filterOnMainPage);
        filterOnMainPage = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(filterOnMainPageId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, filterOnMainPage.id(), filterOnMainPage.original().analyseCost(), 1d);
        measureService.defineAutoMeasure(projectId, filterOnMainPage.id(), filterOnMainPage.original().backendCost(), 2d);
        measureService.defineAutoMeasure(projectId, filterOnMainPage.id(), filterOnMainPage.original().frontendCost(), 3d);
        measureService.defineAutoMeasure(projectId, filterOnMainPage.id(), filterOnMainPage.original().devOpsCost(), 6d);
        measureService.defineAutoMeasure(projectId, filterOnMainPage.id(), filterOnMainPage.original().otherCost(), 7d);
        measureService.defineAutoMeasure(projectId, filterOnMainPage.id(), filterOnMainPage.original().tmCost(), 8d);
        measureService.defineAutoMeasure(projectId, filterOnMainPage.id(), filterOnMainPage.original().pmCost(), 9d);

        var usersOnMainPageId = genId();
        var usersOnMainPage = new CostProjectItem(usersOnMainPageId, "Users on main page");
        project = projectItemService.createCostSubItem(projectId, mainPage.id(), usersOnMainPage);
        usersOnMainPage = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(usersOnMainPageId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, usersOnMainPage.id(), usersOnMainPage.original().analyseCost(), 11d);
        measureService.defineAutoMeasure(projectId, usersOnMainPage.id(), usersOnMainPage.original().backendCost(), 12d);
        measureService.defineAutoMeasure(projectId, usersOnMainPage.id(), usersOnMainPage.original().frontendCost(), 13d);
        measureService.defineAutoMeasure(projectId, usersOnMainPage.id(), usersOnMainPage.original().devOpsCost(), 16d);
        measureService.defineAutoMeasure(projectId, usersOnMainPage.id(), usersOnMainPage.original().otherCost(), 17d);
        measureService.defineAutoMeasure(projectId, usersOnMainPage.id(), usersOnMainPage.original().tmCost(), 18d);
        measureService.defineAutoMeasure(projectId, usersOnMainPage.id(), usersOnMainPage.original().pmCost(), 19d);

        var secondPageId = genId();
        var secondPage = new CostProjectItem(secondPageId, "Second page");
        project = projectItemService.createCostItem(projectId, secondPage);
        secondPage = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(secondPageId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, secondPage.id(), secondPage.original().analyseCost(), 21d);
        measureService.defineAutoMeasure(projectId, secondPage.id(), secondPage.original().backendCost(), 22d);
        measureService.defineAutoMeasure(projectId, secondPage.id(), secondPage.original().frontendCost(), 23d);
        measureService.defineAutoMeasure(projectId, secondPage.id(), secondPage.original().devOpsCost(), 26d);
        measureService.defineAutoMeasure(projectId, secondPage.id(), secondPage.original().otherCost(), 27d);
        measureService.defineAutoMeasure(projectId, secondPage.id(), secondPage.original().tmCost(), 28d);
        measureService.defineAutoMeasure(projectId, secondPage.id(), secondPage.original().pmCost(), 29d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var filterOnMainPageCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(filterOnMainPageId))
                .findFirst().get();
        var itemMeasureMap = filterOnMainPageCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = filterOnMainPageCalculated.original();
        assertEquals(1.5, itemMeasureMap.get(original.analyseCost()).value());
        assertEquals(2, itemMeasureMap.get(original.backendCost()).value());
        assertEquals(3, itemMeasureMap.get(original.frontendCost()).value());
        assertEquals(5, itemMeasureMap.get(original.devCost()).value());
        assertEquals(1.5, itemMeasureMap.get(original.qaCost()).value());
        assertEquals(1.5, itemMeasureMap.get(original.devOpsCost()).value());
        assertEquals(7, itemMeasureMap.get(original.otherCost()).value());
        assertEquals(1.5, itemMeasureMap.get(original.tmCost()).value());
        assertEquals(5.4, itemMeasureMap.get(original.pmCost()).value());
        assertEquals(23.4, itemMeasureMap.get(original.fullCost()).value());

        var multipliedByK = filterOnMainPageCalculated.multipliedByKWithRound();
        assertEquals(2, itemMeasureMap.get(multipliedByK.analyseCost()).value());
        assertEquals(3, itemMeasureMap.get(multipliedByK.backendCost()).value());
        assertEquals(4, itemMeasureMap.get(multipliedByK.frontendCost()).value());
        assertEquals(7, itemMeasureMap.get(multipliedByK.devCost()).value());
        assertEquals(2, itemMeasureMap.get(multipliedByK.qaCost()).value());
        assertEquals(2, itemMeasureMap.get(multipliedByK.devOpsCost()).value());
        assertEquals(10, itemMeasureMap.get(multipliedByK.otherCost()).value());
        assertEquals(2, itemMeasureMap.get(multipliedByK.tmCost()).value());
        assertEquals(8, itemMeasureMap.get(multipliedByK.pmCost()).value());
        assertEquals(33, itemMeasureMap.get(multipliedByK.fullCost()).value());

        var multipliedByKWithRound = filterOnMainPageCalculated.multipliedByKWithRound5();
        assertEquals(5, itemMeasureMap.get(multipliedByKWithRound.analyseCost()).value());
        assertEquals(5, itemMeasureMap.get(multipliedByKWithRound.backendCost()).value());
        assertEquals(5, itemMeasureMap.get(multipliedByKWithRound.frontendCost()).value());
        assertEquals(10, itemMeasureMap.get(multipliedByKWithRound.devCost()).value());
        assertEquals(5, itemMeasureMap.get(multipliedByKWithRound.qaCost()).value());
        assertEquals(5, itemMeasureMap.get(multipliedByKWithRound.devOpsCost()).value());
        assertEquals(10, itemMeasureMap.get(multipliedByKWithRound.otherCost()).value());
        assertEquals(5, itemMeasureMap.get(multipliedByKWithRound.tmCost()).value());
        assertEquals(10, itemMeasureMap.get(multipliedByKWithRound.pmCost()).value());
        assertEquals(50, itemMeasureMap.get(multipliedByKWithRound.fullCost()).value());


        var usersOnMainPageCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(usersOnMainPageId))
                .findFirst().get();
        itemMeasureMap = usersOnMainPageCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = usersOnMainPageCalculated.original();
        assertEquals(7.5, itemMeasureMap.get(original.analyseCost()).value());
        assertEquals(12, itemMeasureMap.get(original.backendCost()).value());
        assertEquals(13, itemMeasureMap.get(original.frontendCost()).value());
        assertEquals(25, itemMeasureMap.get(original.devCost()).value());
        assertEquals(7.5, itemMeasureMap.get(original.qaCost()).value());
        assertEquals(7.5, itemMeasureMap.get(original.devOpsCost()).value());
        assertEquals(17, itemMeasureMap.get(original.otherCost()).value());
        assertEquals(7.5, itemMeasureMap.get(original.tmCost()).value());
        assertEquals(21.6, itemMeasureMap.get(original.pmCost()).value());
        assertEquals(93.6, itemMeasureMap.get(original.fullCost()).value());

        multipliedByK = usersOnMainPageCalculated.multipliedByKWithRound();
        assertEquals(10, itemMeasureMap.get(multipliedByK.analyseCost()).value());
        assertEquals(16, itemMeasureMap.get(multipliedByK.backendCost()).value());
        assertEquals(17, itemMeasureMap.get(multipliedByK.frontendCost()).value());
        assertEquals(33, itemMeasureMap.get(multipliedByK.devCost()).value());
        assertEquals(10, itemMeasureMap.get(multipliedByK.qaCost()).value());
        assertEquals(10, itemMeasureMap.get(multipliedByK.devOpsCost()).value());
        assertEquals(23, itemMeasureMap.get(multipliedByK.otherCost()).value());
        assertEquals(10, itemMeasureMap.get(multipliedByK.tmCost()).value());
        assertEquals(29, itemMeasureMap.get(multipliedByK.pmCost()).value());
        assertEquals(125, itemMeasureMap.get(multipliedByK.fullCost()).value());

        multipliedByKWithRound = usersOnMainPageCalculated.multipliedByKWithRound5();
        assertEquals(10, itemMeasureMap.get(multipliedByKWithRound.analyseCost()).value());
        assertEquals(20, itemMeasureMap.get(multipliedByKWithRound.backendCost()).value());
        assertEquals(20, itemMeasureMap.get(multipliedByKWithRound.frontendCost()).value());
        assertEquals(35, itemMeasureMap.get(multipliedByKWithRound.devCost()).value());
        assertEquals(10, itemMeasureMap.get(multipliedByKWithRound.qaCost()).value());
        assertEquals(10, itemMeasureMap.get(multipliedByKWithRound.devOpsCost()).value());
        assertEquals(25, itemMeasureMap.get(multipliedByKWithRound.otherCost()).value());
        assertEquals(10, itemMeasureMap.get(multipliedByKWithRound.tmCost()).value());
        assertEquals(30, itemMeasureMap.get(multipliedByKWithRound.pmCost()).value());
        assertEquals(130, itemMeasureMap.get(multipliedByKWithRound.fullCost()).value());


        var mainPageCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(mainPage.id()))
                .findFirst().get();
        itemMeasureMap = mainPageCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = mainPageCalculated.original();
        assertEquals(9, itemMeasureMap.get(original.analyseCost()).value());
        assertEquals(14, itemMeasureMap.get(original.backendCost()).value());
        assertEquals(16, itemMeasureMap.get(original.frontendCost()).value());
        assertEquals(30, itemMeasureMap.get(original.devCost()).value());
        assertEquals(9, itemMeasureMap.get(original.qaCost()).value());
        assertEquals(9, itemMeasureMap.get(original.devOpsCost()).value());
        assertEquals(24, itemMeasureMap.get(original.otherCost()).value());
        assertEquals(9, itemMeasureMap.get(original.tmCost()).value());
        assertEquals(27, itemMeasureMap.get(original.pmCost()).value());
        assertEquals(117, itemMeasureMap.get(original.fullCost()).value());

        multipliedByK = mainPageCalculated.multipliedByKWithRound();
        assertEquals(12, itemMeasureMap.get(multipliedByK.analyseCost()).value());
        assertEquals(19, itemMeasureMap.get(multipliedByK.backendCost()).value());
        assertEquals(21, itemMeasureMap.get(multipliedByK.frontendCost()).value());
        assertEquals(40, itemMeasureMap.get(multipliedByK.devCost()).value());
        assertEquals(12, itemMeasureMap.get(multipliedByK.qaCost()).value());
        assertEquals(12, itemMeasureMap.get(multipliedByK.devOpsCost()).value());
        assertEquals(33, itemMeasureMap.get(multipliedByK.otherCost()).value());
        assertEquals(12, itemMeasureMap.get(multipliedByK.tmCost()).value());
        assertEquals(37, itemMeasureMap.get(multipliedByK.pmCost()).value());
        assertEquals(158, itemMeasureMap.get(multipliedByK.fullCost()).value());

        multipliedByKWithRound = mainPageCalculated.multipliedByKWithRound5();
        assertEquals(15, itemMeasureMap.get(multipliedByKWithRound.analyseCost()).value());
        assertEquals(25, itemMeasureMap.get(multipliedByKWithRound.backendCost()).value());
        assertEquals(25, itemMeasureMap.get(multipliedByKWithRound.frontendCost()).value());
        assertEquals(45, itemMeasureMap.get(multipliedByKWithRound.devCost()).value());
        assertEquals(15, itemMeasureMap.get(multipliedByKWithRound.qaCost()).value());
        assertEquals(15, itemMeasureMap.get(multipliedByKWithRound.devOpsCost()).value());
        assertEquals(35, itemMeasureMap.get(multipliedByKWithRound.otherCost()).value());
        assertEquals(15, itemMeasureMap.get(multipliedByKWithRound.tmCost()).value());
        assertEquals(40, itemMeasureMap.get(multipliedByKWithRound.pmCost()).value());
        assertEquals(180, itemMeasureMap.get(multipliedByKWithRound.fullCost()).value());


        var secondPageCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(secondPageId))
                .findFirst().get();
        itemMeasureMap = secondPageCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = secondPageCalculated.original();
        assertEquals(13.5, itemMeasureMap.get(original.analyseCost()).value());
        assertEquals(22, itemMeasureMap.get(original.backendCost()).value());
        assertEquals(23, itemMeasureMap.get(original.frontendCost()).value());
        assertEquals(45, itemMeasureMap.get(original.devCost()).value());
        assertEquals(13.5, itemMeasureMap.get(original.qaCost()).value());
        assertEquals(13.5, itemMeasureMap.get(original.devOpsCost()).value());
        assertEquals(27, itemMeasureMap.get(original.otherCost()).value());
        assertEquals(13.5, itemMeasureMap.get(original.tmCost()).value());
        assertEquals(37.8, itemMeasureMap.get(original.pmCost()).value());
        assertEquals(163.8, itemMeasureMap.get(original.fullCost()).value());

        multipliedByK = secondPageCalculated.multipliedByKWithRound();
        assertEquals(18, itemMeasureMap.get(multipliedByK.analyseCost()).value());
        assertEquals(29, itemMeasureMap.get(multipliedByK.backendCost()).value());
        assertEquals(30, itemMeasureMap.get(multipliedByK.frontendCost()).value());
        assertEquals(59, itemMeasureMap.get(multipliedByK.devCost()).value());
        assertEquals(18, itemMeasureMap.get(multipliedByK.qaCost()).value());
        assertEquals(18, itemMeasureMap.get(multipliedByK.devOpsCost()).value());
        assertEquals(36, itemMeasureMap.get(multipliedByK.otherCost()).value());
        assertEquals(18, itemMeasureMap.get(multipliedByK.tmCost()).value());
        assertEquals(50, itemMeasureMap.get(multipliedByK.pmCost()).value());
        assertEquals(217, itemMeasureMap.get(multipliedByK.fullCost()).value());

        multipliedByKWithRound = secondPageCalculated.multipliedByKWithRound5();
        assertEquals(20, itemMeasureMap.get(multipliedByKWithRound.analyseCost()).value());
        assertEquals(30, itemMeasureMap.get(multipliedByKWithRound.backendCost()).value());
        assertEquals(30, itemMeasureMap.get(multipliedByKWithRound.frontendCost()).value());
        assertEquals(60, itemMeasureMap.get(multipliedByKWithRound.devCost()).value());
        assertEquals(20, itemMeasureMap.get(multipliedByKWithRound.qaCost()).value());
        assertEquals(20, itemMeasureMap.get(multipliedByKWithRound.devOpsCost()).value());
        assertEquals(40, itemMeasureMap.get(multipliedByKWithRound.otherCost()).value());
        assertEquals(20, itemMeasureMap.get(multipliedByKWithRound.tmCost()).value());
        assertEquals(50, itemMeasureMap.get(multipliedByKWithRound.pmCost()).value());
        assertEquals(230, itemMeasureMap.get(multipliedByKWithRound.fullCost()).value());


        var rootItemId = project.rootItemId();
        var fullCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(rootItemId))
                .findFirst().get();
        itemMeasureMap = fullCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = fullCalculated.original();
        assertEquals(22.5, itemMeasureMap.get(original.analyseCost()).value());
        assertEquals(36, itemMeasureMap.get(original.backendCost()).value());
        assertEquals(39, itemMeasureMap.get(original.frontendCost()).value());
        assertEquals(75, itemMeasureMap.get(original.devCost()).value());
        assertEquals(22.5, itemMeasureMap.get(original.qaCost()).value());
        assertEquals(22.5, itemMeasureMap.get(original.devOpsCost()).value());
        assertEquals(51, itemMeasureMap.get(original.otherCost()).value());
        assertEquals(22.5, itemMeasureMap.get(original.tmCost()).value());
        assertEquals(64.8, itemMeasureMap.get(original.pmCost()).value());
        assertEquals(280.8, itemMeasureMap.get(original.fullCost()).value());

        multipliedByK = fullCalculated.multipliedByKWithRound();
        assertEquals(30, itemMeasureMap.get(multipliedByK.analyseCost()).value());
        assertEquals(48, itemMeasureMap.get(multipliedByK.backendCost()).value());
        assertEquals(51, itemMeasureMap.get(multipliedByK.frontendCost()).value());
        assertEquals(99, itemMeasureMap.get(multipliedByK.devCost()).value());
        assertEquals(30, itemMeasureMap.get(multipliedByK.qaCost()).value());
        assertEquals(30, itemMeasureMap.get(multipliedByK.devOpsCost()).value());
        assertEquals(69, itemMeasureMap.get(multipliedByK.otherCost()).value());
        assertEquals(30, itemMeasureMap.get(multipliedByK.tmCost()).value());
        assertEquals(87, itemMeasureMap.get(multipliedByK.pmCost()).value());
        assertEquals(375, itemMeasureMap.get(multipliedByK.fullCost()).value());

        multipliedByKWithRound = fullCalculated.multipliedByKWithRound5();
        assertEquals(35, itemMeasureMap.get(multipliedByKWithRound.analyseCost()).value());
        assertEquals(55, itemMeasureMap.get(multipliedByKWithRound.backendCost()).value());
        assertEquals(55, itemMeasureMap.get(multipliedByKWithRound.frontendCost()).value());
        assertEquals(105, itemMeasureMap.get(multipliedByKWithRound.devCost()).value());
        assertEquals(35, itemMeasureMap.get(multipliedByKWithRound.qaCost()).value());
        assertEquals(35, itemMeasureMap.get(multipliedByKWithRound.devOpsCost()).value());
        assertEquals(75, itemMeasureMap.get(multipliedByKWithRound.otherCost()).value());
        assertEquals(35, itemMeasureMap.get(multipliedByKWithRound.tmCost()).value());
        assertEquals(90, itemMeasureMap.get(multipliedByKWithRound.pmCost()).value());
        assertEquals(410, itemMeasureMap.get(multipliedByKWithRound.fullCost()).value());


        var aggregatedHoursCostItemId = project.aggregateItems().aggregatedHoursCostItemId();
        var aggregatedHoursCostItem = project.projectItems().stream()
                .filter(item -> item.id().equals(aggregatedHoursCostItemId))
                .findFirst().get();
        itemMeasureMap = aggregatedHoursCostItem.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = aggregatedHoursCostItem.original();
        assertEquals(22.5, itemMeasureMap.get(original.analyseCost()).value());
        assertEquals(36, itemMeasureMap.get(original.backendCost()).value());
        assertEquals(39, itemMeasureMap.get(original.frontendCost()).value());
        assertEquals(75, itemMeasureMap.get(original.devCost()).value());
        assertEquals(22.5, itemMeasureMap.get(original.qaCost()).value());
        assertEquals(22.5, itemMeasureMap.get(original.devOpsCost()).value());
        assertEquals(51, itemMeasureMap.get(original.otherCost()).value());
        assertEquals(22.5, itemMeasureMap.get(original.tmCost()).value());
        assertEquals(64.8, itemMeasureMap.get(original.pmCost()).value());
        assertEquals(280.8, itemMeasureMap.get(original.fullCost()).value());

        multipliedByK = aggregatedHoursCostItem.multipliedByKWithRound();
        assertEquals(30, itemMeasureMap.get(multipliedByK.analyseCost()).value());
        assertEquals(48, itemMeasureMap.get(multipliedByK.backendCost()).value());
        assertEquals(51, itemMeasureMap.get(multipliedByK.frontendCost()).value());
        assertEquals(99, itemMeasureMap.get(multipliedByK.devCost()).value());
        assertEquals(30, itemMeasureMap.get(multipliedByK.qaCost()).value());
        assertEquals(30, itemMeasureMap.get(multipliedByK.devOpsCost()).value());
        assertEquals(69, itemMeasureMap.get(multipliedByK.otherCost()).value());
        assertEquals(30, itemMeasureMap.get(multipliedByK.tmCost()).value());
        assertEquals(87, itemMeasureMap.get(multipliedByK.pmCost()).value());
        assertEquals(375, itemMeasureMap.get(multipliedByK.fullCost()).value());

        multipliedByKWithRound = aggregatedHoursCostItem.multipliedByKWithRound5();
        assertEquals(35, itemMeasureMap.get(multipliedByKWithRound.analyseCost()).value());
        assertEquals(55, itemMeasureMap.get(multipliedByKWithRound.backendCost()).value());
        assertEquals(55, itemMeasureMap.get(multipliedByKWithRound.frontendCost()).value());
        assertEquals(105, itemMeasureMap.get(multipliedByKWithRound.devCost()).value());
        assertEquals(35, itemMeasureMap.get(multipliedByKWithRound.qaCost()).value());
        assertEquals(35, itemMeasureMap.get(multipliedByKWithRound.devOpsCost()).value());
        assertEquals(75, itemMeasureMap.get(multipliedByKWithRound.otherCost()).value());
        assertEquals(35, itemMeasureMap.get(multipliedByKWithRound.tmCost()).value());
        assertEquals(90, itemMeasureMap.get(multipliedByKWithRound.pmCost()).value());
        assertEquals(410, itemMeasureMap.get(multipliedByKWithRound.fullCost()).value());


        var aggregatedMoneyWithoutNdsCostItemId = project.aggregateItems().aggregatedMoneyWithoutNdsCostItemId();
        var aggregatedMoneyWithoutNdsCostItem = project.projectItems().stream()
                .filter(item -> item.id().equals(aggregatedMoneyWithoutNdsCostItemId))
                .findFirst().get();
        itemMeasureMap = aggregatedMoneyWithoutNdsCostItem.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = aggregatedMoneyWithoutNdsCostItem.original();
        assertEquals(56250, itemMeasureMap.get(original.analyseCost()).value());
        assertEquals(90000, itemMeasureMap.get(original.backendCost()).value());
        assertEquals(97500, itemMeasureMap.get(original.frontendCost()).value());
        assertEquals(187500, itemMeasureMap.get(original.devCost()).value());
        assertEquals(56250, itemMeasureMap.get(original.qaCost()).value());
        assertEquals(56250, itemMeasureMap.get(original.devOpsCost()).value());
        assertEquals(127500, itemMeasureMap.get(original.otherCost()).value());
        assertEquals(56250, itemMeasureMap.get(original.tmCost()).value());
        assertEquals(162000, itemMeasureMap.get(original.pmCost()).value());
        assertEquals(702000, itemMeasureMap.get(original.fullCost()).value());

        multipliedByK = aggregatedMoneyWithoutNdsCostItem.multipliedByKWithRound();
        assertEquals(75000, itemMeasureMap.get(multipliedByK.analyseCost()).value());
        assertEquals(120000, itemMeasureMap.get(multipliedByK.backendCost()).value());
        assertEquals(127500, itemMeasureMap.get(multipliedByK.frontendCost()).value());
        assertEquals(247500, itemMeasureMap.get(multipliedByK.devCost()).value());
        assertEquals(75000, itemMeasureMap.get(multipliedByK.qaCost()).value());
        assertEquals(75000, itemMeasureMap.get(multipliedByK.devOpsCost()).value());
        assertEquals(172500, itemMeasureMap.get(multipliedByK.otherCost()).value());
        assertEquals(75000, itemMeasureMap.get(multipliedByK.tmCost()).value());
        assertEquals(217500, itemMeasureMap.get(multipliedByK.pmCost()).value());
        assertEquals(937500, itemMeasureMap.get(multipliedByK.fullCost()).value());

        multipliedByKWithRound = aggregatedMoneyWithoutNdsCostItem.multipliedByKWithRound5();
        assertEquals(87500, itemMeasureMap.get(multipliedByKWithRound.analyseCost()).value());
        assertEquals(137500, itemMeasureMap.get(multipliedByKWithRound.backendCost()).value());
        assertEquals(137500, itemMeasureMap.get(multipliedByKWithRound.frontendCost()).value());
        assertEquals(262500, itemMeasureMap.get(multipliedByKWithRound.devCost()).value());
        assertEquals(87500, itemMeasureMap.get(multipliedByKWithRound.qaCost()).value());
        assertEquals(87500, itemMeasureMap.get(multipliedByKWithRound.devOpsCost()).value());
        assertEquals(187500, itemMeasureMap.get(multipliedByKWithRound.otherCost()).value());
        assertEquals(87500, itemMeasureMap.get(multipliedByKWithRound.tmCost()).value());
        assertEquals(225000, itemMeasureMap.get(multipliedByKWithRound.pmCost()).value());
        assertEquals(1025000, itemMeasureMap.get(multipliedByKWithRound.fullCost()).value());


        var aggregatedMoneyWithNds20CostItemId = project.aggregateItems().aggregatedMoneyWithNds20CostItemId();
        var aggregatedMoneyWithNds20CostItem = project.projectItems().stream()
                .filter(item -> item.id().equals(aggregatedMoneyWithNds20CostItemId))
                .findFirst().get();
        itemMeasureMap = aggregatedMoneyWithNds20CostItem.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = aggregatedMoneyWithNds20CostItem.original();
        assertEquals(67500, itemMeasureMap.get(original.analyseCost()).value());
        assertEquals(108000, itemMeasureMap.get(original.backendCost()).value());
        assertEquals(117000, itemMeasureMap.get(original.frontendCost()).value());
        assertEquals(225000, itemMeasureMap.get(original.devCost()).value());
        assertEquals(67500, itemMeasureMap.get(original.qaCost()).value());
        assertEquals(67500, itemMeasureMap.get(original.devOpsCost()).value());
        assertEquals(153000, itemMeasureMap.get(original.otherCost()).value());
        assertEquals(67500, itemMeasureMap.get(original.tmCost()).value());
        assertEquals(194400, itemMeasureMap.get(original.pmCost()).value());
        assertEquals(842400, itemMeasureMap.get(original.fullCost()).value());

        multipliedByK = aggregatedMoneyWithNds20CostItem.multipliedByKWithRound();
        assertEquals(90000, itemMeasureMap.get(multipliedByK.analyseCost()).value());
        assertEquals(144000, itemMeasureMap.get(multipliedByK.backendCost()).value());
        assertEquals(153000, itemMeasureMap.get(multipliedByK.frontendCost()).value());
        assertEquals(297000, itemMeasureMap.get(multipliedByK.devCost()).value());
        assertEquals(90000, itemMeasureMap.get(multipliedByK.qaCost()).value());
        assertEquals(90000, itemMeasureMap.get(multipliedByK.devOpsCost()).value());
        assertEquals(207000, itemMeasureMap.get(multipliedByK.otherCost()).value());
        assertEquals(90000, itemMeasureMap.get(multipliedByK.tmCost()).value());
        assertEquals(261000, itemMeasureMap.get(multipliedByK.pmCost()).value());
        assertEquals(1125000, itemMeasureMap.get(multipliedByK.fullCost()).value());

        multipliedByKWithRound = aggregatedMoneyWithNds20CostItem.multipliedByKWithRound5();
        assertEquals(105000, itemMeasureMap.get(multipliedByKWithRound.analyseCost()).value());
        assertEquals(165000, itemMeasureMap.get(multipliedByKWithRound.backendCost()).value());
        assertEquals(165000, itemMeasureMap.get(multipliedByKWithRound.frontendCost()).value());
        assertEquals(315000, itemMeasureMap.get(multipliedByKWithRound.devCost()).value());
        assertEquals(105000, itemMeasureMap.get(multipliedByKWithRound.qaCost()).value());
        assertEquals(105000, itemMeasureMap.get(multipliedByKWithRound.devOpsCost()).value());
        assertEquals(225000, itemMeasureMap.get(multipliedByKWithRound.otherCost()).value());
        assertEquals(105000, itemMeasureMap.get(multipliedByKWithRound.tmCost()).value());
        assertEquals(270000, itemMeasureMap.get(multipliedByKWithRound.pmCost()).value());
        assertEquals(1230000, itemMeasureMap.get(multipliedByKWithRound.fullCost()).value());
    }

    @Test
    void calculateOriginalDevWithManualBackendMeasure() {
        var project = projectService.create(new CostProject(genId(), "Test project with backend manual measure"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        //backend + frontend
        assertEquals(3, measures.get(original.devCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        //child backend + child frontend
        assertEquals(3, measures.get(original.devCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();

        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        //parent backend + parent frontend
        assertEquals(12, measures.get(original.devCost()).value());
    }

    @Test
    void calculateOriginalDevWithManualFrontendMeasure() {
        var project = projectService.create(new CostProject(genId(), "Test project with frontend manual measure"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        //backend + frontend
        assertEquals(3, measures.get(original.devCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().frontendCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        //child backend + child frontend
        assertEquals(3, measures.get(original.devCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();

        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(10, measures.get(original.frontendCost()).value());
        //parent backend + parent frontend
        assertEquals(11, measures.get(original.devCost()).value());
    }

    @Test
    void calculateOriginalQaWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with manual measures"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        //dev * 0.3
        assertEquals(0.9, measures.get(original.qaCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.qaCost()).value());

        //DEV MANUAL
        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().devCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.qaCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(10, measures.get(original.devCost()).value());
        assertEquals(3, measures.get(original.qaCost()).value());


        //BACKEND MANUAL
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.qaCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        assertEquals(3.6, measures.get(original.qaCost()).value());


        //FRONTEND MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().frontendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.qaCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(10, measures.get(original.frontendCost()).value());
        assertEquals(11, measures.get(original.devCost()).value());
        assertEquals(3.3, measures.get(original.qaCost()).value());
    }

    @Test
    void calculateOriginalAnalyseWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with manual measures"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        //dev * 0.3
        assertEquals(0.9, measures.get(original.analyseCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.analyseCost()).value());

        //DEV MANUAL
        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().devCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.analyseCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(10, measures.get(original.devCost()).value());
        assertEquals(3, measures.get(original.analyseCost()).value());


        //BACKEND MANUAL
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.analyseCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        assertEquals(3.6, measures.get(original.analyseCost()).value());


        //FRONTEND MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().frontendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.analyseCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(10, measures.get(original.frontendCost()).value());
        assertEquals(11, measures.get(original.devCost()).value());
        assertEquals(3.3, measures.get(original.analyseCost()).value());
    }

    @Test
    void calculateOriginalDevopsWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with manual measures"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        //dev * 0.3
        assertEquals(0.9, measures.get(original.devOpsCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.devOpsCost()).value());

        //DEV MANUAL
        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().devCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.devOpsCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(10, measures.get(original.devCost()).value());
        assertEquals(3, measures.get(original.devOpsCost()).value());


        //BACKEND MANUAL
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.devOpsCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        assertEquals(3.6, measures.get(original.devOpsCost()).value());


        //FRONTEND MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().frontendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.devOpsCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(10, measures.get(original.frontendCost()).value());
        assertEquals(11, measures.get(original.devCost()).value());
        assertEquals(3.3, measures.get(original.devOpsCost()).value());
    }

    @Test
    void calculateOriginalTmWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with manual measures"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        //dev * 0.3
        assertEquals(0.9, measures.get(original.tmCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.tmCost()).value());

        //DEV MANUAL
        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().devCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.tmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(10, measures.get(original.devCost()).value());
        assertEquals(3, measures.get(original.tmCost()).value());


        //BACKEND MANUAL
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.tmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        assertEquals(3.6, measures.get(original.tmCost()).value());


        //FRONTEND MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().frontendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.tmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(10, measures.get(original.frontendCost()).value());
        assertEquals(11, measures.get(original.devCost()).value());
        assertEquals(3.3, measures.get(original.tmCost()).value());
    }

    @Test
    void calculateOriginalPmWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with manual measures"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.qaCost()).value());
        assertEquals(0.9, measures.get(original.analyseCost()).value());
        assertEquals(0.9, measures.get(original.devOpsCost()).value());
        assertEquals(0.9, measures.get(original.tmCost()).value());
        assertEquals(1.98, measures.get(original.pmCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(0.9, measures.get(original.qaCost()).value());
        assertEquals(0.9, measures.get(original.analyseCost()).value());
        assertEquals(0.9, measures.get(original.devOpsCost()).value());
        assertEquals(0.9, measures.get(original.tmCost()).value());
        assertEquals(1.98, measures.get(original.pmCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        //BACKEND MANUAL
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(1.98, measures.get(original.pmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        assertEquals(7.92, measures.get(original.pmCost()).value());


        //FRONTEND MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().frontendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(1.98, measures.get(original.pmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(10, measures.get(original.frontendCost()).value());
        assertEquals(11, measures.get(original.devCost()).value());
        assertEquals(7.26, measures.get(original.pmCost()).value());


        //DEV MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().devCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(1.98, measures.get(original.pmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(10, measures.get(original.devCost()).value());
        assertEquals(6.6, measures.get(original.pmCost()).value());


        //QA MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().qaCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(1.98, measures.get(original.pmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(10, measures.get(original.qaCost()).value());
        assertEquals(4.71, measures.get(original.pmCost()).value());


        //ANALYSE MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().qaCost(), 0d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().analyseCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(1.98, measures.get(original.pmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(10, measures.get(original.analyseCost()).value());
        assertEquals(4.71, measures.get(original.pmCost()).value());


        //DEVOPS MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().qaCost(), 0d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().analyseCost(), 0d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().devOpsCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(1.98, measures.get(original.pmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(10, measures.get(original.devOpsCost()).value());
        assertEquals(4.71, measures.get(original.pmCost()).value());


        //TM MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().qaCost(), 0d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().analyseCost(), 0d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devOpsCost(), 0d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().tmCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(1.98, measures.get(original.pmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(10, measures.get(original.tmCost()).value());
        assertEquals(4.71, measures.get(original.pmCost()).value());
    }

    @Test
    void calculateOriginalFullWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with manual measures"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(8.58, measures.get(original.fullCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(8.58, measures.get(original.fullCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        //BACKEND MANUAL
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(8.58, measures.get(original.fullCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        assertEquals(34.32, measures.get(original.fullCost()).value());


        //FRONTEND MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().frontendCost(), 10d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(8.58, measures.get(original.fullCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(10, measures.get(original.frontendCost()).value());
        assertEquals(11, measures.get(original.devCost()).value());
        assertEquals(31.46, measures.get(original.fullCost()).value());


        //DEV MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().devCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(8.58, measures.get(original.fullCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(10, measures.get(original.devCost()).value());
        assertEquals(28.60, measures.get(original.fullCost()).value());


        //QA MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().qaCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(8.58, measures.get(original.fullCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(10, measures.get(original.qaCost()).value());
        assertEquals(20.41, measures.get(original.fullCost()).value());


        //ANALYSE MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().qaCost(), 0d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().analyseCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(8.58, measures.get(original.fullCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(10, measures.get(original.analyseCost()).value());
        assertEquals(20.41, measures.get(original.fullCost()).value());


        //DEVOPS MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().qaCost(), 0d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().analyseCost(), 0d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().devOpsCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(8.58, measures.get(original.fullCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(10, measures.get(original.devOpsCost()).value());
        assertEquals(20.41, measures.get(original.fullCost()).value());


        //TM MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().qaCost(), 0d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().analyseCost(), 0d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devOpsCost(), 0d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().tmCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(8.58, measures.get(original.fullCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(10, measures.get(original.tmCost()).value());
        assertEquals(20.41, measures.get(original.fullCost()).value());


        //TM MANUAL
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().frontendCost(), 2d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devCost(), 3d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().qaCost(), 0d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().analyseCost(), 0d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().devOpsCost(), 0d);
        measureService.defineAutoMeasure(projectId, parent.id(), parent.original().tmCost(), 0d);
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().pmCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(8.58, measures.get(original.fullCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        assertEquals(10, measures.get(original.pmCost()).value());
        assertEquals(16.60, measures.get(original.fullCost()).value());
    }

    @Test
    void calculateRoundDevWithManualBackendMeasure() {
        var project = projectService.create(new CostProject(genId(), "Test project with backend manual measure"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        var round = childCalculated.multipliedByKWithRound();
        assertEquals(4, measures.get(round.devCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(4, measures.get(round.devCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = childCalculated.multipliedByKWithRound();
        assertEquals(4, measures.get(round.devCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();

        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(16, measures.get(round.devCost()).value());
    }

    @Test
    void calculateRoundQaWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with backend manual measure"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        var round = childCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.qaCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.qaCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = childCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.qaCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();

        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(5, measures.get(round.qaCost()).value());
    }

    @Test
    void calculateRoundAnalyseWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with backend manual measure"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        var round = childCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.analyseCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.analyseCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = childCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.analyseCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();

        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(5, measures.get(round.analyseCost()).value());
    }

    @Test
    void calculateRoundDevopsWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with backend manual measure"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        var round = childCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.devOpsCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.devOpsCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = childCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.devOpsCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();

        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(5, measures.get(round.devOpsCost()).value());
    }

    @Test
    void calculateRoundTmWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with backend manual measure"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        var round = childCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.tmCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.tmCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = childCalculated.multipliedByKWithRound();
        assertEquals(2, measures.get(round.tmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();

        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(5, measures.get(round.tmCost()).value());
    }

    @Test
    void calculateRoundPmWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with backend manual measure"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        var round = childCalculated.multipliedByKWithRound();
        assertEquals(3, measures.get(round.pmCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(3, measures.get(round.pmCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = childCalculated.multipliedByKWithRound();
        assertEquals(3, measures.get(round.pmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();

        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(11, measures.get(round.pmCost()).value());
    }

    @Test
    void calculateRoundFullWithManualDependencyMeasures() {
        var project = projectService.create(new CostProject(genId(), "Test project with backend manual measure"),
                false);
        var projectId = project.id();

        var parentId = genId();
        var parent = new CostProjectItem(parentId, "Parent");
        projectItemService.createCostItem(projectId, parent);

        var childId = genId();
        var child = new CostProjectItem(childId, "Child");
        project = projectItemService.createCostSubItem(projectId, parent.id(), child);
        child = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(childId))
                .findFirst().get();
        measureService.defineAutoMeasure(projectId, child.id(), child.original().backendCost(), 1d);
        measureService.defineAutoMeasure(projectId, child.id(), child.original().frontendCost(), 2d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        var childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        var measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        var original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        var round = childCalculated.multipliedByKWithRound();
        assertEquals(3, measures.get(round.pmCost()).value());

        var parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();
        //from child
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(3, measures.get(round.pmCost()).value());

        parent = project.projectItems().stream()
                .filter(projectItem -> projectItem.id().equals(parentId))
                .findFirst().get();
        measureService.defineManualMeasure(projectId, parent.id(), parent.original().backendCost(), 10d);

        project = projectMongoRepository.findById(projectId).get();
        project = calculation.calculate(project);

        childCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(childId))
                .findFirst().get();
        measures = childCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = childCalculated.original();
        assertEquals(1, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(3, measures.get(original.devCost()).value());
        round = childCalculated.multipliedByKWithRound();
        assertEquals(3, measures.get(round.pmCost()).value());

        parentCalculated = project.projectItems().stream()
                .filter(item -> item.id().equals(parentId))
                .findFirst().get();
        measures = parentCalculated.measures().stream()
                .collect(Collectors.toMap(CostProjectItemMeasure::id, m -> m));
        original = parentCalculated.original();

        assertEquals(10, measures.get(original.backendCost()).value());
        assertEquals(2, measures.get(original.frontendCost()).value());
        assertEquals(12, measures.get(original.devCost()).value());
        round = parentCalculated.multipliedByKWithRound();
        assertEquals(11, measures.get(round.pmCost()).value());
    }

    private String genId() {
        return new ObjectId().toString();
    }

}
