package com.haulmont.projectplanning.costestimation.mongo.dto.importer;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

public record CostProjectImportInfo(
        @Id
        String costProjectId,
        Long lastModified
) {
        public CostProjectImportInfo() {
                this(new ObjectId().toString(), null);
        }

        @PersistenceConstructor
        public CostProjectImportInfo {
        }
}
