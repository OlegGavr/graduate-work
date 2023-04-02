package com.haulmont.projectplanning.costestimation.importer.excel;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.importer.AbstractTransactionalCostProjectImporter;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetail;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectRisk;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectItemService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectMeasureService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectOrderingService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectRiskService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import com.haulmont.projectplanning.exception.CostEstimationException;
import com.haulmont.projectplanning.exception.importer.excel.ExcelImporterIoException;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import org.apache.commons.math3.util.Precision;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectRisk.DEFAULT_RISK;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findCostItemById;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findCostItemByName;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Only one level hierarchy is supported
 */
@Order // max int by default
@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ExcelCostImporter extends AbstractTransactionalCostProjectImporter {

    private static Logger logger = LoggerFactory.getLogger(ExcelCostImporter.class);

    private Calculation calculation;

    private CostProjectService costProjectService;

    private CostProjectItemService costProjectItemService;

    private CostProjectOrderingService costProjectOrderingService;

    private CostProjectMeasureService costProjectItemMeasureService;

    private CostProjectRiskService costProjectRiskService;

    private CostProjectMongoRepository costProjectMongoRepository;

    private LanguageDescriptor languageDescriptor;

    private Workbook workbook;

    private Sheet sheet;

    private FormulaEvaluator evaluator;

    // curr iteration context
    private TableHeader tableHeader;

    private CostProject costProject;

    private Row currRow;

    private CostProjectItem currCostItem;

    private CostProjectItem currParentCostItem = null;


    public ExcelCostImporter(Calculation calculation,
                             CostProjectService costProjectService,
                             CostProjectItemService costProjectItemService,
                             CostProjectOrderingService costProjectOrderingService,
                             CostProjectMeasureService costProjectItemMeasureService,
                             CostProjectRiskService costProjectRiskService,
                             CostProjectMongoRepository costProjectMongoRepository) {

        super(calculation, costProjectService, costProjectItemService, costProjectMongoRepository);
        this.calculation = calculation;
        this.costProjectService = costProjectService;
        this.costProjectItemService = costProjectItemService;
        this.costProjectOrderingService = costProjectOrderingService;
        this.costProjectItemMeasureService = costProjectItemMeasureService;
        this.costProjectRiskService = costProjectRiskService;
        this.costProjectMongoRepository = costProjectMongoRepository;
    }

    @Transactional // performance purpose
    public CostProject doImport(InputStream inputStream, ExcelCostNamedImportParams params) {
        return super.doImport(inputStream, params);
    }

    @Transactional // performance purpose
    public CostProject doImport(String costProjectId, InputStream inputStream, ExcelCostNamedImportParams params) {
        return super.doImport(costProject, inputStream, params);
    }

    @Transactional // performance purpose
    public CostProject doImport(CostProject costProject, InputStream inputStream, ExcelCostNamedImportParams params) {
        return super.doImport(costProject, inputStream, params);
    }

    @Transactional // performance purpose
    public CostProject doImport(Workbook workbook, NamedImportParams params) {
        return doImport(workbook, new ExcelCostNamedImportParamsImpl(params));
    }

    @Transactional // performance purpose
    public CostProject doImport(Workbook workbook, ExcelCostNamedImportParams params) {
        var costProject = costProjectService.create(false);

        try {
            return doImport(costProject, workbook, params);
        } catch (CostEstimationException e) {
            costProjectMongoRepository.deleteById(costProject.id());

            throw e;
        }
    }
    @Transactional // performance purpose
    public CostProject doImport(String costProjectId, Workbook workbook, NamedImportParams params) {
        return doImport(costProjectId, workbook, new ExcelCostNamedImportParamsImpl(params));
    }

    @Transactional // performance purpose
    public CostProject doImport(String costProjectId, Workbook workbook, ExcelCostNamedImportParams params) {
        var costProject = costProjectMongoRepository.findById(costProjectId).orElseThrow();

        return doImport(costProject, workbook, params);
    }

    @Transactional // performance purpose
    public CostProject doImport(CostProject costProject, Workbook workbook, NamedImportParams params) {
        return doImport(costProject, workbook, new ExcelCostNamedImportParamsImpl(params));
    }



    @Transactional // performance purpose
    public CostProject doImport(CostProject costProject, Workbook workbook, ExcelCostNamedImportParams params) {

        var actualCostProject = switch (params.applyStrategy()) {
            case APPEND -> costProject;
            case REPLACE -> costProjectItemService.deleteAllCostItems(costProject.id());
            case OVERLAY -> costProject;
        };

        actualCostProject = this.internalDoImport(actualCostProject, workbook, params);

        if (params.recalculate()) {
            actualCostProject = calculation.calculate(actualCostProject);
        }

        return actualCostProject;
    }

    @Override
    protected CostProject internalDoImport(CostProject costProject, InputStream inputStream, NamedImportParams params) {
        var wrapperParam = new ExcelCostNamedImportParamsImpl(params);

        var lWorkbook = Try.of(() -> WorkbookFactory.create(new BufferedInputStream(inputStream)))
                .getOrElseThrow(e -> new ExcelImporterIoException(e));

        return internalDoImport(costProject, lWorkbook, wrapperParam);
    }

    protected CostProject internalDoImport(CostProject costProject, Workbook workbook, ExcelCostNamedImportParams params) {

        this.costProject = costProject;
        this.workbook = workbook;

        this.evaluator = this.workbook.getCreationHelper().createFormulaEvaluator();

        this.languageDescriptor = Optional.ofNullable(params.languageDescriptor())
                .or(() -> autoDefineLanguageDescriptor(this.workbook)).orElseThrow();

        this.sheet = this.workbook.getSheet(this.languageDescriptor.sheetName);

        return internalDoImportSheet();
    }

    private Optional<LanguageDescriptor> autoDefineLanguageDescriptor(Workbook workbook) {
        var sheet = workbook.getSheet(LanguageDescriptor.RU.sheetName());
        if (sheet != null) {
            return Optional.of(LanguageDescriptor.RU);
        }

        sheet = workbook.getSheet(LanguageDescriptor.EN.sheetName());
        if (sheet != null) {
            return Optional.of(LanguageDescriptor.EN);
        }

        return Optional.empty();
    }

    private CostProject internalDoImportSheet() {

        this.tableHeader = createTableHeader();

        logger.debug("Main. Table Header {}", tableHeader);

        // analyze rows
        var rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            currRow = rowIterator.next();

            var rowType = defineRowType(currRow, true);

            if (logger.isTraceEnabled()) {
                var rowString = StreamSupport.stream(currRow.spliterator(), false)
                        .map(Cell::toString).collect(joining(" | "));

                logger.trace("Main. Work with row type: {}, Row: {}", rowType, rowString);
            }

            switch (rowType) {
                case TABLE_HEADER_LEVEL_2 -> processHeaderLevel2();
                case FEATURE -> defineFeature();
                case TOTAL_HOURS -> defineTotalHours();
                case TOTAL_MONEY_WITHOUT_NDS -> defineTotalMoneyWithoutNds();
                case TOTAL_MONEY_WITH_NDS_20 -> defineTotalMoneyWithNds20();
                // ignore other cases
            }
        }

        return costProject;
    }

    private void processHeaderLevel2() {
        // define risks
        var riskValuesRow = sheet.getRow(currRow.getRowNum() + 1);

        var riskMap = new HashMap<String, Double>();
        var isRiskCellsFound = new AtomicBoolean(false);
        currRow.spliterator().forEachRemaining(cell -> {
            var cellName = cell.getStringCellValue();
            if (!isRiskCellsFound.get()) {
                if (languageDescriptor.defaultRiskColumnName().equals(cellName)) {
                    isRiskCellsFound.set(true);
                }
            }

            if (isRiskCellsFound.get()) {
                var value = riskValuesRow.getCell(cell.getColumnIndex()).getNumericCellValue();
                riskMap.put(cellName, value);
            }
        });

        var defaultRisk = riskMap.get(languageDescriptor.defaultRiskColumnName());
        if (defaultRisk == null) {
            defaultRisk = DEFAULT_RISK;
        }

        var projectRisk = new CostProjectRisk(
                defaultRisk,
                riskMap.get(languageDescriptor.devRiskColumnName()),
                riskMap.get(languageDescriptor.qaRiskColumnName()),
                riskMap.get(languageDescriptor.baRiskColumnName()),
                riskMap.get(languageDescriptor.devOpsRiskColumnName()),
                riskMap.get(languageDescriptor.tmRiskColumnName()),
                riskMap.get(languageDescriptor.pmRiskColumnName()));

        this.costProject = costProjectRiskService
                .defineRisks(costProject.id(), projectRisk, false);


        // define money per hours
        var moneyPerHour = riskMap.get(languageDescriptor.moneyPerHourColumnName());
        if (moneyPerHour != null) {
            this.costProject = costProjectService
                    .defineMoneyPerHour(costProject.id(), moneyPerHour.intValue(), false);
        }
    }

    private void defineTotalMoneyWithoutNds() {
        var totalHoursCostItemId = costProject.aggregateItems().aggregatedMoneyWithoutNdsCostItemId();
        this.currCostItem = findCostItemById(costProject, totalHoursCostItemId).orElseThrow();

        defineMeasureBasedOnDetails(currCostItem.original(), tableHeader.original());
        defineMeasureBasedOnDetails(currCostItem.multipliedByKWithRound(), tableHeader.multiplyByKWithRound());
        defineMeasureBasedOnDetails(currCostItem.multipliedByKWithRound5(), tableHeader.multiplyByKWithRound5());
    }

    private void defineTotalMoneyWithNds20() {
        var totalHoursCostItemId = costProject.aggregateItems().aggregatedMoneyWithNds20CostItemId();
        this.currCostItem = findCostItemById(costProject, totalHoursCostItemId).orElseThrow();

        defineMeasureBasedOnDetails(currCostItem.original(), tableHeader.original());
        defineMeasureBasedOnDetails(currCostItem.multipliedByKWithRound(), tableHeader.multiplyByKWithRound());
        defineMeasureBasedOnDetails(currCostItem.multipliedByKWithRound5(), tableHeader.multiplyByKWithRound5());
    }

    private void defineTotalHours() {
        var totalHoursCostItemId = costProject.aggregateItems().aggregatedHoursCostItemId();
        this.currCostItem = findCostItemById(costProject, totalHoursCostItemId).orElseThrow();

        defineMeasureBasedOnDetails(currCostItem.original(), tableHeader.original());
        defineMeasureBasedOnDetails(currCostItem.multipliedByKWithRound(), tableHeader.multiplyByKWithRound());
        defineMeasureBasedOnDetails(currCostItem.multipliedByKWithRound5(), tableHeader.multiplyByKWithRound5());
    }

    private void defineFeature() {

        var titleCell = currRow.getCell(tableHeader.titleIdx);
        var titleValue = titleCell.getStringCellValue();

        this.currCostItem = findCostItemByName(costProject, titleValue).orElse(null);
        if (currCostItem == null) {
            this.currCostItem = new CostProjectItem(new ObjectId().toString(), titleValue);
            this.costProject = costProjectItemService.createCostItem(costProject.id(), currCostItem, false);

            this.currCostItem = findCostItemById(costProject, currCostItem.id()).orElseThrow();
        }

        if (isHeaderBasedOnStyle(titleCell)) {
            this.currParentCostItem = currCostItem;
        }

        // check after and equals for the case if two bold item in a row
        if (currParentCostItem != null && !currParentCostItem.id().equals(currCostItem.id())) {
            costProject = costProjectOrderingService.moveCostItemAsSubItem(
                    costProject.id(), currParentCostItem.id(), currCostItem.id(), false);
        }

        defineMeasureBasedOnDetails(currCostItem.original(), tableHeader.original());
        defineMeasureBasedOnDetails(currCostItem.multipliedByKWithRound(), tableHeader.multiplyByKWithRound());
        defineMeasureBasedOnDetails(currCostItem.multipliedByKWithRound5(), tableHeader.multiplyByKWithRound5());
    }

    private boolean isHeaderBasedOnStyle(Cell cell) {
        if (cell.getCellStyle() instanceof XSSFCellStyle xssfTitleCell) {
            return xssfTitleCell.getFont().getBold();
        }

        if (cell.getCellStyle() instanceof HSSFCellStyle hssfTitleCell) {
            return hssfTitleCell.getFont(workbook).getBold();
        }

        return false;
    }

    private void defineMeasureBasedOnDetails(CostProjectItemDetail costProjectItemDetail,
                                             TableBlockHeader tableBlockHeader) {

        BiConsumer<String, Integer> localDefineMeasureBasedOnType = (mId, cellIdx) -> {
            if (cellIdx != null) {
                defineMeasureBasedOnType(currCostItem, mId, currRow.getCell(cellIdx, CREATE_NULL_AS_BLANK));
            }
        };

        localDefineMeasureBasedOnType.accept(costProjectItemDetail.devCost(), tableBlockHeader.devIdx());
        localDefineMeasureBasedOnType.accept(costProjectItemDetail.qaCost(), tableBlockHeader.qaIdx());
        localDefineMeasureBasedOnType.accept(costProjectItemDetail.analyseCost(), tableBlockHeader.analyseIdx());
        localDefineMeasureBasedOnType.accept(costProjectItemDetail.devOpsCost(), tableBlockHeader.devOpsIdx());
        localDefineMeasureBasedOnType.accept(costProjectItemDetail.tmCost(), tableBlockHeader.tmIdx());
        localDefineMeasureBasedOnType.accept(costProjectItemDetail.pmCost(), tableBlockHeader.managementIdx());
        localDefineMeasureBasedOnType.accept(costProjectItemDetail.fullCost(), tableBlockHeader.totalIdx());
    }

    private void defineMeasureBasedOnType(CostProjectItem costItem, String measureId, Cell cell) {

        if (cell.getCellType() == CellType.NUMERIC) {
            this.costProject = costProjectItemMeasureService.defineManualMeasure(
                    costProject.id(), costItem.id(), measureId, Precision.round(cell.getNumericCellValue(), 5), false);
        }

        if (cell.getCellType() == CellType.FORMULA) {
            this.costProject = costProjectItemMeasureService.defineAutoMeasure(
                    costProject.id(), costItem.id(), measureId, Precision.round(cell.getNumericCellValue(), 5));
        }
    }

    private TableHeader createTableHeader() {
        Row level1Header = null;
        Row level2Header = null;

        // create table header
        var rowIterator = this.sheet.rowIterator();
        while (rowIterator.hasNext()) {
            var row = rowIterator.next();

            var rowType = defineRowType(row, false);

            if (logger.isTraceEnabled()) {
                var rowString = StreamSupport.stream(row.spliterator(), false)
                        .map(Cell::toString).collect(joining(" | "));

                logger.trace("Create Header. Work with row type: {}, Row: {}", rowType, rowString);
            }

            if (rowType == RowType.TABLE_HEADER_LEVEL_1) {
                level1Header = row;

                continue;
            }

            if (rowType == RowType.TABLE_HEADER_LEVEL_2) {
                level2Header = row;

                continue;
            }

            if (level1Header != null && level2Header != null) {
                break;
            }
        }

        if (level1Header == null || level2Header == null) {
            throw new RuntimeException(format("One of the headers not found. Level1: %s, level2: %s", level1Header, level2Header));
        }

        if (logger.isTraceEnabled()) {
            StreamSupport.stream(level1Header.spliterator(), false)
                    .forEach(c -> logger.info("Create Header. Header Level 1. Cell. Idx: {}, V: {}, T: {}",
                            c.getColumnIndex(), c.getStringCellValue(), c.getCellType()));

            StreamSupport.stream(level2Header.spliterator(), false)
                    .forEach(c -> logger.info("Create Header. Header Level 2. Cell. Idx: {}, V: {}, T: {}",
                            c.getColumnIndex(), c.getStringCellValue(), c.getCellType()));
        }

        var level1HeaderCells = StreamSupport.stream(level1Header.spliterator(), false)
                .filter(c -> c.getCellType() != CellType.BLANK)
                .toList();

        var level2HeaderLastCell = (int) level2Header.getLastCellNum();
        Function<String, Tuple2<Integer, Integer>> findRangeCellsHeader = (String name) -> {

            var blockCell = level1HeaderCells.stream()
                    .filter(c -> c.getStringCellValue().contains(name))
                    .findFirst().orElseThrow();

            var blockCellIdx = level1HeaderCells.indexOf(blockCell);

            var rightBound = blockCellIdx + 1 < level1HeaderCells.size()
                    ? level1HeaderCells.get(blockCellIdx + 1).getColumnIndex() : level2HeaderLastCell;

            return Tuple.of(blockCell.getColumnIndex(), rightBound);
        };

        var titleCell = StreamSupport.stream(level2Header.spliterator(), false)
                .filter(c -> c.getStringCellValue().contains(languageDescriptor.featureColumnName()))
                .findFirst().orElseThrow();

        var originalCellRange = findRangeCellsHeader.apply(languageDescriptor.initialEstimationColumnName());
        var originalBlockHeader = createTableBlockHeader(originalCellRange._1,
                originalCellRange._2, level2Header);

        var multiplyByKWithRoundCellRange = findRangeCellsHeader.apply(languageDescriptor.withRiskEstimationColumnName());
        var multiplyByKWithRoundBlockHeader = createTableBlockHeader(multiplyByKWithRoundCellRange._1,
                multiplyByKWithRoundCellRange._2, level2Header);

        var multiplyByKWithRound5CellRange = findRangeCellsHeader.apply(languageDescriptor.roundedEstimationColumnName());
        var multiplyByKWithRound5BlockHeader = createTableBlockHeader(multiplyByKWithRound5CellRange._1,
                multiplyByKWithRound5CellRange._2, level2Header);

        return new TableHeader(titleCell.getColumnIndex(), originalBlockHeader,
                multiplyByKWithRoundBlockHeader, multiplyByKWithRound5BlockHeader);
    }

    private TableBlockHeader createTableBlockHeader(Integer from, Integer to, Row level2Header) {

        var cells = StreamSupport.stream(level2Header.spliterator(), false)
                .filter(c -> c.getColumnIndex() >= from && c.getColumnIndex() < to).toList();

        Function<String, Integer> findCellIdx = (String name) -> cells.stream()
                .filter(c -> c.getStringCellValue().contains(name))
                .findFirst().map(Cell::getColumnIndex).orElse(null);

        var devCellIdx = findCellIdx.apply(languageDescriptor.devColumnName());
        var qaCellIdx = findCellIdx.apply(languageDescriptor.qaColumnName());
        var analyseCellIdx = findCellIdx.apply(languageDescriptor.baColumnName());
        var devOpsCellIdx = findCellIdx.apply(languageDescriptor.devOpsColumnName());
        var tmCellIdx = findCellIdx.apply(languageDescriptor.tmColumnName());
        var managementCellIdx = findCellIdx.apply(languageDescriptor.pmColumnName());
        var totalCellIdx = findCellIdx.apply(languageDescriptor.totalColumnName());

        return new TableBlockHeader(devCellIdx, qaCellIdx, analyseCellIdx,
                devOpsCellIdx, tmCellIdx, managementCellIdx, totalCellIdx);
    }

    private RowType defineRowType(Row row, Boolean withEvaluation) {
        var firstNotEmpty = StreamSupport.stream(row.spliterator(), false)
                .filter(c -> !c.getCellType().equals(CellType.BLANK))
                .findFirst();

        if (firstNotEmpty.isEmpty()) {
            return RowType.OTHER;
        }

        var cell = firstNotEmpty.get();

        CellValue evalCell;
        if (withEvaluation && cell.getCellType().equals(CellType.FORMULA)) {
            evalCell = evaluator.evaluate(cell);
        } else if (cell.getCellType().equals(CellType.STRING)) {
            evalCell = new CellValue(cell.getStringCellValue());
        } else {
            return RowType.OTHER;
        }

        var cellValue = evalCell.getStringValue();

        if (cellValue == null) {
            return RowType.OTHER;
        }

        if (cellValue.contains(languageDescriptor.doNotSendCellName())) {
            return RowType.DO_NOT_SEND;
        }

        if (cellValue.contains(languageDescriptor.totalHoursCellName())) {
            return RowType.TOTAL_HOURS;
        }

        if (cellValue.contains(languageDescriptor.totalMoneyWithoutNdsCellName())) {
            return RowType.TOTAL_MONEY_WITHOUT_NDS;
        }

        if (cellValue.contains(languageDescriptor.totalMoneyWithNdsCellName())) {
            return RowType.TOTAL_MONEY_WITH_NDS_20;
        }

        if (cellValue.contains(languageDescriptor.initialEstimationColumnName())) {
            return RowType.TABLE_HEADER_LEVEL_1;
        }

        if (cellValue.contains(languageDescriptor.featureColumnName())) {
            return RowType.TABLE_HEADER_LEVEL_2;
        }

        return RowType.FEATURE;
    }

    @Override
    public Boolean isApplicable(InputStream inputStream) {
        var lWorkbook = Try.of(() -> WorkbookFactory.create(inputStream))
                .getOrElseThrow(e -> new ExcelImporterIoException(e));

        return isApplicable(lWorkbook);
    }

    public Boolean isApplicable(Workbook workbook) {

        var lLanguageDescriptor = Optional.ofNullable(languageDescriptor)
                .filter(ld -> workbook.getSheet(languageDescriptor.sheetName()) != null)
                .or(() -> autoDefineLanguageDescriptor(workbook));

        return lLanguageDescriptor.isPresent();
    }


    public record LanguageDescriptor(
        String sheetName,
        String doNotSendCellName,
        String totalHoursCellName,
        String totalMoneyWithoutNdsCellName,
        String totalMoneyWithNdsCellName,
        String initialEstimationColumnName,
        String withRiskEstimationColumnName,
        String roundedEstimationColumnName,
        String defaultRiskColumnName,
        String devRiskColumnName,
        String qaRiskColumnName,
        String baRiskColumnName,
        String devOpsRiskColumnName,
        String tmRiskColumnName,
        String pmRiskColumnName,
        String moneyPerHourColumnName,
        String featureColumnName,
        String devColumnName,
        String qaColumnName,
        String baColumnName,
        String devOpsColumnName,
        String tmColumnName,
        String pmColumnName,
        String totalColumnName
    ) {
        public static final LanguageDescriptor RU = new LanguageDescriptor("Оценка", "Не отправлять в таком виде!",
                "Всего, часов", "Всего, рублей без НДС", "Всего, рублей с НДС 20%", "Первичная оценка",
                "Оценка с риском", "Оценка округленная", "Риск", "DEV", "QA", "BA", "Devops", "TM", "PM",
                "Ставка (рублей за человеко-час)", "Функциональность", "Разработка", "Тестирование",
                "Анализ", "Devops", "TM", "Управление", "Всего");

        public static final LanguageDescriptor EN = new LanguageDescriptor("Estimate", "Не отправлять в таком виде!",
                "Total, hours", "Total, money", "Total, money", "Original estimate",
                "With risk", "Rounded", "Risk rate", "DEV", "QA", "BA", "Devops", "TM", "PM",
                "Rate (per man-hour)", "Function", "Dev", "QA", "BA", "Devops", "TM", "PM", "Total");
    }

    public enum RowType {
        DO_NOT_SEND, TOTAL_HOURS, TOTAL_MONEY_WITHOUT_NDS, TOTAL_MONEY_WITH_NDS_20,
        TABLE_HEADER_LEVEL_1, TABLE_HEADER_LEVEL_2, FEATURE, OTHER
    }

    protected record TableHeader(
            Integer titleIdx,
            TableBlockHeader original,
            TableBlockHeader multiplyByKWithRound,
            TableBlockHeader multiplyByKWithRound5
    ) {
    }

    protected record TableBlockHeader(
            Integer devIdx,
            Integer qaIdx,
            Integer analyseIdx,
            Integer devOpsIdx,
            Integer tmIdx,
            Integer managementIdx,
            Integer totalIdx
    ) {
    }

    public interface ExcelCostNamedImportParams extends NamedImportParams {

        String LANGUAGE_DESCRIPTOR = "languageDescriptor";

        LanguageDescriptor languageDescriptor();
    }

    public static class ExcelCostNamedImportParamsImpl extends NamedImportParamsImpl
            implements ExcelCostNamedImportParams {

        public ExcelCostNamedImportParamsImpl() {
            params.put(LANGUAGE_DESCRIPTOR, LanguageDescriptor.RU);
        }

        public ExcelCostNamedImportParamsImpl(Map<String, Object> params) {
            super(params);
        }

        @Override
        public LanguageDescriptor languageDescriptor() {
            return (LanguageDescriptor) params.get(LANGUAGE_DESCRIPTOR);
        }


        public ExcelCostNamedImportParamsImpl languageDescriptor(LanguageDescriptor languageDescriptorValue) {
            params.put(LANGUAGE_DESCRIPTOR, languageDescriptorValue);
            return this;
        }
    }
}
