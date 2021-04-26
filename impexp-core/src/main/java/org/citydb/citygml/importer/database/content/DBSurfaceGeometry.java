/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.citygml.importer.database.content;

import org.citydb.citygml.common.xlink.DBXlinkLinearRing;
import org.citydb.citygml.common.xlink.DBXlinkSolidGeometry;
import org.citydb.citygml.common.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.LocalAppearanceHandler;
import org.citydb.citygml.importer.util.RingValidator;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.database.DatabaseType;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.SequenceEnum;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.XlinkType;
import org.citydb.log.Logger;
import org.citydb.util.CoreConstants;
import org.citygml4j.model.citygml.texturedsurface._AbstractAppearance;
import org.citygml4j.model.citygml.texturedsurface._AppearanceProperty;
import org.citygml4j.model.citygml.texturedsurface._SimpleTexture;
import org.citygml4j.model.citygml.texturedsurface._TexturedSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiPolygon;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolid;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurfacePatch;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.OrientableSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.PolygonPatch;
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;
import org.citygml4j.model.gml.geometry.primitives.Rectangle;
import org.citygml4j.model.gml.geometry.primitives.Sign;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.Surface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Triangle;
import org.citygml4j.model.gml.geometry.primitives.TrianglePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.util.walker.GeometryWalker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DBSurfaceGeometry implements DBImporter {
    private final Connection batchConn;
    private final CityGMLImportManager importer;

    private final PreparedStatement psGeomElem;
    private final PreparedStatement psNextSeqValues;
    private final GeometryConverter geometryConverter;
    private final DBAppearance appearanceImporter;
    private final IdManager ids;
	private final LocalAppearanceHandler localAppearanceHandler;
	private final RingValidator ringValidator;

	private final boolean replaceGmlId;
	private final boolean importAppearance;
	private final int nullGeometryType;
	private final String nullGeometryTypeName;
	private final int isXlinkValue;

	private int dbSrid;
    private boolean applyTransformation;
    private boolean isImplicit;
    private int batchCounter;

    public DBSurfaceGeometry(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
        this.batchConn = batchConn;
        this.importer = importer;

        replaceGmlId = config.getImportConfig().getResourceId().isUUIDModeReplace();
        dbSrid = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
        importAppearance = config.getImportConfig().getAppearances().isSetImportAppearance();
        applyTransformation = config.getImportConfig().getAffineTransformation().isEnabled();
        nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
        nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
        String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

        isXlinkValue = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 1, 0) >= 0 ?
                XlinkType.LOCAL.value() :
                XlinkType.GLOBAL.value();

        String gmlIdCodespace = importer.getInternalConfig().getCurrentGmlIdCodespace();
        if (gmlIdCodespace != null)
            gmlIdCodespace = "'" + gmlIdCodespace + "', ";

        StringBuilder stmt = new StringBuilder()
                .append("insert into ").append(schema).append(".surface_geometry (id, gmlid, ").append(gmlIdCodespace != null ? "gmlid_codespace, " : "")
                .append("parent_id, root_id, is_solid, is_composite, is_triangulated, is_xlink, is_reverse, geometry, solid_geometry, implicit_geometry, cityobject_id) values ")
                .append("(?, ?, ").append(gmlIdCodespace != null ? gmlIdCodespace : "").append("?, ?, ?, ?, ?, ?, ?, ?, ");

        if (importer.getDatabaseAdapter().getDatabaseType() == DatabaseType.POSTGIS) {
            // the current PostGIS JDBC driver lacks support for geometry objects of type PolyhedralSurface
            // thus, we have to use the database function ST_GeomFromEWKT to insert such geometries
            // TODO: rework as soon as the JDBC driver supports PolyhedralSurface
            stmt.append("ST_GeomFromEWKT(?), ");
        } else
            stmt.append("?, ");

        stmt.append("?, ?)");

        psGeomElem = batchConn.prepareStatement(stmt.toString());
        psNextSeqValues = batchConn.prepareStatement(importer.getDatabaseAdapter().getSQLAdapter().getNextSequenceValuesQuery(SequenceEnum.SURFACE_GEOMETRY_ID_SEQ.getName()));

        appearanceImporter = importer.getImporter(DBAppearance.class);
        localAppearanceHandler = importer.getLocalAppearanceHandler();
        geometryConverter = importer.getGeometryConverter();
        ids = new IdManager();
        ringValidator = new RingValidator();
    }

    protected long doImport(AbstractGeometry geometry, long cityObjectId) throws CityGMLImportException, SQLException {
        // check whether we can deal with the geometry
        if (!geometryConverter.isSurfaceGeometry(geometry)) {
            importer.logOrThrowErrorMessage("Unsupported geometry type " + importer.getObjectSignature(geometry));
            return 0;
        }

        try {
            long id = ids.prepare(geometry);
            if (id == 0) {
                importer.logOrThrowErrorMessage("Failed to acquire primary keys from surface geometry sequence.");
                return 0;
            }

            if (geometry.isSetId())
                geometry.setLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID, geometry.getId());

            return doImport(geometry, id, 0, id, false, false, false, cityObjectId);
        } finally {
            ids.clear();
        }
    }

    protected long importImplicitGeometry(AbstractGeometry geometry) throws CityGMLImportException, SQLException {
        // if affine transformation is activated we apply the user-defined affine
        // transformation to the transformation matrix associated with the implicit geometry.
        // thus, we do not need to apply it to the coordinate values
        boolean _applyTransformation = applyTransformation;
        int _dbSrid = dbSrid;

        try {
            isImplicit = true;
            applyTransformation = false;
            dbSrid = 0;
            return doImport(geometry, 0);
        } finally {
            isImplicit = false;
            applyTransformation = _applyTransformation;
            dbSrid = _dbSrid;
        }
    }

    private void doImport(AbstractGeometry geometry, long parentId, long rootId, boolean reverse, boolean isXlink, boolean isCopy, long cityObjectId) throws CityGMLImportException, SQLException {
        long id = geometry instanceof OrientableSurface ? parentId : ids.next();
        doImport(geometry, id, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
    }

    private long doImport(AbstractGeometry geometry, long id, long parentId, long rootId, boolean reverse, boolean isXlink, boolean isCopy, long cityObjectId) throws CityGMLImportException, SQLException {
        importer.updateGeometryCounter(geometry.getGMLClass());

        if (!isCopy)
            isCopy = geometry.hasLocalProperty(CoreConstants.GEOMETRY_ORIGINAL);

        if (!isXlink)
            isXlink = geometry.hasLocalProperty(CoreConstants.GEOMETRY_XLINK);

        // gml:id handling
        String origGmlId, gmlId;
        origGmlId = gmlId = geometry.getId();
        if (gmlId == null || replaceGmlId) {
            if (!geometry.hasLocalProperty(CoreConstants.GEOMETRY_ORIGINAL)) {
                if (!geometry.hasLocalProperty("origGmlId")) {
                    gmlId = importer.generateNewGmlId();
                    geometry.setId(gmlId);
                    geometry.setLocalProperty("origGmlId", origGmlId);
                } else
                    origGmlId = (String) geometry.getLocalProperty("origGmlId");
            } else {
                AbstractGeometry original = (AbstractGeometry) geometry.getLocalProperty(CoreConstants.GEOMETRY_ORIGINAL);
                if (!original.hasLocalProperty("origGmlId")) {
                    gmlId = importer.generateNewGmlId();
                    original.setId(gmlId);
                    original.setLocalProperty("origGmlId", origGmlId);
                } else
                    gmlId = original.getId();

                geometry.setId(gmlId);
            }
        }

        // ok, now we can have a look at different gml geometry objects
        // firstly, handle simple surface geometries
        // a simple polygon
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;

            if (polygon.isSetExterior()) {
                List<List<Double>> pointList = new ArrayList<>();
                AbstractRing exterior = polygon.getExterior().getRing();
                if (exterior != null) {
                    List<Double> points = exterior.toList3d(reverse);
                    if (!ringValidator.validate(points, exterior))
                        return 0;

                    if (applyTransformation)
                        importer.getAffineTransformer().transformCoordinates(points);

                    pointList.add(points);
                    int ringNo = 0;
                    importer.updateGeometryCounter(GMLClass.LINEAR_RING);

                    // well, taking care about geometry is not enough... this ring could
                    // be referenced by a <textureCoordinates> element. since we cannot store
                    // the gml:id of linear rings in the database, we have to remember its id
                    if (importAppearance && !isCopy && exterior.isSetId()) {
                        if (localAppearanceHandler != null && localAppearanceHandler.hasParameterizedTextures())
                            localAppearanceHandler.registerLinearRing(exterior.getId(), id, reverse);

                        // the ring could also be the target of a global appearance
                        importer.propagateXlink(new DBXlinkLinearRing(exterior.getId(), id, ringNo, reverse));
                    }

                    if (polygon.isSetInterior()) {
                        for (AbstractRingProperty property : polygon.getInterior()) {
                            AbstractRing interior = property.getRing();
                            if (interior != null) {
                                List<Double> interiorPoints = interior.toList3d(reverse);
                                if (!ringValidator.validate(interiorPoints, interior))
                                    continue;

                                if (applyTransformation)
                                    importer.getAffineTransformer().transformCoordinates(interiorPoints);

                                pointList.add(interiorPoints);
                                importer.updateGeometryCounter(GMLClass.LINEAR_RING);

                                // also remember the gml:id of interior rings
                                if (importAppearance && !isCopy && interior.isSetId()) {
                                    if (localAppearanceHandler != null && localAppearanceHandler.hasParameterizedTextures())
                                        localAppearanceHandler.registerLinearRing(interior.getId(), id, reverse);

                                    // the ring could also be the target of a global appearance
                                    importer.propagateXlink(new DBXlinkLinearRing(interior.getId(), id, ++ringNo, reverse));
                                }
                            }
                        }
                    }

                    double[][] coordinates = new double[pointList.size()][];
                    for (int i = 0; i < pointList.size(); i++)
						coordinates[i] = pointList.get(i).stream().mapToDouble(Double::doubleValue).toArray();

                    GeometryObject geometryObject = GeometryObject.createPolygon(coordinates, 3, dbSrid);
                    Object object = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn);

                    if (origGmlId != null && !isCopy)
                        importer.putGeometryId(origGmlId, id, rootId, reverse, gmlId);

                    psGeomElem.setLong(1, id);
                    psGeomElem.setString(2, gmlId);
                    psGeomElem.setLong(4, rootId);
                    psGeomElem.setInt(5, 0);
                    psGeomElem.setInt(6, 0);
                    psGeomElem.setInt(7, 0);
                    psGeomElem.setInt(8, isXlink ? isXlinkValue : 0);
                    psGeomElem.setInt(9, reverse ? 1 : 0);
                    psGeomElem.setNull(11, nullGeometryType, nullGeometryTypeName);

                    if (parentId != 0)
                        psGeomElem.setLong(3, parentId);
                    else
                        psGeomElem.setNull(3, Types.NULL);

                    if (!isImplicit) {
                        psGeomElem.setObject(10, object);
                        psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);
                    } else {
                        psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
                        psGeomElem.setObject(12, object);
                    }

                    if (cityObjectId != 0)
                        psGeomElem.setLong(13, cityObjectId);
                    else
                        psGeomElem.setNull(13, Types.NULL);

                    addBatch();
                }
            }
        }

		// texturedSurface
		// this is a CityGML class, not a GML class.
		else if (geometry instanceof _TexturedSurface) {
			_TexturedSurface texturedSurface = (_TexturedSurface) geometry;
			AbstractSurface surface;

			boolean negativeOrientation = false;
			if (texturedSurface.isSetOrientation() && texturedSurface.getOrientation() == Sign.MINUS) {
				reverse = !reverse;
				negativeOrientation = true;
			}

			String targetURI;

			if (texturedSurface.isSetBaseSurface()) {
				SurfaceProperty property = texturedSurface.getBaseSurface();
				if (property.isSetSurface()) {
					surface = property.getSurface();
					if (!surface.isSetId())
						surface.setId(importer.generateNewGmlId());

					// appearance and mapping target
					targetURI = surface.getId();

					// do mapping
					if (origGmlId != null && !isCopy)
						importer.putGeometryId(origGmlId, -1, -1, negativeOrientation, targetURI);

					if (surface instanceof Polygon) {
						// make sure all exterior and interior rings do have a gml:id to assign texture coordinates
						Polygon polygon = (Polygon) surface;

						if (polygon.isSetExterior()) {
							LinearRing exteriorRing = (LinearRing) polygon.getExterior().getRing();
							if (exteriorRing != null && !exteriorRing.isSetId())
								exteriorRing.setId(targetURI);
						}

						if (polygon.isSetInterior()) {
							for (AbstractRingProperty abstractRingProperty : polygon.getInterior()) {
								LinearRing interiorRing = (LinearRing) abstractRingProperty.getRing();
								if (!interiorRing.isSetId())
									interiorRing.setId(importer.generateNewGmlId());
							}
						}
					}

					doImport(surface, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(id, parentId, rootId, reverse, href, cityObjectId));
						targetURI = href.replaceAll("^#", "");

						// do mapping
						if (origGmlId != null && !isCopy)
							importer.putGeometryId(origGmlId, -1, -1, negativeOrientation, targetURI);

						// well, regarding appearances we cannot work on remote geometries so far...
						importer.logOrThrowErrorMessage(importer.getObjectSignature(texturedSurface, origGmlId) +
								": Texture information for referenced geometry objects are not supported.");
					}

					return 0;
				}
			} else {
				importer.logOrThrowErrorMessage(importer.getObjectSignature(texturedSurface, origGmlId) +
						": The textured surface lacks a base surface.");
				return 0;
			}

			if (importAppearance && !isCopy && texturedSurface.isSetAppearance()) {
				for (_AppearanceProperty property : texturedSurface.getAppearance()) {
					if (property.isSetAppearance()) {
						_AbstractAppearance appearance = property.getAppearance();

						// we can only assign textures to polygons
						if (appearance instanceof _SimpleTexture && !(surface instanceof Polygon)) {
							importer.logOrThrowErrorMessage(importer.getObjectSignature(texturedSurface, origGmlId) +
									": Texture coordinates are only supported for base surfaces of type gml:Polygon.");
							continue;
						}

						boolean isFront = !(property.isSetOrientation() && property.getOrientation() == Sign.MINUS);
						appearanceImporter.importTexturedSurface(appearance, surface, cityObjectId, isFront, targetURI);
					} else {
						String href = property.getHref();
						if (href != null && href.length() != 0)
							appearanceImporter.importTexturedSurfaceXlink(href, id, cityObjectId);
					}
				}
			}
		}

        // ok, handle complexes, composites and aggregates
        // orientableSurface
        else if (geometry instanceof OrientableSurface) {
            OrientableSurface orientableSurface = (OrientableSurface) geometry;

            boolean negativeOrientation = false;
            if (orientableSurface.isSetOrientation() && orientableSurface.getOrientation() == Sign.MINUS) {
                reverse = !reverse;
                negativeOrientation = true;
            }

            if (orientableSurface.isSetBaseSurface()) {
                SurfaceProperty property = orientableSurface.getBaseSurface();
                String mapping = null;

                if (property.isSetSurface()) {
                    AbstractSurface surface = property.getSurface();
                    if (!surface.isSetId())
                        surface.setId(importer.generateNewGmlId());

                    // mapping target
                    mapping = surface.getId();

					doImport(surface, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
				} else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0) {
                        importer.propagateXlink(new DBXlinkSurfaceGeometry(id, parentId, rootId, reverse, href, cityObjectId));
                        mapping = href.replaceAll("^#", "");
                    }
                }

                // do mapping
                if (origGmlId != null && !isCopy)
                    importer.putGeometryId(origGmlId, -1, -1, negativeOrientation, mapping);
            } else {
				importer.logOrThrowErrorMessage(importer.getObjectSignature(orientableSurface, origGmlId) +
						": The orientable surface lacks a base surface.");
			}
        }

        // compositeSurface
        else if (geometry instanceof CompositeSurface) {
            CompositeSurface compositeSurface = (CompositeSurface) geometry;

            if (origGmlId != null && !isCopy)
                importer.putGeometryId(origGmlId, id, rootId, reverse, gmlId);

            // set root entry
            psGeomElem.setLong(1, id);
            psGeomElem.setString(2, gmlId);
            psGeomElem.setLong(4, rootId);
            psGeomElem.setInt(5, 0);
            psGeomElem.setInt(6, 1);
            psGeomElem.setInt(7, 0);
            psGeomElem.setInt(8, isXlink ? isXlinkValue : 0);
            psGeomElem.setInt(9, reverse ? 1 : 0);
            psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(11, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);

            if (parentId != 0)
                psGeomElem.setLong(3, parentId);
            else
                psGeomElem.setNull(3, Types.NULL);

            if (cityObjectId != 0)
                psGeomElem.setLong(13, cityObjectId);
            else
                psGeomElem.setNull(13, Types.NULL);

            addBatch();

            // set parentId
            parentId = id;

            // get surfaceMember
            if (compositeSurface.isSetSurfaceMember()) {
                for (SurfaceProperty property : compositeSurface.getSurfaceMember()) {
                    if (property.isSetSurface()) {
						doImport(property.getSurface(), parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
                    } else {
                        String href = property.getHref();
                        if (href != null && href.length() != 0)
                            importer.propagateXlink(new DBXlinkSurfaceGeometry(id, parentId, rootId, reverse, href, cityObjectId));
                    }
                }
            }
        }

        // TriangulatedSurface, TIN
        else if (geometry instanceof TriangulatedSurface) {
            TriangulatedSurface triangulatedSurface = (TriangulatedSurface) geometry;

            if (origGmlId != null && !isCopy)
                importer.putGeometryId(origGmlId, id, rootId, reverse, gmlId);

            // set root entry
            psGeomElem.setLong(1, id);
            psGeomElem.setString(2, gmlId);
            psGeomElem.setLong(4, rootId);
            psGeomElem.setInt(5, 0);
            psGeomElem.setInt(6, 0);
            psGeomElem.setInt(7, 1);
            psGeomElem.setInt(8, isXlink ? isXlinkValue : 0);
            psGeomElem.setInt(9, reverse ? 1 : 0);
            psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(11, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);

            if (parentId != 0)
                psGeomElem.setLong(3, parentId);
            else
                psGeomElem.setNull(3, Types.NULL);

            if (cityObjectId != 0)
                psGeomElem.setLong(13, cityObjectId);
            else
                psGeomElem.setNull(13, Types.NULL);

            addBatch();

            // set parentId
            parentId = id;

            // get triangles
            if (triangulatedSurface.isSetTrianglePatches()) {
                TrianglePatchArrayProperty property = triangulatedSurface.getTrianglePatches();
                if (property.isSetTriangle()) {
                    for (Triangle triangle : property.getTriangle()) {
                        Polygon polygon = new Polygon();
                        polygon.setExterior(triangle.getExterior());
                        doImport(polygon, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
                    }
                }
            }
        }

		// Surface
		else if (geometry instanceof Surface) {
			Surface surface = (Surface) geometry;

			if (origGmlId != null && !isCopy)
				importer.putGeometryId(origGmlId, id, rootId, reverse, gmlId);

			int nrOfPatches = surface.isSetPatches() && surface.getPatches().isSetSurfacePatch() ?
					surface.getPatches().getSurfacePatch().size() : 0;

			// add a composite surface as root unless there is only one surface patch
			if (nrOfPatches != 1) {
				psGeomElem.setLong(1, id);
				psGeomElem.setString(2, gmlId);
				psGeomElem.setLong(4, rootId);
				psGeomElem.setInt(5, 0);
				psGeomElem.setInt(6, 1);
				psGeomElem.setInt(7, 0);
				psGeomElem.setInt(8, isXlink ? isXlinkValue : 0);
				psGeomElem.setInt(9, reverse ? 1 : 0);
				psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
				psGeomElem.setNull(11, nullGeometryType, nullGeometryTypeName);
				psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);

				if (parentId != 0)
					psGeomElem.setLong(3, parentId);
				else
					psGeomElem.setNull(3, Types.NULL);

				if (cityObjectId != 0)
					psGeomElem.setLong(13, cityObjectId);
				else
					psGeomElem.setNull(13, Types.NULL);

				addBatch();

				// set parentId
				parentId = id;
			}

			// import surface patches
			if (nrOfPatches > 0) {
				for (AbstractSurfacePatch patch : surface.getPatches().getSurfacePatch()) {
					Polygon polygon = new Polygon();
					if (nrOfPatches == 1)
						polygon.setId(gmlId);

					if (patch instanceof Rectangle) {
						Rectangle rectangle = (Rectangle) patch;
						polygon = new Polygon();
						polygon.setExterior(rectangle.getExterior());
					} else if (patch instanceof Triangle) {
						Triangle triangle = (Triangle) patch;
						polygon = new Polygon();
						polygon.setExterior(triangle.getExterior());
					} else if (patch instanceof PolygonPatch) {
						PolygonPatch polygonPatch = (PolygonPatch) patch;
						polygon.setExterior(polygonPatch.getExterior());
						polygon.setInterior(polygonPatch.getInterior());
					}

					doImport(polygon, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
				}
			}
		}

        // Solid
        else if (geometry instanceof Solid) {
            Solid solid = (Solid) geometry;

            if (origGmlId != null && !isCopy)
                importer.putGeometryId(origGmlId, id, rootId, reverse, gmlId);

            // set root entry
            psGeomElem.setLong(1, id);
            psGeomElem.setString(2, gmlId);
            psGeomElem.setLong(4, rootId);
            psGeomElem.setInt(5, 1);
            psGeomElem.setInt(6, 0);
            psGeomElem.setInt(7, 0);
            psGeomElem.setInt(8, isXlink ? isXlinkValue : 0);
            psGeomElem.setInt(9, reverse ? 1 : 0);
            psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);

            // create solid geometry object
            Object object = null;
            if (id == rootId) {
                GeometryObject geometryObject = geometryConverter.getSolid(solid);
                if (geometryObject != null)
                    object = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn);
                else {
                    // we cannot build the solid geometry in main memory
                    // possibly the solid references surfaces from another feature per xlink
                    // so, remember its id to build the solid geometry later
                    importer.propagateXlink(new DBXlinkSolidGeometry(id));
                }
            }

            if (object != null)
                psGeomElem.setObject(11, object);
            else
                psGeomElem.setNull(11, nullGeometryType, nullGeometryTypeName);

            if (parentId != 0)
                psGeomElem.setLong(3, parentId);
            else
                psGeomElem.setNull(3, Types.NULL);

            if (cityObjectId != 0)
                psGeomElem.setLong(13, cityObjectId);
            else
                psGeomElem.setNull(13, Types.NULL);

            addBatch();

            // set parentId
            parentId = id;

            // get exterior
            if (solid.isSetExterior()) {
                SurfaceProperty property = solid.getExterior();
                if (property.isSetSurface()) {
                    doImport(property.getSurface(), parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0)
                        importer.propagateXlink(new DBXlinkSurfaceGeometry(id, parentId, rootId, reverse, href, cityObjectId));
                }
            }

            // interiors are not supported
            if (solid.isSetInterior()) {
                importer.logOrThrowErrorMessage(importer.getObjectSignature(solid, origGmlId) +
                        ": Interior shells of solids are not supported.");
            }
        }

        // CompositeSolid
        else if (geometry instanceof CompositeSolid) {
            CompositeSolid compositeSolid = (CompositeSolid) geometry;

            if (origGmlId != null && !isCopy)
                importer.putGeometryId(origGmlId, id, rootId, reverse, gmlId);

            // set root entry
            psGeomElem.setLong(1, id);
            psGeomElem.setString(2, gmlId);
            psGeomElem.setLong(4, rootId);
            psGeomElem.setInt(5, 1);
            psGeomElem.setInt(6, 1);
            psGeomElem.setInt(7, 0);
            psGeomElem.setInt(8, isXlink ? isXlinkValue : 0);
            psGeomElem.setInt(9, reverse ? 1 : 0);
            psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);

            // create composite solid geometry object
            Object object = null;
            if (id == rootId) {
                GeometryObject geometryObject = geometryConverter.getCompositeSolid(compositeSolid);
                if (geometryObject != null) {
					object = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn);
				} else {
                    // we cannot build the solid geometry in main memory
                    // possibly the solid references surfaces from another feature per xlink
                    // so, remember its id to build the solid geometry later
                    importer.propagateXlink(new DBXlinkSolidGeometry(id));
                }
            }

            if (object != null)
                psGeomElem.setObject(11, object);
            else
                psGeomElem.setNull(11, nullGeometryType, nullGeometryTypeName);

            if (parentId != 0)
                psGeomElem.setLong(3, parentId);
            else
                psGeomElem.setNull(3, Types.NULL);

            if (cityObjectId != 0)
                psGeomElem.setLong(13, cityObjectId);
            else
                psGeomElem.setNull(13, Types.NULL);

            addBatch();

            // set parentId
            parentId = id;

            // get solidMember
            if (compositeSolid.isSetSolidMember()) {
                for (SolidProperty property : compositeSolid.getSolidMember()) {
                    if (property.isSetSolid()) {
                        doImport(property.getSolid(), parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
                    } else {
                        String href = property.getHref();
                        if (href != null && href.length() != 0)
                            importer.propagateXlink(new DBXlinkSurfaceGeometry(id, parentId, rootId, reverse, href, cityObjectId));
                    }
                }
            }
        }

        // MultiPolygon
        else if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;

            if (origGmlId != null && !isCopy)
                importer.putGeometryId(origGmlId, id, rootId, reverse, gmlId);

            // set root entry
            psGeomElem.setLong(1, id);
            psGeomElem.setString(2, gmlId);
            psGeomElem.setLong(4, rootId);
            psGeomElem.setInt(5, 0);
            psGeomElem.setInt(6, 0);
            psGeomElem.setInt(7, 0);
            psGeomElem.setInt(8, isXlink ? isXlinkValue : 0);
            psGeomElem.setInt(9, reverse ? 1 : 0);
            psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(11, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);

            if (parentId != 0)
                psGeomElem.setLong(3, parentId);
            else
                psGeomElem.setNull(3, Types.NULL);

            if (cityObjectId != 0)
                psGeomElem.setLong(13, cityObjectId);
            else
                psGeomElem.setNull(13, Types.NULL);

            addBatch();

            // set parentId
            parentId = id;

            // get polygonMember
            if (multiPolygon.isSetPolygonMember()) {
                for (PolygonProperty property : multiPolygon.getPolygonMember()) {
                    if (property.isSetPolygon()) {
                        doImport(property.getPolygon(), parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
                    } else {
                        String href = property.getHref();
                        if (href != null && href.length() != 0)
                            importer.propagateXlink(new DBXlinkSurfaceGeometry(id, parentId, rootId, reverse, href, cityObjectId));
                    }
                }
            }
        }

        // MultiSurface
        else if (geometry instanceof MultiSurface) {
            MultiSurface multiSurface = (MultiSurface) geometry;

            if (origGmlId != null && !isCopy)
                importer.putGeometryId(origGmlId, id, rootId, reverse, gmlId);

            // set root entry
            psGeomElem.setLong(1, id);
            psGeomElem.setString(2, gmlId);
            psGeomElem.setLong(4, rootId);
            psGeomElem.setInt(5, 0);
            psGeomElem.setInt(6, 0);
            psGeomElem.setInt(7, 0);
            psGeomElem.setInt(8, isXlink ? isXlinkValue : 0);
            psGeomElem.setInt(9, reverse ? 1 : 0);
            psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(11, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);

            if (parentId != 0)
                psGeomElem.setLong(3, parentId);
            else
                psGeomElem.setNull(3, Types.NULL);

            if (cityObjectId != 0)
                psGeomElem.setLong(13, cityObjectId);
            else
                psGeomElem.setNull(13, Types.NULL);

            addBatch();

            // set parentId
            parentId = id;

            // get surfaceMember
            if (multiSurface.isSetSurfaceMember()) {
                for (SurfaceProperty property : multiSurface.getSurfaceMember()) {
                    if (property.isSetSurface()) {
						doImport(property.getSurface(), parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
                    } else {
                        String href = property.getHref();
                        if (href != null && href.length() != 0)
                            importer.propagateXlink(new DBXlinkSurfaceGeometry(id, parentId, rootId, reverse, href, cityObjectId));
                    }
                }
            }

            // get surfaceMembers
            if (multiSurface.isSetSurfaceMembers()) {
                SurfaceArrayProperty property = multiSurface.getSurfaceMembers();
                if (property.isSetSurface()) {
                    for (AbstractSurface abstractSurface : property.getSurface())
						doImport(abstractSurface, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
                }
            }
        }

        // MultiSolid
        else if (geometry instanceof MultiSolid) {
            MultiSolid multiSolid = (MultiSolid) geometry;

            if (origGmlId != null && !isCopy)
                importer.putGeometryId(origGmlId, id, rootId, reverse, gmlId);

            // set root entry
            psGeomElem.setLong(1, id);
            psGeomElem.setString(2, gmlId);
            psGeomElem.setLong(4, rootId);
            psGeomElem.setInt(5, 0);
            psGeomElem.setInt(6, 0);
            psGeomElem.setInt(7, 0);
            psGeomElem.setInt(8, isXlink ? isXlinkValue : 0);
            psGeomElem.setInt(9, reverse ? 1 : 0);
            psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(11, nullGeometryType, nullGeometryTypeName);
            psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);

            if (parentId != 0)
                psGeomElem.setLong(3, parentId);
            else
                psGeomElem.setNull(3, Types.NULL);

            if (cityObjectId != 0)
                psGeomElem.setLong(13, cityObjectId);
            else
                psGeomElem.setNull(13, Types.NULL);

            addBatch();

            // set parentId
            parentId = id;

            // get solidMember
            if (multiSolid.isSetSolidMember()) {
                for (SolidProperty property : multiSolid.getSolidMember()) {
                    if (property.isSetSolid()) {
                        doImport(property.getSolid(), parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
                    } else {
                        String href = property.getHref();
                        if (href != null && href.length() != 0)
                            importer.propagateXlink(new DBXlinkSurfaceGeometry(id, parentId, rootId, reverse, href, cityObjectId));
                    }
                }
            }

            // get SolidMembers
            if (multiSolid.isSetSolidMembers()) {
                SolidArrayProperty property = multiSolid.getSolidMembers();
                if (property.isSetSolid()) {
                    for (AbstractSolid abstractSolid : property.getSolid())
                        doImport(abstractSolid, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
                }
            }
        }

        // fallback
        else {
            Logger.getInstance().warn(importer.getObjectSignature(geometry) + ": Unsupported geometry. Trying to map to a MultiSurface geometry.");
            MultiSurface multiSurface = geometryConverter.convertToMultiSurface(geometry);
            if (!multiSurface.getSurfaceMember().isEmpty())
                doImport(multiSurface, id, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
            else {
                importer.logOrThrowErrorMessage("Failed to map " + importer.getObjectSignature(geometry) + " to a MultiSurface geometry.");
                return 0;
            }
        }

        return id;
    }

    private void addBatch() throws CityGMLImportException, SQLException {
        psGeomElem.addBatch();
        if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
            importer.executeBatch(TableEnum.SURFACE_GEOMETRY);
    }

    @Override
    public void executeBatch() throws CityGMLImportException, SQLException {
        if (batchCounter > 0) {
            psGeomElem.executeBatch();
            batchCounter = 0;
        }
    }

    @Override
    public void close() throws CityGMLImportException, SQLException {
        psGeomElem.close();
        psNextSeqValues.close();
    }

    private class IdManager extends GeometryWalker {
        private long[] ids;
        private int count;
        private int index;

        @Override
        public void visit(AbstractGeometry geometry) {
        	if (!(geometry instanceof OrientableSurface))
        		count++;
        }

        @Override
        public void visit(AbstractSurfacePatch surfacePatch) {
            count++;
        }

        private void clear() {
            reset();
            ids = null;
            count = 0;
            index = 0;
        }

        private long prepare(AbstractGeometry geometry) throws SQLException {
            clear();

            // count number of tuples to be inserted into database
            geometry.accept(this);
            if (count == 0)
                return 0;

            // retrieve sequence values
            psNextSeqValues.setInt(1, count);
            try (ResultSet rs = psNextSeqValues.executeQuery()) {
                ids = new long[count];
                int i = 0;

                while (rs.next())
                    ids[i++] = rs.getLong(1);

                return next();
            }
        }

        private long next() {
            return ids[index++];
        }
    }
}
