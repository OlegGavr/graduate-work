package com.haulmont.projectplanning.costestimation.mongo.dto.sharepoint;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

public record CostProjectSharepointLink(

        @Id
        String costProjectId,

        String sharePointLink
) {

        public CostProjectSharepointLink() {
                this(new ObjectId().toString(), null);
        }

        @PersistenceConstructor
        public CostProjectSharepointLink {
        }
}
