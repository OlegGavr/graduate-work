package com.haulmont.projectplanning.costestimation.repository.mongo.sharepoint;

import com.haulmont.projectplanning.costestimation.mongo.dto.sharepoint.CostProjectSharepointLink;

import java.util.Optional;

public interface CostProjectSharepointLinkCustomMongoRepository {

    Optional<CostProjectSharepointLink> findByCostProjectId(String costProjectId);
}
