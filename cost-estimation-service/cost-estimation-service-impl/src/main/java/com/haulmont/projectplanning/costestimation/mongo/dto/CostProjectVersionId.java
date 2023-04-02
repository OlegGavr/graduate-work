package com.haulmont.projectplanning.costestimation.mongo.dto;

import org.springframework.data.mongodb.core.mapping.Field;

public record CostProjectVersionId(
        String id, @Field("_version") Integer versionId) { }
