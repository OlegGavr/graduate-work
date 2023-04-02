package com.haulmont.projectplanning.costestimation.calc.calculator;

import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph;
import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.GraphMeasureVertex;
import com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Set;

import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType.MULTIPLICATION;

@Component
public class MultiplicationCalculator extends Calculator {

    @Override
    public boolean isApplicable(GraphMeasureCalculationType calculationType) {
        return calculationType == MULTIPLICATION;
    }

    @Nullable
    @Override
    Double innerCalculate(CachedGraph graph, Set<DefaultEdge> outgoingEdges, GraphMeasureVertex currentVertex) {
        var targetVertex = graph.getGraph().getEdgeTarget(outgoingEdges.iterator().next());

        var optionalValue = graph.getValue(targetVertex.measureId());

        if (optionalValue.isEmpty()) {
            return null;
        }

        var projectRisk = graph.getProject().projectRisk();

        var factor = switch (currentVertex.measurePart()) {
            case HOURS, AGGREGATION_HOURS -> getFactorByMeasureType(projectRisk, currentVertex.type())
                    .orElse(projectRisk.defaultRisk());
            case AGGREGATION_MONEY_WITHOUT_NDS -> (double) graph.getProject().moneyPerHour();
            case AGGREGATION_MONEY_WITH_NDS -> 1.2;
        };

        return Math.round(optionalValue.get() * factor * 2) / 2.0;
    }
}
