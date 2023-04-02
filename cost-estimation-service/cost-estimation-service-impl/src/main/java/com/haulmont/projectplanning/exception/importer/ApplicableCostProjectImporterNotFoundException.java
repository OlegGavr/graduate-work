package com.haulmont.projectplanning.exception.importer;

public class ApplicableCostProjectImporterNotFoundException extends CostProjectImporterException {

    public ApplicableCostProjectImporterNotFoundException() {
    }

    public ApplicableCostProjectImporterNotFoundException(String message) {
        super(message);
    }

    public ApplicableCostProjectImporterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicableCostProjectImporterNotFoundException(Throwable cause) {
        super(cause);
    }
}
