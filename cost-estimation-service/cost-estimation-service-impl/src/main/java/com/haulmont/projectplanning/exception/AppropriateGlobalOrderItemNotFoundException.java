package com.haulmont.projectplanning.exception;

public class AppropriateGlobalOrderItemNotFoundException extends CostEstimationException {

    public AppropriateGlobalOrderItemNotFoundException() {
    }

    public AppropriateGlobalOrderItemNotFoundException(String message) {
        super(message);
    }

    public AppropriateGlobalOrderItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppropriateGlobalOrderItemNotFoundException(Throwable cause) {
        super(cause);
    }
}
