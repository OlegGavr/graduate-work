package com.haulmont.projectplanning.costestimation.mongo.converter;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectVersion;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectVersionId;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;

// keep using in feature
@ReadingConverter
public class CostProjectVersionReadConverter implements Converter<Document, CostProjectVersion> {

    private MongoConverter mongoConverter;

    public CostProjectVersionReadConverter(MongoConverter mongoConverter) {
        this.mongoConverter = mongoConverter;
    }

    @Override
    public CostProjectVersion convert(Document source) {

        var id = mongoConverter.read(CostProjectVersionId.class,
                (Document) source.get("_id"));

        // extract ProjectCost
        var copiedDocument = new Document(source);
        copiedDocument.put("_id", id.id());
        var projectCost = mongoConverter.read(CostProject.class, copiedDocument);

        var projectCostVersion = new CostProjectVersion(null, null);
//        projectCostVersion.setId(id);
//        projectCostVersion.setProjectCost(projectCost);

        return projectCostVersion;
    }
}
