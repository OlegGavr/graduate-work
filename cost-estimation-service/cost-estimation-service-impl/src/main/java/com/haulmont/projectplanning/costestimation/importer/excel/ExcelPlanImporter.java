package com.haulmont.projectplanning.costestimation.importer.excel;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.importer.AbstractTransactionalCostProjectImporter;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItem;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectItemDetail;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectItemService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectMeasureService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectOrderingService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import com.haulmont.projectplanning.exception.CostEstimationException;
import com.haulmont.projectplanning.exception.importer.excel.ExcelImporterIoException;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import io.vavr.control.Try;
import org.apache.commons.math3.util.Precision;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;

import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findCostItemById;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findCostItemByName;
import static com.haulmont.projectplanning.costestimation.tool.CostProjectItemTools.findParentCostItemById;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Only one level hierarchy is supported
 */
@Order(15)
@Component
@Scope(SCOPE_PROTOTYPE)
public class ExcelPlanImporter extends AbstractTransactionalCostProjectImporter {

    private static Logger logger = LoggerFactory.getLogger(ExcelPlanImporter.class);

    public static final String DEFAULT_SHEET_NAME_RU = "Скоуп Работ";

    public static final String DEFAULT_SHEET_NAME_EN = "Scope of work";

    private Calculation calculation;

    private CostProjectService costProjectService;

    private CostProjectItemService costProjectItemService;

    private CostProjectOrderingService costProjectOrderingService;

    private CostProjectMeasureService costProjectItemMeasureService;

    private CostProjectMongoRepository costProjectMongoRepository;

    private String sheetName;

    private Workbook workbook;

    private Sheet sheet;

    private FormulaEvaluator evaluator;

    // curr iteration context
    private TableHeader tableHeader;

    private CostProject costProject;

    private Row currRow;

    private CostProjectItem currCostItem;

    private Deque<ExcelFeatureStackItem> featureStack = new ArrayDeque<>();


    public ExcelPlanImporter(Calculation calculation,
                             CostProjectService costProjectService,
                             CostProjectItemService costProjectItemService,
                             CostProjectOrderingService costProjectOrderingService,
                             CostProjectMeasureService costProjectItemMeasureService,
                             CostProjectMongoRepository costProjectMongoRepository) {
        super(calculation, costProjectService, costProjectItemService, costProjectMongoRepository);

        this.calculation = calculation;
        this.costProjectService = costProjectService;
        this.costProjectItemService = costProjectItemService;
        this.costProjectOrderingService = costProjectOrderingService;
        this.costProjectItemMeasureService = costProjectItemMeasureService;
        this.costProjectMongoRepository = costProjectMongoRepository;
    }


    @Transactional // performance purpose
    public CostProject doImport(Workbook workbook, NamedImportParams params) {
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
        var costProject = costProjectMongoRepository.findById(costProjectId).orElseThrow();

        return doImport(costProject, workbook, params);
    }

    @Transactional // performance purpose
    public CostProject doImport(CostProject costProject, Workbook workbook, NamedImportParams params) {

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
        var lWorkbook = Try.of(() -> WorkbookFactory.create(new BufferedInputStream(inputStream)))
                .getOrElseThrow(e -> new ExcelImporterIoException(e));

        return internalDoImport(costProject, lWorkbook, params);
    }

    protected CostProject internalDoImport(CostProject costProject, Workbook workbook, NamedImportParams params) {
        this.costProject = costProject;

        this.workbook = workbook;

        this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        this.sheetName = Optional.ofNullable(sheetName)
                .or(() -> autoDefineSheetName(workbook)).orElseThrow();

        this.sheet = workbook.getSheet(this.sheetName);

        return internalDoImportSheet();
    }

    private Optional<String> autoDefineSheetName(Workbook workbook) {
        var lSheet = workbook.getSheet(DEFAULT_SHEET_NAME_RU);
        if (lSheet != null) {
            return Optional.of(DEFAULT_SHEET_NAME_RU);
        }

        lSheet = workbook.getSheet(DEFAULT_SHEET_NAME_EN);
        if (lSheet != null) {
            return Optional.of(DEFAULT_SHEET_NAME_EN);
        }

        return Optional.empty();
    }

    @Transactional // performance purpose
    public CostProject doImportSheet(String costProjectId, Workbook workbook, Sheet sheet) {

        this.costProject = costProjectMongoRepository.findById(costProjectId).orElseThrow();

        return this.doImportSheet(costProject, workbook, sheet);
    }

    @Transactional // performance purpose
    public CostProject doImportSheet(CostProject costProject, Workbook workbook, Sheet sheet) {

        this.costProject = costProject;
        this.workbook = workbook;
        this.sheet = sheet;
        this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        return internalDoImportSheet();
    }

