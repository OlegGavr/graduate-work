package com.haulmont.projectplanning.costestimation.template.registry;

import com.haulmont.projectplanning.costestimation.template.CostProjectTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
public class CostProjectTemplateRegistryImpl implements CostProjectTemplateRegistry {

    private CostProjectClasspathTemplateRegistry costProjectClasspathTemplateRegistry;

    public CostProjectTemplateRegistryImpl(CostProjectClasspathTemplateRegistry costProjectClasspathTemplateRegistry) {
        this.costProjectClasspathTemplateRegistry = costProjectClasspathTemplateRegistry;
    }


    @Override
    public List<CostProjectTemplate> allTemplates() {
        return costProjectClasspathTemplateRegistry.allTemplates();
    }

    @Override
    public CostProjectTemplate findTemplateById(String templateId) {
        return costProjectClasspathTemplateRegistry.findTemplateById(templateId);
    }
}
