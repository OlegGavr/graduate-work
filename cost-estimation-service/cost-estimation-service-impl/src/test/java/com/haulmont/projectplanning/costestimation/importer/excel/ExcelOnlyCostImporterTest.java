package com.haulmont.projectplanning.costestimation.importer.excel;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.importer.CostProjectImporter.NamedImportParamsImpl;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectMeasureService;
import com.haulmont.projectplanning.costestimation.template.registry.CostProjectTemplateRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@SpringBootTest
class ExcelOnlyCostImporterTest {

    @Autowired
    ObjectFactory<ExcelOnlyCostImporter> hseExcelImporterFactory;

    @Autowired
    CostProjectMeasureService measureService;

    @Autowired
    CostProjectMongoRepository projectMongoRepository;

    @Autowired
    Calculation calculation;

    @Autowired
    CostProjectTemplateRegistry costProjectTemplateRegistry;

    @Autowired
    private ResourceLoader resourceLoader;

    // test for data
    @Test
    void checkExcelImporting() {
        // given
        String resourceName = "FullHseExample.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        hseExcelImporterFactory.getObject().doImport(resourceAsStream);

    }

    @Test
    void checkExcelAnotherTemplateImporting() {
        // given
        String resourceName = "AnotherTemplate.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        hseExcelImporterFactory.getObject().doImport(resourceAsStream);

    }

    @Test
    void checkEstimationSampleImporting() {
        // given
        String resourceName = "EstimationSample.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        hseExcelImporterFactory.getObject().doImport(resourceAsStream);

    }

    @Test
    void checkEstimationRuTemplateWithSameFeatureName() {
        // given
        String resourceName = "BasicFixPriceRu.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        hseExcelImporterFactory.getObject().doImport(resourceAsStream,
                new NamedImportParamsImpl().recalculate(false));

    }

    @Test
    void checkEstimationRuTemplateImporting() throws IOException {
        // given
        var ruBasicTemplatePath = costProjectTemplateRegistry
                .findTemplateById("62b5d08f6eff973dee4bf1e4").path();
        var resourceAsStream = resourceLoader
                .getResource(ruBasicTemplatePath).getInputStream();

        // when
        hseExcelImporterFactory.getObject().doImport(resourceAsStream);

    }

    @Test
    void checkEstimationEnTemplateImporting() throws IOException {
        // given
        var ruBasicTemplatePath = costProjectTemplateRegistry
                .findTemplateById("62b5d0966eff973dee4bf1e5").path();
        var resourceAsStream = resourceLoader
                .getResource(ruBasicTemplatePath).getInputStream();

        // when
        hseExcelImporterFactory.getObject().doImport(resourceAsStream);
    }

    @Test
    void checkTimeForMiddleProject() {
        // given
        String resourceName = "FullHseExample.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        var project = hseExcelImporterFactory.getObject().doImport(resourceAsStream);

        calculation.calculate(project);

        project = projectMongoRepository.findById(project.id()).orElseThrow();

        var projectItem = project.projectItems().get(3);
        var measure = projectItem.measures().stream()
                .filter(m -> m.id().equals(projectItem.original().frontendCost()))
                .findFirst().orElseThrow();

        project = measureService.defineManualMeasure(project.id(), projectItem.id(), measure.id(), 10d);

        calculation.calculate(project);
    }
}
