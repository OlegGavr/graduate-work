package com.haulmont.projectplanning.exception.importer;

import com.haulmont.projectplanning.exception.CostEstimationException;

public class CostProjectImporterException extends CostEstimationException {

    public CostProjectImporterException() {
    }

    public CostProjectImporterException(String message) {
        super(message);
    }

    public CostProjectImporterException(String message, Throwable cause) {
        super(message, cause);
    }

    public CostProjectImporterException(Throwable cause) {
        super(cause);
    }
}
