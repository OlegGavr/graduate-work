package com.haulmont.projectplanning.costestimation.calc.builder;

import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.CachedMeasure;
import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.GraphLinkType;
import com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.GraphMeasureVertex;
import com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureBlock;
import com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType;
import com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasurePart;
import com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetail;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

import static com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.GraphLinkType.CHILD;
import static com.haulmont.projectplanning.costestimation.calc.graph.CachedGraph.GraphLinkType.NEIGHBOR;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType.MULTIPLICATION;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType.MULTIPLICATION_BY_K_WITH_ROUND;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType.MULTIPLICATION_BY_K_WITH_ROUND5;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType.SIMPLE;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureCalculationType.SUM;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.ANALYSE;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.BACKEND;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.DEV;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.DEVOPS;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.FRONTEND;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.FULL;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.ITEM;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.OTHER;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.PM;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.QA;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.SUM_WITHOUT_PM;
import static com.haulmont.projectplanning.costestimation.calc.graph.GraphMeasureType.TM;
import static com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType.AUTO;
import static com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetailMeasureType.MANUAL;

public class GraphMeasureVertexBuilder {

    private final CostProjectItem projectItem;
    private final GraphMeasureBlock measureBlock;
    private final boolean hasChild;
    private final CostProjectItemDetail itemDetail;
    private final GraphMeasurePart measurePart;
    private final Map<String, CachedMeasure> measureCache;

    public GraphMeasureVertexBuilder(CostProjectItem projectItem, GraphMeasureBlock measureBlock,
                                     GraphMeasurePart measurePart, boolean hasChild,
                                     Map<String, CachedMeasure> measureCache) {
        this.projectItem = projectItem;
        this.measureBlock = measureBlock;
        this.measurePart = measurePart;
        this.hasChild = hasChild;
        this.measureCache = new HashMap<>(measureCache);

        this.itemDetail = switch (measureBlock) {
            case ORIGINAL -> projectItem.original();
            case MULTIPLICATION_BY_K_WITH_ROUND -> projectItem.multipliedByKWithRound();
            case MULTIPLICATION_BY_K_WITH_ROUND5 -> projectItem.multipliedByKWithRound5();
        };
    }

    public GraphMeasureVertex item() {
        return new GraphMeasureVertex(ITEM, projectItem.id(), null, AUTO,
                measureBlock, measurePart, CHILD, false);
    }

    public GraphMeasureVertex analyse() {
        var currentMeasureId = itemDetail.analyseCost();
        var currentMeasureType = getMeasureType(currentMeasureId, ANALYSE);

        var calculationType = switch (measurePart) {
            case HOURS, AGGREGATION_HOURS -> switch (measureBlock) {
                case ORIGINAL -> getCalculationTypeDevDependency();
                case MULTIPLICATION_BY_K_WITH_ROUND -> getCalculationTypeForRoundDevDependency(currentMeasureType);
                case MULTIPLICATION_BY_K_WITH_ROUND5 -> getCalculationTypeForRound5DevDependency(currentMeasureType);
            };
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> MULTIPLICATION;
        };

        var linkType = getLinkType(calculationType, measurePart);

        return new GraphMeasureVertex(ANALYSE, calculationType, projectItem.id(),
                currentMeasureId, currentMeasureType.current, measureBlock, measurePart, linkType, false);
    }

    public GraphMeasureVertex qa() {
        var currentMeasureId = itemDetail.qaCost();
        var currentMeasureType = getMeasureType(currentMeasureId, QA);

        var calculationType = switch (measurePart) {
            case HOURS, AGGREGATION_HOURS -> switch (measureBlock) {
                case ORIGINAL -> getCalculationTypeDevDependency();
                case MULTIPLICATION_BY_K_WITH_ROUND -> getCalculationTypeForRoundDevDependency(currentMeasureType);
                case MULTIPLICATION_BY_K_WITH_ROUND5 -> getCalculationTypeForRound5DevDependency(currentMeasureType);
            };
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> MULTIPLICATION;
        };

        var linkType = getLinkType(calculationType, measurePart);

        return new GraphMeasureVertex(QA, calculationType, projectItem.id(),
                currentMeasureId, currentMeasureType.current, measureBlock, measurePart, linkType, false);
    }

