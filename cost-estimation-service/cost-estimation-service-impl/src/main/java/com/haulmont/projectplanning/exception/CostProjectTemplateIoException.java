package com.haulmont.projectplanning.exception;

public class CostProjectTemplateIoException extends CostEstimationException {

    private String templateName;

    public CostProjectTemplateIoException(String templateName) {
        this.templateName = templateName;
    }

    public CostProjectTemplateIoException(String message, String templateName) {
        super(message);
        this.templateName = templateName;
    }

    public CostProjectTemplateIoException(String message, Throwable cause, String templateName) {
        super(message, cause);
        this.templateName = templateName;
    }

    public CostProjectTemplateIoException(Throwable cause, String templateName) {
        super(cause);
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}
