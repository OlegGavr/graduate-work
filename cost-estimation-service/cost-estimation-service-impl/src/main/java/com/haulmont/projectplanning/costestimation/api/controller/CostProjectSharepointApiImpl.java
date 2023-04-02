package com.haulmont.projectplanning.costestimation.api.controller;

import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectMapper;
import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectSharePointLinkStatusMapper;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectSharepointLinkService;
import com.haulmont.projectplanning.organization.api.controller.CostProjectSharepointApi;
import com.haulmont.projectplanning.organization.api.model.CostProjectDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectSharePointLinkDto;
import com.haulmont.projectplanning.organization.api.model.SharePointLinkStatusDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CostProjectSharepointApiImpl implements CostProjectSharepointApi {


    private CostProjectService costProjectService;

    private CostProjectSharepointLinkService costProjectSharepointLinkService;

    private CostProjectMapper costProjectMapper;

    private CostProjectSharePointLinkStatusMapper costProjectSharePointLinkStatusMapper;

    public CostProjectSharepointApiImpl(CostProjectService costProjectService,
                                        CostProjectSharepointLinkService costProjectSharepointLinkService,
                                        CostProjectMapper costProjectMapper,
                                        CostProjectSharePointLinkStatusMapper costProjectSharePointLinkStatusMapper) {
        this.costProjectService = costProjectService;
        this.costProjectSharepointLinkService = costProjectSharepointLinkService;
        this.costProjectMapper = costProjectMapper;
        this.costProjectSharePointLinkStatusMapper = costProjectSharePointLinkStatusMapper;
    }

    @Override
    public ResponseEntity<CostProjectDto> defineSharePointLink(String projectId, CostProjectSharePointLinkDto costProjectSharePointLinkDto) {
        var actualCostProject = costProjectService
                .defineSharePointLink(projectId, costProjectSharePointLinkDto.getSharePointLink());

        var actualCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(actualCostProject);

        return ResponseEntity.ok(actualCostProjectDto);
    }

    @Override
    public ResponseEntity<SharePointLinkStatusDto> checkSharePointLinkStatus(String projectId) {

        var sharepointLinkStatus = costProjectSharepointLinkService.checkSharePointLinkStatus(projectId);
        var sharePointLinkStatusDto = costProjectSharePointLinkStatusMapper
                .sharePointLinkStatusToSharePointLinkStatusDto(sharepointLinkStatus);

        return ResponseEntity.ok(sharePointLinkStatusDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> updateProjectBySharePointLink(String projectId) {
        var actualCostProject = costProjectSharepointLinkService.updateBySharePointLink(projectId);
        var costProjectDto = costProjectMapper.costProjectToCostProjectDto(actualCostProject);

        return ResponseEntity.ok(costProjectDto);
    }
}