    public GraphMeasureVertex backend() {
        var currentMeasureId = itemDetail.backendCost();
        var currentMeasureType = getMeasureType(currentMeasureId, BACKEND);

        var calculationType = switch (measurePart) {
            case HOURS, AGGREGATION_HOURS -> switch (measureBlock) {
                case ORIGINAL -> hasChild ? SUM : SIMPLE;
                case MULTIPLICATION_BY_K_WITH_ROUND -> getCalculationTypeForRoundDevDependency(currentMeasureType);
                case MULTIPLICATION_BY_K_WITH_ROUND5 -> getCalculationTypeForRound5(currentMeasureType);
            };
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> MULTIPLICATION;
        };

        var linkType = getLinkType(calculationType, measurePart);

        return new GraphMeasureVertex(BACKEND, calculationType, projectItem.id(),
                currentMeasureId, currentMeasureType.current, measureBlock, measurePart, linkType, false);
    }

    public GraphMeasureVertex frontend() {
        var currentMeasureId = itemDetail.frontendCost();
        var currentMeasureType = getMeasureType(currentMeasureId, FRONTEND);

        var calculationType = switch (measurePart) {
            case HOURS, AGGREGATION_HOURS -> switch (measureBlock) {
                case ORIGINAL -> hasChild ? SUM : SIMPLE;
                case MULTIPLICATION_BY_K_WITH_ROUND -> getCalculationTypeForRoundDevDependency(currentMeasureType);
                case MULTIPLICATION_BY_K_WITH_ROUND5 -> getCalculationTypeForRound5(currentMeasureType);
            };
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> MULTIPLICATION;
        };

        var linkType = getLinkType(calculationType, measurePart);

        return new GraphMeasureVertex(FRONTEND, calculationType, projectItem.id(),
                currentMeasureId, currentMeasureType.current, measureBlock, measurePart, linkType, false);
    }

    public GraphMeasureVertex dev() {
        var currentMeasureId = itemDetail.devCost();
        var currentMeasureType = getMeasureType(currentMeasureId, DEV);

        var calculationType = switch (measurePart) {
            case HOURS, AGGREGATION_HOURS -> switch (measureBlock) {
                case ORIGINAL -> SUM;
                case MULTIPLICATION_BY_K_WITH_ROUND -> getCalculationTypeForRoundDev();
                case MULTIPLICATION_BY_K_WITH_ROUND5 -> getCalculationTypeForRound5Dev();
            };
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> MULTIPLICATION;
        };

        var linkType = getLinkType(calculationType, measurePart);
        if (backendMeasureType().original == MANUAL || frontendMeasureType().original == MANUAL) {
            linkType = NEIGHBOR;
        }

        return new GraphMeasureVertex(DEV, calculationType, projectItem.id(),
                currentMeasureId, currentMeasureType.current, measureBlock, measurePart, linkType, false);
    }

    public GraphMeasureVertex devOps() {
        var currentMeasureId = itemDetail.devOpsCost();
        var currentMeasureType = getMeasureType(currentMeasureId, DEVOPS);

        var calculationType = switch (measurePart) {
            case HOURS, AGGREGATION_HOURS -> switch (measureBlock) {
                case ORIGINAL -> getCalculationTypeDevDependency();
                case MULTIPLICATION_BY_K_WITH_ROUND -> getCalculationTypeForRoundDevDependency(currentMeasureType);
                case MULTIPLICATION_BY_K_WITH_ROUND5 -> getCalculationTypeForRound5DevDependency(currentMeasureType);
            };
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> MULTIPLICATION;
        };

        var linkType = getLinkType(calculationType, measurePart);

        return new GraphMeasureVertex(DEVOPS, calculationType, projectItem.id(),
                currentMeasureId, currentMeasureType.current, measureBlock, measurePart, linkType, false);
    }

    public GraphMeasureVertex other() {
        var currentMeasureId = itemDetail.otherCost();
        var currentMeasureType = getMeasureType(currentMeasureId, OTHER);

        var calculationType = switch (measurePart) {
            case HOURS, AGGREGATION_HOURS -> switch (measureBlock) {
                case ORIGINAL -> hasChild ? SUM : SIMPLE;
                case MULTIPLICATION_BY_K_WITH_ROUND -> getCalculationTypeForRoundDevDependency(currentMeasureType);
                case MULTIPLICATION_BY_K_WITH_ROUND5 -> getCalculationTypeForRound5DevDependency(currentMeasureType);
            };
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> MULTIPLICATION;
        };

        var linkType = getLinkType(calculationType, measurePart);

        return new GraphMeasureVertex(OTHER, calculationType, projectItem.id(),
                currentMeasureId, currentMeasureType.current, measureBlock, measurePart, linkType, false);
    }

