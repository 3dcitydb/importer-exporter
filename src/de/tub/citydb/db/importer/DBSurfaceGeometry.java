package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.xlink.DBXlinkLinearRing;
import de.tub.citydb.db.xlink.DBXlinkSurfaceGeometry;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.util.UUIDManager;
import de.tub.citygml4j.implementation.gml._3_1_1.LinearRingImpl;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.texturedsurface._Appearance;
import de.tub.citygml4j.model.citygml.texturedsurface._AppearanceProperty;
import de.tub.citygml4j.model.citygml.texturedsurface._TexturedSurface;
import de.tub.citygml4j.model.gml.AbstractGeometricPrimitive;
import de.tub.citygml4j.model.gml.AbstractGeometry;
import de.tub.citygml4j.model.gml.AbstractRingProperty;
import de.tub.citygml4j.model.gml.AbstractSolid;
import de.tub.citygml4j.model.gml.AbstractSurface;
import de.tub.citygml4j.model.gml.AbstractSurfacePatch;
import de.tub.citygml4j.model.gml.CompositeSolid;
import de.tub.citygml4j.model.gml.CompositeSurface;
import de.tub.citygml4j.model.gml.GMLClass;
import de.tub.citygml4j.model.gml.GeometricComplex;
import de.tub.citygml4j.model.gml.GeometricPrimitiveProperty;
import de.tub.citygml4j.model.gml.LinearRing;
import de.tub.citygml4j.model.gml.MultiPolygon;
import de.tub.citygml4j.model.gml.MultiSolid;
import de.tub.citygml4j.model.gml.MultiSurface;
import de.tub.citygml4j.model.gml.OrientableSurface;
import de.tub.citygml4j.model.gml.Polygon;
import de.tub.citygml4j.model.gml.PolygonProperty;
import de.tub.citygml4j.model.gml.Rectangle;
import de.tub.citygml4j.model.gml.Solid;
import de.tub.citygml4j.model.gml.SolidArrayProperty;
import de.tub.citygml4j.model.gml.SolidProperty;
import de.tub.citygml4j.model.gml.Surface;
import de.tub.citygml4j.model.gml.SurfaceArrayProperty;
import de.tub.citygml4j.model.gml.SurfacePatchArrayProperty;
import de.tub.citygml4j.model.gml.SurfaceProperty;
import de.tub.citygml4j.model.gml.Triangle;
import de.tub.citygml4j.model.gml.TrianglePatchArrayProperty;
import de.tub.citygml4j.model.gml.TriangulatedSurface;

