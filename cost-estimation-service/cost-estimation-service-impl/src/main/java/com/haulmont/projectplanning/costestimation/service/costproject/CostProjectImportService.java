package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.importer.auto.AutoCostProjectImporter;
import com.haulmont.projectplanning.costestimation.importer.excel.ExcelOnlyCostImporter;
import com.haulmont.projectplanning.costestimation.importer.excel.ExcelPlanImporter;
import com.haulmont.projectplanning.costestimation.importer.excel.ExcelPlanWithCostImporter;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.importer.CostProjectImportInfo;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectImportInfoMongoRepository;
import com.microsoft.graph.models.BaseItem;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class CostProjectImportService {

    private ObjectFactory<AutoCostProjectImporter> autoCostProjectImporter;
    private ObjectFactory<ExcelPlanImporter> excelPlanImporter;
    private ObjectFactory<ExcelOnlyCostImporter> excelOnlyCostImporter;
    private ObjectFactory<ExcelPlanWithCostImporter> excelPlanWithCostImporter;

    private CostProjectImportInfoMongoRepository costProjectImportInfoMongoRepository;

    public CostProjectImportService(ObjectFactory<AutoCostProjectImporter> autoCostProjectImporter,
                                    ObjectFactory<ExcelPlanImporter> excelPlanImporter,
                                    ObjectFactory<ExcelOnlyCostImporter> excelOnlyCostImporter,
                                    ObjectFactory<ExcelPlanWithCostImporter> excelPlanWithCostImporter,
                                    CostProjectImportInfoMongoRepository costProjectImportInfoMongoRepository) {
        this.autoCostProjectImporter = autoCostProjectImporter;
        this.excelPlanImporter = excelPlanImporter;
        this.excelOnlyCostImporter = excelOnlyCostImporter;
        this.excelPlanWithCostImporter = excelPlanWithCostImporter;
        this.costProjectImportInfoMongoRepository = costProjectImportInfoMongoRepository;
    }

    public CostProject autoImportByMultipartFile(String projectId, MultipartFile file) {
        try (var is = file.getInputStream()) {
            var costProject = autoCostProjectImporter.getObject().doImport(projectId, is);
            var importInfo = new CostProjectImportInfo(projectId, file.getResource().lastModified());

            costProjectImportInfoMongoRepository.save(importInfo);

            return costProject;
        } catch (IOException e) {
            throw new RuntimeException("Exception during loading file", e);
        }
    }

    public CostProject autoImportBySharePointBaseItem(String projectId, BaseItem baseItem, InputStream inputStream) {
        try (var is = inputStream) {
            var costProject = autoCostProjectImporter.getObject().doImport(projectId, is);

            //noinspection ConstantConditions
            var importInfo = new CostProjectImportInfo(projectId, baseItem.lastModifiedDateTime.toEpochSecond());

            costProjectImportInfoMongoRepository.save(importInfo);

            return costProject;
        } catch (IOException e) {
            throw new RuntimeException("Exception during loading file", e);
        }
    }

    public CostProject costImportByMultipartFile(String projectId, MultipartFile file) {
        try (var is = file.getInputStream()) {
            var costProject = excelOnlyCostImporter.getObject().doImport(projectId, is);
            var importInfo = new CostProjectImportInfo(projectId, file.getResource().lastModified());

            costProjectImportInfoMongoRepository.save(importInfo);

            return costProject;
        } catch (IOException e) {
            throw new RuntimeException("Exception during loading file", e);
        }
    }

    public CostProject planImportByMultipartFile(String projectId, MultipartFile file) {
        try (var is = file.getInputStream()) {
            var costProject = excelPlanImporter.getObject().doImport(projectId, is);
            var importInfo = new CostProjectImportInfo(projectId, file.getResource().lastModified());

            costProjectImportInfoMongoRepository.save(importInfo);

            return costProject;
        } catch (IOException e) {
            throw new RuntimeException("Exception during loading file", e);
        }
    }

    public CostProject planWithCostImportByMultipartFile(String projectId, MultipartFile file) {
        try (var is = file.getInputStream()) {
            var costProject = excelPlanWithCostImporter.getObject().doImport(projectId, is);
            var importInfo = new CostProjectImportInfo(projectId, file.getResource().lastModified());

            costProjectImportInfoMongoRepository.save(importInfo);

            return costProject;
        } catch (IOException e) {
            throw new RuntimeException("Exception during loading file", e);
        }
    }
}
