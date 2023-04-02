package com.haulmont.projectplanning.costestimation.importer.auto;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.haulmont.projectplanning.costestimation.importer.CostProjectImporter;
import com.haulmont.projectplanning.costestimation.mongo.dto.CostProject;
import com.haulmont.projectplanning.costestimation.repository.mongo.CostProjectMongoRepository;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectItemService;
import com.haulmont.projectplanning.costestimation.service.costproject.CostProjectService;
import com.haulmont.projectplanning.exception.importer.ApplicableCostProjectImporterNotFoundException;
import io.vavr.control.Try;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;

import static java.nio.file.StandardOpenOption.READ;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Primary
@Scope(SCOPE_PROTOTYPE)
public class AutoCostProjectImporter implements CostProjectImporter {

    private static Logger logger = LoggerFactory.getLogger(AutoCostProjectImporter.class);

    private List<CostProjectImporter> costProjectImporters;

    public AutoCostProjectImporter(Calculation calculation,
                                   CostProjectService costProjectService,
                                   CostProjectItemService costProjectItemService,
                                   CostProjectMongoRepository costProjectMongoRepository,
                                   List<CostProjectImporter> costProjectImporters) {
        this.costProjectImporters = costProjectImporters.stream()
                // exclude yourself
                .filter(importer -> ! importer.getClass().equals(AutoCostProjectImporter.class))
                .toList();
    }

    @Override
    public Boolean isApplicable(InputStream inputStream) {
        return true;
    }

    @Override
    public CostProject doImport(InputStream inputStream) {

        return internalDoImport(inputStream, CostProjectImporter::doImport);
    }

    @Override
    public CostProject doImport(InputStream inputStream, NamedImportParams params) {
        return internalDoImport(inputStream, (importer, is)
                -> importer.doImport(is, params));
    }

    @Override
    public CostProject doImport(String costProjectId, InputStream inputStream) {
        return internalDoImport(inputStream, (importer, is)
                -> importer.doImport(costProjectId, is));
    }

    @Override
    public CostProject doImport(String costProjectId, InputStream inputStream, NamedImportParams params) {
        return internalDoImport(inputStream, (importer, is)
                -> importer.doImport(costProjectId, is, params));

    }

    @Override
    public CostProject doImport(CostProject costProject, InputStream inputStream) {
        return internalDoImport(inputStream, (importer, is)
                -> importer.doImport(costProject, is));

    }

    @Override
    public CostProject doImport(CostProject costProject, InputStream inputStream, NamedImportParams params) {
        return internalDoImport(inputStream, (importer, is)
                -> importer.doImport(costProject, is, params));
    }

    private CostProject internalDoImport(InputStream inputStream,
                                         BiFunction<CostProjectImporter, InputStream, CostProject> doImport) {
        var tmpFile = createTemporaryFile(inputStream);

        logger.debug("Temporary file: {}", tmpFile);

        try {
            var applicableImporter = findApplicableImporter(tmpFile);

            logger.info("Selected CostProjectImporter: {}", applicableImporter);

            try (var is = new BufferedInputStream(Files.newInputStream(tmpFile, READ))) {

                return doImport.apply(applicableImporter, is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            Try.run(() -> Files.delete(tmpFile))
                    .getOrElseThrow(e -> new RuntimeException(e));
        }
    }

    private CostProjectImporter findApplicableImporter(Path tmpFile) {

        for (CostProjectImporter importer : costProjectImporters) {
            try (var is = new BufferedInputStream(Files.newInputStream(tmpFile, READ))) {

                // first found importer is allowed because importers sorted by order annotation
                var isApplicable = importer.isApplicable(is);
                if (isApplicable) {
                    return importer;
                }

            } catch (IOException e) {
                logger.warn("Importer generate exception during isApplicable", e);
            }
        }

        throw new ApplicableCostProjectImporterNotFoundException("Applicable importer not found");
    }

    private Path createTemporaryFile(InputStream inputStream) {
        var tmpFile = Try.of(() -> Files.createTempFile("auto_import", null))
                .getOrElseThrow(e -> new RuntimeException(e));

        // flush input stream to tmpFile
        try (var os = new BufferedOutputStream(Files.newOutputStream(tmpFile))) {
            IOUtils.copy(inputStream, os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return tmpFile;
    }

}
