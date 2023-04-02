package com.haulmont.projectplanning.costestimation.exporter.excel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class ExcelCostExporterTest {

    @Autowired
    private ObjectFactory<ExcelCostExporter> excelCostExporter;

    //Just for export project
    @Test
    void exportProjectToExcel() {
        var costProjectId = "62b056fefdca71117579c1fb";
        try {
            var path = Paths.get("src/test/resources/CostPlanExported.xlsx");
            Files.deleteIfExists(path);
            var tempReportFile = Files.createFile(path);

            try (var os = new BufferedOutputStream(Files.newOutputStream(tempReportFile))) {
                excelCostExporter.getObject().doExport(costProjectId, os);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error during exporting project to Excel", e);
        }
    }
}
