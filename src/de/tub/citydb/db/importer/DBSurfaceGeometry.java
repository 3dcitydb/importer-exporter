/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
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

import org.citygml4j.impl.jaxb.gml._3_1_1.LinearRingImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.appearance.AppearanceModule;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.texturedsurface._Appearance;
import org.citygml4j.model.citygml.texturedsurface._AppearanceProperty;
import org.citygml4j.model.citygml.texturedsurface._TexturedSurface;
import org.citygml4j.model.gml.AbstractGeometry;
import org.citygml4j.model.gml.AbstractRingProperty;
import org.citygml4j.model.gml.AbstractSolid;
import org.citygml4j.model.gml.AbstractSurface;
import org.citygml4j.model.gml.AbstractSurfacePatch;
import org.citygml4j.model.gml.CompositeSolid;
import org.citygml4j.model.gml.CompositeSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.GeometricComplex;
import org.citygml4j.model.gml.GeometricPrimitiveProperty;
import org.citygml4j.model.gml.LinearRing;
import org.citygml4j.model.gml.MultiPolygon;
import org.citygml4j.model.gml.MultiSolid;
import org.citygml4j.model.gml.MultiSurface;
import org.citygml4j.model.gml.OrientableSurface;
import org.citygml4j.model.gml.Polygon;
import org.citygml4j.model.gml.PolygonProperty;
import org.citygml4j.model.gml.Rectangle;
import org.citygml4j.model.gml.Solid;
import org.citygml4j.model.gml.SolidArrayProperty;
import org.citygml4j.model.gml.SolidProperty;
import org.citygml4j.model.gml.Surface;
import org.citygml4j.model.gml.SurfaceArrayProperty;
import org.citygml4j.model.gml.SurfacePatchArrayProperty;
import org.citygml4j.model.gml.SurfaceProperty;
import org.citygml4j.model.gml.Triangle;
import org.citygml4j.model.gml.TrianglePatchArrayProperty;
import org.citygml4j.model.gml.TriangulatedSurface;
import org.citygml4j.util.CityGMLModules;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.xlink.DBXlinkLinearRing;
import de.tub.citydb.db.xlink.DBXlinkSurfaceGeometry;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.UUIDManager;
import de.tub.citydb.util.Util;

