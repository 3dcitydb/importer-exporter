package org.citydb.citygml.exporter.database.content;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.GeometrySetter;

import java.sql.SQLException;

public interface SurfaceGeometryBatchExporter {
    void addBatch(long id, GeometrySetter.AbstractGeometry setter);
    void addBatch(long id, GeometrySetter.Surface setter);
    void addBatch(long id, GeometrySetter.MultiSurface setter);
    void addBatch(long id, GeometrySetter.Solid setter);
    void addBatch(long id, GeometrySetter.MultiSolid setter);
    void addBatch(long id, GeometrySetter.Tin setter);
    void clearBatch();
    void executeBatch() throws CityGMLExportException, SQLException;
}
