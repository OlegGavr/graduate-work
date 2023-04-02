package com.haulmont.projectplanning.costestimation.api.controller;

import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectTemplateMapper;
import com.haulmont.projectplanning.costestimation.template.registry.CostProjectTemplateRegistry;
import com.haulmont.projectplanning.organization.api.controller.CostProjectTemplateApi;
import com.haulmont.projectplanning.organization.api.model.CostProjectTemplateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CostProjectTemplateApiImpl implements CostProjectTemplateApi {

    @Autowired
    private CostProjectTemplateRegistry costProjectTemplateRegistry;

    @Autowired
    private CostProjectTemplateMapper costProjectTemplateMapper;

    @Override
    public ResponseEntity<List<CostProjectTemplateDto>> findAllCostProjectTemplates() {
        var costProjectTemplates = costProjectTemplateRegistry.allTemplates();

        var costProjectTemplateDtos = costProjectTemplates.stream()
                .map(costProjectTemplateMapper::costProjectTemplateToCostProjectTemplateDto)
                .toList();

        return ResponseEntity.ok(costProjectTemplateDtos);
    }
}
