package com.haulmont.projectplanning.costestimation.repository.mongo.costproject.risk;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectRisk;

public interface CostProjectRiskCustomMongoRepository {

    CostProject updateCostProjectRisk(String projectId, CostProjectRisk costProjectRisk);

    CostProject updateDefaultRisk(String projectId, Double riskValue);

    CostProject updateDevRisk(String projectId, Double riskValue);

    CostProject updateQaRisk(String projectId, Double riskValue);

    CostProject updateBaRisk(String projectId, Double riskValue);

    CostProject updateDevOpsRisk(String projectId, Double riskValue);

    CostProject updateTmRisk(String projectId, Double riskValue);

    CostProject updatePmRisk(String projectId, Double riskValue);
}
