package com.haulmont.projectplanning.costestimation.api.mapper;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectAggregateItems;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.organization.api.model.CostProjectAggregateDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectShortDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Mapper(componentModel = "spring", uses = {CostProjectItemMapper.class, CostProjectRiskMapper.class})
public abstract class CostProjectMapper {

    private static Logger logger = LoggerFactory.getLogger(CostProjectMapper.class);

    @Autowired
    private CostProjectItemMapper costProjectItemMapper;

    public abstract CostProjectShortDto costProjectToCostProjectShortDto(CostProject projectCost);

    public abstract CostProject costProjectShortDtoToCostProject(CostProjectShortDto projectCost);

    @Mapping(target = "projectItems", ignore = true)
    @Mapping(target = "aggregate" , ignore = true )
    @Mapping(target = "risk", source = "projectRisk")
    public abstract CostProjectDto costProjectToCostProjectDtoWithoutProjectItems(CostProject projectCost);

    public CostProjectDto costProjectToCostProjectDto(CostProject costProject) {
        var costProjectDto = costProjectToCostProjectDtoWithoutProjectItems(costProject);

        var idOnProjectItem = costProject.projectItems().stream()
                .collect(Collectors.toMap(CostProjectItem::id, identity()));

        var sortedCostProjectItemDtos = costProject.globalOrder().stream()
                .flatMap(go -> Optional.ofNullable(idOnProjectItem.get(go.projectItemId())).stream())
                .map(ci -> {
                    // top level items shouldn't have parentId
                    if (ci.parentItemId().equals(costProject.rootItemId())) {
                        return costProjectItemMapper.costProjectItemToCostProjectItemDtoWithoutParent(ci);
                    } else {
                        return costProjectItemMapper.costProjectItemToCostProjectItemDto(ci);
                    }
                })
                .toList();

        costProjectDto.setProjectItems(sortedCostProjectItemDtos);

        var aggregateDto = costProjectAggregateItemsToCostProjectAggregateDto(
                costProject.aggregateItems(), idOnProjectItem);

        costProjectDto.setAggregate(aggregateDto);

        return costProjectDto;
    }

    private CostProjectAggregateDto costProjectAggregateItemsToCostProjectAggregateDto(
            CostProjectAggregateItems aggregateItems, Map<String, CostProjectItem> idOnProjectItem) {

        Function<String, CostProjectItemDto> mapByIdWithConvert =
                (String costItemId) -> costProjectItemMapper
                        .costProjectItemToCostProjectItemDtoWithoutParent(
                                idOnProjectItem.get(costItemId));

        return new CostProjectAggregateDto()
                .aggregatedHours(mapByIdWithConvert.apply(
                        aggregateItems.aggregatedHoursCostItemId()))
                .aggregatedMoneyWithoutNds(mapByIdWithConvert.apply(
                        aggregateItems.aggregatedMoneyWithoutNdsCostItemId()))
                .aggregatedMoneyWithNds20(mapByIdWithConvert.apply(
                        aggregateItems.aggregatedMoneyWithNds20CostItemId()));
    }

    public abstract CostProject costProjectDtoToCostProject(CostProjectDto projectCostDto);
}
