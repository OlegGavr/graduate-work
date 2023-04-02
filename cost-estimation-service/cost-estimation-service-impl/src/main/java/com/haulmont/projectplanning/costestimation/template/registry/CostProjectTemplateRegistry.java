package com.haulmont.projectplanning.costestimation.template.registry;

import com.haulmont.projectplanning.costestimation.template.CostProjectTemplate;

import java.util.List;

public interface CostProjectTemplateRegistry {

    List<CostProjectTemplate> allTemplates();

    CostProjectTemplate findTemplateById(String templateId);
}
