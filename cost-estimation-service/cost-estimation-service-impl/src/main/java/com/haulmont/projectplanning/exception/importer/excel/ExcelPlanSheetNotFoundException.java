package com.haulmont.projectplanning.exception.importer.excel;

public class ExcelPlanSheetNotFoundException extends ExcelCostProjectImporterException {

    public ExcelPlanSheetNotFoundException() {
    }

    public ExcelPlanSheetNotFoundException(String message) {
        super(message);
    }

    public ExcelPlanSheetNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelPlanSheetNotFoundException(Throwable cause) {
        super(cause);
    }
}
