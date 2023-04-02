package com.haulmont.projectplanning.costestimation.api.controller;

import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectMapper;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectImportService;
import com.haulmont.projectplanning.organization.api.controller.CostProjectImportApi;
import com.haulmont.projectplanning.organization.api.model.CostProjectDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class CostProjectImportApiImpl implements CostProjectImportApi {

    private CostProjectMapper costProjectMapper;

    private CostProjectImportService costProjectImportService;

    public CostProjectImportApiImpl(CostProjectMapper costProjectMapper,
                                    CostProjectImportService costProjectImportService) {
        this.costProjectMapper = costProjectMapper;
        this.costProjectImportService = costProjectImportService;
    }

    @Override
    public ResponseEntity<CostProjectDto> autoImport(String projectId, MultipartFile fileName) {
        var costProject = costProjectImportService
                .autoImportByMultipartFile(projectId, fileName);

        var costProjectDto = costProjectMapper.costProjectToCostProjectDto(costProject);
        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> excelImportHseTypeProject(String projectId, MultipartFile fileName) {
        var costProject = costProjectImportService
                .planWithCostImportByMultipartFile(projectId, fileName);
        var costProjectDto = costProjectMapper.costProjectToCostProjectDto(costProject);
        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> excelImportHsePlanProject(String projectId, MultipartFile fileName) {
        var costProject = costProjectImportService
                .planImportByMultipartFile(projectId, fileName);

        var costProjectDto = costProjectMapper.costProjectToCostProjectDto(costProject);
        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> excelImportHaulmontCostProject(String projectId, MultipartFile fileName) {
        var costProject = costProjectImportService
                .costImportByMultipartFile(projectId, fileName);

        var costProjectDto = costProjectMapper.costProjectToCostProjectDto(costProject);
        return ResponseEntity.ok(costProjectDto);
    }
}
