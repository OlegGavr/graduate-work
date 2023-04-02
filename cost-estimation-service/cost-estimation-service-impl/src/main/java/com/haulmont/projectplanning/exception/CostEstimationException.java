package com.haulmont.projectplanning.exception;

public class CostEstimationException extends RuntimeException {
    public CostEstimationException() {
    }

    public CostEstimationException(String message) {
        super(message);
    }

    public CostEstimationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CostEstimationException(Throwable cause) {
        super(cause);
    }
}
