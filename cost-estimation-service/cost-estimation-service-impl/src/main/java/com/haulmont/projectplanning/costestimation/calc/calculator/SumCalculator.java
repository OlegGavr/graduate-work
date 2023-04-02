package com.haulmont.projectplanning.costestimation.calc.calculator;

import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph;
import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.GraphMeasureVertex;
import com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType.SUM;

@Component
public class SumCalculator extends Calculator {

    @Override
    public boolean isApplicable(GraphMeasureCalculationType calculationType) {
        return calculationType == SUM;
    }

    @Nullable
    @Override
    Double innerCalculate(CachedGraph graph, Set<DefaultEdge> outgoingEdges, GraphMeasureVertex currentVertex) {
        var sum = outgoingEdges.stream()
                .map(graph.getGraph()::getEdgeTarget)
                .map(target -> graph.getValue(target.measureId()))
                .filter(Optional::isPresent)
                .reduce(Optional.empty(), (res, v) -> res
                        .map(rv -> v.get() + rv).or(() -> v));

        return sum.map(s -> (double) Math.round(s * 100) / 100).orElse(null);
    }
}
