package com.haulmont.projectplanning.exception.importer.excel;

import com.haulmont.projectplanning.exception.CostEstimationException;

public class ExcelCostSheetNotFoundException extends CostEstimationException {

    public ExcelCostSheetNotFoundException() {
    }

    public ExcelCostSheetNotFoundException(String message) {
        super(message);
    }

    public ExcelCostSheetNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelCostSheetNotFoundException(Throwable cause) {
        super(cause);
    }
}
