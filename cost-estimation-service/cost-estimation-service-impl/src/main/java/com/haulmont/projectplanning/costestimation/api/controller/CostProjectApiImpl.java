package com.haulmont.projectplanning.costestimation.api.controller;

import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectMapper;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectTemplateService;
import com.haulmont.projectplanning.organization.api.controller.CostProjectApi;
import com.haulmont.projectplanning.organization.api.model.ApplyCostProjectTemplateDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectDefineMoneyPerHourDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectDefineNameDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectShortDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CostProjectApiImpl implements CostProjectApi {

    private CostProjectMapper costProjectMapper;

    private CostProjectService costProjectService;

    private CostProjectTemplateService costProjectTemplateService;

    private CostProjectMongoRepository projectCostMongoRepository;

    public CostProjectApiImpl(CostProjectMapper costProjectMapper,
                              CostProjectService costProjectService,
                              CostProjectTemplateService costProjectTemplateService,
                              CostProjectMongoRepository projectCostMongoRepository) {
        this.costProjectMapper = costProjectMapper;
        this.costProjectService = costProjectService;
        this.costProjectTemplateService = costProjectTemplateService;
        this.projectCostMongoRepository = projectCostMongoRepository;
    }

    @Override
    public ResponseEntity<CostProjectDto> createCostProject(CostProjectShortDto costProjectShortDto) {
        var templateProjectCost = costProjectMapper.costProjectShortDtoToCostProject(costProjectShortDto);

        var costProject = costProjectService.create(templateProjectCost, true);

        var costProjectDto = costProjectMapper.costProjectToCostProjectDto(costProject);

        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> createEmptyCostProject() {

        var emptyProject = costProjectService.create(true);

        var costProjectDto = costProjectMapper.costProjectToCostProjectDto(emptyProject);

        return ResponseEntity.ok(costProjectDto);
    }

    @Override
    public ResponseEntity<List<CostProjectShortDto>> deleteCostProjects(List<String> projectIds) {
        projectCostMongoRepository.deleteAllById(projectIds);

        return findAllCostProjects();
    }

    @Override
    public ResponseEntity<List<CostProjectShortDto>> findAllCostProjects() {
        var allProjectCost = projectCostMongoRepository.findAll();

        var allProjectCostShortDtos = allProjectCost.stream()
                .map(costProjectMapper::costProjectToCostProjectShortDto)
                .toList();

        return ResponseEntity.ok(allProjectCostShortDtos);
    }

    @Override
    public ResponseEntity<CostProjectDto> findCostProjectById(String id) {
        var projectCost = projectCostMongoRepository.findById(id);

        if (projectCost.isEmpty()) {
            return ResponseEntity.ok(new CostProjectDto());
        }

        var projectCostDto = costProjectMapper
                .costProjectToCostProjectDto(projectCost.get());

        return ResponseEntity.ok(projectCostDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> defineCostProjectMoneyPerHour(String projectId,
                                                                             CostProjectDefineMoneyPerHourDto costProjectDefineMoneyPerHourDto) {

        var updatedCostProject = costProjectService
                .defineMoneyPerHour(projectId, costProjectDefineMoneyPerHourDto.getMoneyPerHour());

        var savedProjectCostDto = costProjectMapper
                .costProjectToCostProjectDto(updatedCostProject);

        return ResponseEntity.ok(savedProjectCostDto);
    }

    @Override
    public ResponseEntity<CostProjectShortDto> defineCostProjectName(String projectId, CostProjectDefineNameDto costProjectDefineNameDto) {
        var updatedCostProject = projectCostMongoRepository
                .updateName(projectId, costProjectDefineNameDto.getName());

        var savedProjectCostDto = costProjectMapper
                .costProjectToCostProjectShortDto(updatedCostProject);

        return ResponseEntity.ok(savedProjectCostDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> applyTemplate(String projectId,
                                                        ApplyCostProjectTemplateDto applyCostProjectTemplateDto) {

        var variant = switch (applyCostProjectTemplateDto.getVariant()) {
            case APPEND -> CostProjectTemplateService.ApplicationVariant.APPEND;
            case REPLACE -> CostProjectTemplateService.ApplicationVariant.REPLACE;
        };

        var updatedCostProject = costProjectTemplateService.applyTemplateById(projectId,
                applyCostProjectTemplateDto.getTemplateId(), variant);

        var updatedCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(updatedCostProject);

        return ResponseEntity.ok(updatedCostProjectDto);
    }
}
