package com.haulmont.projectplanning.exception.importer.excel;

import com.haulmont.projectplanning.exception.importer.CostProjectImporterException;

public class ExcelCostProjectImporterException extends CostProjectImporterException {

    public ExcelCostProjectImporterException() {
    }

    public ExcelCostProjectImporterException(String message) {
        super(message);
    }

    public ExcelCostProjectImporterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelCostProjectImporterException(Throwable cause) {
        super(cause);
    }
}
