package com.haulmont.projectplanning.costestimation.calc;

import com.haulmont.projectplanning.costestimation.calc.calculator.Calculator;
import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph;
import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.CachedMeasure;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectMeasureService;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class Calculation {

    private static Logger logger = LoggerFactory.getLogger(Calculation.class);

    final CostProjectMongoRepository projectMongoRepository;
    final CostProjectMeasureService costProjectItemMeasureService;
    final List<Calculator> calculators;

    public Calculation(CostProjectMongoRepository projectMongoRepository,
                       CostProjectMeasureService costProjectItemMeasureService, List<Calculator> calculators) {
        this.projectMongoRepository = projectMongoRepository;
        this.costProjectItemMeasureService = costProjectItemMeasureService;
        this.calculators = calculators;
    }

    @Transactional // performance purpose
    public CostProject calculate(CostProject project) {
        var cachedGraph = new CachedGraph(project)
                .withCalculators(calculators)
                .build();
        cachedGraph = cachedGraph.recalculate();

        updateChangedMeasures(cachedGraph);

        return projectMongoRepository.findById(project.id()).orElseThrow();
    }

    private void updateChangedMeasures(CachedGraph graph) {
        logger.debug("Start update measures in database");
        var stopWatch = new StopWatch();
        stopWatch.start();

        graph.getMeasuresByItem().forEach((projectItemId, cachedMeasures) -> {
            if (cachedMeasures.stream().anyMatch(CachedMeasure::isChange)) {
                var measuresByProjectItem = cachedMeasures.stream()
                        .map(CachedMeasure::getMeasure)
                        .toList();

                projectMongoRepository.updateMeasures(graph.getProject().id(), projectItemId, measuresByProjectItem);
            }
        });

        stopWatch.stop();
        logger.debug("Finish update measures in database, time: {}", stopWatch.getTime());
    }
}
