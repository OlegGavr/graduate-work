package com.haulmont.projectplanning.costestimation.importer;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectItemService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import com.haulmont.projectplanning.exception.CostEstimationException;

import java.io.InputStream;

public abstract class AbstractCostProjectImporter implements CostProjectImporter {

    private Calculation calculation;

    private CostProjectService costProjectService;

    private CostProjectItemService costProjectItemService;

    private CostProjectMongoRepository costProjectMongoRepository;

    public AbstractCostProjectImporter(Calculation calculation, CostProjectService costProjectService,
                                       CostProjectItemService costProjectItemService, CostProjectMongoRepository costProjectMongoRepository) {
        this.calculation = calculation;
        this.costProjectService = costProjectService;
        this.costProjectItemService = costProjectItemService;
        this.costProjectMongoRepository = costProjectMongoRepository;
    }

    @Override
    public CostProject doImport(InputStream inputStream) {
        return this.doImport(inputStream, new NamedImportParamsImpl());
    }

    @Override
    public CostProject doImport(InputStream inputStream, NamedImportParams params) {
        var costProject = costProjectService.create(false);

        try {
            return doImport(costProject, inputStream, params);
        } catch (CostEstimationException e) {
            costProjectMongoRepository.deleteById(costProject.id());

            throw e;
        }
    }

    @Override
    public CostProject doImport(String costProjectId, InputStream inputStream) {
        return doImport(costProjectId, inputStream, new NamedImportParamsImpl());
    }

    @Override
    public CostProject doImport(String costProjectId, InputStream inputStream, NamedImportParams params) {
        var costProject = costProjectMongoRepository.findById(costProjectId).orElseThrow();

        return doImport(costProject, inputStream, params);
    }

    @Override
    public CostProject doImport(CostProject costProject, InputStream inputStream) {
        return doImport(costProject, inputStream, new NamedImportParamsImpl());
    }

    @Override
    public CostProject doImport(CostProject costProject, InputStream inputStream, NamedImportParams params) {
        var actualCostProject = switch (params.applyStrategy()) {
            case APPEND -> costProject;
            case REPLACE -> costProjectItemService.deleteAllCostItems(costProject.id());
            case OVERLAY -> costProject;
        };

        actualCostProject = this.internalDoImport(actualCostProject, inputStream, params);

        if (params.recalculate()) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    protected abstract CostProject internalDoImport(CostProject costProject, InputStream inputStream, NamedImportParams params);
}
