package com.haulmont.projectplanning.costestimation.api.controller;

import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectMapper;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectRiskService;
import com.haulmont.projectplanning.organization.api.controller.CostProjectRiskApi;
import com.haulmont.projectplanning.organization.api.model.CostProjectDefineRiskDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CostProjectRiskApiImpl implements CostProjectRiskApi {

    private CostProjectRiskService costProjectRiskService;

    private CostProjectMapper costProjectMapper;

    public CostProjectRiskApiImpl(CostProjectRiskService costProjectRiskService,
                                  CostProjectMapper costProjectMapper) {
        this.costProjectRiskService = costProjectRiskService;
        this.costProjectMapper = costProjectMapper;
    }

    @Override
    public ResponseEntity<CostProjectDto> defineBaRisk(String projectId, CostProjectDefineRiskDto costProjectDefineRiskDto) {
        var costProject = costProjectRiskService.defineBaRisk(
                projectId, costProjectDefineRiskDto.getRiskValue());

        var costProjectDto = costProjectMapper
                .costProjectToCostProjectDto(costProject);

        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> defineDefaultRisk(String projectId, CostProjectDefineRiskDto costProjectDefineRiskDto) {
        var costProject = costProjectRiskService.defineDefaultRisk(
                projectId, costProjectDefineRiskDto.getRiskValue());

        var costProjectDto = costProjectMapper
                .costProjectToCostProjectDto(costProject);

        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> defineDevOpsRisk(String projectId, CostProjectDefineRiskDto costProjectDefineRiskDto) {
        var costProject = costProjectRiskService.defineDevOpsRisk(
                projectId, costProjectDefineRiskDto.getRiskValue());

        var costProjectDto = costProjectMapper
                .costProjectToCostProjectDto(costProject);

        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> defineDevRisk(String projectId, CostProjectDefineRiskDto costProjectDefineRiskDto) {
        var costProject = costProjectRiskService.defineDevRisk(
                projectId, costProjectDefineRiskDto.getRiskValue());

        var costProjectDto = costProjectMapper
                .costProjectToCostProjectDto(costProject);

        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> definePmRisk(String projectId, CostProjectDefineRiskDto costProjectDefineRiskDto) {
        var costProject = costProjectRiskService.definePmRisk(
                projectId, costProjectDefineRiskDto.getRiskValue());

        var costProjectDto = costProjectMapper
                .costProjectToCostProjectDto(costProject);

        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> defineQaRisk(String projectId, CostProjectDefineRiskDto costProjectDefineRiskDto) {
        var costProject = costProjectRiskService.defineQaRisk(
                projectId, costProjectDefineRiskDto.getRiskValue());

        var costProjectDto = costProjectMapper
                .costProjectToCostProjectDto(costProject);

        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> defineTmRisk(String projectId, CostProjectDefineRiskDto costProjectDefineRiskDto) {
        var costProject = costProjectRiskService.defineTmRisk(
                projectId, costProjectDefineRiskDto.getRiskValue());

        var costProjectDto = costProjectMapper
                .costProjectToCostProjectDto(costProject);

        return ResponseEntity.ok(costProjectDto);
    }
}
