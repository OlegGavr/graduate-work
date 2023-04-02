package com.haulmont.projectplanning.costestimation.api.controller;

import com.haulmont.projectplanning.costestimation.exporter.excel.ExcelCostExporter;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.organization.api.controller.CostProjectExportApi;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
public class CostProjectExportApiImpl implements CostProjectExportApi {

    private CostProjectMongoRepository costProjectMongoRepository;
    private ObjectFactory<ExcelCostExporter> excelCostExporter;

    public CostProjectExportApiImpl(CostProjectMongoRepository costProjectMongoRepository,
                                    ObjectFactory<ExcelCostExporter> excelCostExporter) {
        this.costProjectMongoRepository = costProjectMongoRepository;
        this.excelCostExporter = excelCostExporter;
    }

    @Override
    public ResponseEntity<Resource> excelExportHaulmontCostProject(String projectId) {

        var costProject = costProjectMongoRepository.findById(projectId).orElseThrow();

        try {
            var tempReportFile = Files.createTempFile(null, null);

            try (var os = new BufferedOutputStream(Files.newOutputStream(tempReportFile))) {
                excelCostExporter.getObject().doExport(projectId, os);
            }

            var fileSize = Files.size(tempReportFile);

            return ResponseEntity.ok()
                    .headers(httpHeaders -> {
                        var fileName = costProject.name() + ".xlsx";
                        httpHeaders.setContentDisposition(
                                ContentDisposition.builder("attachment")
                                        .filename(fileName, StandardCharsets.UTF_8)
                                        .build());
                        httpHeaders.setContentLength(fileSize);
                    })
                    .body(new FileSystemResource(tempReportFile));


        } catch (IOException e) {
            throw new RuntimeException("Error during exporting project to Excel", e);
        }
    }
}
