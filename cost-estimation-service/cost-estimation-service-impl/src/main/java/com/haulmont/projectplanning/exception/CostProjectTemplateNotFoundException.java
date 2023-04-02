package com.haulmont.projectplanning.exception;

public class CostProjectTemplateNotFoundException extends CostProjectTemplateIoException {

    public CostProjectTemplateNotFoundException(String templateName) {
        super(templateName);
    }

    public CostProjectTemplateNotFoundException(String message, String templateName) {
        super(message, templateName);
    }

    public CostProjectTemplateNotFoundException(String message, Throwable cause, String templateName) {
        super(message, cause, templateName);
    }

    public CostProjectTemplateNotFoundException(Throwable cause, String templateName) {
        super(cause, templateName);
    }
}
