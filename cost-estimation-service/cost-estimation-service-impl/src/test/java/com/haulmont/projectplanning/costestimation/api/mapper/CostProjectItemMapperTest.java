package com.haulmont.projectplanning.costestimation.api.mapper;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetail;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType.AUTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CostProjectItemMapperTest {

    @Autowired
    CostProjectItemMapper costProjectItemMapper;

    @Test
    void checkThatMappingCostProjectItemDetailsOnDtoWorksAsWell() {
        // given
        var measures = new ArrayList<CostProjectItemMeasure>(24);
        for (int i = 0; i < 24; i++) {
            measures.add(new CostProjectItemMeasure(new ObjectId().toString(), AUTO));
        }

        var original = new CostProjectItemDetail(measures.get(0).id(), measures.get(1).id(),
                measures.get(2).id(), measures.get(3).id(), measures.get(4).id(), measures.get(5).id(),
                measures.get(6).id(), measures.get(7).id(), measures.get(8).id(), measures.get(9).id());
        var multipliedByKWithRound = new CostProjectItemDetail(measures.get(10).id(), measures.get(11).id(),
                measures.get(12).id(), measures.get(13).id(), measures.get(14).id(), measures.get(16).id(),
                measures.get(16).id(), measures.get(17).id(), measures.get(18).id(), measures.get(19).id());
        var multipliedByKWithRound5 = new CostProjectItemDetail(measures.get(20).id(), measures.get(21).id(),
                measures.get(22).id(), measures.get(23).id(), measures.get(24).id(), measures.get(25).id(),
                measures.get(26).id(), measures.get(27).id(), measures.get(28).id(), measures.get(29).id());

        var costProjectItem = new CostProjectItem(new ObjectId().toString(), "Test item",
                measures, original, multipliedByKWithRound, multipliedByKWithRound5, new ObjectId().toString());

        // when
        var costProjectItemDto = costProjectItemMapper
                .costProjectItemToCostProjectItemDto(costProjectItem);

        // then
        assertEquals(measures.stream().map(CostProjectItemMeasure::id).toList(),
                List.of(costProjectItemDto.getOriginal().getAnalyseCost().getId(),
                        costProjectItemDto.getOriginal().getBackendCost().getId(),
                        costProjectItemDto.getOriginal().getFrontendCost().getId(),
                        costProjectItemDto.getOriginal().getDevCost().getId(),
                        costProjectItemDto.getOriginal().getQaCost().getId(),
                        costProjectItemDto.getOriginal().getDevOpsCost().getId(),
                        costProjectItemDto.getOriginal().getOtherCost().getId(),
                        costProjectItemDto.getOriginal().getSumCost().getId(),
                        costProjectItemDto.getMultipliedByK().getAnalyseCost().getId(),
                        costProjectItemDto.getMultipliedByK().getBackendCost().getId(),
                        costProjectItemDto.getMultipliedByK().getFrontendCost().getId(),
                        costProjectItemDto.getMultipliedByK().getDevCost().getId(),
                        costProjectItemDto.getMultipliedByK().getQaCost().getId(),
                        costProjectItemDto.getMultipliedByK().getDevOpsCost().getId(),
                        costProjectItemDto.getMultipliedByK().getOtherCost().getId(),
                        costProjectItemDto.getMultipliedByK().getSumCost().getId(),
                        costProjectItemDto.getMultipliedByKWithRound().getAnalyseCost().getId(),
                        costProjectItemDto.getMultipliedByKWithRound().getBackendCost().getId(),
                        costProjectItemDto.getMultipliedByKWithRound().getFrontendCost().getId(),
                        costProjectItemDto.getMultipliedByKWithRound().getDevCost().getId(),
                        costProjectItemDto.getMultipliedByKWithRound().getQaCost().getId(),
                        costProjectItemDto.getMultipliedByKWithRound().getDevOpsCost().getId(),
                        costProjectItemDto.getMultipliedByKWithRound().getOtherCost().getId(),
                        costProjectItemDto.getMultipliedByKWithRound().getSumCost().getId()));

    }
}
