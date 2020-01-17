package org.citydb.citygml.importer.reader;

import org.citydb.concurrent.WorkerPool;
import org.citydb.file.InputFile;
import org.citygml4j.model.citygml.CityGML;

public interface FeatureReader extends AutoCloseable {
    long getValidationErrors();
    void read(InputFile inputFile, WorkerPool<CityGML> workerPool) throws FeatureReadException;
    void close() throws FeatureReadException;
}