    public GraphMeasureVertex tm() {
        var currentMeasureId = itemDetail.tmCost();
        var currentMeasureType = getMeasureType(currentMeasureId, TM);

        var calculationType = switch (measurePart) {
            case HOURS, AGGREGATION_HOURS -> switch (measureBlock) {
                case ORIGINAL -> getCalculationTypeDevDependency();
                case MULTIPLICATION_BY_K_WITH_ROUND -> getCalculationTypeForRoundDevDependency(currentMeasureType);
                case MULTIPLICATION_BY_K_WITH_ROUND5 -> getCalculationTypeForRound5DevDependency(currentMeasureType);
            };
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> MULTIPLICATION;
        };

        var linkType = getLinkType(calculationType, measurePart);

        return new GraphMeasureVertex(TM, calculationType, projectItem.id(),
                currentMeasureId, currentMeasureType.current, measureBlock, measurePart, linkType, false);
    }

    public GraphMeasureVertex pm() {
        var currentMeasureId = itemDetail.pmCost();
        var currentMeasureType = getMeasureType(currentMeasureId, PM);

        var calculationType = switch (measurePart) {
            case HOURS, AGGREGATION_HOURS -> switch (measureBlock) {
                case ORIGINAL -> getCalculationTypePm();
                case MULTIPLICATION_BY_K_WITH_ROUND -> getCalculationTypeForRoundPm();
                case MULTIPLICATION_BY_K_WITH_ROUND5 -> getCalculationTypeForRound5Pm();
            };
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> MULTIPLICATION;
        };

        var linkType = getLinkType(calculationType, measurePart);

        return new GraphMeasureVertex(PM, calculationType, projectItem.id(),
                currentMeasureId, currentMeasureType.current, measureBlock, measurePart, linkType, false);
    }

    /**
     * This is fake vertex. Need only for calculate PM vertex.
     * Random measureId is needed here.
     */
    public GraphMeasureVertex sumWithoutPm() {
        return new GraphMeasureVertex(SUM_WITHOUT_PM, SUM, projectItem.id(),
                new ObjectId().toString(), AUTO, measureBlock, measurePart, NEIGHBOR, true);
    }

    public GraphMeasureVertex full() {
        var calculationType = switch (measurePart) {
            case HOURS, AGGREGATION_HOURS -> SUM;
            case AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> MULTIPLICATION;
        };

        var linkType = CHILD;
        if (!hasChild
                || frontendMeasureType().original == MANUAL || backendMeasureType().original == MANUAL
                || devMeasureType().original == MANUAL
                || analyseMeasureType().original == MANUAL
                || qaMeasureType().original == MANUAL || devopsMeasureType().original == MANUAL
                || otherMeasureType().original == MANUAL
                || tmMeasureType().original == MANUAL || pmMeasureType().original == MANUAL) {
            linkType = NEIGHBOR;
        }

        return new GraphMeasureVertex(FULL, calculationType, projectItem.id(),
                itemDetail.fullCost(), AUTO, measureBlock, measurePart, linkType, false);
    }

