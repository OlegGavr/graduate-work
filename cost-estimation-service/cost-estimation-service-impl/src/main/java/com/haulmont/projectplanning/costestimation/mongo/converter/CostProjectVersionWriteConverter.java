package com.haulmont.projectplanning.costestimation.mongo.converter;

import com.haulmont.projectplanning.costestimation.mongo.dto.CostProjectVersion;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;

import java.util.Map;
// keep using in feature
@WritingConverter
public class CostProjectVersionWriteConverter implements Converter<CostProjectVersion, Document> {

    private MongoConverter mongoConverter;

    public CostProjectVersionWriteConverter(MongoConverter mongoConverter) {
        this.mongoConverter = mongoConverter;

    }

    @Override
    public Document convert(CostProjectVersion source) {

        Document document = new Document();

        document.put("_id", mongoConverter.convertToMongoType(source.id()));

        //noinspection unchecked,rawtypes
        document.putAll((Map) mongoConverter.convertToMongoType(source.projectCost()));


        return document;
    }
}
