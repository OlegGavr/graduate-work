package com.haulmont.projectplanning.costestimation.importer.auto;

import com.haulmont.projectplanning.costestimation.importer.CostProjectImporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AutoCostProjectImporterTest {

    @Autowired
    ObjectFactory<AutoCostProjectImporter> autoCostProjectImporter;

    @Test
    void checkExcelImporting() {
        // given
        String resourceName = "FullHseExample.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        autoCostProjectImporter.getObject()
                .doImport(resourceAsStream);

    }


    @Test
    void checkEstimationRuTemplateWithSameFeatureName() {
        // given
        String resourceName = "BasicFixPriceRu.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        autoCostProjectImporter.getObject().doImport(resourceAsStream,
                new CostProjectImporter.NamedImportParamsImpl().recalculate(false));

    }
}