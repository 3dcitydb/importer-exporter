package org.citydb.citygml.exporter.database.content;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.MultiSurfaceSetter;
import org.citydb.citygml.exporter.util.SolidSetter;

import java.sql.SQLException;

public interface SurfaceGeometryBatchExporter {
    void addBatch(long id, MultiSurfaceSetter setter);
    void addBatch(long id, SolidSetter setter);
    void clearBatch();
    void executeBatch() throws CityGMLExportException, SQLException;
}
