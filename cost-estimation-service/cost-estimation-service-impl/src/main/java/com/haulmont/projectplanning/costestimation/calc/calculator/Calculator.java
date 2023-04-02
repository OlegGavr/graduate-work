package com.haulmont.projectplanning.costestimation.calc.calculator;

import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph;
import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.GraphMeasureVertex;
import com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType;
import com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectRisk;
import org.jgrapht.graph.DefaultEdge;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

public abstract class Calculator {

    public abstract boolean isApplicable(GraphMeasureCalculationType calculationType);

    @Nullable
    public Double calculate(CachedGraph graph, GraphMeasureVertex currentVertex) {
        var edges = graph.getGraph().outgoingEdgesOf(currentVertex);

        if (edges.isEmpty()) {
            return null;
        }

        return innerCalculate(graph, edges, currentVertex);
    }

    @Nullable
    abstract Double innerCalculate(CachedGraph graph, Set<DefaultEdge> outgoingEdges,
                                   GraphMeasureVertex currentVertex);

    protected Optional<Double> getFactorByMeasureType(CostProjectRisk risk, GraphMeasureType measureType) {
        return switch (measureType) {
            case ANALYSE -> ofNullable(risk.ba());
            case BACKEND, FRONTEND, DEV -> ofNullable(risk.dev());
            case QA -> ofNullable(risk.qa());
            case DEVOPS -> ofNullable(risk.devOps());
            case TM -> ofNullable(risk.tm());
            case PM -> ofNullable(risk.pm());
            default -> ofNullable(risk.defaultRisk());
        };
    }
}
