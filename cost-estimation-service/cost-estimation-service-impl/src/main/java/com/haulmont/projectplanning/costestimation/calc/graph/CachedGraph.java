package com.haulmont.projectplanning.costestimation.calc.graph;

import com.haulmont.projectplanning.costestimation.calc.builder.GraphMeasureVertexBuilder;
import com.haulmont.projectplanning.costestimation.calc.calculator.Calculator;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemMeasure;
import com.haulmont.projectplanning.costestimation.tool.Records;
import org.apache.commons.lang.time.StopWatch;
import org.jgrapht.Graph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.GraphLinkType.CHILD;
import static com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.GraphLinkType.NEIGHBOR;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureBlock.MULTIPLICATION_BY_K_WITH_ROUND;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureBlock.MULTIPLICATION_BY_K_WITH_ROUND5;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureBlock.ORIGINAL;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType.MULTIPLICATION;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType.SIMPLE;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasurePart.AGGREGATION_HOURS;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasurePart.AGGREGATION_MONEY_WITHOUT_NDS;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasurePart.AGGREGATION_MONEY_WITH_NDS;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasurePart.HOURS;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.FULL;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.ITEM;
import static com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType.MANUAL;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public class CachedGraph {

    private static Logger logger = LoggerFactory.getLogger(CachedGraph.class);

    private Graph<GraphMeasureVertex, DefaultEdge> graph;
    private Map<String, CachedMeasure> measureCache = new HashMap<>();

    private CostProject project;
    private List<Calculator> calculators;

    public CachedGraph(CostProject project) {
        this.project = project;
    }

    public CachedGraph withCalculators(List<Calculator> calculators) {
        this.calculators = calculators;

        return this;
    }

    public CachedGraph build() {
        var idOnProjectItems = project.projectItems().stream()
                .collect(toMap(CostProjectItem::id, identity()));

        var sortedProjectItems = newArrayList(project.projectItems().get(0));

        project.globalOrder().stream()
                .flatMap(go -> ofNullable(idOnProjectItems.get(go.projectItemId())).stream())
                .forEach(sortedProjectItems::add);

        project.projectItems().forEach(item -> {
            for (CostProjectItemMeasure measure : item.measures()) {
                measureCache.put(measure.id(), new CachedMeasure(item.id(), measure));
            }
        });

        this.graph = buildFullTree(sortedProjectItems);

        // Only for debug.
        // NOT FOR PRODUCTION!
        // Because DOTExport has a hard rules for show names

//        CostProject finalProject = project;
//        DOTExporter<GraphMeasureVertex, DefaultEdge> exporter =
//                new DOTExporter<>(v ->
//                {
//                    var costProjectItem = finalProject.projectItems().stream()
//                            .filter(projectItem -> projectItem.id().equals(v.projectItemId()))
//                            .findFirst().get();
//
//                    return (costProjectItem.name() + "_" + v.type().toString() +
//                            "_" + (v.measureBlock() == null ? "" : "_" + v.measureBlock()) +
//                            "_" + v.measureType() +
//                            "_" + v.calculationType())
//                            .replaceAll(" ", "_")
//                            .replaceAll("%", "_")
//                            .replaceAll("\\.", "_");
//                });
//
//        var writer = new StringWriter();
//
//        System.out.println("RESULT GRAPH");
//        exporter.exportGraph(graph, writer);
//        System.out.println(writer);

        return this;
    }

    private Graph<GraphMeasureVertex, DefaultEdge> buildFullTree(List<CostProjectItem> sortedItems) {
        return buildTreeWithChild(sortedItems, sortedItems.get(0));
    }

    private Graph<GraphMeasureVertex, DefaultEdge> buildTreeWithChild(List<CostProjectItem> sortedItems,
                                                                      CostProjectItem currentItem) {
        var childItems = sortedItems.stream()
                .filter(item -> currentItem.id().equals(item.parentItemId()))
                .toList();

        if (graph == null) {
            graph = new SimpleDirectedGraph<>(DefaultEdge.class);

            var hasChild = !childItems.isEmpty();
            buildTreeItemBlocks(currentItem, HOURS, hasChild);

            buildAggregateItems(currentItem);
        }

        for (CostProjectItem childItem : childItems) {
            var hasChild = sortedItems.stream().anyMatch(item -> childItem.id().equals(item.parentItemId()));
            buildTreeItemBlocks(childItem, HOURS, hasChild);

            linkParentToChild(currentItem, childItem);

            buildTreeWithChild(sortedItems, childItem);
        }

        return graph;
    }

    private void buildAggregateItems(CostProjectItem rootItem) {
        var aggregatedHoursCostItem = project.projectItems().stream()
                .filter(item -> item.id().equals(project.aggregateItems().aggregatedHoursCostItemId()))
                .findFirst()
                .get();

        buildTreeItemBlocks(aggregatedHoursCostItem, AGGREGATION_HOURS, true);


        var aggregatedMoneyWithoutNdsCostItem = project.projectItems().stream()
                .filter(item -> item.id().equals(project.aggregateItems().aggregatedMoneyWithoutNdsCostItemId()))
                .findFirst()
                .get();

        buildTreeItemBlocks(aggregatedMoneyWithoutNdsCostItem, AGGREGATION_MONEY_WITHOUT_NDS, true);


        var aggregatedMoneyWithNds20CostItem = project.projectItems().stream()
                .filter(item -> item.id().equals(project.aggregateItems().aggregatedMoneyWithNds20CostItemId()))
                .findFirst()
                .get();

        buildTreeItemBlocks(aggregatedMoneyWithNds20CostItem, AGGREGATION_MONEY_WITH_NDS, true);


        linkParentToChild(aggregatedHoursCostItem, rootItem);
        linkParentToChild(aggregatedMoneyWithoutNdsCostItem, aggregatedHoursCostItem);
        linkParentToChild(aggregatedMoneyWithNds20CostItem, aggregatedMoneyWithoutNdsCostItem);
    }

    private void buildTreeItemBlocks(CostProjectItem projectItem, GraphMeasurePart measurePart, boolean hasChild) {

        buildOriginalTreeItem(projectItem, measurePart, hasChild);
        buildMultipliedByKWithRoundTreeItem(projectItem, measurePart, hasChild);
        buildMultipliedByKWithRound5TreeItem(projectItem, measurePart, hasChild);

        var originalVertexList = graph.vertexSet().stream()
                .filter(v -> v.projectItemId.equals(projectItem.id())
                        && v.measureBlock == ORIGINAL
                        && v.type != FULL
                ).toList();

        var multipliedByKRoundVertexList = graph.vertexSet().stream()
                .filter(v -> v.projectItemId.equals(projectItem.id())
                        && v.measureBlock == MULTIPLICATION_BY_K_WITH_ROUND
                        && v.type != FULL
                ).toList();

        var multipliedByKRound5VertexList = graph.vertexSet().stream()
                .filter(v -> v.projectItemId.equals(projectItem.id())
                        && v.measureBlock == MULTIPLICATION_BY_K_WITH_ROUND5
                        && v.type != FULL
                ).toList();

        multipliedByKRoundVertexList.forEach(sourceVertex -> originalVertexList.stream()
                .filter(v -> v.type == sourceVertex.type
                        && sourceVertex.linkType == NEIGHBOR)
                .findFirst()
                .ifPresentOrElse(vertex -> graph.addEdge(sourceVertex, vertex, new DefaultEdge()), () ->
                        logger.debug("MultipliedByKWithRound {} not found vertex in Original for link",
                                sourceVertex)));

        multipliedByKRound5VertexList.forEach(sourceVertex -> multipliedByKRoundVertexList.stream()
                .filter(v -> v.type == sourceVertex.type
                        && sourceVertex.linkType == NEIGHBOR)
                .findFirst()
                .ifPresentOrElse(vertex -> graph.addEdge(sourceVertex, vertex, new DefaultEdge()), () ->
                        logger.debug("MultipliedByKWithRound5 {} not found vertex in MultipliedByKWithRound for link",
                                sourceVertex)));
    }

    private void buildOriginalTreeItem(CostProjectItem projectItem, GraphMeasurePart measurePart, boolean hasChild) {
        buildTreeBlock(projectItem, ORIGINAL, measurePart, hasChild);
    }

    private void buildMultipliedByKWithRoundTreeItem(CostProjectItem projectItem, GraphMeasurePart measurePart,
                                                     boolean hasChild) {
        buildTreeBlock(projectItem, MULTIPLICATION_BY_K_WITH_ROUND, measurePart, hasChild);
    }

    private void buildMultipliedByKWithRound5TreeItem(CostProjectItem projectItem, GraphMeasurePart measurePart,
                                                      boolean hasChild) {
        buildTreeBlock(projectItem, MULTIPLICATION_BY_K_WITH_ROUND5, measurePart, hasChild);
    }

    private void buildTreeBlock(CostProjectItem projectItem, GraphMeasureBlock measureBlock,
                                GraphMeasurePart measurePart, boolean hasChild) {
        var vertexBuilder = new GraphMeasureVertexBuilder(projectItem, measureBlock, measurePart, hasChild, measureCache);

        var itemVertex = vertexBuilder.item();
        var analyseVertex = vertexBuilder.analyse();
        var qaVertex = vertexBuilder.qa();
        var backendVertex = vertexBuilder.backend();
        var frontendVertex = vertexBuilder.frontend();
        var devVertex = vertexBuilder.dev();
        var devOpsVertex = vertexBuilder.devOps();
        var tmVertex = vertexBuilder.tm();
        var sumWithoutPmVertex = vertexBuilder.sumWithoutPm();
        var pmVertex = vertexBuilder.pm();
        var otherVertex = vertexBuilder.other();
        var fullVertex = vertexBuilder.full();

        graph.addVertex(itemVertex);
        graph.addVertex(fullVertex);
        graph.addVertex(analyseVertex);
        graph.addVertex(qaVertex);
        graph.addVertex(devVertex);
        graph.addVertex(backendVertex);
        graph.addVertex(frontendVertex);
        graph.addVertex(devOpsVertex);
        graph.addVertex(otherVertex);
        graph.addVertex(tmVertex);
        graph.addVertex(sumWithoutPmVertex);
        graph.addVertex(pmVertex);

        graph.addEdge(itemVertex, analyseVertex, new DefaultEdge());
        graph.addEdge(itemVertex, qaVertex, new DefaultEdge());
        graph.addEdge(itemVertex, backendVertex, new DefaultEdge());
        graph.addEdge(itemVertex, frontendVertex, new DefaultEdge());
        graph.addEdge(itemVertex, devVertex, new DefaultEdge());
        graph.addEdge(itemVertex, devOpsVertex, new DefaultEdge());
        graph.addEdge(itemVertex, otherVertex, new DefaultEdge());
        graph.addEdge(itemVertex, tmVertex, new DefaultEdge());
        graph.addEdge(itemVertex, sumWithoutPmVertex, new DefaultEdge());
        graph.addEdge(itemVertex, pmVertex, new DefaultEdge());
        graph.addEdge(itemVertex, fullVertex, new DefaultEdge());


        switch (measurePart) {
            case HOURS -> {

                switch (measureBlock) {
                    case ORIGINAL -> {
                        if (hasChild) {
                            if (devVertex.linkType == NEIGHBOR) {
                                graph.addEdge(devVertex, backendVertex, new DefaultEdge());
                                graph.addEdge(devVertex, frontendVertex, new DefaultEdge());
                            }

                            if (pmVertex.linkType == NEIGHBOR) {
                                graph.addEdge(pmVertex, sumWithoutPmVertex, new DefaultEdge());
                            }

                            if (fullVertex.linkType == NEIGHBOR) {
                                graph.addEdge(fullVertex, analyseVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, devVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, qaVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, devOpsVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, otherVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, tmVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, pmVertex, new DefaultEdge());
                            }

                            if (qaVertex.calculationType == MULTIPLICATION) {
                                graph.addEdge(qaVertex, devVertex, new DefaultEdge());
                            }

                            if (analyseVertex.calculationType == MULTIPLICATION) {
                                graph.addEdge(analyseVertex, devVertex, new DefaultEdge());
                            }

                            if (devOpsVertex.calculationType == MULTIPLICATION) {
                                graph.addEdge(devOpsVertex, devVertex, new DefaultEdge());
                            }

                            if (tmVertex.calculationType == MULTIPLICATION) {
                                graph.addEdge(tmVertex, devVertex, new DefaultEdge());
                            }
                        } else {
                            graph.addEdge(devVertex, backendVertex, new DefaultEdge());
                            graph.addEdge(devVertex, frontendVertex, new DefaultEdge());

                            graph.addEdge(pmVertex, sumWithoutPmVertex, new DefaultEdge());

                            graph.addEdge(fullVertex, analyseVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, devVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, qaVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, devOpsVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, otherVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, tmVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, pmVertex, new DefaultEdge());

                            graph.addEdge(qaVertex, devVertex, new DefaultEdge());
                            graph.addEdge(analyseVertex, devVertex, new DefaultEdge());
                            graph.addEdge(devOpsVertex, devVertex, new DefaultEdge());
                            graph.addEdge(tmVertex, devVertex, new DefaultEdge());

                        }
                    }
                    case MULTIPLICATION_BY_K_WITH_ROUND, MULTIPLICATION_BY_K_WITH_ROUND5 -> {
                        if (hasChild) {
                            if (fullVertex.linkType == NEIGHBOR) {
                                graph.addEdge(fullVertex, analyseVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, devVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, qaVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, devOpsVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, otherVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, tmVertex, new DefaultEdge());
                                graph.addEdge(fullVertex, pmVertex, new DefaultEdge());
                            }
                        } else {
                            graph.addEdge(fullVertex, analyseVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, devVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, qaVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, devOpsVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, otherVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, tmVertex, new DefaultEdge());
                            graph.addEdge(fullVertex, pmVertex, new DefaultEdge());
                        }
                    }
                }

                graph.addEdge(sumWithoutPmVertex, devVertex, new DefaultEdge());
                graph.addEdge(sumWithoutPmVertex, analyseVertex, new DefaultEdge());
                graph.addEdge(sumWithoutPmVertex, qaVertex, new DefaultEdge());
                graph.addEdge(sumWithoutPmVertex, devOpsVertex, new DefaultEdge());
                graph.addEdge(sumWithoutPmVertex, otherVertex, new DefaultEdge());
                graph.addEdge(sumWithoutPmVertex, tmVertex, new DefaultEdge());
            }
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> {
            }
        }
    }

    private void linkParentToChild(CostProjectItem parentItem, CostProjectItem childItem) {
        var parentVertexList = graph.vertexSet().stream()
                .filter(v -> v.projectItemId.equals(parentItem.id())
                        && v.linkType == CHILD)
                .toList();

        var childVertexList = graph.vertexSet().stream()
                .filter(v -> v.projectItemId.equals(childItem.id()))
                .toList();

        parentVertexList.forEach(parentVertex -> childVertexList.stream()
                .filter(v -> v.type == parentVertex.type
                        && v.measureBlock == parentVertex.measureBlock)
                .findFirst()
                .ifPresent(vertex -> graph.addEdge(parentVertex, vertex, new DefaultEdge())));
    }

    public CachedGraph recalculate() {
        logger.debug("Start calculation full graph");
        var stopWatch = new StopWatch();
        stopWatch.start();

        var iterator = new DepthFirstIterator<>(graph);

        iterator.addTraversalListener(new TraversalListener<>() {
            @Override
            public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
            }

            @Override
            public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
            }

            @Override
            public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
            }

            @Override
            public void vertexTraversed(VertexTraversalEvent<GraphMeasureVertex> e) {
            }

            @Override
            public void vertexFinished(VertexTraversalEvent<GraphMeasureVertex> e) {
                var currentVertex = e.getVertex();

                calculateCostAndChangeInCache(currentVertex);
            }
        });

        iterator.forEachRemaining(currentVertex -> {
        });

        stopWatch.stop();
        logger.debug("Finish calculation graph, time: {}", stopWatch.getTime());

        return this;
    }

    private void calculateCostAndChangeInCache(GraphMeasureVertex currentVertex) {
        if (currentVertex.type == ITEM
                || getType(currentVertex.measureId) == MANUAL
                || currentVertex.calculationType == SIMPLE) {
            return;
        }

        var calculatorOptional = calculators.stream()
                .filter(calc -> calc.isApplicable(currentVertex.calculationType))
                .findFirst();
        if (calculatorOptional.isEmpty()) {
            logger.debug("Calculator not found for {}", currentVertex.calculationType);
            return;
        }
        var calculator = calculatorOptional.get();

        var result = calculator.calculate(this, currentVertex);

        changeCostInCache(currentVertex, result);
    }

    private void changeCostInCache(GraphMeasureVertex vertex, Double value) {
        changeValue(vertex.measureId, value, vertex.isTemp);
    }

    public void changeValue(String measureId, @Nullable Double value, boolean isTemp) {

        var cachedMeasure = measureCache.get(measureId);
        CostProjectItemMeasure measure;
        String itemId = null;

        if (cachedMeasure == null) {
            measure = new CostProjectItemMeasure(measureId);
        } else {
            measure = cachedMeasure.measure;
            itemId = cachedMeasure.itemId;
        }

        // Map.of doesn't support values as null
        var overrideValue = new HashMap<String, Object>();
        overrideValue.put("value", value);
        var updatedMeasure = Records.clone(measure, overrideValue);

        var isChange = !Objects.equals(measure.value(), value) && !isTemp;
        var updatedCachedMeasure = new CachedMeasure(isChange, itemId, updatedMeasure);

        measureCache.put(measureId, updatedCachedMeasure);
    }

    public Map<String, List<CachedMeasure>> getMeasuresByItem() {
        return measureCache.values().stream()
                .filter(cachedMeasure -> cachedMeasure.itemId != null)
                .collect(groupingBy(cachedMeasure -> cachedMeasure.itemId));
    }

    public Optional<Double> getValue(@Nullable String measureId) {
        if (measureId == null) {
            return empty();
        }

        var cachedMeasure = measureCache.get(measureId);
        if (cachedMeasure == null) {
            return empty();
        }

        return ofNullable(cachedMeasure.measure.value());
    }

    @Nullable
    public CostProjectItemDetailMeasureType getType(@Nullable String measureId) {
        var optional = ofNullable(measureId)
                .map(measureCache::get);

        if (optional.isEmpty()) {
            return null;
        }

        return optional.get().measure.type();
    }

    public Graph<GraphMeasureVertex, DefaultEdge> getGraph() {
        return graph;
    }

    public CostProject getProject() {
        return project;
    }

    public record GraphMeasureVertex(
            UUID id,
            String projectItemId,
            String measureId,
            CostProjectItemDetailMeasureType measureType,
            GraphMeasureType type,
            GraphMeasureCalculationType calculationType,
            GraphMeasureBlock measureBlock,
            GraphMeasurePart measurePart,
            GraphLinkType linkType,
            boolean isTemp
    ) {

        public GraphMeasureVertex(GraphMeasureType type, String projectItemId, String measureId,
                                  CostProjectItemDetailMeasureType measureType,
                                  GraphMeasureBlock measureBlock, GraphMeasurePart measurePart,
                                  GraphLinkType linkType, boolean isTemp) {
            this(randomUUID(), projectItemId, measureId, measureType, type, SIMPLE,
                    measureBlock, measurePart, linkType, isTemp);
        }

        public GraphMeasureVertex(GraphMeasureType type, GraphMeasureCalculationType calculationType,
                                  String projectItemId, String measureId, CostProjectItemDetailMeasureType measureType,
                                  GraphMeasureBlock measureBlock, GraphMeasurePart measurePart,
                                  GraphLinkType linkType, boolean isTemp) {
            this(randomUUID(), projectItemId, measureId, measureType, type, calculationType,
                    measureBlock, measurePart, linkType, isTemp);
        }
    }

    public static class CachedMeasure {
        boolean isChange = false;
        String itemId;
        CostProjectItemMeasure measure;

        public CachedMeasure(String itemId, CostProjectItemMeasure measure) {
            this.itemId = itemId;
            this.measure = measure;
        }

        public CachedMeasure(boolean isChange, String itemId, CostProjectItemMeasure measure) {
            this.isChange = isChange;
            this.itemId = itemId;
            this.measure = measure;
        }

        public boolean isChange() {
            return isChange;
        }

        public String getItemId() {
            return itemId;
        }

        public CostProjectItemMeasure getMeasure() {
            return measure;
        }
    }

    public enum GraphLinkType {
        CHILD, NEIGHBOR
    }
}