    private MeasureType getMeasureType(String currentMeasureId, GraphMeasureType graphMeasureType) {
        var cachedMeasure = measureCache.get(currentMeasureId);

        CostProjectItemDetailMeasureType current = cachedMeasure != null ? cachedMeasure.getMeasure().type() : AUTO;
        CostProjectItemDetailMeasureType original = null;
        CostProjectItemDetailMeasureType round = null;

        switch (graphMeasureType) {
            case ANALYSE -> {
                original = measureCache.get(projectItem.original().analyseCost()).getMeasure().type();
                round = measureCache.get(projectItem.multipliedByKWithRound().analyseCost()).getMeasure().type();
            }
            case BACKEND -> {
                original = measureCache.get(projectItem.original().backendCost()).getMeasure().type();
                round = measureCache.get(projectItem.multipliedByKWithRound().backendCost()).getMeasure().type();
            }
            case FRONTEND -> {
                original = measureCache.get(projectItem.original().frontendCost()).getMeasure().type();
                round = measureCache.get(projectItem.multipliedByKWithRound().frontendCost()).getMeasure().type();
            }
            case DEV -> {
                original = measureCache.get(projectItem.original().devCost()).getMeasure().type();
                round = measureCache.get(projectItem.multipliedByKWithRound().devCost()).getMeasure().type();
            }
            case QA -> {
                original = measureCache.get(projectItem.original().qaCost()).getMeasure().type();
                round = measureCache.get(projectItem.multipliedByKWithRound().qaCost()).getMeasure().type();
            }
            case DEVOPS -> {
                original = measureCache.get(projectItem.original().devOpsCost()).getMeasure().type();
                round = measureCache.get(projectItem.multipliedByKWithRound().devOpsCost()).getMeasure().type();
            }
            case OTHER -> {
                original = measureCache.get(projectItem.original().otherCost()).getMeasure().type();
                round = measureCache.get(projectItem.multipliedByKWithRound().otherCost()).getMeasure().type();
            }
            case TM -> {
                original = measureCache.get(projectItem.original().tmCost()).getMeasure().type();
                round = measureCache.get(projectItem.multipliedByKWithRound().tmCost()).getMeasure().type();
            }
            case PM -> {
                original = measureCache.get(projectItem.original().pmCost()).getMeasure().type();
                round = measureCache.get(projectItem.multipliedByKWithRound().pmCost()).getMeasure().type();
            }
            case FULL -> {
                original = measureCache.get(projectItem.original().fullCost()).getMeasure().type();
                round = measureCache.get(projectItem.multipliedByKWithRound().fullCost()).getMeasure().type();
            }
        }

        return new MeasureType(current, original, round);
    }

    private GraphMeasureCalculationType getCalculationTypePm() {
        if (!hasChild
                || devMeasureType().original == MANUAL
                || backendMeasureType().original == MANUAL
                || frontendMeasureType().original == MANUAL
                || qaMeasureType().original == MANUAL
                || analyseMeasureType().original == MANUAL
                || devopsMeasureType().original == MANUAL
                || tmMeasureType().original == MANUAL
        ) {
            return MULTIPLICATION;
        }

        return SUM;
    }

    private GraphMeasureCalculationType getCalculationTypeDevDependency() {
        if (!hasChild
                || devMeasureType().original == MANUAL
                || backendMeasureType().original == MANUAL
                || frontendMeasureType().original == MANUAL
        ) {
            return MULTIPLICATION;
        }

        return SUM;
    }

    private GraphMeasureCalculationType getCalculationTypeForRoundDevDependency(MeasureType measureType) {
        if (!hasChild
                || measureType.original == MANUAL
                || backendMeasureType().original == MANUAL
                || frontendMeasureType().original == MANUAL
                || devMeasureType().original == MANUAL
        ) {
            return MULTIPLICATION_BY_K_WITH_ROUND;
        }

        return SUM;
    }

    private GraphMeasureCalculationType getCalculationTypeForRoundPm() {
        if (!hasChild
                || pmMeasureType().original == MANUAL
                || devMeasureType().original == MANUAL
                || backendMeasureType().original == MANUAL
                || frontendMeasureType().original == MANUAL
                || qaMeasureType().original == MANUAL
                || analyseMeasureType().original == MANUAL
                || devopsMeasureType().original == MANUAL
                || tmMeasureType().original == MANUAL
        ) {
            return MULTIPLICATION_BY_K_WITH_ROUND;
        }

        return SUM;
    }

    private GraphMeasureCalculationType getCalculationTypeForRound5(MeasureType measureType) {
        if (!hasChild || measureType.original == MANUAL || measureType.round == MANUAL) {
            return MULTIPLICATION_BY_K_WITH_ROUND5;
        }

        return SUM;
    }

    private GraphMeasureCalculationType getCalculationTypeForRound5DevDependency(MeasureType measureType) {
        var devMeasureType = devMeasureType();
        var backendMeasureType = backendMeasureType();
        var frontendMeasureType = frontendMeasureType();
        if (!hasChild
                || measureType.original == MANUAL || measureType.round == MANUAL
                || devMeasureType.original == MANUAL || devMeasureType.round == MANUAL
                || backendMeasureType.original == MANUAL || backendMeasureType.round == MANUAL
                || frontendMeasureType.original == MANUAL || frontendMeasureType.round == MANUAL
        ) {
            return MULTIPLICATION_BY_K_WITH_ROUND5;
        }

        return SUM;
    }

