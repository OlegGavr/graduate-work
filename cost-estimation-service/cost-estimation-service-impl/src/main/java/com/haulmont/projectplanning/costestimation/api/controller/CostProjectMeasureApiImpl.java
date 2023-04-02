package com.haulmont.projectplanning.costestimation.api.controller;

import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectMapper;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectMeasureService;
import com.haulmont.projectplanning.organization.api.controller.CostProjectMeasureApi;
import com.haulmont.projectplanning.organization.api.model.CostProjectDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemManualMeasureValueDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CostProjectMeasureApiImpl implements CostProjectMeasureApi {

    private CostProjectMapper costProjectMapper;
    private CostProjectMeasureService costProjectItemMeasureService;

    public CostProjectMeasureApiImpl(CostProjectMapper costProjectMapper, CostProjectMeasureService costProjectItemMeasureService) {
        this.costProjectMapper = costProjectMapper;
        this.costProjectItemMeasureService = costProjectItemMeasureService;
    }

    @Override
    public ResponseEntity<CostProjectDto> defineManualMeasureValue(String projectId,
                                                                   String itemId, String measureId,
                                                                   CostProjectItemManualMeasureValueDto manualMeasureValueDto) {

        var modifiedCostProject = costProjectItemMeasureService.defineManualMeasure(projectId,
                itemId, measureId, manualMeasureValueDto.getValue());

        var modifiedCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(modifiedCostProject);

        return ResponseEntity.ok(modifiedCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> clearMeasureValue(String projectId, String itemId, String measureId) {
        var modifiedCostProject = costProjectItemMeasureService
                .clearMeasure(projectId, itemId, measureId, true);

        var modifiedCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(modifiedCostProject);

        return ResponseEntity.ok(modifiedCostProjectDto);
    }
}
