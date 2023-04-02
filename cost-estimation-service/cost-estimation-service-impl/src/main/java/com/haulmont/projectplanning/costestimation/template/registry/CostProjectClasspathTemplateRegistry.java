package com.haulmont.projectplanning.costestimation.template.registry;

import com.haulmont.projectplanning.costestimation.template.CostProjectTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CostProjectClasspathTemplateRegistry implements CostProjectTemplateRegistry {

    private List<CostProjectTemplate> classpathTemplates = new ArrayList<>();

    private Map<String, CostProjectTemplate> classpathTemplatesById = new HashMap<>();

    @PostConstruct
    protected void init() {
        classpathTemplates.add(new CostProjectTemplate("62b5d08f6eff973dee4bf1e4", "Базовый (Ru)", "classpath:template/excel/BasicFixPriceRu.xlsx"));
        classpathTemplates.add(new CostProjectTemplate("62b5d0966eff973dee4bf1e5", "Базовый (En)", "classpath:template/excel/BasicFixPriceEn.xlsx"));

        classpathTemplates.forEach(t -> classpathTemplatesById.put(t.id(), t));
    }

    @Override
    public List<CostProjectTemplate> allTemplates() {
        return this.classpathTemplates;
    }

    @Override
    @Nullable
    public CostProjectTemplate findTemplateById(String templateId) {
        return classpathTemplatesById.get(templateId);
    }
}
