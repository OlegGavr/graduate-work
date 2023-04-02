package com.haulmont.projectplanning.costestimation.repository.mongo;

import com.haulmont.projectplanning.costestimation.mongo.dto.importer.CostProjectImportInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CostProjectImportInfoMongoRepository extends MongoRepository<CostProjectImportInfo, String> {
    Optional<CostProjectImportInfo> findByCostProjectId(String projectId);
}
