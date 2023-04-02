package com.haulmont.projectplanning.costestimation.calc.calculator;

import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph;
import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.GraphMeasureVertex;
import com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Set;

import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType.MULTIPLICATION_BY_K_WITH_ROUND;

@Component
public class MultiplicationByKWithRoundCalculator extends Calculator {

    @Override
    public boolean isApplicable(GraphMeasureCalculationType calculationType) {
        return calculationType == MULTIPLICATION_BY_K_WITH_ROUND;
    }

    @Nullable
    @Override
    Double innerCalculate(CachedGraph graph, Set<DefaultEdge> outgoingEdges, GraphMeasureVertex currentVertex) {
        var targetVertex = graph.getGraph().getEdgeTarget(outgoingEdges.iterator().next());

        var optionalValue = graph.getValue(targetVertex.measureId());

        if (optionalValue.isEmpty()) {
            return null;
        }

        double value = optionalValue.get();
        var projectRisk = graph.getProject().projectRisk();
        var factor = projectRisk.defaultRisk();

        return Math.round((value + (value * factor)) * 2) / 2.0;
    }
}
