package com.haulmont.projectplanning.costestimation.api.mapper;

import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectSharepointLinkService.SharepointLinkStatus;
import com.haulmont.projectplanning.organization.api.model.SharePointLinkStatusDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CostProjectSharePointLinkStatusMapper {

    SharePointLinkStatusDto sharePointLinkStatusToSharePointLinkStatusDto(SharepointLinkStatus sharepointLinkStatus);
}
