package com.haulmont.projectplanning.costestimation.api.controller;

import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectMapper;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectOrderingService;
import com.haulmont.projectplanning.organization.api.controller.CostProjectStructureApi;
import com.haulmont.projectplanning.organization.api.model.ChangeLevelRequestDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectDto;
import com.haulmont.projectplanning.organization.api.model.MovingRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CostProjectStructureApiImpl implements CostProjectStructureApi {

    private CostProjectOrderingService costProjectOrderingService;

    private CostProjectMapper costProjectMapper;

    public CostProjectStructureApiImpl(CostProjectOrderingService costProjectOrderingService,
                                       CostProjectMapper costProjectMapper) {
        this.costProjectOrderingService = costProjectOrderingService;
        this.costProjectMapper = costProjectMapper;
    }

    @Override
    public ResponseEntity<CostProjectDto> addCostItemAfter(String projectId,
                                                           MovingRequestDto movingRequestDto) {

        var actualCostProject = movingRequestDto.getIds().stream()
                .map(id -> costProjectOrderingService
                        .moveCostItemAfterAnotherItem(projectId,
                                movingRequestDto.getAnchor(), id, true))
                .reduce((f, s) -> s).orElseThrow();

        var actualCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(actualCostProject);

        return ResponseEntity.ok(actualCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> addCostItemBefore(String projectId,
                                                            MovingRequestDto movingRequestDto) {

        var actualCostProject = movingRequestDto.getIds().stream()
                .map(id -> costProjectOrderingService
                        .moveCostItemBeforeAnotherItem(projectId,
                                movingRequestDto.getAnchor(), id, true))
                .reduce((f, s) -> s).orElseThrow();

        var actualCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(actualCostProject);

        return ResponseEntity.ok(actualCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> addSubCostItem(String projectId,
                                                         MovingRequestDto movingRequestDto) {

        var actualCostProject = movingRequestDto.getIds().stream()
                .map(id -> costProjectOrderingService
                        .moveCostItemAsSubItem(projectId,
                                movingRequestDto.getAnchor(), id))
                .reduce((f, s) -> s).orElseThrow();

        var actualCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(actualCostProject);

        return ResponseEntity.ok(actualCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> goCostItemLevelDown(String projectId,
                                                              ChangeLevelRequestDto changeLevelRequestDto) {

        var actualCostProject = changeLevelRequestDto.getIds().stream()
                .map(id -> costProjectOrderingService
                        .moveCostItemOnLevelDown(projectId, id))
                .reduce((f, s) -> s).orElseThrow();

        var actualCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(actualCostProject);

        return ResponseEntity.ok(actualCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> goCostItemLevelUp(String projectId,
                                                            ChangeLevelRequestDto changeLevelRequestDto) {

        var actualCostProject = changeLevelRequestDto.getIds().stream()
                .map(id -> costProjectOrderingService
                        .moveCostItemOnLevelUp(projectId, id))
                .reduce((f, s) -> s).orElseThrow();

        var actualCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(actualCostProject);

        return ResponseEntity.ok(actualCostProjectDto);
    }
}
