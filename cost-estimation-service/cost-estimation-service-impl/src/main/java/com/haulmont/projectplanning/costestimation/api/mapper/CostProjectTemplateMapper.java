package com.haulmont.projectplanning.costestimation.api.mapper;

import com.haulmont.projectplanning.costestimation.template.CostProjectTemplate;
import com.haulmont.projectplanning.organization.api.model.CostProjectTemplateDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CostProjectTemplateMapper {

    CostProjectTemplateDto costProjectTemplateToCostProjectTemplateDto(CostProjectTemplate costProjectTemplate);

}
