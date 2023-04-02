package com.haulmont.projectplanning.costestimation.repository.mongo;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectVersion;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectVersionId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CostProjectVersionMongoRepository extends MongoRepository<CostProjectVersion, CostProjectVersionId> {
}