    private GraphMeasureCalculationType getCalculationTypeForRound5Pm() {
        var pmMeasureType = pmMeasureType();
        var devMeasureType = devMeasureType();
        var backendMeasureType = backendMeasureType();
        var frontendMeasureType = frontendMeasureType();
        var qaMeasureType = qaMeasureType();
        var analyseMeasureType = analyseMeasureType();
        var devopsMeasureType = devopsMeasureType();
        var tmMeasureType = tmMeasureType();
        if (!hasChild
                || pmMeasureType.original == MANUAL || pmMeasureType.round == MANUAL
                || devMeasureType.original == MANUAL || devMeasureType.round == MANUAL
                || backendMeasureType.original == MANUAL || backendMeasureType.round == MANUAL
                || frontendMeasureType.original == MANUAL || frontendMeasureType.round == MANUAL
                || qaMeasureType.original == MANUAL || qaMeasureType.round == MANUAL
                || analyseMeasureType.original == MANUAL || analyseMeasureType.round == MANUAL
                || devopsMeasureType.original == MANUAL || devopsMeasureType.round == MANUAL
                || tmMeasureType.original == MANUAL || tmMeasureType.round == MANUAL
        ) {
            return MULTIPLICATION_BY_K_WITH_ROUND5;
        }

        return SUM;
    }

    private GraphMeasureCalculationType getCalculationTypeForRoundDev() {
        if (!hasChild
                || devMeasureType().original == MANUAL
                || backendMeasureType().original == MANUAL
                || frontendMeasureType().original == MANUAL) {
            return MULTIPLICATION_BY_K_WITH_ROUND;
        }

        return SUM;
    }

    private GraphMeasureCalculationType getCalculationTypeForRound5Dev() {
        var devMeasureType = devMeasureType();
        var backendMeasureType = backendMeasureType();
        var frontendMeasureType = frontendMeasureType();
        if (!hasChild
                || devMeasureType.original == MANUAL || devMeasureType.round == MANUAL
                || backendMeasureType.original == MANUAL || backendMeasureType.round == MANUAL
                || frontendMeasureType.original == MANUAL || frontendMeasureType.round == MANUAL
        ) {
            return MULTIPLICATION_BY_K_WITH_ROUND5;
        }

        return SUM;
    }

    private GraphLinkType getLinkType(GraphMeasureCalculationType calculationType, GraphMeasurePart measurePart) {
        return switch (measurePart) {
            case HOURS -> switch (calculationType) {
                case SIMPLE, SUM -> CHILD;
                case MULTIPLICATION, MULTIPLICATION_BY_K_WITH_ROUND, MULTIPLICATION_BY_K_WITH_ROUND5 -> NEIGHBOR;
            };
            case AGGREGATION_HOURS, AGGREGATION_MONEY_WITHOUT_NDS, AGGREGATION_MONEY_WITH_NDS -> CHILD;
        };
    }

    private record MeasureType(
            CostProjectItemDetailMeasureType current,
            CostProjectItemDetailMeasureType original,
            CostProjectItemDetailMeasureType round
    ) {
    }

    private MeasureType backendMeasureType() {
        return getMeasureType(itemDetail.backendCost(), BACKEND);
    }

    private MeasureType analyseMeasureType() {
        return getMeasureType(itemDetail.analyseCost(), ANALYSE);
    }

    private MeasureType frontendMeasureType() {
        return getMeasureType(itemDetail.frontendCost(), FRONTEND);
    }

    private MeasureType devMeasureType() {
        return getMeasureType(itemDetail.devCost(), DEV);
    }

    private MeasureType qaMeasureType() {
        return getMeasureType(itemDetail.qaCost(), QA);
    }

    private MeasureType devopsMeasureType() {
        return getMeasureType(itemDetail.devOpsCost(), DEVOPS);
    }

    private MeasureType otherMeasureType() {
        return getMeasureType(itemDetail.otherCost(), OTHER);
    }

    private MeasureType tmMeasureType() {
        return getMeasureType(itemDetail.tmCost(), TM);
    }

    private MeasureType pmMeasureType() {
        return getMeasureType(itemDetail.pmCost(), PM);
    }
}
