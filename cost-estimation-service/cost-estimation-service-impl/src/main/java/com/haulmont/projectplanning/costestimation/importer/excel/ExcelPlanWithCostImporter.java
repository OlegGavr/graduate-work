package com.haulmont.projectplanning.costestimation.importer.excel;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.importer.AbstractCostProjectImporter;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectItemService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import com.haulmont.projectplanning.exception.importer.excel.ExcelImporterIoException;
import io.vavr.control.Try;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.InputStream;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@SuppressWarnings("UnusedReturnValue")
@Order(10)
@Component
@Scope(SCOPE_PROTOTYPE)
public class ExcelPlanWithCostImporter extends AbstractCostProjectImporter {

    private ObjectFactory<ExcelCostImporter> excelCostImporter;

    private ObjectFactory<ExcelPlanImporter> excelPlanImporter;

    public ExcelPlanWithCostImporter(Calculation calculation,
                                     CostProjectService costProjectService,
                                     CostProjectItemService costProjectItemService,
                                     CostProjectMongoRepository costProjectMongoRepository,
                                     ObjectFactory<ExcelCostImporter> excelCostImporter,
                                     ObjectFactory<ExcelPlanImporter> excelPlanImporter) {

        super(calculation, costProjectService, costProjectItemService, costProjectMongoRepository);
        this.excelCostImporter = excelCostImporter;
        this.excelPlanImporter = excelPlanImporter;
    }


    @Override
    public CostProject internalDoImport(CostProject costProject, InputStream inputStream, NamedImportParams params) {
        var excelPlanImporterObject = excelPlanImporter.getObject();
        var excelCostImporterObject = excelCostImporter.getObject();

        var workbook = Try.of(() -> WorkbookFactory.create(new BufferedInputStream(inputStream)))
                .getOrElseThrow(e -> new ExcelImporterIoException(e));

        var overriddenApplyStrategy = new NamedImportParamsImpl(params)
                .applyStrategy(ApplyStrategy.OVERLAY);

        var actualCostProject = excelPlanImporterObject.doImport(costProject, workbook, overriddenApplyStrategy);
        actualCostProject = excelCostImporterObject.doImport(costProject, workbook, overriddenApplyStrategy);

        return actualCostProject;
    }

    @Override
    public Boolean isApplicable(InputStream inputStream) {
        var lWorkbook = Try.of(() -> WorkbookFactory.create(inputStream))
                .getOrElseThrow(e -> new ExcelImporterIoException(e));

        var applicablePlan = excelPlanImporter.getObject().isApplicable(lWorkbook);
        var applicableCost = excelCostImporter.getObject().isApplicable(lWorkbook);

        return applicablePlan && applicableCost;
    }
}
