package com.haulmont.projectplanning.costestimation.api.mapper;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetail;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemCreateDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemDetailsDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemMeasureDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Mapper(componentModel = "spring")
public abstract class CostProjectItemMapper {

    @Autowired
    private CostProjectItemMeasureMapper costProjectItemMeasureMapper;

    public CostProjectItemDto costProjectItemToCostProjectItemDto(CostProjectItem costProjectItem) {

        var costProjectItemDto = costProjectItemToCostProjectItemDtoWithoutDetailsBlocks(costProjectItem);

        var idOnMeasure = costProjectItem.measures().stream()
                .map(costProjectItemMeasureMapper
                        ::costProjectItemMeasureToCostProjectItemMeasureDto)
                .collect(toMap(CostProjectItemMeasureDto::getId, identity()));

        var originalBlockDetailsDto = createCostProjectItemDetailsDto(
                costProjectItem.original(), idOnMeasure);
        var multipliedByKWithRoundBlockDetailsDto = createCostProjectItemDetailsDto(
                costProjectItem.multipliedByKWithRound(), idOnMeasure);
        var multipliedByKWithRound5BlockDetailsDto = createCostProjectItemDetailsDto(
                costProjectItem.multipliedByKWithRound5(), idOnMeasure);

        costProjectItemDto.setOriginal(originalBlockDetailsDto);
        costProjectItemDto.setMultipliedByK(multipliedByKWithRoundBlockDetailsDto);
        costProjectItemDto.setMultipliedByKWithRound(multipliedByKWithRound5BlockDetailsDto);

        return costProjectItemDto;
    }

    private CostProjectItemDetailsDto createCostProjectItemDetailsDto(
            CostProjectItemDetail itemDetail, Map<String, CostProjectItemMeasureDto> idOnMeasureDtos) {

        var costProjectItemDetailsDto = new CostProjectItemDetailsDto();
        costProjectItemDetailsDto.setAnalyseCost(idOnMeasureDtos.get(itemDetail.analyseCost()));
        costProjectItemDetailsDto.setBackendCost(idOnMeasureDtos.get(itemDetail.backendCost()));
        costProjectItemDetailsDto.setFrontendCost(idOnMeasureDtos.get(itemDetail.frontendCost()));
        costProjectItemDetailsDto.setDevCost(idOnMeasureDtos.get(itemDetail.devCost()));
        costProjectItemDetailsDto.setQaCost(idOnMeasureDtos.get(itemDetail.qaCost()));
        costProjectItemDetailsDto.setDevOpsCost(idOnMeasureDtos.get(itemDetail.devOpsCost()));
        costProjectItemDetailsDto.setOtherCost(idOnMeasureDtos.get(itemDetail.otherCost()));
        costProjectItemDetailsDto.setTmCost(idOnMeasureDtos.get(itemDetail.tmCost()));
        costProjectItemDetailsDto.setPmCost(idOnMeasureDtos.get(itemDetail.pmCost()));
        costProjectItemDetailsDto.setSumCost(idOnMeasureDtos.get(itemDetail.fullCost()));

        return costProjectItemDetailsDto;
    }

    public CostProjectItemDto costProjectItemToCostProjectItemDtoWithoutParent(CostProjectItem projectItem) {
        var costProjectItemDto = costProjectItemToCostProjectItemDto(projectItem);

        costProjectItemDto.setParentId(null);

        return costProjectItemDto;
    }

    @Mapping(source = "parentItemId", target = "parentId")
    @Mapping(target = "original", ignore = true)
    @Mapping(target = "multipliedByK", ignore = true)
    @Mapping(target = "multipliedByKWithRound", ignore = true)
    public abstract CostProjectItemDto costProjectItemToCostProjectItemDtoWithoutDetailsBlocks(CostProjectItem projectCostItem);

    @Mapping(target = "original", ignore = true)
    @Mapping(target = "multipliedByKWithRound", ignore = true)
    @Mapping(target = "multipliedByKWithRound5", ignore = true)
    public abstract CostProjectItem costProjectItemDtoToCostProjectItem(CostProjectItemDto projectCostItemDto);

    public abstract CostProjectItem costProjectItemCreateDtoToCostProjectItem(CostProjectItemCreateDto projectCostItemDto);
}
