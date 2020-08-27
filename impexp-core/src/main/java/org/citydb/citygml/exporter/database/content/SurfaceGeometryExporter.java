package org.citydb.citygml.exporter.database.content;

import org.citydb.citygml.exporter.util.GeometrySetter;
import org.citydb.citygml.exporter.util.GeometrySetterHandler;

public interface SurfaceGeometryExporter {
    void addBatch(long id, GeometrySetterHandler handler);
    void addBatch(long id, GeometrySetter.AbstractGeometry setter);
    void addBatch(long id, GeometrySetter.Surface setter);
    void addBatch(long id, GeometrySetter.CompositeSurface setter);
    void addBatch(long id, GeometrySetter.MultiSurface setter);
    void addBatch(long id, GeometrySetter.Polygon setter);
    void addBatch(long id, GeometrySetter.MultiPolygon setter);
    void addBatch(long id, GeometrySetter.Solid setter);
    void addBatch(long id, GeometrySetter.CompositeSolid setter);
    void addBatch(long id, GeometrySetter.MultiSolid setter);
    void addBatch(long id, GeometrySetter.Tin setter);
}
