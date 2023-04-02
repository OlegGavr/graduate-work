package com.haulmont.projectplanning.costestimation.api.controller;

import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectItemMapper;
import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectMapper;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectItemService;
import com.haulmont.projectplanning.organization.api.controller.CostProjectItemApi;
import com.haulmont.projectplanning.organization.api.model.CostProjectDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemCreateDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemDefineCommentDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemDefineNameDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemDeleteDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CostProjectItemApiImpl implements CostProjectItemApi {

    private CostProjectMapper costProjectMapper;
    private CostProjectItemMapper costProjectItemMapper;
    private CostProjectItemService costProjectItemService;
    private CostProjectMongoRepository costProjectMongoRepository;

    public CostProjectItemApiImpl(CostProjectItemService costProjectItemService,
                                  CostProjectMongoRepository costProjectMongoRepository,
                                  CostProjectMapper costProjectMapper,
                                  CostProjectItemMapper costProjectItemMapper) {
        this.costProjectItemService = costProjectItemService;
        this.costProjectMongoRepository = costProjectMongoRepository;
        this.costProjectMapper = costProjectMapper;
        this.costProjectItemMapper = costProjectItemMapper;
    }

    @Override
    public ResponseEntity<CostProjectDto> createCostItem(String id, String itemId,
                                                         CostProjectItemCreateDto costProjectItemDto) {

        if (itemId == null) {
            return createCostItemTop(id, costProjectItemDto);
        }

        return createCostItemAfter(id, itemId, costProjectItemDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> createCostItemAfter(String projectId, String itemId, CostProjectItemCreateDto costProjectItemDto) {
        var costProjectItemTemplate = costProjectItemMapper
                .costProjectItemCreateDtoToCostProjectItem(costProjectItemDto);

        var modifiedCostProject = costProjectItemService.createCostItemAfter(projectId, itemId, costProjectItemTemplate);

        var modifiedCostProjectDto = costProjectMapper.costProjectToCostProjectDto(modifiedCostProject);

        return ResponseEntity.ok(modifiedCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> createCostItemBefore(String projectId, String itemId, CostProjectItemCreateDto costProjectItemDto) {
        var costProjectItemTemplate = costProjectItemMapper
                .costProjectItemCreateDtoToCostProjectItem(costProjectItemDto);

        var modifiedCostProject = costProjectItemService.createCostItemBefore(projectId, itemId, costProjectItemTemplate);

        var modifiedCostProjectDto = costProjectMapper.costProjectToCostProjectDto(modifiedCostProject);

        return ResponseEntity.ok(modifiedCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> createCostItemTop(String projectId, CostProjectItemCreateDto costProjectItemDto) {

        var costProjectItemTemplate = costProjectItemMapper
                .costProjectItemCreateDtoToCostProjectItem(costProjectItemDto);

        var modifiedCostProject = costProjectItemService.createCostItem(projectId, costProjectItemTemplate);

        var modifiedCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(modifiedCostProject);

        return ResponseEntity.ok(modifiedCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> createCostSubItem(String id, String parentId,
                                                            CostProjectItemCreateDto costProjectItemDto) {

        var costProjectItemTemplate = costProjectItemMapper
                .costProjectItemCreateDtoToCostProjectItem(costProjectItemDto);

        var modifiedCostProject = costProjectItemService.createCostSubItem(id, parentId, costProjectItemTemplate);

        var modifiedCostProjectDto = costProjectMapper.costProjectToCostProjectDto(modifiedCostProject);

        return ResponseEntity.ok(modifiedCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> deleteCostItems(String projectId, CostProjectItemDeleteDto costProjectItemDeleteDto) {
        var modifiedCostProject = costProjectItemService.deleteAllCostItemsById(projectId, costProjectItemDeleteDto.getIds());

        var modifiedCostProjectDto = costProjectMapper.costProjectToCostProjectDto(modifiedCostProject);

        return ResponseEntity.ok(modifiedCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> findCostItemById(String id, String itemId, CostProjectItemDto costProjectItemDto) {
        return CostProjectItemApi.super.findCostItemById(id, itemId, costProjectItemDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> updateCostItem(String id, String itemId,
                                                         CostProjectItemDto projectCostItemDto) {

        throw new UnsupportedOperationException("Method isn't supported");
//        var projectCostItem = costProjectItemMapper
//                .costProjectItemDtoToCostProjectItem(projectCostItemDto);
//
//        var updatedProjectCost = costProjectMongoRepository
//                .updateCostItem(id, projectCostItem);
//
//
//        var updatedProjectCostDto = costProjectMapper
//                .costProjectToCostProjectDtoWithoutProjectItems(updatedProjectCost);
//
//        return ResponseEntity.ok(updatedProjectCostDto);
    }


    @Override
    public ResponseEntity<CostProjectDto> deleteCostItem(String projectId, String itemId) {
        var modifiedCostProject = costProjectItemService
                .deleteAllCostItemsById(projectId, List.of(itemId));

        var modifiedCostProjectDto = costProjectMapper
                .costProjectToCostProjectDto(modifiedCostProject);

        return ResponseEntity.ok(modifiedCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> defineCostItemName(String projectId, String itemId,
                                                             CostProjectItemDefineNameDto costProjectItemDefineNameDto) {

        var modifiedCostProject = costProjectMongoRepository
                .updateCostItemName(projectId, itemId, costProjectItemDefineNameDto.getName());

        var modifiedCostProjectDto = costProjectMapper.costProjectToCostProjectDto(modifiedCostProject);

        return ResponseEntity.ok(modifiedCostProjectDto);
    }

    @Override
    public ResponseEntity<CostProjectDto> defineCostItemComment(String projectId, String itemId,
                                                                CostProjectItemDefineCommentDto commentDto) {

        var modifiedCostProject = costProjectMongoRepository
                .updateCostItemComment(projectId, itemId, commentDto.getComment());

        var modifiedCostProjectDto = costProjectMapper.costProjectToCostProjectDto(modifiedCostProject);

        return ResponseEntity.ok(modifiedCostProjectDto);
    }
}
