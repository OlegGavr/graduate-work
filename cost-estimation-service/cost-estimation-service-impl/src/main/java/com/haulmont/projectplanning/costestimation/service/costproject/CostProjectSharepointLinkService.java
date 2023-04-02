package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.importer.CostProjectImportInfo;
import com.haulmont.projectplanning.costestimation.mongo.dto.sharepoint.CostProjectSharepointLink;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectImportInfoMongoRepository;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectSharepointLinkMongoRepository;
import com.haulmont.projectplanning.costestimation.sharepoint.SharepointFileApiAdapter;
import org.springframework.stereotype.Component;

import static com.haulmont.projectplanning.costestimation.service.costproject.CostProjectSharepointLinkService.SharePointLinkAvailabilityStatus.AVAILABLE;
import static com.haulmont.projectplanning.costestimation.service.costproject.CostProjectSharepointLinkService.SharePointLinkAvailabilityStatus.NOT_AVAILABLE;

@Component
public class CostProjectSharepointLinkService {

    private CostProjectImportService costProjectImportService;

    private CostProjectMongoRepository costProjectMongoRepository;

    private CostProjectImportInfoMongoRepository costProjectImportInfoMongoRepository;

    private CostProjectSharepointLinkMongoRepository costProjectSharepointLinkMongoRepository;

    private SharepointFileApiAdapter sharepointFileApiAdapter;

    public CostProjectSharepointLinkService(CostProjectImportService costProjectImportService,
                                            CostProjectMongoRepository costProjectMongoRepository,
                                            CostProjectImportInfoMongoRepository costProjectImportInfoMongoRepository,
                                            CostProjectSharepointLinkMongoRepository costProjectSharepointLinkMongoRepository,
                                            SharepointFileApiAdapter sharepointFileApiAdapter) {
        this.costProjectImportService = costProjectImportService;
        this.costProjectMongoRepository = costProjectMongoRepository;
        this.costProjectImportInfoMongoRepository = costProjectImportInfoMongoRepository;
        this.costProjectSharepointLinkMongoRepository = costProjectSharepointLinkMongoRepository;
        this.sharepointFileApiAdapter = sharepointFileApiAdapter;
    }

    public SharepointLinkStatus checkSharePointLinkStatus(String projectId) {
        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        var costProjectImportInfo = costProjectImportInfoMongoRepository
                .findByCostProjectId(projectId).orElseGet(CostProjectImportInfo::new);

        var costProjectSharepointLink = costProjectSharepointLinkMongoRepository
                .findByCostProjectId(projectId).orElseGet(CostProjectSharepointLink::new);

        var sharepointItem = sharepointFileApiAdapter
                .requestItemBySharedLink(costProject.sharePointLink());

        if (sharepointItem == null) {
            return new SharepointLinkStatus(NOT_AVAILABLE, null);
        }

        // check link isn't changed
        if (! costProject.sharePointLink().equals(costProjectSharepointLink.sharePointLink())) {
            return new SharepointLinkStatus(AVAILABLE, true);
        }

        //noinspection ConstantConditions
        if (sharepointItem.lastModifiedDateTime.toEpochSecond()
                != costProjectImportInfo.lastModified()) {

            return new SharepointLinkStatus(AVAILABLE, true);
        }

        return new SharepointLinkStatus(AVAILABLE, false);
    }

    public CostProject updateBySharePointLink(String projectId) {
        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        var link = costProject.sharePointLink();
        var driveItem = sharepointFileApiAdapter.requestItemBySharedLink(link);
        var contentStream = sharepointFileApiAdapter.requestContentBySharedLink(link);

        var actualCostProject = costProjectImportService
                .autoImportBySharePointBaseItem(projectId, driveItem, contentStream);

        var costProjectSharepointLink = new CostProjectSharepointLink(projectId, link);
        costProjectSharepointLinkMongoRepository.save(costProjectSharepointLink);

        return actualCostProject;
    }

    public record SharepointLinkStatus(
            SharePointLinkAvailabilityStatus availabilityStatus,
            Boolean needToUpdate
    ) {}

    public enum SharePointLinkAvailabilityStatus {
        AVAILABLE, NOT_AVAILABLE
    }

}
