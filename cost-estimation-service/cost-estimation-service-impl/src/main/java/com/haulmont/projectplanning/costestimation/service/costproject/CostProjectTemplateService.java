package com.haulmont.projectplanning.costestimation.service.costproject;

import com.haulmont.projectplanning.costestimation.importer.excel.ExcelCostImporter;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.service.costproject.internal.InternalCostProjectItemService;
import com.haulmont.projectplanning.costestimation.template.registry.CostProjectTemplateRegistry;
import com.haulmont.projectplanning.exception.CostProjectTemplateIoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
public class CostProjectTemplateService {

    private static Logger logger = LoggerFactory.getLogger(CostProjectTemplateService.class);

    public static final String TEMPLATE_BASE_PATH = "classpath:template/excel/%s";

    private ResourceLoader resourceLoader;

    private InternalCostProjectItemService internalCostProjectItemService;

    private CostProjectTemplateRegistry costProjectTemplateRegistry;

    private ObjectFactory<ExcelCostImporter> excelCostImporter;

    public CostProjectTemplateService(ResourceLoader resourceLoader,
                                      InternalCostProjectItemService internalCostProjectItemService,
                                      CostProjectTemplateRegistry costProjectTemplateRegistry,
                                      ObjectFactory<ExcelCostImporter> excelCostImporter) {

        this.resourceLoader = resourceLoader;
        this.internalCostProjectItemService = internalCostProjectItemService;
        this.costProjectTemplateRegistry = costProjectTemplateRegistry;
        this.excelCostImporter = excelCostImporter;
    }

    @Transactional
    public CostProject applyTemplateById(String costProjectId, String templateId, ApplicationVariant variant) {

        var template = costProjectTemplateRegistry.findTemplateById(templateId);

        var templatePath = template.path();

        logger.debug("CostProject template path: {}", templatePath);

        // possible move to registry
        var resource = resourceLoader.getResource(templatePath);

        switch (variant) {
            case APPEND -> {
                // do nothing
            }
            case REPLACE -> internalCostProjectItemService
                    .deleteAllCostItems(costProjectId);
        }

        // import template
        try (var templateIs = resource.getInputStream()) {
            return excelCostImporter.getObject().doImport(costProjectId, templateIs);

        } catch (IOException e) {
            throw new CostProjectTemplateIoException(e, templateId);
        }
    }

    public enum ApplicationVariant {
        APPEND, REPLACE
    }
}
