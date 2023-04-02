package com.haulmont.projectplanning.costestimation.repository.mongo;

import com.haulmont.projectplanning.costestimation.mongo.dto.sharepoint.CostProjectSharepointLink;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CostProjectSharepointLinkMongoRepository extends MongoRepository<CostProjectSharepointLink, String> {
    Optional<CostProjectSharepointLink> findByCostProjectId(String costProjectId);

}
