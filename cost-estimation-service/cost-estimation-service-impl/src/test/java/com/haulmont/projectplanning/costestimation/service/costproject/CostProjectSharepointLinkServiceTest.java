package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CostProjectSharepointLinkServiceTest {

    @Autowired
    CostProjectService costProjectService;

    @Autowired
    CostProjectMongoRepository costProjectMongoRepository;

    @Autowired
    CostProjectSharepointLinkService costProjectSharepointLinkService;

    @Test
    void checkStatusByLink() {
        var costProjectId = "62d165c26731b31b81e0c9cf";

        var costProject = costProjectService.defineSharePointLink(costProjectId,
                "https://haulmonts.sharepoint.com/:x:/s/Custom_Solutions/EZQBmgTUUdFAoL6o5Bo9GDsBJgGsK_4JXjGZ5BLOnMdGCg?e=lngMHq");

        var sharepointLinkStatus = costProjectSharepointLinkService
                .checkSharePointLinkStatus(costProjectId);

        if (sharepointLinkStatus.needToUpdate()) {
            costProject = costProjectSharepointLinkService.updateBySharePointLink(costProjectId);
        }

        System.out.println(sharepointLinkStatus);
    }

}