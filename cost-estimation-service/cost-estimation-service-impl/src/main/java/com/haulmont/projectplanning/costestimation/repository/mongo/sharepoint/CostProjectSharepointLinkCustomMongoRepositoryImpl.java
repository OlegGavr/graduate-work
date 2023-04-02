package com.haulmont.projectplanning.costestimation.repository.mongo.sharepoint;

import com.haulmont.projectplanning.costestimation.mongo.dto.sharepoint.CostProjectSharepointLink;

import java.util.Optional;

public class CostProjectSharepointLinkCustomMongoRepositoryImpl implements CostProjectSharepointLinkCustomMongoRepository {
    @Override
    public Optional<CostProjectSharepointLink> findByCostProjectId(String costProjectId) {
        return Optional.empty();
    }
}
