package com.haulmont.projectplanning.costestimation.importer;


import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectItemService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

public abstract class AbstractTransactionalCostProjectImporter extends AbstractCostProjectImporter {

    public AbstractTransactionalCostProjectImporter(Calculation calculation, CostProjectService costProjectService, CostProjectItemService costProjectItemService, CostProjectMongoRepository costProjectMongoRepository) {
        super(calculation, costProjectService, costProjectItemService, costProjectMongoRepository);
    }

    @Override
    @Transactional // performance purpose
    public CostProject doImport(InputStream inputStream) {
        return super.doImport(inputStream);
    }

    @Override
    @Transactional // performance purpose
    public CostProject doImport(InputStream inputStream, NamedImportParams params) {
        return super.doImport(inputStream, params);
    }

    @Override
    @Transactional // performance purpose
    public CostProject doImport(String costProjectId, InputStream inputStream) {
        return super.doImport(costProjectId, inputStream);
    }

    @Override
    @Transactional // performance purpose
    public CostProject doImport(String costProjectId, InputStream inputStream, NamedImportParams params) {
        return super.doImport(costProjectId, inputStream, params);
    }

    @Override
    @Transactional // performance purpose
    public CostProject doImport(CostProject costProject, InputStream inputStream) {
        return super.doImport(costProject, inputStream);
    }

    @Override
    @Transactional // performance purpose
    public CostProject doImport(CostProject costProject, InputStream inputStream, NamedImportParams params) {
        return super.doImport(costProject, inputStream, params);
    }
}
