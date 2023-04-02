package com.haulmont.projectplanning.costestimation.importer.excel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ExcelPlanWithCostImporterTest {

    @Autowired
    ObjectFactory<ExcelPlanWithCostImporter> hseExcelImporterFactory;

    // test for data
    @Test
    void checkExcelImporting() throws IOException {
        // given
        String resourceName = "FullHseExample.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        hseExcelImporterFactory.getObject().doImport(resourceAsStream);

    }

    @Test
    void checkExcelAnotherTemplateImporting() throws IOException {
        // given
        String resourceName = "AnotherTemplate.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        hseExcelImporterFactory.getObject().doImport(resourceAsStream);

    }

    @Test
    void checkRiskExcelImporting() throws IOException {
        // given
        String resourceName = "FullHseExample.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        var project = hseExcelImporterFactory.getObject().doImport(resourceAsStream);

        var risk = project.projectRisk();

        assertEquals(0.3, risk.defaultRisk());
        assertEquals(0.3, risk.qa());
        assertEquals(0.35, risk.ba());
        assertEquals(0.1, risk.devOps());
        assertEquals(0.1, risk.tm());
        assertEquals(0.15, risk.pm());
    }
}
