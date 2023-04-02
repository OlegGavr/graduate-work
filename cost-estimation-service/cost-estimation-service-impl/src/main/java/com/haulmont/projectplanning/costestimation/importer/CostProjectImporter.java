package com.haulmont.projectplanning.costestimation.importer;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import lombok.experimental.Delegate;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public interface CostProjectImporter {

    Boolean isApplicable(InputStream inputStream);

    CostProject doImport(InputStream inputStream);

    CostProject doImport(InputStream inputStream, NamedImportParams params);

    CostProject doImport(String costProjectId, InputStream inputStream);

    CostProject doImport(String costProjectId, InputStream inputStream, NamedImportParams params);

    CostProject doImport(CostProject costProject, InputStream inputStream);

    CostProject doImport(CostProject costProject, InputStream inputStream, NamedImportParams params);

    enum ApplyStrategy {
        APPEND, REPLACE, OVERLAY
    }

    interface NamedImportParams extends Map<String, Object> {

        String RECALCULATE = "recalculate";
        String APPLY_STRATEGY = "applyStrategy";

        Boolean recalculate();

        ApplyStrategy applyStrategy();
    }

    class NamedImportParamsImpl implements NamedImportParams {

        @Delegate
        protected Map<String, Object> params;

        public NamedImportParamsImpl() {
            this.params = new HashMap<>();

            // defaults
            params.put(RECALCULATE, false);
            params.put(APPLY_STRATEGY, ApplyStrategy.REPLACE);
        }

        public NamedImportParamsImpl(Map<String, Object> params) {
            this.params = new HashMap<>(params);
        }

        @Override
        public Boolean recalculate() {
            return (Boolean) params.get(RECALCULATE);
        }

        public NamedImportParamsImpl recalculate(Boolean recalculateValue) {
            params.put(RECALCULATE, recalculateValue);
            return this;
        }

        @Override
        public ApplyStrategy applyStrategy() {
            return (ApplyStrategy) params.get(APPLY_STRATEGY);
        }

        public NamedImportParamsImpl applyStrategy(ApplyStrategy applyStrategyValue) {
            params.put(APPLY_STRATEGY, applyStrategyValue);
            return this;
        }
    }
}
