package com.haulmont.projectplanning.costestimation.repository.mongo.costproject;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;

public interface CostProjectCustomMongoRepository {

    CostProject updateName(String id, String projectName);

    CostProject updateMoneyPerHour(String id, Integer moneyPerHour);

    CostProject updateSharePointLink(String projectId, String sharePointLink);
}
