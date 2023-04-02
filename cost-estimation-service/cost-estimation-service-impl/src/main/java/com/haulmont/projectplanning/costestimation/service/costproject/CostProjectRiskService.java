package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectRisk;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CostProjectRiskService {

    private CostProjectMongoRepository costProjectMongoRepository;

    private Calculation calculation;

    public CostProjectRiskService(CostProjectMongoRepository costProjectMongoRepository,
                                  Calculation calculation) {
        this.costProjectMongoRepository = costProjectMongoRepository;
        this.calculation = calculation;
    }

    @Transactional
    public CostProject defineRisks(String costProjectId, CostProjectRisk template) {
        return this.defineRisks(costProjectId, template, true);
    }

    @Transactional
    public CostProject defineRisks(String costProjectId, CostProjectRisk template,
                                   Boolean recalculate) {

        var actualCostProject = costProjectMongoRepository
                .updateCostProjectRisk(costProjectId, template);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject defineBaRisk(String projectId, Double riskValue) {
        return this.defineBaRisk(projectId, riskValue, true);
    }

    @Transactional
    public CostProject defineBaRisk(String projectId, Double riskValue,
                                    Boolean recalculate) {
        var actualCostProject = costProjectMongoRepository
                .updateBaRisk(projectId, riskValue);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject defineDevRisk(String projectId, Double riskValue) {
        return this.defineDevRisk(projectId, riskValue, true);
    }

    @Transactional
    public CostProject defineDevRisk(String projectId, Double riskValue,
                                     Boolean recalculate) {
        var actualCostProject = costProjectMongoRepository
                .updateDevRisk(projectId, riskValue);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject defineDefaultRisk(String projectId, Double riskValue) {
        return this.defineDefaultRisk(projectId, riskValue, true);
    }

    @Transactional
    public CostProject defineDefaultRisk(String projectId, Double riskValue,
                                         Boolean recalculate) {
        var actualCostProject = costProjectMongoRepository
                .updateDefaultRisk(projectId, riskValue);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject defineDevOpsRisk(String projectId, Double riskValue) {

        return this.defineDevOpsRisk(projectId, riskValue, true);
    }

    @Transactional
    public CostProject defineDevOpsRisk(String projectId, Double riskValue,
                                        Boolean recalculate) {
        var actualCostProject = costProjectMongoRepository
                .updateDevOpsRisk(projectId, riskValue);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject definePmRisk(String projectId, Double riskValue) {
        return this.definePmRisk(projectId, riskValue, true);
    }

    @Transactional
    public CostProject definePmRisk(String projectId, Double riskValue,
                                    Boolean recalculate) {
        var actualCostProject = costProjectMongoRepository
                .updatePmRisk(projectId, riskValue);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject defineQaRisk(String projectId, Double riskValue) {
        return this.defineQaRisk(projectId, riskValue, true);
    }

    @Transactional
    public CostProject defineQaRisk(String projectId, Double riskValue,
                                    Boolean recalculate) {
        var actualCostProject = costProjectMongoRepository
                .updateQaRisk(projectId, riskValue);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Transactional
    public CostProject defineTmRisk(String projectId, Double riskValue) {
        return this.defineTmRisk(projectId, riskValue, true);
    }

    @Transactional
    public CostProject defineTmRisk(String projectId, Double riskValue,
                                    Boolean recalculate) {
        var actualCostProject = costProjectMongoRepository
                .updateTmRisk(projectId, riskValue);

        // recalculate
        if (recalculate) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }
}
