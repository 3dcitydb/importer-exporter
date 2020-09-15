package org.citydb.citygml.exporter.database.content;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.GeometrySetter;
import org.citydb.citygml.exporter.util.GeometrySetterHandler;

import java.sql.SQLException;

public interface SurfaceGeometryExporter {
    void addBatch(long id, GeometrySetterHandler handler) throws CityGMLExportException, SQLException;
    void addBatch(long id, GeometrySetter.AbstractGeometry setter) throws CityGMLExportException, SQLException;
    void addBatch(long id, GeometrySetter.Surface setter) throws CityGMLExportException, SQLException;
    void addBatch(long id, GeometrySetter.CompositeSurface setter) throws CityGMLExportException, SQLException;
    void addBatch(long id, GeometrySetter.MultiSurface setter) throws CityGMLExportException, SQLException;
    void addBatch(long id, GeometrySetter.Polygon setter) throws CityGMLExportException, SQLException;
    void addBatch(long id, GeometrySetter.MultiPolygon setter) throws CityGMLExportException, SQLException;
    void addBatch(long id, GeometrySetter.Solid setter) throws CityGMLExportException, SQLException;
    void addBatch(long id, GeometrySetter.CompositeSolid setter) throws CityGMLExportException, SQLException;
    void addBatch(long id, GeometrySetter.MultiSolid setter) throws CityGMLExportException, SQLException;
    void addBatch(long id, GeometrySetter.Tin setter) throws CityGMLExportException, SQLException;
}