public class DBSurfaceGeometry implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psParentElem;
	private PreparedStatement psMemberElem;
	private DBDeprecatedMaterialModel materialModelImporter;

	private int dbSrid;
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
		dbSrid = config.getInternal().getOpenConnection().getMetaData().getSrid();
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
		// firstly, handle simple surface geometries
		// a single linearRing
		if (surfaceGeometryType == GMLClass.LINEARRING) {
			LinearRing linearRing = (LinearRing)surfaceGeometry;
			List<Double> points = ((LinearRingImpl)linearRing).toList(reverse);

			if (points != null && !points.isEmpty()) {
				Double x = points.get(0);
				Double y = points.get(1);
				Double z = points.get(2);
				int nrOfPoints = points.size();
				
				if (!x.equals(points.get(nrOfPoints - 3)) ||
						!y.equals(points.get(nrOfPoints - 2)) ||
						!z.equals(points.get(nrOfPoints - 1))) {
					// repair unclosed ring because sdoapi fails to do its job...
					StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
							linearRing.getGMLClass(), 
							origGmlId));
					msg.append(": Ring is not closed. Appending first coordinate to fix it.");
					LOG.warn(msg.toString());
					
					points.add(x);
					points.add(y);
					points.add(z);
					++nrOfPoints;
				}
				
				if (nrOfPoints < 4) {
					// invalid ring...
					StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
							linearRing.getGMLClass(), 
							origGmlId));
					msg.append(": Ring contains less than 4 coordinates. Skipping invalid ring.");
					LOG.error(msg.toString());
					return;
				}
				
				double[] ordinates = new double[points.size()];

				int i = 0;
				for (Double point : points)
					ordinates[i++] = point.doubleValue();

				if (importAppearance) {
					if (origGmlId == null)
						origGmlId = gmlId;							

					if (linearRing.isSetId())
						dbImporterManager.propagateXlink(new DBXlinkLinearRing(
								origGmlId,
								origGmlId,
								0));
				}

				JGeometry geom = JGeometry.createLinearPolygon(ordinates, 3, dbSrid);
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

			if (polygon.isSetExterior()) {
				List<List<Double>> pointList = new ArrayList<List<Double>>();
				LinearRing exteriorLinearRing = (LinearRing)polygon.getExterior().getRing();
				if (exteriorLinearRing != null) {
					List<Double> points = ((LinearRingImpl)exteriorLinearRing).toList(reverse);

					if (points != null && !points.isEmpty()) {
						Double x = points.get(0);
						Double y = points.get(1);
						Double z = points.get(2);
						int nrOfPoints = points.size();
						
						if (!x.equals(points.get(nrOfPoints - 3)) ||
								!y.equals(points.get(nrOfPoints - 2)) ||
								!z.equals(points.get(nrOfPoints - 1))) {
							// repair unclosed ring because sdoapi fails to do its job...
							StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
									exteriorLinearRing.getGMLClass(), 
									origGmlId));
							msg.append(": Exterior ring is not closed. Appending first coordinate to fix it.");
							LOG.warn(msg.toString());
							
							points.add(x);
							points.add(y);
							points.add(z);
							++nrOfPoints;
						}					
						
						if (nrOfPoints < 4) {
							// invalid ring...
							StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
									exteriorLinearRing.getGMLClass(), 
									origGmlId));
							msg.append(": Exterior ring contains less than 4 coordinates. Skipping invalid ring.");
							LOG.error(msg.toString());
							return;
						}
						
						pointList.add(points);
						int ringNo = 0;
						dbImporterManager.updateGeometryCounter(GMLClass.LINEARRING);

						// well, taking care about geometry is not enough... this ring could
						// be referenced by a <textureCoordinates> element. since we cannot store
						// the gml:id of linear rings in the database, we have to remember its id
						if (importAppearance) {
							if (origGmlId == null)
								origGmlId = gmlId;							

							if (exteriorLinearRing.isSetId())
								dbImporterManager.propagateXlink(new DBXlinkLinearRing(
										exteriorLinearRing.getId(),
										origGmlId,
										ringNo));
						}

						if (polygon.isSetInterior()) {
							for (AbstractRingProperty abstractRingProperty : polygon.getInterior()) {
								LinearRing interiorLinearRing = (LinearRing)abstractRingProperty.getRing();
								List<Double> interiorPoints = ((LinearRingImpl)interiorLinearRing).toList(reverse);

								if (interiorPoints != null && !interiorPoints.isEmpty()) {									
									x = interiorPoints.get(0);
									y = interiorPoints.get(1);
									z = interiorPoints.get(2);
									nrOfPoints = interiorPoints.size();
									
									if (!x.equals(interiorPoints.get(nrOfPoints - 3)) ||
											!y.equals(interiorPoints.get(nrOfPoints - 2)) ||
											!z.equals(interiorPoints.get(nrOfPoints - 1))) {
										// repair unclosed ring because sdoapi fails to do its job...
										StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
												interiorLinearRing.getGMLClass(), 
												origGmlId));
										msg.append(": Interior ring is not closed. Appending first coordinate to fix it.");
										LOG.warn(msg.toString());
										
										interiorPoints.add(x);
										interiorPoints.add(y);
										interiorPoints.add(z);
										++nrOfPoints;
									}	
									
									if (nrOfPoints < 4) {
										// invalid ring...
										StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
												interiorLinearRing.getGMLClass(), 
												origGmlId));
										msg.append(": Interior ring contains less than 4 coordinates. Skipping invalid ring.");
										LOG.error(msg.toString());
										return;
									}
									
									pointList.add(interiorPoints);

									ringNo++;
									dbImporterManager.updateGeometryCounter(GMLClass.LINEARRING);

									// also remember the gml:id of interior rings in case it is
									// referenced by a <textureCoordinates> element
									if (importAppearance && interiorLinearRing.isSetId())
										dbImporterManager.propagateXlink(new DBXlinkLinearRing(
												interiorLinearRing.getId(),
												origGmlId,
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

						JGeometry geom = JGeometry.createLinearPolygon(pointArray, 3, dbSrid);
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
			if (orientableSurface.isSetOrientation() && orientableSurface.getOrientation().equals("-")) {
				reverse = !reverse;
				negativeOrientation = true;
			}

			if (orientableSurface.isSetBaseSurface()) {
				SurfaceProperty surfaceProperty = orientableSurface.getBaseSurface();
				String mapping = null;

				if (surfaceProperty.isSetSurface()) {
					AbstractSurface abstractSurface = surfaceProperty.getSurface();
					if (!abstractSurface.isSetId())
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
						if (polygon.isSetExterior()) {
							LinearRing exteriorRing = (LinearRing)polygon.getExterior().getRing();
							if (exteriorRing != null && !exteriorRing.isSetId())
								exteriorRing.setId(targetURI);
						}

						if (polygon.isSetInterior()) {
							for (AbstractRingProperty abstractRingProperty : polygon.getInterior()) {
								LinearRing interiorRing = (LinearRing)abstractRingProperty.getRing();

								if (!interiorRing.isSetId())
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

			if (importAppearance && texturedSurface.isSetAppearance()) {
				for (_AppearanceProperty appearanceProperty : texturedSurface.getAppearance()) {
					if (appearanceProperty.isSetAppearance()) {
						_Appearance appearance = appearanceProperty.getAppearance();

						// how to map texture coordinates to a composite surface of
						// arbitrary depth?
						if (appearance.getCityGMLClass() == CityGMLClass._SIMPLETEXTURE &&
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
							CoreModule core = (CoreModule)appearanceProperty.getCityGMLModule().getModuleDependencies().getModule(CityGMLModuleType.CORE);
							AppearanceModule app = (AppearanceModule)CityGMLModules.getModuleByTypeAndVersion(CityGMLModuleType.APPEARANCE, core.getModuleVersion());

							boolean success = materialModelImporter.insertXlink(href, surfaceGeometryId, cityObjectId, app);

							if (!success) {
								LOG.error("XLink reference '" + href + "' could not be resolved.");
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
			if (compositeSurface.isSetSurfaceMember()) {
				for (SurfaceProperty surfaceProperty : compositeSurface.getSurfaceMember()) {
					if (surfaceProperty.isSetSurface()) {
						AbstractSurface abstractSurface = surfaceProperty.getSurface();

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
			if (surface.isSetPatches()) {
				SurfacePatchArrayProperty arrayProperty = surface.getPatches();
				if (arrayProperty.isSetSurfacePatch()) {
					for (AbstractSurfacePatch surfacePatch : arrayProperty.getSurfacePatch()) {

						if (surfacePatch.getGMLClass() == GMLClass.RECTANGLE) {
							Rectangle rectangle = (Rectangle)surfacePatch;
							if (rectangle.isSetExterior()) {
								LinearRing exteriorLinearRing = (LinearRing)rectangle.getExterior().getRing();
								if (exteriorLinearRing != null) 
									insert(exteriorLinearRing, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
							}
						}

						else if (surfacePatch.getGMLClass() == GMLClass.TRIANGLE) {
							Triangle triangle = (Triangle)surfacePatch;
							if (triangle.isSetExterior()) {
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
			if (triangulatedSurface.isSetTrianglePatches()) {
				TrianglePatchArrayProperty arrayProperty = triangulatedSurface.getTrianglePatches();
				if (arrayProperty.isSetTriangle()) {
					for (Triangle trianglePatch : arrayProperty.getTriangle()) {
						if (trianglePatch.isSetExterior()) {
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
			if (solid.isSetExterior()) {
				SurfaceProperty exteriorSurface = solid.getExterior();

				if (exteriorSurface.isSetSurface()) {
					AbstractSurface abstractSurface = exteriorSurface.getSurface();

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
			if (solid.isSetInterior()) {
				StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
						solid.getGMLClass(), 
						origGmlId));
				msg.append(": gml:interior is not supported.");
				
				LOG.error(msg.toString());
			}
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
			if (compositeSolid.isSetSolidMember()) {
				for (SolidProperty solidProperty : compositeSolid.getSolidMember()) {
					if (solidProperty.isSetSolid()) {
						surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
						insert(solidProperty.getSolid(), surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
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
			if (multiPolygon.isSetPolygonMember()) {
				for (PolygonProperty polygonProperty : multiPolygon.getPolygonMember()) {
					if (polygonProperty.isSetPolygon())
						insert(polygonProperty.getPolygon(), surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
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
			if (multiSurface.isSetSurfaceMember()) {
				for (SurfaceProperty surfaceProperty : multiSurface.getSurfaceMember()) {
					if (surfaceProperty.isSetSurface()) {
						AbstractSurface abstractSurface = surfaceProperty.getSurface();

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
			if (multiSurface.isSetSurfaceMembers()) {
				SurfaceArrayProperty surfaceArrayProperty = multiSurface.getSurfaceMembers();

				if (surfaceArrayProperty.isSetSurface()) {
					for (AbstractSurface abstractSurface : surfaceArrayProperty.getSurface()) {

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
			if (multiSolid.isSetSolidMember()) {
				for (SolidProperty solidProperty : multiSolid.getSolidMember()) {
					if (solidProperty.isSetSolid()) {
						surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
						insert(solidProperty.getSolid(), surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
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
			if (multiSolid.isSetSolidMembers()) {
				SolidArrayProperty solidArrayProperty = multiSolid.getSolidMembers();

				if (solidArrayProperty.isSetSolid()) {
					for (AbstractSolid abstractSolid : solidArrayProperty.getSolid()) {
						surfaceGeometryId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);
						insert(abstractSolid, surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
					}
				}
			}
		}

		// GeometricComplex
		else if (surfaceGeometryType == GMLClass.GEOMETRICCOMPLEX) {
			GeometricComplex geometricComplex = (GeometricComplex)surfaceGeometry;

			if (geometricComplex.isSetElement()) {
				for (GeometricPrimitiveProperty geometricPrimitiveProperty : geometricComplex.getElement()) {
					if (geometricPrimitiveProperty.isSetGeometricPrimitive())
						insert(geometricPrimitiveProperty.getGeometricPrimitive(), surfaceGeometryId, parentId, rootId, reverse, cityObjectId);
					else {
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
	public void close() throws SQLException {
		psParentElem.close();
		psMemberElem.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.SURFACE_GEOMETRY;
	}

}