    private CostProject internalDoImportSheet() {

        this.tableHeader = createTableHeader();

        logger.debug("Main. Table Header {}", tableHeader);

        // analyze rows
        var rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            this.currRow = rowIterator.next();

            var rowType = defineRowType(currRow, true);

            if (logger.isTraceEnabled()) {
                var rowString = StreamSupport.stream(currRow.spliterator(), false)
                        .map(Cell::toString).collect(joining(" | "));

                logger.debug("Main. Work with row type: {}, Row: {}", rowType, rowString);
            }

            switch (rowType) {
                case FEATURE -> createFeature(tableHeader, currRow);
                case TABLE_HEADER -> {
                    // ignore always
                }
                case OTHER -> {
                    // skip always
                }
            }
        }
        return costProject;
    }

    private void createFeature(TableHeader tableHeader, Row row) {

        var titleCell = StreamSupport.stream(row.spliterator(), false)
                .filter(c -> c.getCellType() != CellType.BLANK).findFirst()
                .orElseThrow();

        this.currCostItem = findCostItemAccordinglyStackByName(titleCell);
        if (currCostItem == null) {
            String titleValue = titleCell.getStringCellValue();

            this.currCostItem = new CostProjectItem(new ObjectId().toString(), titleValue);
            this.costProject = costProjectItemService.createCostItem(costProject.id(), currCostItem, false);

            this.currCostItem = findCostItemById(costProject, currCostItem.id()).orElseThrow();
        }

        // remove items from the stack on the same level or lower in Excel
        rollUpStackToParentCell(titleCell);
        if (! featureStack.isEmpty()) {
            var topOnStack = featureStack.peek();

            this.costProject = costProjectOrderingService.moveCostItemAsSubItem(
                    costProject.id(), topOnStack.costItemId(), currCostItem.id(), false);
        }

        featureStack.push(new ExcelFeatureStackItem(currCostItem.id(), currCostItem.name(), titleCell.getColumnIndex()));

        defineMeasureBasedOnDetails(currCostItem.original(), tableHeader.original());
        defineComment();
    }

    private void rollUpStackToParentCell(Cell cell) {
        while (featureStack.peek() != null
                && ! isParentStackItem(featureStack.peek(), cell)) {

            featureStack.pop(); // remove element from stack
        }
    }

    private CostProjectItem findCostItemAccordinglyStackByName(Cell titleCell) {
        var costItem = findCostItemByName(this.costProject, titleCell.getStringCellValue()).orElse(null);

        if (costItem == null) {
            return null;
        }

        var parentCostItem = findParentCostItemById(this.costProject, costItem.id()).orElseThrow();

        var parentStackItem = findParentStackItem(titleCell);
        if (parentStackItem == null) {
            return null;
        }

        return parentStackItem.excelRowTitle().equals(parentCostItem.name()) ? costItem : null;
    }

    private ExcelFeatureStackItem findParentStackItem(Cell cell) {

        return featureStack.stream()
                .filter(s -> isParentStackItem(s, cell))
                .findFirst().orElse(null);
    }

    private boolean isParentStackItem(ExcelFeatureStackItem stackItem, Cell cell) {
        return stackItem.excelColumnNum() < cell.getColumnIndex();
    }

    private void defineMeasureBasedOnDetails(CostProjectItemDetail costProjectItemDetail,
                                             TableBlockHeader tableBlockHeader) {

        BiConsumer<String, Integer> localDefineMeasureBasedOnType = (mId, cellIdx) -> {
            if (cellIdx == null) {
                return;
            }

            var cell = currRow.getCell(cellIdx);
            if (cell == null) {
                return;
            }

            defineMeasureBasedOnType(mId, cell);
        };

        localDefineMeasureBasedOnType.accept(costProjectItemDetail.backendCost(), tableBlockHeader.beIdx());
        localDefineMeasureBasedOnType.accept(costProjectItemDetail.frontendCost(), tableBlockHeader.feIdx());
        localDefineMeasureBasedOnType.accept(costProjectItemDetail.qaCost(), tableBlockHeader.qaIdx());
    }
    private void defineMeasureBasedOnType(String measureId, Cell cell) {

        if (cell.getCellType() == CellType.NUMERIC) {
            this.costProject = costProjectItemMeasureService.defineManualMeasure(
                    costProject.id(), currCostItem.id(), measureId, Precision.round(cell.getNumericCellValue(), 5), false);
        }

        if (cell.getCellType() == CellType.FORMULA) {
            this.costProject = costProjectItemMeasureService.defineAutoMeasure(
                    costProject.id(), currCostItem.id(), measureId, Precision.round(cell.getNumericCellValue(), 5));
        }
    }

    private void defineComment() {
        if (tableHeader.commentIdx() == null) {
            return;
        }

        var cell = currRow.getCell(tableHeader.commentIdx());
        if (cell == null) {
            return;
        }

        var cellValue = cell.getStringCellValue();
        this.costProject = costProjectMongoRepository
                .updateCostItemComment(costProject.id(), currCostItem.id(), cellValue);
    }

    private TableHeader createTableHeader() {
        Row headerRow = null;

        // find header row
        var rowIterator = this.sheet.rowIterator();
        while (rowIterator.hasNext()) {
            var row = rowIterator.next();

            var rowType = defineRowType(row, false);

            if (logger.isTraceEnabled()) {
                var rowString = Stream.ofAll(StreamSupport.stream(row.spliterator(), false))
                        .map(Cell::toString)
                        .collect(joining(" | "));

                logger.trace("Create Header. Work with row type: {}, Row: {}", rowType, rowString);
            }

            if (rowType == RowType.TABLE_HEADER) {
                headerRow = row;

                break;
            }
        }

        if (headerRow == null) {
            throw new RuntimeException(format("Header not found: %s", headerRow));
        }

        if (logger.isTraceEnabled()) {
            StreamSupport.stream(headerRow.spliterator(), false)
                    .forEach(c -> logger.info("Create Header. Cell. Column Index: {}, V: {}, T: {}",
                            c.getColumnIndex(), c.getStringCellValue(), c.getCellType()));
        }

        var headerCells = StreamSupport.stream(headerRow.spliterator(), false)
                .filter(c -> c.getCellType() != CellType.BLANK)
                .toList();

        var firstNotFeatureColumn = headerCells.stream()
                .filter(c -> ! c.getStringCellValue().contains("Feature"))
                .findFirst().orElseThrow();

        var firstNotFeatureColumnIndex = firstNotFeatureColumn.getColumnIndex();
        logger.debug("Feature column index: {}", firstNotFeatureColumnIndex);

        var originalBlockHeader = createTableBlockHeader(headerRow);

        var commentColumnIndex = headerCells.stream()
                .filter(c -> List.of("Comment", "Комментарий").contains(c.getStringCellValue()))
                .findAny().map(Cell::getColumnIndex).orElse(null);

        // all cells from first column to the first not Feature column
        return new TableHeader(Tuple.of(0, firstNotFeatureColumnIndex),
                originalBlockHeader, commentColumnIndex);
    }

    private TableBlockHeader createTableBlockHeader(Row header) {
        var beIdx = new AtomicReference<Integer>();
        var feIdx = new AtomicReference<Integer>();
        var qaIdx = new AtomicReference<Integer>();

        header.spliterator().forEachRemaining(cell -> {
            var columnIndex = cell.getColumnIndex();

            switch (cell.getStringCellValue()) {
                case "BE" -> beIdx.set(columnIndex);
                case "FE" -> feIdx.set(columnIndex);
                case "QA" -> qaIdx.set(columnIndex);
            }
        });

        return new TableBlockHeader(beIdx.get(), feIdx.get(), qaIdx.get());
    }

    private RowType defineRowType(Row row, Boolean withEvaluation) {
        var firstNotEmpty = StreamSupport.stream(row.spliterator(), false)
                .filter(c -> ! c.getCellType().equals(CellType.BLANK))
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

        if (cellValue.contains("Feature")) {
            return RowType.TABLE_HEADER;
        }

        return RowType.FEATURE;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    @Override
    public Boolean isApplicable(InputStream inputStream) {

        var lWorkbook = Try.of(() -> WorkbookFactory.create(inputStream))
                .getOrElseThrow(e -> new ExcelImporterIoException(e));

        return isApplicable(lWorkbook);
    }

    public Boolean isApplicable(Workbook workbook) {

        var lSheetName = Optional.ofNullable(this.sheetName)
                .filter(name -> workbook.getSheet(name) != null)
                .or(() -> autoDefineSheetName(workbook));

        return lSheetName.isPresent();
    }

    public enum RowType {
        TABLE_HEADER, FEATURE, OTHER
    }

    protected record TableHeader(
            Tuple2<Integer, Integer> titleIdxRange,
            TableBlockHeader original,
            Integer commentIdx
    ) {}

    protected record TableBlockHeader(
            Integer beIdx,
            Integer feIdx,
            Integer qaIdx
    ) {}

    protected record ExcelFeatureStackItem(
            String costItemId,
            String excelRowTitle,
            Integer excelColumnNum
    ) {}
}
