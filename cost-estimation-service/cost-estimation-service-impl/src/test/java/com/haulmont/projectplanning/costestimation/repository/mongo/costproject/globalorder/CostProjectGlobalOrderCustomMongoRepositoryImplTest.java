package com.haulmont.projectplanning.costestimation.repository.mongo.costproject.globalorder;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectGlobalOrder;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectItemService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CostProjectGlobalOrderCustomMongoRepositoryImplTest {

    @Autowired
    CostProjectService costProjectService;

    @Autowired
    CostProjectItemService costProjectItemService;

    @Autowired
    CostProjectMongoRepository costProjectMongoRepository;


    @Test
    void checkThatAddingItemsToConcretePositionWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var l1Item1 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(new ObjectId().toString()));
        var l2Item1 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.projectItemId()));
        var l2Item2 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.projectItemId()));
        var l1Item2 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.parentItemIds().get(0)));

        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l1Item1);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l1Item2);

        // when
        var modifiedCostProject = costProjectMongoRepository
                .addAllGlobalOrdersToTheConcretePosition(
                        costProject.id(), List.of(l2Item1, l2Item2), 1);

        // then
        assertEquals(List.of(l1Item1, l2Item1, l2Item2, l1Item2), modifiedCostProject.globalOrder());
    }

    @Test
    void checkThatRemoveGlobalOrderByProjectItemIdWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var l1Item1 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(new ObjectId().toString()));
        var l1Item2 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.parentItemIds().get(0)));

        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l1Item1);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l1Item2);

        // when
        var modifiedCostProject = costProjectMongoRepository
                .deleteGlobalOrderByProjectItemId(
                        costProject.id(), l1Item1.projectItemId());

        // then
        assertEquals(List.of(l1Item2), modifiedCostProject.globalOrder());
    }

    @Test
    void checkThatRemoveGlobalOrderByParentsWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var l1Item1 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(new ObjectId().toString()));
        var l2Item1 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.projectItemId()));
        var l2Item2 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.projectItemId()));
        var l1Item2 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.parentItemIds().get(0)));
        var l2Item3 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item2.projectItemId()));
        var l2Item4 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item2.projectItemId()));

        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l1Item1);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item1);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item2);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l1Item2);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item3);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item4);

        // when
        var modifiedCostProject = costProjectMongoRepository
                .deleteAllGlobalOrdersByConcreteParentIn(
                        costProject.id(), l1Item1.projectItemId());

        // then
        assertEquals(List.of(l1Item1, l1Item2, l2Item3, l2Item4),
                modifiedCostProject.globalOrder());
    }

    @Test
    void checkThatRemoveGlobalOrderByProjectItemIdsWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var l1Item1 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(new ObjectId().toString()));
        var l2Item1 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.projectItemId()));
        var l2Item2 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.projectItemId()));
        var l1Item2 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.parentItemIds().get(0)));
        var l2Item3 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item2.projectItemId()));
        var l2Item4 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item2.projectItemId()));

        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l1Item1);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item1);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item2);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l1Item2);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item3);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item4);

        // when
        var modifiedCostProject = costProjectMongoRepository
                .deleteAllGlobalOrdersByProjectItemId(
                        costProject.id(), List.of(l1Item2.projectItemId(),
                                l2Item3.projectItemId(), l2Item4.projectItemId()));

        // then
        assertEquals(List.of(l1Item1, l2Item1, l2Item2), modifiedCostProject.globalOrder());
    }

    @Test
    void checkThatRemoveAllGlobalOrdersWorksAsWell() {
        // given
        var costProject = costProjectService.create(false);
        var l1Item1 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(new ObjectId().toString()));
        var l2Item1 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.projectItemId()));
        var l2Item2 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.projectItemId()));
        var l1Item2 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item1.parentItemIds().get(0)));
        var l2Item3 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item2.projectItemId()));
        var l2Item4 = new CostProjectGlobalOrder(new ObjectId().toString(), List.of(l1Item2.projectItemId()));

        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l1Item1);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item1);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item2);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l1Item2);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item3);
        costProjectMongoRepository.addGlobalOrderToTheEnd(costProject.id(), l2Item4);

        // when
        var modifiedCostProject = costProjectMongoRepository.deleteAllGlobalOrders(costProject.id());

        // then
        assertEquals(List.of(), modifiedCostProject.globalOrder());
    }
}