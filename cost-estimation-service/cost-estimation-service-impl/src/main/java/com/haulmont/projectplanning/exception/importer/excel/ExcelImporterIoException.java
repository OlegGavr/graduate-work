package com.haulmont.projectplanning.exception.importer.excel;

import com.haulmont.projectplanning.exception.CostEstimationException;

public class ExcelImporterIoException extends CostEstimationException {

    public ExcelImporterIoException() {
    }

    public ExcelImporterIoException(String message) {
        super(message);
    }

    public ExcelImporterIoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelImporterIoException(Throwable cause) {
        super(cause);
    }
}
