package com.haulmont.projectplanning.costestimation.repository.mongo;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.repository.mongo.costproject.CostProjectCustomMongoRepository;
import com.haulmont.projectplanning.costestimation.repository.mongo.costproject.costitem.CostProjectItemCustomMongoRepository;
import com.haulmont.projectplanning.costestimation.repository.mongo.costproject.costitem.measure.CostProjectItemMeasureCustomMongoRepository;
import com.haulmont.projectplanning.costestimation.repository.mongo.costproject.globalorder.CostProjectGlobalOrderCustomMongoRepository;
import com.haulmont.projectplanning.costestimation.repository.mongo.costproject.risk.CostProjectRiskCustomMongoRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CostProjectMongoRepository extends MongoRepository<CostProject, String>,
        CostProjectCustomMongoRepository,
        CostProjectItemCustomMongoRepository,
        CostProjectGlobalOrderCustomMongoRepository,
        CostProjectItemMeasureCustomMongoRepository,
        CostProjectRiskCustomMongoRepository {
}
