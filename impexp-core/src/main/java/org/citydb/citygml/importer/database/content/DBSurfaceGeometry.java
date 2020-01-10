/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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

import org.citydb.citygml.common.database.xlink.DBXlinkLinearRing;
import org.citydb.citygml.common.database.xlink.DBXlinkSolidGeometry;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.LocalAppearanceHandler;
import org.citydb.citygml.importer.util.RingValidator;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.database.DatabaseType;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.SequenceEnum;
import org.citydb.database.schema.TableEnum;
import org.citydb.util.CoreConstants;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.texturedsurface._AbstractAppearance;
import org.citygml4j.model.citygml.texturedsurface._AppearanceProperty;
import org.citygml4j.model.citygml.texturedsurface._TexturedSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiPolygon;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolid;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplex;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurfacePatch;
import org.citygml4j.model.gml.geometry.primitives.GeometricPrimitiveProperty;
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

	private PreparedStatement psGeomElem;
	private PreparedStatement psNextSeqValues;
	private GeometryConverter geometryConverter;
	private DBAppearance appearanceImporter;
	private PrimaryKeyManager pkManager;

	private int dbSrid;
	private boolean replaceGmlId;
	private boolean importAppearance;
	private boolean applyTransformation;
	private boolean isImplicit;
	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;
	private LocalAppearanceHandler localAppearanceHandler;
	private RingValidator ringValidator;

	public DBSurfaceGeometry(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		dbSrid = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
		importAppearance = config.getProject().getImporter().getAppearances().isSetImportAppearance();
		applyTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();
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

		pkManager = new PrimaryKeyManager();
		ringValidator = new RingValidator();
	}

	protected long doImport(AbstractGeometry surfaceGeometry, long cityObjectId) throws CityGMLImportException, SQLException {
		// check whether we can deal with the geometry
		if (!geometryConverter.isSurfaceGeometry(surfaceGeometry)) {
			importer.logOrThrowErrorMessage("Unsupported geometry type " + importer.getObjectSignature(surfaceGeometry));
			return 0;
		}

		boolean success = pkManager.retrieveIds(surfaceGeometry);
		if (!success) {
			importer.logOrThrowErrorMessage("Failed to acquire primary key values for surface geometry from database.");
			return 0;
		}

		if (surfaceGeometry.isSetId())
			surfaceGeometry.setLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID, surfaceGeometry.getId());

		long surfaceGeometryId = pkManager.nextId();
		doImport(surfaceGeometry, surfaceGeometryId, 0, surfaceGeometryId, false, false, false, cityObjectId);
		pkManager.clear();

		return surfaceGeometryId;
	}

	protected long importImplicitGeometry(AbstractGeometry surfaceGeometry) throws CityGMLImportException, SQLException {
		// if affine transformation is activated we apply the user-defined affine
		// transformation to the transformation matrix associated with the implicit geometry.
		// thus, we do not need to apply it to the coordinate values
		boolean _applyTransformation = applyTransformation;
		int _dbSrid = dbSrid;

		try {
			isImplicit = true;
			applyTransformation = false;
			dbSrid = 0;
			return doImport(surfaceGeometry, 0);
		} finally {
			isImplicit = false;
			applyTransformation = _applyTransformation;
			dbSrid = _dbSrid;
		}
	}

	private void doImport(AbstractGeometry surfaceGeometry,
			long surfaceGeometryId,
			long parentId,
			long rootId,
			boolean reverse,
			boolean isXlink,
			boolean isCopy,
			long cityObjectId) throws CityGMLImportException, SQLException {
		GMLClass surfaceGeometryType = surfaceGeometry.getGMLClass();
		importer.updateGeometryCounter(surfaceGeometryType);

		if (!isCopy)
			isCopy = surfaceGeometry.hasLocalProperty(CoreConstants.GEOMETRY_ORIGINAL);

		if (!isXlink)
			isXlink = surfaceGeometry.hasLocalProperty(CoreConstants.GEOMETRY_XLINK);

		// gml:id handling
		String origGmlId, gmlId;
		origGmlId = gmlId = surfaceGeometry.getId();
		if (gmlId == null || replaceGmlId) {
			if (!surfaceGeometry.hasLocalProperty(CoreConstants.GEOMETRY_ORIGINAL)) {
				if (!surfaceGeometry.hasLocalProperty("origGmlId")) {
					gmlId = importer.generateNewGmlId();
					surfaceGeometry.setId(gmlId);
					surfaceGeometry.setLocalProperty("origGmlId", origGmlId);
				} else
					origGmlId = (String) surfaceGeometry.getLocalProperty("origGmlId");
			} else {
				AbstractGeometry original = (AbstractGeometry) surfaceGeometry.getLocalProperty(CoreConstants.GEOMETRY_ORIGINAL);
				if (!original.hasLocalProperty("origGmlId")) {
					gmlId = importer.generateNewGmlId();
					original.setId(gmlId);
					original.setLocalProperty("origGmlId", origGmlId);
				} else
					gmlId = original.getId();

				surfaceGeometry.setId(gmlId);
			}
		}

		// ok, now we can have a look at different gml geometry objects
		// firstly, handle simple surface geometries
		// a simple polygon
		if (surfaceGeometryType == GMLClass.POLYGON) {
			Polygon polygon = (Polygon)surfaceGeometry;

			if (polygon.isSetExterior()) {
				List<List<Double>> pointList = new ArrayList<>();
				AbstractRing exteriorRing = polygon.getExterior().getRing();
				if (exteriorRing != null) {
					List<Double> points = exteriorRing.toList3d(reverse);
					if (!ringValidator.validate(points, exteriorRing))
						return;

					if (applyTransformation)
						importer.getAffineTransformer().transformCoordinates(points);

					pointList.add(points);
					int ringNo = 0;
					importer.updateGeometryCounter(GMLClass.LINEAR_RING);

					// well, taking care about geometry is not enough... this ring could
					// be referenced by a <textureCoordinates> element. since we cannot store
					// the gml:id of linear rings in the database, we have to remember its id
					if (importAppearance && !isCopy && exteriorRing.isSetId()) {
						if (localAppearanceHandler != null && localAppearanceHandler.hasParameterizedTextures())
							localAppearanceHandler.registerLinearRing(exteriorRing.getId(), surfaceGeometryId, reverse);

						// the ring could also be the target of a global appearance
						importer.propagateXlink(new DBXlinkLinearRing(
								exteriorRing.getId(),
								surfaceGeometryId,
								ringNo,
								reverse));
					}

					if (polygon.isSetInterior()) {
						for (AbstractRingProperty abstractRingProperty : polygon.getInterior()) {
							AbstractRing interiorRing = abstractRingProperty.getRing();
							if (interiorRing != null) {
								List<Double> interiorPoints = interiorRing.toList3d(reverse);
								if (!ringValidator.validate(interiorPoints, interiorRing))
									continue;

								if (applyTransformation)
									importer.getAffineTransformer().transformCoordinates(interiorPoints);

								pointList.add(interiorPoints);
								importer.updateGeometryCounter(GMLClass.LINEAR_RING);

								// also remember the gml:id of interior rings in case it is
								// referenced by a <textureCoordinates> element
								if (importAppearance && !isCopy && interiorRing.isSetId()) {
									if (localAppearanceHandler != null && localAppearanceHandler.hasParameterizedTextures())
										localAppearanceHandler.registerLinearRing(interiorRing.getId(), surfaceGeometryId, reverse);

									// the ring could also be the target of a global appearance
									importer.propagateXlink(new DBXlinkLinearRing(
											interiorRing.getId(),
											surfaceGeometryId,
											++ringNo,
											reverse));
								}
							}
						}
					}

					double[][] coordinates = new double[pointList.size()][];
					int i = 0;
					for (List<Double> coordsList : pointList) {
						double[] coords = new double[coordsList.size()];

						int j = 0;
						for (Double coord : coordsList) {
							coords[j] = coord;
							j++;
						}

						coordinates[i] = coords;
						i++;
					}

					GeometryObject geomObj = GeometryObject.createPolygon(coordinates, 3, dbSrid);
					Object obj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, batchConn);

					if (origGmlId != null && !isCopy)
						importer.putGeometryUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId);

					psGeomElem.setLong(1, surfaceGeometryId);
					psGeomElem.setString(2, gmlId);
					psGeomElem.setLong(4, rootId);
					psGeomElem.setInt(5, 0);
					psGeomElem.setInt(6, 0);
					psGeomElem.setInt(7, 0);
					psGeomElem.setInt(8, isXlink ? 1 : 0);
					psGeomElem.setInt(9, reverse ? 1 : 0);
					psGeomElem.setNull(11, nullGeometryType, nullGeometryTypeName);

					if (parentId != 0)
						psGeomElem.setLong(3, parentId);
					else
						psGeomElem.setNull(3, Types.NULL);

					if (!isImplicit) {
						psGeomElem.setObject(10, obj);
						psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);
					} else {
						psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
						psGeomElem.setObject(12, obj);
					}

					if (cityObjectId != 0)
						psGeomElem.setLong(13, cityObjectId);
					else
						psGeomElem.setNull(13, Types.NULL);

					addBatch();
				}
			}
		}

		// ok, handle complexes, composites and aggregates
		// orientableSurface
		else if (surfaceGeometryType == GMLClass.ORIENTABLE_SURFACE) {
			OrientableSurface orientableSurface = (OrientableSurface)surfaceGeometry;

			boolean negativeOrientation = false;
			if (orientableSurface.isSetOrientation() && orientableSurface.getOrientation() == Sign.MINUS) {
				reverse = !reverse;
				negativeOrientation = true;
			}

			if (orientableSurface.isSetBaseSurface()) {
				SurfaceProperty surfaceProperty = orientableSurface.getBaseSurface();
				String mapping = null;

				if (surfaceProperty.isSetSurface()) {
					AbstractSurface abstractSurface = surfaceProperty.getSurface();
					if (!abstractSurface.isSetId())
						abstractSurface.setId(importer.generateNewGmlId());

					// mapping target
					mapping = abstractSurface.getId();

					switch (abstractSurface.getGMLClass()) {
					case _TEXTURED_SURFACE:
					case ORIENTABLE_SURFACE:
						doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
						break;
					case POLYGON:
					case COMPOSITE_SURFACE:
					case SURFACE:
					case TRIANGULATED_SURFACE:
					case TIN:
						surfaceGeometryId = pkManager.nextId();
						doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
						break;
					default:
						importer.logOrThrowErrorMessage(importer.getObjectSignature(orientableSurface, origGmlId) +
								": " + abstractSurface.getGMLClass() + " is not supported as the base surface.");
					}

				} else {
					String href = surfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								surfaceGeometryId,
								parentId,
								rootId,
								reverse,
								href,
								cityObjectId));

						mapping = href.replaceAll("^#", "");
					}
				}

				// do mapping
				if (origGmlId != null && !isCopy)
					importer.putGeometryUID(origGmlId, -1, -1, negativeOrientation, mapping);
			}
		}

		// texturedSurface
		// this is a CityGML class, not a GML class.
		else if (surfaceGeometryType == GMLClass._TEXTURED_SURFACE) {
			_TexturedSurface texturedSurface = (_TexturedSurface)surfaceGeometry;
			AbstractSurface abstractSurface;

			boolean negativeOrientation = false;
			if (texturedSurface.isSetOrientation() && texturedSurface.getOrientation() == Sign.MINUS) {
				reverse = !reverse;
				negativeOrientation = true;
			}

			String targetURI;

			if (texturedSurface.isSetBaseSurface()) {
				SurfaceProperty surfaceProperty = texturedSurface.getBaseSurface();
				if (surfaceProperty.isSetSurface()) {
					abstractSurface = surfaceProperty.getSurface();

					if (!abstractSurface.isSetId())
						abstractSurface.setId(importer.generateNewGmlId());

					// appearance and mapping target
					targetURI = abstractSurface.getId();

					// do mapping
					if (origGmlId != null && !isCopy)
						importer.putGeometryUID(origGmlId, -1, -1, negativeOrientation, targetURI);

					switch (abstractSurface.getGMLClass()) {
					case _TEXTURED_SURFACE:
					case ORIENTABLE_SURFACE:
						doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
						break;
					case POLYGON:
						Polygon polygon = (Polygon)abstractSurface;

						// make sure all exterior and interior rings do have a gml:id
						// in order to assign texture coordinates
						if (polygon.isSetExterior()) {
							LinearRing exteriorRing = (LinearRing)polygon.getExterior().getRing();
							if (exteriorRing != null && !exteriorRing.isSetId())
								exteriorRing.setId(targetURI);
						}

						if (polygon.isSetInterior()) {
							for (AbstractRingProperty abstractRingProperty : polygon.getInterior()) {
								LinearRing interiorRing = (LinearRing)abstractRingProperty.getRing();
								if (!interiorRing.isSetId())
									interiorRing.setId(importer.generateNewGmlId());
							}
						}
					case COMPOSITE_SURFACE:
					case SURFACE:
					case TRIANGULATED_SURFACE:
					case TIN:
						surfaceGeometryId = pkManager.nextId();
						doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
						break;
					default:
						importer.logOrThrowErrorMessage(importer.getObjectSignature(texturedSurface, origGmlId) +
								": " + abstractSurface.getGMLClass() + " is not supported as the base surface.");
					}

				} else {
					String href = surfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								surfaceGeometryId,
								parentId,
								rootId,
								reverse,
								href,
								cityObjectId));

						targetURI = href.replaceAll("^#", "");

						// do mapping
						if (origGmlId != null && !isCopy)
							importer.putGeometryUID(origGmlId, -1, -1, negativeOrientation, targetURI);

						// well, regarding appearances we cannot work on remote geometries so far...
						importer.logOrThrowErrorMessage(importer.getObjectSignature(texturedSurface, origGmlId) +
								": Texture information for referenced geometry objects are not supported.");
					}

					return;
				}
			} else {
				// we cannot continue without having a base surface...
				importer.logOrThrowErrorMessage(importer.getObjectSignature(texturedSurface, origGmlId) +
						": Failed to find base surface of textured surface.");
				return;
			}

			if (importAppearance && !isCopy && texturedSurface.isSetAppearance()) {
				for (_AppearanceProperty appearanceProperty : texturedSurface.getAppearance()) {
					if (appearanceProperty.isSetAppearance()) {
						_AbstractAppearance appearance = appearanceProperty.getAppearance();

						// how to map texture coordinates to a composite surface of
						// arbitrary depth?
						if (appearance.getCityGMLClass() == CityGMLClass._SIMPLE_TEXTURE &&
								abstractSurface.getGMLClass() != GMLClass.POLYGON) {
							importer.logOrThrowErrorMessage(importer.getObjectSignature(texturedSurface, origGmlId) +
									": Texture coordinates are only supported for base surfaces of type gml:Polygon.");
							continue;
						}

						boolean isFront = !(appearanceProperty.isSetOrientation() &&
								appearanceProperty.getOrientation() == Sign.MINUS);

						appearanceImporter.importTexturedSurface(appearance, abstractSurface, cityObjectId, isFront, targetURI);
					} else {
						String href = appearanceProperty.getHref();
						if (href != null && href.length() != 0)
							appearanceImporter.importTexturedSurfaceXlink(href, surfaceGeometryId, cityObjectId);
					}
				}
			}
		}

		// compositeSurface
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SURFACE) {
			CompositeSurface compositeSurface = (CompositeSurface)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				importer.putGeometryUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId);

			// set root entry
			psGeomElem.setLong(1, surfaceGeometryId);
			psGeomElem.setString(2, gmlId);
			psGeomElem.setLong(4, rootId);
			psGeomElem.setInt(5, 0);
			psGeomElem.setInt(6, 1);
			psGeomElem.setInt(7, 0);
			psGeomElem.setInt(8, isXlink ? 1 : 0);
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
			parentId = surfaceGeometryId;

			// get surfaceMember
			if (compositeSurface.isSetSurfaceMember()) {
				for (SurfaceProperty surfaceProperty : compositeSurface.getSurfaceMember()) {
					if (surfaceProperty.isSetSurface()) {
						AbstractSurface abstractSurface = surfaceProperty.getSurface();

						switch (abstractSurface.getGMLClass()) {
						case _TEXTURED_SURFACE:
						case ORIENTABLE_SURFACE:
							doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						case POLYGON:
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							surfaceGeometryId = pkManager.nextId();
							doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						default:
							importer.logOrThrowErrorMessage(importer.getObjectSignature(compositeSurface, origGmlId) +
									": " + abstractSurface.getGMLClass() + " is not supported as member surface.");
						}

					} else {
						String href = surfaceProperty.getHref();
						if (href != null && href.length() != 0) {
							importer.propagateXlink(new DBXlinkSurfaceGeometry(
									surfaceGeometryId,
									parentId,
									rootId,
									reverse,
									href,
									cityObjectId));
						}
					}
				}
			}
		}

		// Surface
		else if (surfaceGeometryType == GMLClass.SURFACE) {
			Surface surface = (Surface)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				importer.putGeometryUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId);

			int nrOfPatches = surface.isSetPatches() && surface.getPatches().isSetSurfacePatch() ?
					surface.getPatches().getSurfacePatch().size() : 0;

			// add a composite surface as root unless there is only one surface patch
			if (nrOfPatches != 1) {
				psGeomElem.setLong(1, surfaceGeometryId);
				psGeomElem.setString(2, gmlId);
				psGeomElem.setLong(4, rootId);
				psGeomElem.setInt(5, 0);
				psGeomElem.setInt(6, 1);
				psGeomElem.setInt(7, 0);
				psGeomElem.setInt(8, isXlink ? 1 : 0);
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
				parentId = surfaceGeometryId;
			}

			// import surface patches
			if (nrOfPatches > 0) {
				for (AbstractSurfacePatch surfacePatch : surface.getPatches().getSurfacePatch()) {
					surfaceGeometryId = pkManager.nextId();

					Polygon polygon = new Polygon();
					if (nrOfPatches == 1)
						polygon.setId(gmlId);

					if (surfacePatch.getGMLClass() == GMLClass.RECTANGLE) {
						Rectangle rectangle = (Rectangle) surfacePatch;
						polygon = new Polygon();
						polygon.setExterior(rectangle.getExterior());
					} else if (surfacePatch.getGMLClass() == GMLClass.TRIANGLE) {
						Triangle triangle = (Triangle) surfacePatch;
						polygon = new Polygon();
						polygon.setExterior(triangle.getExterior());
					} else if (surfacePatch.getGMLClass() == GMLClass.POLYGON_PATCH) {
						PolygonPatch polygonPatch = (PolygonPatch) surfacePatch;
						polygon.setExterior(polygonPatch.getExterior());
						polygon.setInterior(polygonPatch.getInterior());
					}

					doImport(polygon, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
				}
			}
		}

		// TriangulatedSurface, TIN
		else if (surfaceGeometryType == GMLClass.TRIANGULATED_SURFACE ||
				surfaceGeometryType == GMLClass.TIN) {
			TriangulatedSurface triangulatedSurface = (TriangulatedSurface)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				importer.putGeometryUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId);

			// set root entry
			psGeomElem.setLong(1, surfaceGeometryId);
			psGeomElem.setString(2, gmlId);
			psGeomElem.setLong(4, rootId);
			psGeomElem.setInt(5, 0);
			psGeomElem.setInt(6, 0);
			psGeomElem.setInt(7, 1);
			psGeomElem.setInt(8, isXlink ? 1 : 0);
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
			parentId = surfaceGeometryId;

			// get triangles
			if (triangulatedSurface.isSetTrianglePatches()) {
				TrianglePatchArrayProperty arrayProperty = triangulatedSurface.getTrianglePatches();
				if (arrayProperty.isSetTriangle()) {
					for (Triangle triangle : arrayProperty.getTriangle()) {
						surfaceGeometryId = pkManager.nextId();
						Polygon polygon = new Polygon();
						polygon.setExterior(triangle.getExterior());
						doImport(polygon, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					}
				}
			}
		}

		// Solid
		else if (surfaceGeometryType == GMLClass.SOLID) {
			Solid solid = (Solid)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				importer.putGeometryUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId);

			// set root entry
			psGeomElem.setLong(1, surfaceGeometryId);
			psGeomElem.setString(2, gmlId);
			psGeomElem.setLong(4, rootId);
			psGeomElem.setInt(5, 1);
			psGeomElem.setInt(6, 0);
			psGeomElem.setInt(7, 0);
			psGeomElem.setInt(8, isXlink ? 1 : 0);
			psGeomElem.setInt(9, reverse ? 1 : 0);
			psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
			psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);

			// create solid geometry object
			Object solidObj = null;
			if (surfaceGeometryId == rootId) {
				GeometryObject geomObj = geometryConverter.getSolid(solid);
				if (geomObj != null)
					solidObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, batchConn);
				else {
					// we cannot build the solid geometry in main memory
					// possibly the solid references surfaces from another feature per xlink
					// so, remember its id to build the solid geometry later
					importer.propagateXlink(new DBXlinkSolidGeometry(surfaceGeometryId));
				}
			}

			if (solidObj != null)
				psGeomElem.setObject(11, solidObj);
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
			parentId = surfaceGeometryId;

			// get Exterior
			if (solid.isSetExterior()) {
				SurfaceProperty exteriorSurface = solid.getExterior();

				if (exteriorSurface.isSetSurface()) {
					AbstractSurface abstractSurface = exteriorSurface.getSurface();

					// we just allow CompositeSurfaces here!
					if (abstractSurface.getGMLClass() == GMLClass.COMPOSITE_SURFACE) {
						surfaceGeometryId = pkManager.nextId();
						doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					}
				} else {
					String href = exteriorSurface.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								surfaceGeometryId,
								parentId,
								rootId,
								reverse,
								href,
								cityObjectId));
					}
				}
			}

			// interior is not supported!
			if (solid.isSetInterior()) {
				importer.logOrThrowErrorMessage(importer.getObjectSignature(solid, origGmlId) +
						": Interior solids are not supported.");
			}
		}

		// CompositeSolid
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SOLID) {
			CompositeSolid compositeSolid = (CompositeSolid)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				importer.putGeometryUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId);

			// set root entry
			psGeomElem.setLong(1, surfaceGeometryId);
			psGeomElem.setString(2, gmlId);
			psGeomElem.setLong(4, rootId);
			psGeomElem.setInt(5, 1);
			psGeomElem.setInt(6, 1);
			psGeomElem.setInt(7, 0);
			psGeomElem.setInt(8, isXlink ? 1 : 0);
			psGeomElem.setInt(9, reverse ? 1 : 0);
			psGeomElem.setNull(10, nullGeometryType, nullGeometryTypeName);
			psGeomElem.setNull(12, nullGeometryType, nullGeometryTypeName);

			// create composite solid geometry object
			Object compositeSolidObj = null;
			if (surfaceGeometryId == rootId) {
				GeometryObject geomObj = geometryConverter.getCompositeSolid(compositeSolid);
				if (geomObj != null)
					compositeSolidObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, batchConn);
				else {
					// we cannot build the solid geometry in main memory
					// possibly the solid references surfaces from another feature per xlink
					// so, remember its id to build the solid geometry later
					importer.propagateXlink(new DBXlinkSolidGeometry(surfaceGeometryId));
				}
			}

			if (compositeSolidObj != null)
				psGeomElem.setObject(11, compositeSolidObj);
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
			parentId = surfaceGeometryId;

			// get solidMember
			if (compositeSolid.isSetSolidMember()) {
				for (SolidProperty solidProperty : compositeSolid.getSolidMember()) {
					if (solidProperty.isSetSolid()) {
						surfaceGeometryId = pkManager.nextId();
						doImport(solidProperty.getSolid(), surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					} else {
						String href = solidProperty.getHref();
						if (href != null && href.length() != 0) {
							importer.propagateXlink(new DBXlinkSurfaceGeometry(
									surfaceGeometryId,
									parentId,
									rootId,
									reverse,
									href,
									cityObjectId));
						}
					}
				}
			}
		}

		// MultiPolygon
		else if (surfaceGeometryType == GMLClass.MULTI_POLYGON) {
			MultiPolygon multiPolygon = (MultiPolygon)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				importer.putGeometryUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId);

			// set root entry
			psGeomElem.setLong(1, surfaceGeometryId);
			psGeomElem.setString(2, gmlId);
			psGeomElem.setLong(4, rootId);
			psGeomElem.setInt(5, 0);
			psGeomElem.setInt(6, 0);
			psGeomElem.setInt(7, 0);
			psGeomElem.setInt(8, isXlink ? 1 : 0);
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
			parentId = surfaceGeometryId;

			// get polygonMember
			if (multiPolygon.isSetPolygonMember()) {
				for (PolygonProperty polygonProperty : multiPolygon.getPolygonMember()) {
					if (polygonProperty.isSetPolygon()) {
						surfaceGeometryId = pkManager.nextId();
						doImport(polygonProperty.getPolygon(), surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					} else {
						String href = polygonProperty.getHref();
						if (href != null && href.length() != 0) {
							importer.propagateXlink(new DBXlinkSurfaceGeometry(
									surfaceGeometryId,
									parentId,
									rootId,
									reverse,
									href,
									cityObjectId));
						}
					}
				}
			}
		}

		// MultiSurface
		else if (surfaceGeometryType == GMLClass.MULTI_SURFACE) {
			MultiSurface multiSurface = (MultiSurface)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				importer.putGeometryUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId);

			// set root entry
			psGeomElem.setLong(1, surfaceGeometryId);
			psGeomElem.setString(2, gmlId);
			psGeomElem.setLong(4, rootId);
			psGeomElem.setInt(5, 0);
			psGeomElem.setInt(6, 0);
			psGeomElem.setInt(7, 0);
			psGeomElem.setInt(8, isXlink ? 1 : 0);
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
			parentId = surfaceGeometryId;

			// get surfaceMember
			if (multiSurface.isSetSurfaceMember()) {
				for (SurfaceProperty surfaceProperty : multiSurface.getSurfaceMember()) {
					if (surfaceProperty.isSetSurface()) {
						AbstractSurface abstractSurface = surfaceProperty.getSurface();

						switch (abstractSurface.getGMLClass()) {
						case _TEXTURED_SURFACE:
						case ORIENTABLE_SURFACE:
							doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						case POLYGON:
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							surfaceGeometryId = pkManager.nextId();
							doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						default:
							importer.logOrThrowErrorMessage(importer.getObjectSignature(multiSurface, origGmlId) +
									": " + abstractSurface.getGMLClass() + " is not supported as member surface.");
						}

					} else {
						String href = surfaceProperty.getHref();
						if (href != null && href.length() != 0) {
							importer.propagateXlink(new DBXlinkSurfaceGeometry(
									surfaceGeometryId,
									parentId,
									rootId,
									reverse,
									href,
									cityObjectId));
						}
					}
				}
			}

			// get surfaceMembers
			if (multiSurface.isSetSurfaceMembers()) {
				SurfaceArrayProperty surfaceArrayProperty = multiSurface.getSurfaceMembers();

				if (surfaceArrayProperty.isSetSurface()) {
					for (AbstractSurface abstractSurface : surfaceArrayProperty.getSurface()) {

						switch (abstractSurface.getGMLClass()) {
						case _TEXTURED_SURFACE:
						case ORIENTABLE_SURFACE:
							doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						case POLYGON:
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							surfaceGeometryId = pkManager.nextId();
							doImport(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						default:
							importer.logOrThrowErrorMessage(importer.getObjectSignature(multiSurface, origGmlId) +
									abstractSurface.getGMLClass() + " is not supported as member surface.");
							return;
						}
					}
				}
			}
		}

		// MultiSolid
		else if (surfaceGeometryType == GMLClass.MULTI_SOLID) {
			MultiSolid multiSolid = (MultiSolid)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				importer.putGeometryUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId);

			// set root entry
			psGeomElem.setLong(1, surfaceGeometryId);
			psGeomElem.setString(2, gmlId);
			psGeomElem.setLong(4, rootId);
			psGeomElem.setInt(5, 0);
			psGeomElem.setInt(6, 0);
			psGeomElem.setInt(7, 0);
			psGeomElem.setInt(8, isXlink ? 1 : 0);
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
			parentId = surfaceGeometryId;

			// get solidMember
			if (multiSolid.isSetSolidMember()) {
				for (SolidProperty solidProperty : multiSolid.getSolidMember()) {
					if (solidProperty.isSetSolid()) {
						surfaceGeometryId = pkManager.nextId();
						doImport(solidProperty.getSolid(), surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					} else {
						String href = solidProperty.getHref();
						if (href != null && href.length() != 0) {
							importer.propagateXlink(new DBXlinkSurfaceGeometry(
									surfaceGeometryId,
									parentId,
									rootId,
									reverse,
									href,
									cityObjectId));
						}
					}
				}
			}

			// get SolidMembers
			if (multiSolid.isSetSolidMembers()) {
				SolidArrayProperty solidArrayProperty = multiSolid.getSolidMembers();

				if (solidArrayProperty.isSetSolid()) {
					for (AbstractSolid abstractSolid : solidArrayProperty.getSolid()) {
						surfaceGeometryId = pkManager.nextId();
						doImport(abstractSolid, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					}
				}
			}
		}

		// GeometricComplex
		else if (surfaceGeometryType == GMLClass.GEOMETRIC_COMPLEX) {
			GeometricComplex geometricComplex = (GeometricComplex)surfaceGeometry;

			if (geometricComplex.isSetElement()) {
				for (GeometricPrimitiveProperty geometricPrimitiveProperty : geometricComplex.getElement()) {
					if (geometricPrimitiveProperty.isSetGeometricPrimitive())
						doImport(geometricPrimitiveProperty.getGeometricPrimitive(), surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					else {
						String href = geometricPrimitiveProperty.getHref();
						if (href != null && href.length() != 0) {
							importer.propagateXlink(new DBXlinkSurfaceGeometry(
									surfaceGeometryId,
									parentId,
									rootId,
									reverse,
									href,
									cityObjectId));
						}
					}
				}
			}
		}

		// MultiGeometry
		else if (surfaceGeometryType == GMLClass.MULTI_GEOMETRY) {
			MultiSurface multiSurface = geometryConverter.convertToMultiSurface((MultiGeometry) surfaceGeometry);
			doImport(multiSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
		}
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

	private class PrimaryKeyManager extends GeometryWalker {
		private long[] ids;
		private int count;
		private int index;

		@Override
		public void visit(AbstractGeometry geometry) {
			switch (geometry.getGMLClass()) {
				case POLYGON:
				case COMPOSITE_SURFACE:
				case SURFACE:
				case TRIANGULATED_SURFACE:
				case TIN:
				case SOLID:
				case COMPOSITE_SOLID:
				case MULTI_POLYGON:
				case MULTI_SURFACE:
				case MULTI_SOLID:
				case MULTI_GEOMETRY:
					count++;
			}
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

		private boolean retrieveIds(AbstractGeometry geometry) throws SQLException {
			clear();

			// count number of tuples to be inserted into database
			geometry.accept(this);
			if (count == 0)
				return false;

			// retrieve sequence values
			psNextSeqValues.setInt(1, count);
			try (ResultSet rs = psNextSeqValues.executeQuery()) {
				ids = new long[count];
				int i = 0;

				while (rs.next())
					ids[i++] = rs.getLong(1);

				return true;
			}
		}

		private long nextId() {
			return ids[index++];
		}
	}

}