public class DBSurfaceGeometry implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psParentElem;
	private PreparedStatement psMemberElem;
	private DBDeprecatedMaterialModel materialModelImporter;

	private String dbSrid;
	private boolean replaceGmlId;
	private boolean importAppearance;
	private int parentBatchCounter;
	private int memberBatchCounter;

	public DBSurfaceGeometry(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		dbSrid = config.getInternal().getDbSrid();
		importAppearance = config.getProject().getImporter().getAppearances().isSetImportAppearance();
		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();

		if (gmlIdCodespace != null && gmlIdCodespace.length() != 0)
			gmlIdCodespace = "'" + gmlIdCodespace + "'";
		else
			gmlIdCodespace = "null";

		psParentElem = batchConn.prepareStatement("insert into SURFACE_GEOMETRY (ID, GMLID, GMLID_CODESPACE, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY) values "
				+ "(?, ?, " + gmlIdCodespace + ", ?, ?, ?, ?, ?, 0, ?, ?)");
		psMemberElem = batchConn.prepareStatement("insert into SURFACE_GEOMETRY (ID, GMLID, GMLID_CODESPACE, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY) values "
				+ "(SURFACE_GEOMETRY_SEQ.nextval, ?, " + gmlIdCodespace + ", ?, ?, 0, 0, 0, 0, ?, ?)");

		materialModelImporter = (DBDeprecatedMaterialModel)dbImporterManager.getDBImporter(DBImporterEnum.DEPRECATED_MATERIAL_MODEL);
	}

	public long insert(AbstractGeometry surfaceGeometry, long cityObjectId) throws SQLException {
		long surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);

		if (surfaceGeometryId != 0)
			insert(surfaceGeometry, surfaceGeometryId, 0, surfaceGeometryId, false, cityObjectId);

		return surfaceGeometryId;
	}

	private void insert(AbstractGeometry surfaceGeometry,
			long surfaceGeometryId,
			long parentId,
			long rootId,
			boolean reverse,
			long cityObjectId) throws SQLException {
		GMLClass surfaceGeometryType = surfaceGeometry.getGMLClass();
		dbImporterManager.updateGeometryCounter(surfaceGeometryType);
	
		// gml:id handling
		String origGmlId, gmlId;
		origGmlId = gmlId = surfaceGeometry.getId();

		if (gmlId == null || replaceGmlId) {
			gmlId = UUIDManager.randomUUID();
			surfaceGeometry.setId(gmlId);
		}
		
		// ok, now we can have a look at different gml geometry objects
		// firstly, handle single surface geometries
		// a simple linearRing
		if (surfaceGeometryType == GMLClass.LINEARRING) {
			LinearRing linearRing = (LinearRing)surfaceGeometry;
			List<Double> points = ((LinearRingImpl)linearRing).toList(reverse);

			if (points != null && !points.isEmpty()) {
				double[] ordinates = new double[points.size()];

				int i = 0;
				for (Double point : points)
					ordinates[i++] = point.doubleValue();

				if (importAppearance) {
					if (origGmlId == null)
						origGmlId = gmlId;							
					
					if (linearRing.getId() != null)
						dbImporterManager.propagateXlink(new DBXlinkLinearRing(
							linearRing.getId(),
							origGmlId,
							0));
				}
				
				JGeometry geom = JGeometry.createLinearPolygon(ordinates, 3, Integer.valueOf(dbSrid));
				STRUCT obj = SyncJGeometry.syncStore(geom, batchConn);

				if (parentId == 0 && rootId == surfaceGeometryId) {
					if (origGmlId != null)
						dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

					psParentElem.setLong(1, surfaceGeometryId);
					psParentElem.setString(2, gmlId);
					psParentElem.setNull(3, 0);
					psParentElem.setLong(4, surfaceGeometryId);
					psParentElem.setInt(5, 0);
					psParentElem.setInt(6, 0);
					psParentElem.setInt(7, 0);
					psParentElem.setInt(8, reverse ? 1 : 0);
					psParentElem.setObject(9, obj);

					addParentBatch();

				} else {
					if (origGmlId != null)
						dbImporterManager.putGmlId(origGmlId, -1, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

					psMemberElem.setString(1, gmlId);
					psMemberElem.setLong(2, parentId);
					psMemberElem.setLong(3, rootId);
					psMemberElem.setInt(4, reverse ? 1 : 0);
					psMemberElem.setObject(5, obj);

					addMemberBatch();
				}
			}
		}

		// a simple polygon
		else if (surfaceGeometryType == GMLClass.POLYGON) {
			Polygon polygon = (Polygon)surfaceGeometry;

			if (polygon.getExterior() != null) {
				List<List<Double>> pointList = new ArrayList<List<Double>>();
				LinearRing exteriorLinearRing = (LinearRing)polygon.getExterior().getRing();
				if (exteriorLinearRing != null) {
					List<Double> points = ((LinearRingImpl)exteriorLinearRing).toList(reverse);

					if (points != null && !points.isEmpty()) {
						pointList.add(points);
						int ringNo = 0;
						dbImporterManager.updateGeometryCounter(GMLClass.LINEARRING);

						// well, taking care about geometry is not enough... this ring could
						// be referenced by a <textureCoordinates> element. since we cannot store
						// the gml:id of linear rings in the database, we have to remember its id
						if (importAppearance) {
							if (origGmlId == null)
								origGmlId = gmlId;							
							
							if (exteriorLinearRing.getId() != null)
								dbImporterManager.propagateXlink(new DBXlinkLinearRing(
									exteriorLinearRing.getId(),
									origGmlId,
									ringNo));
						}

						if (polygon.getInterior() != null) {
							List<AbstractRingProperty> abstractRingPropertyList = polygon.getInterior();
							for (AbstractRingProperty abstractRingProperty : abstractRingPropertyList) {
								LinearRing interiorLinearRing = (LinearRing)abstractRingProperty.getRing();
								List<Double> interiorPoints = ((LinearRingImpl)interiorLinearRing).toList(reverse);

								if (interiorPoints != null && !interiorPoints.isEmpty()) {
									pointList.add(interiorPoints);

									ringNo++;
									dbImporterManager.updateGeometryCounter(GMLClass.LINEARRING);

									// also remember the gml:id of interior rings in case it is
									// referenced by a <textureCoordinates> element
									if (importAppearance && interiorLinearRing.getId() != null)
										dbImporterManager.propagateXlink(new DBXlinkLinearRing(
												interiorLinearRing.getId(),
												gmlId,
												ringNo));
								}
							}

							// we need this dummy entry to know the maximum number of found rings later on...
							if (importAppearance && ringNo > 0)
								dbImporterManager.propagateXlink(new DBXlinkLinearRing(
										null,
										origGmlId,
										ringNo));
						}

						Object[] pointArray = new Object[pointList.size()];
						int i = 0;
						for (List<Double> coordsList : pointList) {
							double[] coords = new double[coordsList.size()];

							int j = 0;
							for (Double coord : coordsList) {
								coords[j] = coord.doubleValue();
								j++;
							}

							pointArray[i] = coords;					
							i++;
						}

						JGeometry geom = JGeometry.createLinearPolygon(pointArray, 3, Integer.valueOf(dbSrid));
						STRUCT obj = SyncJGeometry.syncStore(geom, batchConn);

						if (parentId == 0 && rootId == surfaceGeometryId) {
							if (origGmlId != null)
								dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

							psParentElem.setLong(1, surfaceGeometryId);
							psParentElem.setString(2, gmlId);
							psParentElem.setNull(3, 0);
							psParentElem.setLong(4, surfaceGeometryId);
							psParentElem.setInt(5, 0);
							psParentElem.setInt(6, 0);
							psParentElem.setInt(7, 0);
							psParentElem.setInt(8, reverse ? 1 : 0);
							psParentElem.setObject(9, obj);

							addParentBatch();

						} else {
							if (origGmlId != null)
								dbImporterManager.putGmlId(origGmlId, -1, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

							psMemberElem.setString(1, gmlId);
							psMemberElem.setLong(2, parentId);
							psMemberElem.setLong(3, rootId);
							psMemberElem.setInt(4, reverse ? 1 : 0);
							psMemberElem.setObject(5, obj);

							addMemberBatch();
						}
					}
				}
			}
		}

		// ok, handle complexes, composites and aggregates
		// orientableSurface
		else if (surfaceGeometryType == GMLClass.ORIENTABLESURFACE) {
			OrientableSurface orientableSurface = (OrientableSurface)surfaceGeometry;

			boolean negativeOrientation = false;
			String orientation = orientableSurface.getOrientation();
			if (orientation != null && orientation.equals("-")) {
				reverse = !reverse;
				negativeOrientation = true;
			}

			if (orientableSurface.getBaseSurface() != null) {
				SurfaceProperty surfaceProperty = orientableSurface.getBaseSurface();
				AbstractSurface abstractSurface = surfaceProperty.getSurface();
				String mapping = null;

				if (abstractSurface != null) {
					if (abstractSurface.getId() == null)
						abstractSurface.setId(UUIDManager.randomUUID());

					// mapping target
					mapping = abstractSurface.getId();

					switch (abstractSurface.getGMLClass()) {
					case POLYGON:
					case _TEXTUREDSURFACE:
					case ORIENTABLESURFACE:
						insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
						break;
					case COMPOSITESURFACE:
					case SURFACE:
					case TRIANGULATEDSURFACE:
					case TIN:
						surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
						insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
						break;
					}

				} else {
					// xlink
					String href = surfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
								surfaceGeometryId,
								parentId,
								rootId,
								reverse,
								href
						);

						dbImporterManager.propagateXlink(xlink);
					}
					
					mapping = href.replaceAll("^#", "");
				}
				
				// do mapping
				if (origGmlId != null)
					dbImporterManager.putGmlId(origGmlId, -1, -1, negativeOrientation, mapping, CityGMLClass.GMLGEOMETRY);
			}
		}

		// texturedSurface
		// this is a CityGML class, not a GML class.
		else if (surfaceGeometryType == GMLClass._TEXTUREDSURFACE) {
			_TexturedSurface texturedSurface = (_TexturedSurface)surfaceGeometry;
			AbstractSurface abstractSurface = null;

			boolean negativeOrientation = false;
			String orientation = texturedSurface.getOrientation();
			if (orientation.equals("-")) {
				reverse = !reverse;
				negativeOrientation = true;
			}

			String targetURI = null;

			if (texturedSurface.getBaseSurface() != null) {
				SurfaceProperty surfaceProperty = texturedSurface.getBaseSurface();
				abstractSurface = surfaceProperty.getSurface();

				if (abstractSurface != null) {
					if (abstractSurface.getId() == null)
						abstractSurface.setId(UUIDManager.randomUUID());

					// appearance and mapping target
					targetURI = abstractSurface.getId();

					// do mapping
					if (origGmlId != null)
						dbImporterManager.putGmlId(origGmlId, -1, -1, negativeOrientation, targetURI, CityGMLClass.GMLGEOMETRY);

					switch (abstractSurface.getGMLClass()) {
					case POLYGON:
						Polygon polygon = (Polygon)abstractSurface;

						// make sure all exterior and interior rings do have a gml:id
						// in order to assign texture coordinates
						if (polygon.getExterior().getRing() != null) {
							LinearRing exterior = (LinearRing)polygon.getExterior().getRing();
							if (exterior.getId() == null)
								exterior.setId(targetURI);
						}

						if (polygon.getInterior() != null) {
							List<AbstractRingProperty> abstractRingPropertyList = polygon.getInterior();
							for (AbstractRingProperty abstractRingProperty : abstractRingPropertyList) {
								LinearRing interiorRing = (LinearRing)abstractRingProperty.getRing();

								if (interiorRing.getId() == null)
									interiorRing.setId(UUIDManager.randomUUID());
							}
						}
					case _TEXTUREDSURFACE:
					case ORIENTABLESURFACE:
						insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);					
						break;
					case COMPOSITESURFACE:
					case SURFACE:
					case TRIANGULATEDSURFACE:
					case TIN:
						surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
						insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
						break;
					}

				} else {
					// xlink
					String href = surfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
								surfaceGeometryId,
								parentId,
								rootId,
								reverse,
								href
						);

						dbImporterManager.propagateXlink(xlink);

						targetURI = href.replaceAll("^#", "");
						
						// do mapping
						if (origGmlId != null)
							dbImporterManager.putGmlId(origGmlId, -1, -1, negativeOrientation, targetURI, CityGMLClass.GMLGEOMETRY);
						
						// well, regarding appearances we cannot work on remote geometries so far....
						dbImporterManager.propagateEvent(new LogMessageEvent(
								"TexturedSurface: Texturinformationen für referenzierte Geometrieobjekte werden nicht unterstützt.",
								LogMessageEnum.ERROR
						));
					}

					return;
				}
			}

			if (importAppearance) {
				if (texturedSurface.getAppearance() != null) {
					List<_AppearanceProperty> appearancePropertyList = texturedSurface.getAppearance();
					for (_AppearanceProperty appearanceProperty : appearancePropertyList) {
						if (appearanceProperty.getAppearance() != null) {
							_Appearance appearance = appearanceProperty.getAppearance();

							// how to map texture coordinates to a composite surface of
							// arbitrary depth?
							if (appearance.getCityGMLClass() == CityGMLClass._SIMPLETEXTURE &&
									abstractSurface.getGMLClass() != GMLClass.POLYGON) {

								LogMessageEvent log = new LogMessageEvent(
										"TexturedSurface: Texturkoordinaten werden nur für gml:Polygon unterstützt.",
										LogMessageEnum.ERROR
								);

								dbImporterManager.propagateEvent(log);
								continue;
							}

							boolean isFront = true;
							if (appearanceProperty.getOrientation() != null &&
									appearanceProperty.getOrientation().equals("-"))
								isFront = false;

							materialModelImporter.insert(appearance, abstractSurface, cityObjectId, isFront, targetURI);
						} else {
							// xlink
							String href = appearanceProperty.getHref();

							if (href != null && href.length() != 0) {
								boolean success = materialModelImporter.insertXlink(href, surfaceGeometryId, cityObjectId, appearanceProperty.getCityGMLModule().getAppearanceDependency());

								if (!success) {
									LogMessageEvent log = new LogMessageEvent(
											"Xlink-Verweis \"" + href + "\" konnte nicht aufgelöst werden.",
											LogMessageEnum.ERROR);

									dbImporterManager.propagateEvent(log);
								}
							}
						}
					}
				}
			}
		}

		// compositeSurface
		else if (surfaceGeometryType == GMLClass.COMPOSITESURFACE) {
			CompositeSurface compositeSurface = (CompositeSurface)surfaceGeometry;

			if (origGmlId != null)
				dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

			// set root entry
			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(4, rootId);
			psParentElem.setInt(5, 0);
			psParentElem.setInt(6, 1);
			psParentElem.setInt(7, 0);
			psParentElem.setInt(8, reverse ? 1 : 0);
			psParentElem.setNull(9, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
			if (parentId != 0)
				psParentElem.setLong(3, parentId);
			else
				psParentElem.setNull(3, 0);

			addParentBatch();

			// set parentId
			parentId = surfaceGeometryId;

			// get surfaceMember
			if (compositeSurface.getSurfaceMember() != null) {
				List<SurfaceProperty> surfacePropertyList = compositeSurface.getSurfaceMember();
				for (SurfaceProperty surfaceProperty : surfacePropertyList) {
					AbstractSurface abstractSurface = surfaceProperty.getSurface();
					if (abstractSurface != null) {
						
						switch (abstractSurface.getGMLClass()) {
						case POLYGON:
						case _TEXTUREDSURFACE:
						case ORIENTABLESURFACE:
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
							break;
						case COMPOSITESURFACE:
						case SURFACE:
						case TRIANGULATEDSURFACE:
						case TIN:
							surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
							break;
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
									href
							));
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

			if (origGmlId != null)
				dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

			// set root entry
			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(4, rootId);
			psParentElem.setInt(5, 0);
			psParentElem.setInt(6, 1);
			psParentElem.setInt(7, 0);
			psParentElem.setInt(8, reverse ? 1 : 0);
			psParentElem.setNull(9, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
			if (parentId != 0)
				psParentElem.setLong(3, parentId);
			else
				psParentElem.setNull(3, 0);

			addParentBatch();

			// set parentId
			parentId = surfaceGeometryId;

			// get surface patches
			if (surface.getPatches() != null) {
				SurfacePatchArrayProperty arrayProperty = surface.getPatches();
				if (arrayProperty.getSurfacePatch() != null) {
					List<? extends AbstractSurfacePatch> surfacePatches = arrayProperty.getSurfacePatch();
					for (AbstractSurfacePatch surfacePatch : surfacePatches) {

						if (surfacePatch.getGMLClass() == GMLClass.RECTANGLE) {
							Rectangle rectangle = (Rectangle)surfacePatch;
							if (rectangle.getExterior() != null) {
								LinearRing exteriorLinearRing = (LinearRing)rectangle.getExterior().getRing();
								if (exteriorLinearRing != null) 
									insert(exteriorLinearRing, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
							}
						}

						else if (surfacePatch.getGMLClass() == GMLClass.TRIANGLE) {
							Triangle triangle = (Triangle)surfacePatch;
							if (triangle.getExterior() != null) {
								LinearRing exteriorLinearRing = (LinearRing)triangle.getExterior().getRing();
								if (exteriorLinearRing != null) 
									insert(exteriorLinearRing, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
							}
						}
					}
				}

			}
		}

		// TriangulatedSurface, TIN
		else if (surfaceGeometryType == GMLClass.TRIANGULATEDSURFACE ||
				surfaceGeometryType == GMLClass.TIN) {
			TriangulatedSurface triangulatedSurface = (TriangulatedSurface)surfaceGeometry;

			if (origGmlId != null)
				dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

			// set root entry
			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(4, rootId);
			psParentElem.setInt(5, 0);
			psParentElem.setInt(6, 0);
			psParentElem.setInt(7, 1);
			psParentElem.setInt(8, reverse ? 1 : 0);
			psParentElem.setNull(9, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
			if (parentId != 0)
				psParentElem.setLong(3, parentId);
			else
				psParentElem.setNull(3, 0);

			addParentBatch();

			// set parentId
			parentId = surfaceGeometryId;

			// get triangles
			if (triangulatedSurface.getTrianglePatches() != null) {
				TrianglePatchArrayProperty arrayProperty = triangulatedSurface.getTrianglePatches();
				if (arrayProperty.getTriangle() != null) {
					List<Triangle> trianglePatches = arrayProperty.getTriangle();
					for (Triangle trianglePatch : trianglePatches) {
						if (trianglePatch.getExterior() != null) {
							LinearRing exteriorLinearRing = (LinearRing)trianglePatch.getExterior().getRing();
							if (exteriorLinearRing != null) 
								insert(exteriorLinearRing, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
						}						
					}
				}
			}
		}

		// Solid
		else if (surfaceGeometryType == GMLClass.SOLID) {
			Solid solid = (Solid)surfaceGeometry;

			if (origGmlId != null)
				dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

			// set root entry
			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(4, rootId);
			psParentElem.setInt(5, 1);
			psParentElem.setInt(6, 0);
			psParentElem.setInt(7, 0);
			psParentElem.setInt(8, reverse ? 1 : 0);
			psParentElem.setNull(9, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
			if (parentId != 0)
				psParentElem.setLong(3, parentId);
			else
				psParentElem.setNull(3, 0);

			addParentBatch();

			// set parentId
			parentId = surfaceGeometryId;

			// get Exterior
			if (solid.getExterior() != null) {
				SurfaceProperty exteriorSurface = solid.getExterior();
				AbstractSurface abstractSurface = exteriorSurface.getSurface();

				if (abstractSurface != null) {
					// we just allow CompositeSurfaces here!
					if (abstractSurface.getGMLClass() == GMLClass.COMPOSITESURFACE) {
						surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
						insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
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
								href
						));
					}
				}
			}

			// interior is not supported!
			if (solid.getInterior() != null)
				System.out.println("InteriorSolid is not supported!");
		}

		// CompositeSolid
		else if (surfaceGeometryType ==GMLClass.COMPOSITESOLID) {
			CompositeSolid compositeSolid = (CompositeSolid)surfaceGeometry;

			if (origGmlId != null)
				dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

			// set root entry
			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(4, rootId);
			psParentElem.setInt(5, 1);
			psParentElem.setInt(6, 1);
			psParentElem.setInt(7, 0);
			psParentElem.setInt(8, reverse ? 1 : 0);
			psParentElem.setNull(9, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
			if (parentId != 0)
				psParentElem.setLong(3, parentId);
			else
				psParentElem.setNull(3, 0);

			addParentBatch();

			// set parentId
			parentId = surfaceGeometryId;

			// get solidMember
			if (compositeSolid.getSolidMember() != null) {
				List<SolidProperty> solidPropertyList = compositeSolid.getSolidMember();
				for (SolidProperty solidProperty : solidPropertyList) {
					AbstractSolid abstractSolid = solidProperty.getSolid();
					if (abstractSolid != null) {
						surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
						insert(abstractSolid, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
					} else {
						// xlink
						String href = solidProperty.getHref();

						if (href != null && href.length() != 0) {
							dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
									surfaceGeometryId,
									parentId,
									rootId,
									reverse,
									href

							));
						}
					}
				}
			}
		}

		// MultiPolygon
		else if (surfaceGeometryType == GMLClass.MULTIPOLYGON) {
			MultiPolygon multiPolygon = (MultiPolygon)surfaceGeometry;

			if (origGmlId != null)
				dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

			// set root entry
			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(4, rootId);
			psParentElem.setInt(5, 0);
			psParentElem.setInt(6, 0);
			psParentElem.setInt(7, 0);
			psParentElem.setInt(8, reverse ? 1 : 0);
			psParentElem.setNull(9, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
			if (parentId != 0)
				psParentElem.setLong(3, parentId);
			else
				psParentElem.setNull(3, 0);

			addParentBatch();

			// set parentId
			parentId = surfaceGeometryId;

			// get polygonMember
			if (multiPolygon.getPolygonMember() != null) {
				List<PolygonProperty> polygonPropertyList = multiPolygon.getPolygonMember();
				for (PolygonProperty polygonProperty : polygonPropertyList) {
					Polygon polygon = polygonProperty.getPolygon();
					if (polygon != null)
						insert(polygon, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
					else {
						// xlink
						String href = polygonProperty.getHref();

						if (href != null && href.length() != 0) {
							dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
									surfaceGeometryId,
									parentId,
									rootId,
									reverse,
									href
							));
						}
					}
				}
			}
		}

		// MultiSurface
		else if (surfaceGeometryType == GMLClass.MULTISURFACE) {
			MultiSurface multiSurface = (MultiSurface)surfaceGeometry;

			if (origGmlId != null)
				dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

			// set root entry
			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(4, rootId);
			psParentElem.setInt(5, 0);
			psParentElem.setInt(6, 0);
			psParentElem.setInt(7, 0);
			psParentElem.setInt(8, reverse ? 1 : 0);
			psParentElem.setNull(9, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
			if (parentId != 0)
				psParentElem.setLong(3, parentId);
			else
				psParentElem.setNull(3, 0);

			addParentBatch();

			// set parentId
			parentId = surfaceGeometryId;

			// get surfaceMember
			if (multiSurface.getSurfaceMember() != null) {
				List<SurfaceProperty> surfacePropertyList = multiSurface.getSurfaceMember();
				for (SurfaceProperty surfaceProperty : surfacePropertyList) {
					AbstractSurface abstractSurface = surfaceProperty.getSurface();
					if (abstractSurface != null) {
						
						switch (abstractSurface.getGMLClass()) {
						case POLYGON:
						case _TEXTUREDSURFACE:
						case ORIENTABLESURFACE:
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
							break;
						case COMPOSITESURFACE:
						case SURFACE:
						case TRIANGULATEDSURFACE:
						case TIN:
							surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
							break;
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
									href
							));
						}
					}
				}
			}

			// get surfaceMembers
			if (multiSurface.getSurfaceMembers() != null) {
				SurfaceArrayProperty surfaceArrayProperty = multiSurface.getSurfaceMembers();
				List<AbstractSurface> abstractSurfaceList = surfaceArrayProperty.getSurface();

				if (abstractSurfaceList != null) {
					for (AbstractSurface abstractSurface : abstractSurfaceList) {

						switch (abstractSurface.getGMLClass()) {
						case POLYGON:
						case _TEXTUREDSURFACE:
						case ORIENTABLESURFACE:
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
							break;
						case COMPOSITESURFACE:
						case SURFACE:
						case TRIANGULATEDSURFACE:
						case TIN:
							surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
							insert(abstractSurface, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
							break;
						}
					}
				}
			}
		}

		// MultiSolid
		else if (surfaceGeometryType == GMLClass.MULTISOLID) {
			MultiSolid multiSolid = (MultiSolid)surfaceGeometry;

			if (origGmlId != null)
				dbImporterManager.putGmlId(origGmlId, surfaceGeometryId, rootId, reverse, gmlId, CityGMLClass.GMLGEOMETRY);

			// set root entry
			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(3, rootId);
			psParentElem.setInt(4, 0);
			psParentElem.setInt(5, 0);
			psParentElem.setInt(6, 0);
			psParentElem.setInt(8, reverse ? 1 : 0);
			psParentElem.setNull(9, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
			if (parentId != 0)
				psParentElem.setLong(3, parentId);
			else
				psParentElem.setNull(3, 0);

			addParentBatch();

			// set parentId
			parentId = surfaceGeometryId;

			// get solidMember
			if (multiSolid.getSolidMember() != null) {
				List<SolidProperty> solidPropertyList = multiSolid.getSolidMember();
				for (SolidProperty solidProperty : solidPropertyList) {
					AbstractSolid abstractSolid = solidProperty.getSolid();
					if (abstractSolid != null) {
						surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
						insert(abstractSolid, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
					} else {
						// xlink
						String href = solidProperty.getHref();

						if (href != null && href.length() != 0) {
							dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
									surfaceGeometryId,
									parentId,
									rootId,
									reverse,
									href
							));
						}
					}
				}
			}

			// get SolidMembers
			if (multiSolid.getSolidMembers() != null) {
				SolidArrayProperty solidArrayProperty = multiSolid.getSolidMembers();
				List<AbstractSolid> abstractSolidList = solidArrayProperty.getSolid();

				if (abstractSolidList != null) {
					for (AbstractSolid abstractSolid : abstractSolidList) {
						surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
						insert(abstractSolid, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
					}
				}
			}
		}

		// GeometricComplex
		else if (surfaceGeometryType == GMLClass.GEOMETRICCOMPLEX) {
			GeometricComplex geometricComplex = (GeometricComplex)surfaceGeometry;

			if (geometricComplex.getElement() != null) {
				List<GeometricPrimitiveProperty> geometricPrimitivePropertyList = geometricComplex.getElement();
				for (GeometricPrimitiveProperty geometricPrimitiveProperty : geometricPrimitivePropertyList) {
					AbstractGeometricPrimitive abstractGeometricPrimitive = geometricPrimitiveProperty.getGeometricPrimitive();
					if (abstractGeometricPrimitive != null) {
						insert(abstractGeometricPrimitive, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
					} else {
						// xlink
						String href = geometricPrimitiveProperty.getHref();

						if (href != null && href.length() != 0) {
							dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
									surfaceGeometryId,
									parentId,
									rootId,
									reverse,
									href
							));
						}
					}
				}
			}
		}
	}

	private void addParentBatch() throws SQLException {
		psParentElem.addBatch();
		
		if (++parentBatchCounter == Internal.ORACLE_MAX_BATCH_SIZE) {
			psParentElem.executeBatch();
			parentBatchCounter = 0;
		}
	}
	
	private void addMemberBatch() throws SQLException {
		psMemberElem.addBatch();
		
		if (++memberBatchCounter == Internal.ORACLE_MAX_BATCH_SIZE) 
			executeBatch();
	}
	
	@Override
	public void executeBatch() throws SQLException {
		psParentElem.executeBatch();
		psMemberElem.executeBatch();
		
		parentBatchCounter = 0;
		memberBatchCounter = 0;
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.SURFACE_GEOMETRY;
	}

}
