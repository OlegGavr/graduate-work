package com.haulmont.projectplanning.costestimation.api.mapper;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemMeasureDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CostProjectItemMeasureMapper {

    CostProjectItemMeasureDto costProjectItemMeasureToCostProjectItemMeasureDto(CostProjectItemMeasure projectCostItemMeasure);

    CostProjectItemMeasure costProjectItemMeasureDtoToCostProjectItemMeasure(CostProjectItemMeasureDto projectCostItemMeasureDto);
}
