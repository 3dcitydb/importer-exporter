/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.database.DatabaseType;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.config.internal.Internal;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkLinearRing;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSolidGeometry;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.modules.citygml.importer.util.LocalTextureCoordinatesResolver;
import org.citydb.modules.citygml.importer.util.RingValidator;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.texturedsurface._AbstractAppearance;
import org.citygml4j.model.citygml.texturedsurface._AppearanceProperty;
import org.citygml4j.model.citygml.texturedsurface._TexturedSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
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
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;
import org.citygml4j.model.gml.geometry.primitives.Rectangle;
import org.citygml4j.model.gml.geometry.primitives.Sign;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.Surface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfacePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Triangle;
import org.citygml4j.model.gml.geometry.primitives.TrianglePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.util.child.ChildInfo;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.util.walker.GeometryWalker;

public class DBSurfaceGeometry implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psGeomElem;
	private PreparedStatement psNextSeqValues;
	private DBDeprecatedMaterialModel materialModelImporter;
	private DBOtherGeometry otherGeometryImporter;
	private PrimaryKeyManager pkManager;

	private int dbSrid;
	private boolean replaceGmlId;
	private boolean importAppearance;
	private boolean applyTransformation;
	private boolean isImplicit;
	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;
	private LocalTextureCoordinatesResolver localTexCoordResolver;
	private RingValidator ringValidator;

	public DBSurfaceGeometry(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		dbSrid = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
		importAppearance = config.getProject().getImporter().getAppearances().isSetImportAppearance();
		applyTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();
		if (gmlIdCodespace != null && gmlIdCodespace.length() > 0)
			gmlIdCodespace = "'" + gmlIdCodespace + "', ";
		else
			gmlIdCodespace = null;		

		StringBuilder stmt = new StringBuilder()
		.append("insert into SURFACE_GEOMETRY (ID, GMLID, ").append(gmlIdCodespace != null ? "GMLID_CODESPACE, " : "").append("PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY, SOLID_GEOMETRY, IMPLICIT_GEOMETRY, CITYOBJECT_ID) values ")
		.append("(?, ?, ").append(gmlIdCodespace != null ? gmlIdCodespace : "").append("?, ?, ?, ?, ?, ?, ?, ?, ");

		if (dbImporterManager.getDatabaseAdapter().getDatabaseType() == DatabaseType.POSTGIS) {
			// the current PostGIS JDBC driver lacks support for geometry objects of type PolyhedralSurface
			// thus, we have to use the database function ST_GeomFromEWKT to insert such geometries
			// TODO: rework as soon as the JDBC driver supports PolyhedralSurface
			stmt.append("ST_GeomFromEWKT(?), ");	
		} else
			stmt.append("?, ");

		stmt.append("?, ?)");

		psGeomElem = batchConn.prepareStatement(stmt.toString());
		psNextSeqValues = batchConn.prepareStatement(dbImporterManager.getDatabaseAdapter().getSQLAdapter().getNextSequenceValuesQuery(DBSequencerEnum.SURFACE_GEOMETRY_ID_SEQ));

		materialModelImporter = (DBDeprecatedMaterialModel)dbImporterManager.getDBImporter(DBImporterEnum.DEPRECATED_MATERIAL_MODEL);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
		pkManager = new PrimaryKeyManager();
		localTexCoordResolver = dbImporterManager.getLocalTextureCoordinatesResolver();
		ringValidator = new RingValidator();
	}

	public boolean isSurfaceGeometry(AbstractGeometry abstractGeometry) {
		switch (abstractGeometry.getGMLClass()) {
		case LINEAR_RING:
		case POLYGON:
		case ORIENTABLE_SURFACE:
		case _TEXTURED_SURFACE:
		case COMPOSITE_SURFACE:
		case SURFACE:
		case TRIANGULATED_SURFACE:
		case TIN:
		case SOLID:
		case COMPOSITE_SOLID:
		case MULTI_POLYGON:
		case MULTI_SURFACE:
		case MULTI_SOLID:
			return true;
		case GEOMETRIC_COMPLEX:
			GeometricComplex complex = (GeometricComplex)abstractGeometry;
			boolean hasUnsupportedGeometry = false;
			for (GeometricPrimitiveProperty primitiveProperty : complex.getElement()) {
				if (primitiveProperty.isSetGeometricPrimitive()) {
					if (!isSurfaceGeometry(primitiveProperty.getGeometricPrimitive())) {
						hasUnsupportedGeometry = true;
						break;
					}
				}
			}

			return hasUnsupportedGeometry;
		default:
			return false;
		}
	}

	public long insert(AbstractGeometry surfaceGeometry, long cityObjectId) throws SQLException {
		// check whether we can deal with the geometry
		if (!isSurfaceGeometry(surfaceGeometry)) {
			StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
					surfaceGeometry.getGMLClass(), 
					surfaceGeometry.getId()));
			msg.append(": Unsupported geometry type.");

			LOG.error(msg.toString());
			return 0;
		}

		boolean success = pkManager.retrieveIds(surfaceGeometry);
		if (!success) {
			StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
					surfaceGeometry.getGMLClass(), 
					surfaceGeometry.getId()));
			msg.append(": Failed to acquire primary key values for surface geometry from database.");

			LOG.error(msg.toString());
			return 0;
		}

		long surfaceGeometryId = pkManager.nextId();
		insert(surfaceGeometry, surfaceGeometryId, 0, surfaceGeometryId, false, false, false, cityObjectId);
		pkManager.clear();

		return surfaceGeometryId;
	}

	public long insertImplicitGeometry(AbstractGeometry surfaceGeometry) throws SQLException {
		// if affine transformation is activated we apply the user-defined affine
		// transformation to the transformation matrix associated with the implicit geometry.
		// thus, we do not need to apply it to the coordinate values
		boolean _applyTransformation = applyTransformation;
		int _dbSrid = dbSrid;

		try {
			isImplicit = true;
			applyTransformation = false;
			dbSrid = 0;
			return insert(surfaceGeometry, 0);
		} finally {
			isImplicit = false;
			applyTransformation = _applyTransformation;
			dbSrid = _dbSrid;
		}
	}

	private void insert(AbstractGeometry surfaceGeometry,
			long surfaceGeometryId,
			long parentId,
			long rootId,
			boolean reverse,
			boolean isXlink,
			boolean isCopy,
			long cityObjectId) throws SQLException {
		GMLClass surfaceGeometryType = surfaceGeometry.getGMLClass();
		dbImporterManager.updateGeometryCounter(surfaceGeometryType);

		if (!isCopy)
			isCopy = surfaceGeometry.hasLocalProperty(Internal.GEOMETRY_ORIGINAL);

		if (!isXlink)
			isXlink = surfaceGeometry.hasLocalProperty(Internal.GEOMETRY_XLINK);

		// gml:id handling
		String origGmlId, gmlId;
		origGmlId = gmlId = surfaceGeometry.getId();

		if (gmlId == null || replaceGmlId) {
			if (!surfaceGeometry.hasLocalProperty(Internal.GEOMETRY_ORIGINAL)) {
				if (!surfaceGeometry.hasLocalProperty("replaceGmlId")) {
					gmlId = DefaultGMLIdManager.getInstance().generateUUID();					
					surfaceGeometry.setId(gmlId);
					surfaceGeometry.setLocalProperty("replaceGmlId", true);
				}
			} else {
				AbstractGeometry original = (AbstractGeometry)surfaceGeometry.getLocalProperty(Internal.GEOMETRY_ORIGINAL);
				if (!original.hasLocalProperty("replaceGmlId")) {
					gmlId = DefaultGMLIdManager.getInstance().generateUUID();					
					original.setId(gmlId);
					original.setLocalProperty("replaceGmlId", true);
				} else
					gmlId = original.getId();

				surfaceGeometry.setId(gmlId);
			}
		}

		// ok, now we can have a look at different gml geometry objects
		// firstly, handle simple surface geometries
		// a single linearRing
		if (surfaceGeometryType == GMLClass.LINEAR_RING) {
			LinearRing linearRing = (LinearRing)surfaceGeometry;
			if (!ringValidator.validate(linearRing, origGmlId))
				return;

			List<Double> points = linearRing.toList3d(reverse);
			if (applyTransformation)
				dbImporterManager.getAffineTransformer().transformCoordinates(points);

			// well, taking care about geometry is not enough... this ring could
			// be referenced by a <textureCoordinates> element. since we cannot store
			// the gml:id of linear rings in the database, we have to remember its id
			if (importAppearance && !isCopy) {
				if (linearRing.isSetId()) {
					if (localTexCoordResolver != null && localTexCoordResolver.isActive())
						localTexCoordResolver.registerLinearRing(linearRing.getId(), surfaceGeometryId, reverse);

					// the ring could also be the target of a global appearance
					dbImporterManager.propagateXlink(new DBXlinkLinearRing(
							linearRing.getId(),
							surfaceGeometryId,
							0,
							reverse));
				}
			}

			double[] coordinates = new double[points.size()];

			int i = 0;
			for (Double point : points)
				coordinates[i++] = point.doubleValue();

			GeometryObject geomObj = GeometryObject.createPolygon(coordinates, 3, dbSrid);
			Object obj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, batchConn);

			if (origGmlId != null && !isCopy)
				dbImporterManager.putUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

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

		// a simple polygon
		else if (surfaceGeometryType == GMLClass.POLYGON) {
			Polygon polygon = (Polygon)surfaceGeometry;

			if (polygon.isSetExterior()) {
				List<List<Double>> pointList = new ArrayList<List<Double>>();
				AbstractRing exteriorAbstractRing = polygon.getExterior().getRing();
				if (exteriorAbstractRing instanceof LinearRing) {
					LinearRing exteriorLinearRing = (LinearRing)exteriorAbstractRing;
					if (!ringValidator.validate(exteriorLinearRing, origGmlId))
						return;

					List<Double> points = exteriorLinearRing.toList3d(reverse);
					if (applyTransformation)
						dbImporterManager.getAffineTransformer().transformCoordinates(points);

					pointList.add(points);
					int ringNo = 0;
					dbImporterManager.updateGeometryCounter(GMLClass.LINEAR_RING);

					// well, taking care about geometry is not enough... this ring could
					// be referenced by a <textureCoordinates> element. since we cannot store
					// the gml:id of linear rings in the database, we have to remember its id
					if (importAppearance && !isCopy) {
						if (exteriorLinearRing.isSetId()) {
							if (localTexCoordResolver != null && localTexCoordResolver.isActive())
								localTexCoordResolver.registerLinearRing(exteriorLinearRing.getId(), surfaceGeometryId, reverse);

							// the ring could also be the target of a global appearance
							dbImporterManager.propagateXlink(new DBXlinkLinearRing(
									exteriorLinearRing.getId(),
									surfaceGeometryId,
									ringNo,
									reverse));
						}
					}

					if (polygon.isSetInterior()) {
						for (AbstractRingProperty abstractRingProperty : polygon.getInterior()) {
							AbstractRing interiorAbstractRing = abstractRingProperty.getRing();
							if (interiorAbstractRing instanceof LinearRing) {								
								LinearRing interiorLinearRing = (LinearRing)interiorAbstractRing;
								if (!ringValidator.validate(interiorLinearRing, origGmlId))
									continue;

								List<Double> interiorPoints = interiorLinearRing.toList3d(reverse);
								if (applyTransformation)
									dbImporterManager.getAffineTransformer().transformCoordinates(interiorPoints);

								pointList.add(interiorPoints);

								dbImporterManager.updateGeometryCounter(GMLClass.LINEAR_RING);

								// also remember the gml:id of interior rings in case it is
								// referenced by a <textureCoordinates> element
								if (importAppearance && !isCopy && interiorLinearRing.isSetId()) {
									if (localTexCoordResolver != null && localTexCoordResolver.isActive())
										localTexCoordResolver.registerLinearRing(interiorLinearRing.getId(), surfaceGeometryId, reverse);

									// the ring could also be the target of a global appearance
									dbImporterManager.propagateXlink(new DBXlinkLinearRing(
											interiorLinearRing.getId(),
											surfaceGeometryId,
											++ringNo,
											reverse));
								}
							} else {
								// invalid ring...
								StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
										interiorAbstractRing.getGMLClass(), 
										origGmlId));
								msg.append(": Only gml:LinearRing elements are supported as interior rings.");
								LOG.error(msg.toString());
								return;
							}
						}
					}

					double[][] coordinates = new double[pointList.size()][];
					int i = 0;
					for (List<Double> coordsList : pointList) {
						double[] coords = new double[coordsList.size()];

						int j = 0;
						for (Double coord : coordsList) {
							coords[j] = coord.doubleValue();
							j++;
						}

						coordinates[i] = coords;	
						i++;
					}

					GeometryObject geomObj = GeometryObject.createPolygon(coordinates, 3, dbSrid);
					Object obj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, batchConn);

					if (origGmlId != null && !isCopy)
						dbImporterManager.putUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

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
				} else {
					// invalid ring...
					StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
							exteriorAbstractRing.getGMLClass(), 
							origGmlId));
					msg.append(": Only gml:LinearRing elements are supported as exterior rings.");
					LOG.error(msg.toString());
					return;
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
						abstractSurface.setId(DefaultGMLIdManager.getInstance().generateUUID());

					// mapping target
					mapping = abstractSurface.getId();

					switch (abstractSurface.getGMLClass()) {
					case _TEXTURED_SURFACE:
					case ORIENTABLE_SURFACE:
						insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
						break;
					case POLYGON:
					case COMPOSITE_SURFACE:
					case SURFACE:
					case TRIANGULATED_SURFACE:
					case TIN:
						surfaceGeometryId = pkManager.nextId();
						insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
						break;
					default:
						LOG.error(abstractSurface.getGMLClass() + " is not supported as the base surface of an " + GMLClass.ORIENTABLE_SURFACE);
					}

				} else {
					// xlink
					String href = surfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								surfaceGeometryId,
								parentId,
								rootId,
								reverse,
								href,
								cityObjectId));
					}

					mapping = href.replaceAll("^#", "");
				}

				// do mapping
				if (origGmlId != null && !isCopy)
					dbImporterManager.putUID(origGmlId, -1, -1, negativeOrientation, mapping, CityGMLClass.ABSTRACT_GML_GEOMETRY);
			}
		}

		// texturedSurface
		// this is a CityGML class, not a GML class.
		else if (surfaceGeometryType == GMLClass._TEXTURED_SURFACE) {
			_TexturedSurface texturedSurface = (_TexturedSurface)surfaceGeometry;
			AbstractSurface abstractSurface = null;

			boolean negativeOrientation = false;
			if (texturedSurface.isSetOrientation() && texturedSurface.getOrientation().equals("-")) {
				reverse = !reverse;
				negativeOrientation = true;
			}

			String targetURI = null;

			if (texturedSurface.isSetBaseSurface()) {
				SurfaceProperty surfaceProperty = texturedSurface.getBaseSurface();
				if (surfaceProperty.isSetSurface()) {
					abstractSurface = surfaceProperty.getSurface();

					if (!abstractSurface.isSetId())
						abstractSurface.setId(DefaultGMLIdManager.getInstance().generateUUID());

					// appearance and mapping target
					targetURI = abstractSurface.getId();

					// do mapping
					if (origGmlId != null && !isCopy)
						dbImporterManager.putUID(origGmlId, -1, -1, negativeOrientation, targetURI, CityGMLClass.ABSTRACT_GML_GEOMETRY);

					switch (abstractSurface.getGMLClass()) {
					case _TEXTURED_SURFACE:
					case ORIENTABLE_SURFACE:
						insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);					
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
									interiorRing.setId(DefaultGMLIdManager.getInstance().generateUUID());
							}
						}
					case COMPOSITE_SURFACE:
					case SURFACE:
					case TRIANGULATED_SURFACE:
					case TIN:
						surfaceGeometryId = pkManager.nextId();
						insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
						break;
					default:
						LOG.error(abstractSurface.getGMLClass() + " is not supported as the base surface of a " + GMLClass._TEXTURED_SURFACE);
					}

				} else {
					// xlink
					String href = surfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								surfaceGeometryId,
								parentId,
								rootId,
								reverse,
								href,
								cityObjectId));

						targetURI = href.replaceAll("^#", "");

						// do mapping
						if (origGmlId != null && !isCopy)
							dbImporterManager.putUID(origGmlId, -1, -1, negativeOrientation, targetURI, CityGMLClass.ABSTRACT_GML_GEOMETRY);

						// well, regarding appearances we cannot work on remote geometries so far...				
						StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
								texturedSurface.getGMLClass(), 
								origGmlId));
						msg.append(": Texture information for referenced geometry objects are not supported.");

						LOG.error(msg.toString());
					}

					return;
				}
			} else {
				// we cannot continue without having a base surface...
				StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
						texturedSurface.getGMLClass(), 
						origGmlId));
				msg.append(": Could not find <baseSurface> element.");

				LOG.error(msg.toString());				
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

							StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
									texturedSurface.getGMLClass(), 
									origGmlId));
							msg.append(": Texture coordinates are only supported for base surfaces of type gml:Polygon.");

							LOG.error(msg.toString());
							continue;
						}

						boolean isFront = !(appearanceProperty.isSetOrientation() && 
								appearanceProperty.getOrientation().equals("-"));

						materialModelImporter.insert(appearance, abstractSurface, cityObjectId, isFront, targetURI);
					} else {
						// xlink
						String href = appearanceProperty.getHref();

						if (href != null && href.length() != 0) {
							boolean success = materialModelImporter.insertXlink(href, surfaceGeometryId, cityObjectId);
							if (!success) {
								LOG.error("XLink reference '" + href + "' could not be resolved.");
							}
						}
					}
				}
			}
		}

		// compositeSurface
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SURFACE) {
			CompositeSurface compositeSurface = (CompositeSurface)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				dbImporterManager.putUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

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
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						case POLYGON:
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							surfaceGeometryId = pkManager.nextId();
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						default:
							LOG.error(abstractSurface.getGMLClass() + " is not supported as member of a " + GMLClass.COMPOSITE_SURFACE);
						}

					} else {
						// xlink
						String href = surfaceProperty.getHref();

						if (href != null && href.length() != 0) {
							dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
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
		// since a surface is a geometric primitive we represent it as composite surface
		// within the database
		else if (surfaceGeometryType == GMLClass.SURFACE) {
			Surface surface = (Surface)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				dbImporterManager.putUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

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

			// get surface patches
			if (surface.isSetPatches()) {
				SurfacePatchArrayProperty arrayProperty = surface.getPatches();
				if (arrayProperty.isSetSurfacePatch()) {
					for (AbstractSurfacePatch surfacePatch : arrayProperty.getSurfacePatch()) {

						if (surfacePatch.getGMLClass() == GMLClass.RECTANGLE) {
							Rectangle rectangle = (Rectangle)surfacePatch;
							if (rectangle.isSetExterior()) {
								LinearRing exteriorLinearRing = (LinearRing)rectangle.getExterior().getRing();
								if (exteriorLinearRing != null) {
									surfaceGeometryId = pkManager.nextId();
									insert(exteriorLinearRing, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
								}
							}
						}

						else if (surfacePatch.getGMLClass() == GMLClass.TRIANGLE) {
							Triangle triangle = (Triangle)surfacePatch;
							if (triangle.isSetExterior()) {
								LinearRing exteriorLinearRing = (LinearRing)triangle.getExterior().getRing();
								if (exteriorLinearRing != null) {
									surfaceGeometryId = pkManager.nextId();
									insert(exteriorLinearRing, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
								}
							}
						}
					}
				}
			}
		}

		// TriangulatedSurface, TIN
		else if (surfaceGeometryType == GMLClass.TRIANGULATED_SURFACE ||
				surfaceGeometryType == GMLClass.TIN) {
			TriangulatedSurface triangulatedSurface = (TriangulatedSurface)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				dbImporterManager.putUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

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
					for (Triangle trianglePatch : arrayProperty.getTriangle()) {
						if (trianglePatch.isSetExterior()) {
							LinearRing exteriorLinearRing = (LinearRing)trianglePatch.getExterior().getRing();
							if (exteriorLinearRing != null) {
								surfaceGeometryId = pkManager.nextId();
								insert(exteriorLinearRing, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							}
						}						
					}
				}
			}
		}

		// Solid
		else if (surfaceGeometryType == GMLClass.SOLID) {
			Solid solid = (Solid)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				dbImporterManager.putUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

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
				GeometryObject geomObj = otherGeometryImporter.getSolid(solid);
				if (geomObj != null) 
					solidObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, batchConn);
				else {
					// we cannot build the solid geometry in main memory 
					// possibly the solid references surfaces from another feature per xlink
					// so, remember its id to build the solid geometry later
					dbImporterManager.propagateXlink(new DBXlinkSolidGeometry(surfaceGeometryId));
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
						insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					}
				} else {
					// xlink
					String href = exteriorSurface.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
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
				StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
						solid.getGMLClass(), 
						origGmlId));
				msg.append(": gml:interior is not supported.");

				LOG.error(msg.toString());
			}
		}

		// CompositeSolid
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SOLID) {
			CompositeSolid compositeSolid = (CompositeSolid)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				dbImporterManager.putUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

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
				GeometryObject geomObj = otherGeometryImporter.getCompositeSolid(compositeSolid);
				if (geomObj != null) 
					compositeSolidObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, batchConn);
				else {
					// we cannot build the solid geometry in main memory 
					// possibly the solid references surfaces from another feature per xlink
					// so, remember its id to build the solid geometry later
					dbImporterManager.propagateXlink(new DBXlinkSolidGeometry(surfaceGeometryId));
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
						insert(solidProperty.getSolid(), surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					} else {
						// xlink
						String href = solidProperty.getHref();

						if (href != null && href.length() != 0) {
							dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
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
				dbImporterManager.putUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

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
						insert(polygonProperty.getPolygon(), surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					} else {
						// xlink
						String href = polygonProperty.getHref();

						if (href != null && href.length() != 0) {
							dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
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
				dbImporterManager.putUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

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
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						case POLYGON:
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							surfaceGeometryId = pkManager.nextId();
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						default:
							LOG.error(abstractSurface.getGMLClass() + " is not supported as member of a " + GMLClass.MULTI_SURFACE);
						}

					} else {
						// xlink
						String href = surfaceProperty.getHref();

						if (href != null && href.length() != 0) {
							dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
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
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						case POLYGON:
						case COMPOSITE_SURFACE:
						case SURFACE:
						case TRIANGULATED_SURFACE:
						case TIN:
							surfaceGeometryId = pkManager.nextId();
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
							break;
						default:
							LOG.error(abstractSurface.getGMLClass() + " is not supported as member of a " + GMLClass.MULTI_SURFACE);
						}
					}
				}
			}
		}

		// MultiSolid
		else if (surfaceGeometryType == GMLClass.MULTI_SOLID) {
			MultiSolid multiSolid = (MultiSolid)surfaceGeometry;

			if (origGmlId != null && !isCopy)
				dbImporterManager.putUID(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.ABSTRACT_GML_GEOMETRY);

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
						insert(solidProperty.getSolid(), surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					} else {
						// xlink
						String href = solidProperty.getHref();

						if (href != null && href.length() != 0) {
							dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
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
						insert(abstractSolid, surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
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
						insert(geometricPrimitiveProperty.getGeometricPrimitive(), surfaceGeometryId, parentId, rootId, reverse, isXlink, isCopy, cityObjectId);
					else {
						// xlink
						String href = geometricPrimitiveProperty.getHref();

						if (href != null && href.length() != 0) {
							dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
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
	}

	private void addBatch() throws SQLException {
		psGeomElem.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.SURFACE_GEOMETRY);
	}

	@Override
	public void executeBatch() throws SQLException {
		psGeomElem.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psGeomElem.close();
		psNextSeqValues.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.SURFACE_GEOMETRY;
	}

	private class PrimaryKeyManager extends GeometryWalker {
		private final ChildInfo info = new ChildInfo();
		private long[] ids;
		private int count;
		private int index;

		@Override
		public void visit(AbstractGeometry geometry) {
			switch (geometry.getGMLClass()) {
			case LINEAR_RING:
				LinearRing ring = (LinearRing)geometry;
				if (info.getParentGeometry(ring).getGMLClass() != GMLClass.POLYGON)
					count++;				
				break;
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
				count++;
			default:
				break;
			}
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
			ResultSet rs = null;
			try {
				psNextSeqValues.setInt(1, count);
				rs = psNextSeqValues.executeQuery();

				ids = new long[count];
				int i = 0;

				while (rs.next())
					ids[i++] = rs.getLong(1);

				return true;
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						//
					}
				}
			}
		}

		private long nextId() {
			return ids[index++];
		}
	}

}
