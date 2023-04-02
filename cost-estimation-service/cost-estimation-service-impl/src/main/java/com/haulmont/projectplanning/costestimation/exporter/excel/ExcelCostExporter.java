package com.haulmont.projectplanning.costestimation.exporter.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.haulmont.projectplanning.costestimation.api.mapper.CostProjectMapper;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.organization.api.model.CostProjectItemDto;
import com.haulmont.projectplanning.organization.api.model.CostProjectRiskDto;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.reporting.DataExtractorImpl;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.BandBuilder;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.structure.impl.ReportTemplateBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ExcelCostExporter {

    public static final String REPORT_TEMPLATE_PATH = "classpath:exporter/excel/CostPlanTemplate.xlsx";

    private ResourceLoader resourceLoader;

    // we use objectMapper because it is easy way to use json loader for YARG
    private ObjectMapper objectMapper;

    private CostProjectMapper costProjectMapper;

    private CostProjectMongoRepository costProjectMongoRepository;

    public ExcelCostExporter(ResourceLoader resourceLoader,
                             ObjectMapper objectMapper, CostProjectMapper costProjectMapper,
                             CostProjectMongoRepository costProjectMongoRepository) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.costProjectMapper = costProjectMapper;
        this.costProjectMongoRepository = costProjectMongoRepository;
    }

    public void doExport(String costProjectId, OutputStream outputStream) throws IOException {

        var costProject = costProjectMongoRepository.findById(costProjectId).orElseThrow();

        // preparing data
        var costProjectDto = costProjectMapper.costProjectToCostProjectDto(costProject);
        var idOnCostProjectItem = costProjectDto.getProjectItems().stream()
                .collect(toMap(CostProjectItemDto::getId, identity()));

        var reportRisk = prepareRisksToReport(costProjectDto.getRisk());
        var reportMoneyPerHour = Map.of("moneyPerHour", costProjectDto.getMoneyPerHour());
        var reportTableAggregates = costProjectDto.getAggregate();

        // prepare parent projectItems
        var parentItems = createTableParentItems(costProject);
        var reportTableParentItems = parentItems.stream()
                .map(pi -> {
                    var costProjectItemDto = idOnCostProjectItem.get(pi.id);
                    costProjectItemDto.setName(pi.name);
                    return costProjectItemDto;
                }).toList();

        // prepare items
        var parentIds = parentItems.stream()
                .map(TableParentItem::id)
                .collect(toSet());
        var reportTableItems = costProjectDto.getProjectItems().stream()
                .filter(i -> ! parentIds.contains(i.getId())).toList();

        // read report template from classpath
//        var reportTemplateFile = ResourceUtils.getFile(REPORT_TEMPLATE_PATH);
        byte[] reportTemplateContent;
        try (var templateIs = resourceLoader.getResource(REPORT_TEMPLATE_PATH).getInputStream()) {
            reportTemplateContent = templateIs.readAllBytes();
        }

        // configure report
        ReportBuilder reportBuilder = new ReportBuilder();

        ReportTemplateBuilder reportTemplateBuilder = new ReportTemplateBuilder()
                .documentContent(reportTemplateContent)
                .documentPath(REPORT_TEMPLATE_PATH)
                .documentName("CostPlan.xlsx")
                .outputType(ReportOutputType.xlsx);

        reportBuilder.template(reportTemplateBuilder.build());
        BandBuilder bandBuilder = new BandBuilder();
        ReportBand aggregate = bandBuilder.name("Aggregate")
                .query("Aggregate", "parameter=tableAggregates$", "json").build();
        ReportBand risk = bandBuilder.name("Risk")
                .query("Risk", "parameter=risk$", "json").build();
        ReportBand moneyPerHour = bandBuilder.name("MoneyPerHour")
                .query("MoneyPerHour", "parameter=moneyPerHour$", "json").build();
        ReportBand costItemsHeader = bandBuilder.name("CostItemsHeader").build();
        ReportBand costItems = bandBuilder.name("CostItems")
                .query("CostItems", "parameter=tableItems$[?(@.parentId == '${CostItemGroups.id}')]", "json").build();
        ReportBand costItemGroups = bandBuilder.name("CostItemGroups")
                .query("CostItemGroups", "parameter=tableParentItems$[*]", "json").child(costItems).build();
        ReportBand costItemsFooter = bandBuilder.name("CostItemsFooter").build();
        ReportBand doNotSend = bandBuilder.name("DoNotSend").build();

        // band ordering is important
        reportBuilder
                .band(doNotSend)
                .band(aggregate)
                .band(costItemsHeader)
                .band(costItemGroups)
                .band(costItemsFooter)
                .band(risk)
                .band(moneyPerHour);

        Report report = reportBuilder.build();


        var loaderFactory = new DefaultLoaderFactory().setJsonDataLoader(new JsonDataLoader());

        var dataExtractor = new DataExtractorImpl(loaderFactory);
        dataExtractor.setPutEmptyRowIfNoDataSelected(false);

        Reporting reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setDataExtractor(dataExtractor);
        reporting.setLoaderFactory(loaderFactory);

        var reportParams = new RunParams(report)
                .param("moneyPerHour", objectMapper.writeValueAsString(reportMoneyPerHour))
                .param("risk", objectMapper.writeValueAsString(reportRisk))
                .param("tableAggregates", objectMapper.writeValueAsString(reportTableAggregates))
                .param("tableParentItems", objectMapper.writeValueAsString(reportTableParentItems))
                .param("tableItems", objectMapper.writeValueAsString(reportTableItems));

        reporting.runReport(reportParams, outputStream);
    }

    // side effect function
    private CostProjectRiskDto prepareRisksToReport(CostProjectRiskDto risk) {
        BiConsumer<Supplier<Double>, Consumer<Double>> prepareRisk =
                (getter, setter) -> {
                    var value = getter.get();
                    if (value == null) {
                        setter.accept(risk.getDefault());
                    }
                };

        prepareRisk.accept(risk::getDev, risk::setDev);
        prepareRisk.accept(risk::getQa, risk::setQa);
        prepareRisk.accept(risk::getBa, risk::setBa);
        prepareRisk.accept(risk::getDevOps, risk::setDevOps);
        prepareRisk.accept(risk::getTm, risk::setTm);
        prepareRisk.accept(risk::getPm, risk::setPm);

        return risk;
    }

    private List<TableParentItem> createTableParentItems(CostProject costProject) {

        var idOnProjectItem = costProject.projectItems().stream()
                .collect(toMap(CostProjectItem::id, identity()));

        var parentChainsBasedOnGo = costProject.globalOrder().stream()
                .map(o -> {
                    if (o.parentItemIds().size() == 1 && o.parentItemIds().get(0).equals(costProject.rootItemId())) {
                        return List.of(o.projectItemId(), costProject.rootItemId());
                    } else {
                        return o.parentItemIds();
                    }
                })
                .distinct().toList();

        return parentChainsBasedOnGo.stream()
                .map(pl -> new TableParentItem(pl.get(0), Lists.reverse(pl).stream()
                        .flatMap(p -> Optional.ofNullable(idOnProjectItem.get(p)).stream())
                        .filter(ci -> ! ci.id().equals(costProject.rootItemId())) // do not include Root Item in Name
                        .map(CostProjectItem::name)
                        .collect(Collectors.joining(". "))))
                .toList();
    }

    private record TableParentItem(
            String id,
            String name
    ) {}
}
