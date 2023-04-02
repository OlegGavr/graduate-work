package com.haulmont.projectplanning.costestimation.api.mapper;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectRisk;
import com.haulmont.projectplanning.organization.api.model.CostProjectRiskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CostProjectRiskMapper {

    @Mapping(target = "default", source = "defaultRisk")
    CostProjectRiskDto costProjectRiskToCostProjectRiskDto(CostProjectRisk costProjectRisk);
}
