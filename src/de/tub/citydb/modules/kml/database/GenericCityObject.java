/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
package de.tub.citydb.modules.kml.database;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;

import net.opengis.kml._2.AltitudeModeEnumType;
import net.opengis.kml._2.BoundaryType;
import net.opengis.kml._2.LineStringType;
import net.opengis.kml._2.LinearRingType;
import net.opengis.kml._2.LinkType;
import net.opengis.kml._2.LocationType;
import net.opengis.kml._2.ModelType;
import net.opengis.kml._2.MultiGeometryType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PolygonType;

import org.citygml.textureAtlas.image.ImageReader;
import org.citygml.textureAtlas.model.TextureImage;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.appearance.X3DMaterial;

import com.sun.j3d.utils.geometry.GeometryInfo;

import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.kmlExporter.Balloon;
import de.tub.citydb.config.project.kmlExporter.BalloonContentMode;
import de.tub.citydb.config.project.kmlExporter.ColladaOptions;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.config.project.kmlExporter.PointAndCurve;
import de.tub.citydb.database.adapter.AbstractDatabaseAdapter;
import de.tub.citydb.database.adapter.BlobExportAdapter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.common.event.GeometryCounterEvent;
import de.tub.citydb.util.Util;

public class GenericCityObject extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "Generic";
	public static final String POINT = "Point";
	public static final String CURVE = "Curve";

	private long sgRootId;
	private Matrix transformation;
	private boolean isPointOrCurve;
	private boolean isPoint;
	
	private double refPointX;
	private double refPointY;
	private double refPointZ;

	public GenericCityObject(Connection connection,
			KmlExporterManager kmlExporterManager,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			AbstractDatabaseAdapter databaseAdapter,
			BlobExportAdapter textureExportAdapter,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandlerImpl balloonTemplateHandler,
			EventDispatcher eventDispatcher,
			Config config) {

		super(connection,
				kmlExporterManager,
				kmlFactory,
				databaseAdapter,
				textureExportAdapter,
				elevationServiceHandler,
				balloonTemplateHandler,
				eventDispatcher,
				config);
	}

	protected List<DisplayForm> getDisplayForms() {
		return config.getProject().getKmlExporter().getGenericCityObjectDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return config.getProject().getKmlExporter().getGenericCityObjectColladaOptions();
	}

	public Balloon getBalloonSettings() {
		if (isPointOrCurve) {
			if (isPoint)
				return config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getPointBalloon();
			else
				return config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getCurveBalloon();
		}
		// default
		return config.getProject().getKmlExporter().getGenericCityObject3DBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	protected String getHighlightingQuery() {
		if (transformation == null)
			return Queries.getGenericCityObjectHighlightingQuery(currentLod, false);
		return Queries.getGenericCityObjectHighlightingQuery(currentLod, true);
		
	}

	public void read(KmlSplittingResult work) {
		PreparedStatement psQuery = null;
		ResultSet rs = null;

		try {
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 0: lodToExportFrom;

			while (currentLod >= minLod) {
				if(!work.getDisplayForm().isAchievableFromLoD(currentLod)) break;

				try {
					psQuery = connection.prepareStatement(Queries.getGenericCityObjectBasisData(currentLod));

					for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
						psQuery.setLong(i, work.getId());
					}

					rs = psQuery.executeQuery();
					if (rs.isBeforeFirst()) {
						rs.next();
						if (rs.getLong(4)!= 0 || rs.getLong(1)!= 0)
							break; // result set not empty
					}

					try { rs.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
					try { psQuery.close(); /* release cursor on DB */ } catch (SQLException sqle) {}

					// check for point or curve
					psQuery = connection.prepareStatement(Queries.getGenericCityObjectPointAndCurveQuery(currentLod),
 							  							  ResultSet.TYPE_SCROLL_INSENSITIVE,
 							  							  ResultSet.CONCUR_READ_ONLY);
					for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
						psQuery.setLong(i, work.getId());
					}
					rs = psQuery.executeQuery();
					if (rs.isBeforeFirst()) {
						isPointOrCurve = true;
						break; // result set not empty
					}
					
					try { rs.close(); // release cursor on DB
					} catch (SQLException sqle) {}
					rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
					try { psQuery.close(); // release cursor on DB
					} catch (SQLException sqle) {}

				}
				catch (Exception e2) {
					try { if (rs != null) rs.close(); } catch (SQLException sqle) {}
					rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
					try { if (psQuery != null) psQuery.close(); } catch (SQLException sqle) {}
				}

				currentLod--;
			}

			if ((rs == null) || // result empty
					((!isPointOrCurve) && !work.getDisplayForm().isAchievableFromLoD(currentLod))) { // give up	
					String fromMessage = " from LoD" + lodToExportFrom;
					if (lodToExportFrom == 5) {
						if (work.getDisplayForm().getForm() == DisplayForm.COLLADA)
							fromMessage = ". LoD1 or higher required";
						else
							fromMessage = " from any LoD";
					}
					Logger.getInstance().info("Could not display object " + work.getGmlId() 
							+ " as " + work.getDisplayForm().getName() + fromMessage + ".");
			}
			else { // result not empty
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, 1, this));

				if (isPointOrCurve) { // point or curve geometry

					kmlExporterManager.print(createPlacemarksForPointOrCurve(rs, work),
							 				 work,
							 				 getBalloonSettings().isBalloonContentInSeparateFile());
				}
				else {					
					// decide whether explicit or implicit geometry
					sgRootId = rs.getLong(4);
					if (sgRootId == 0) {
						sgRootId = rs.getLong(1);
						if (sgRootId != 0) {
							double[] ordinatesArray = geometryConverterAdapter.getPoint(rs.getObject(2)).getCoordinates(0);
							refPointX = ordinatesArray[0];
							refPointY = ordinatesArray[1];
							refPointZ = ordinatesArray[2];

							String transformationString = rs.getString(3);
							if (transformationString != null) {
								List<Double> m = Util.string2double(transformationString, "\\s+");
								if (m != null && m.size() >= 16) {
									transformation = new Matrix(4, 4);
									transformation.setMatrix(m.subList(0, 16));
								}
							}
						}
					}

					try { rs.close(); // release cursor on DB
					} catch (SQLException sqle) {}
					rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
					try { psQuery.close(); // release cursor on DB
					} catch (SQLException sqle) {}

					Boolean isImplcitGeometry = true;
					if (transformation == null) { // no implicit geometry
						isImplcitGeometry = false;
					}
					
					psQuery = connection.prepareStatement(Queries.getGenericCityObjectGeometryContents(work.getDisplayForm(), databaseAdapter.getSQLAdapter(), isImplcitGeometry),
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					psQuery.setLong(1, sgRootId);
					rs = psQuery.executeQuery();

					// get the proper displayForm (for highlighting)
					int indexOfDf = getDisplayForms().indexOf(work.getDisplayForm());
					if (indexOfDf != -1) {
						work.setDisplayForm(getDisplayForms().get(indexOfDf));
					}

					switch (work.getDisplayForm().getForm()) {
					case DisplayForm.FOOTPRINT:
						kmlExporterManager.print(createPlacemarksForFootprint(rs, work),
								work,
								getBalloonSettings().isBalloonContentInSeparateFile());
						break;
					case DisplayForm.EXTRUDED:

						PreparedStatement psQuery2 = connection.prepareStatement(Queries.GET_EXTRUDED_HEIGHT(databaseAdapter.getDatabaseType()));
						for (int i = 1; i <= psQuery2.getParameterMetaData().getParameterCount(); i++) {
							psQuery2.setLong(i, work.getId());
						}
						ResultSet rs2 = psQuery2.executeQuery();
						rs2.next();
						double measuredHeight = rs2.getDouble("envelope_measured_height");
						try { rs2.close(); // release cursor on DB
						} catch (SQLException e) {}
						try { psQuery2.close(); // release cursor on DB
						} catch (SQLException e) {}

						kmlExporterManager.print(createPlacemarksForExtruded(rs, work, measuredHeight, false),
								work,
								getBalloonSettings().isBalloonContentInSeparateFile());
						break;
					case DisplayForm.GEOMETRY:
						setGmlId(work.getGmlId());
						setId(work.getId());
						if (config.getProject().getKmlExporter().getFilter().isSetComplexFilter()) { // region
							if (work.getDisplayForm().isHighlightingEnabled()) {
								kmlExporterManager.print(createPlacemarksForHighlighting(work),
										work,
										getBalloonSettings().isBalloonContentInSeparateFile());
							}
							kmlExporterManager.print(createPlacemarksForGeometry(rs, work),
									work,
									getBalloonSettings().isBalloonContentInSeparateFile());
						}
						else { // reverse order for single objects
							kmlExporterManager.print(createPlacemarksForGeometry(rs, work),
									work,
									getBalloonSettings().isBalloonContentInSeparateFile());
							//						kmlExporterManager.print(createPlacemarkForEachSurfaceGeometry(rs, work.getGmlId(), false));
							if (work.getDisplayForm().isHighlightingEnabled()) {
								//							kmlExporterManager.print(createPlacemarkForEachHighlingtingGeometry(work),
								//							 						 work,
								//							 						 getBalloonSetings().isBalloonContentInSeparateFile());
								kmlExporterManager.print(createPlacemarksForHighlighting(work),
										work,
										getBalloonSettings().isBalloonContentInSeparateFile());
							}
						}
						break;
					case DisplayForm.COLLADA:
						setGmlId(work.getGmlId()); // must be set before fillGenericObjectForCollada
						setId(work.getId());	   // due to implicit geometries randomized with gmlId.hashCode()
						fillGenericObjectForCollada(rs);

						if (getGeometryAmount() > GEOMETRY_AMOUNT_WARNING) {
							Logger.getInstance().info("Object " + work.getGmlId() + " has more than " + GEOMETRY_AMOUNT_WARNING + " geometries. This may take a while to process...");
						}

						List<Point3d> anchorCandidates = setOrigins(); // setOrigins() called mainly for the side-effect
						double zOffset = getZOffsetFromConfigOrDB(work.getId());
						if (zOffset == Double.MAX_VALUE) {
							if (transformation != null) {
								anchorCandidates.clear();
								anchorCandidates.add(new Point3d(0,0,0)); // will be turned into refPointX,Y,Z by convertToWGS84
							}
							zOffset = getZOffsetFromGEService(work.getId(), anchorCandidates);
						}
						setZOffset(zOffset);

						ColladaOptions colladaOptions = getColladaOptions();
						setIgnoreSurfaceOrientation(colladaOptions.isIgnoreSurfaceOrientation());
						try {
							if (work.getDisplayForm().isHighlightingEnabled()) {
								//							kmlExporterManager.print(createPlacemarkForEachHighlingtingGeometry(work),
								//													 work,
								//													 getBalloonSetings().isBalloonContentInSeparateFile());
								kmlExporterManager.print(createPlacemarksForHighlighting(work),
										work,
										getBalloonSettings().isBalloonContentInSeparateFile());
							}
						}
						catch (Exception ioe) {
							ioe.printStackTrace();
						}

						break;
					}
				}				
			}
		}
		catch (SQLException sqlEx) {
			Logger.getInstance().error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
			return;
		}
		catch (JAXBException jaxbEx) {
			return;
		}
		finally {
			if (rs != null)
				try { rs.close(); } catch (SQLException e) {}
			if (psQuery != null)
				try { psQuery.close(); } catch (SQLException e) {}
		}
	}

	protected GeometryObject applyTransformationMatrix(GeometryObject geomObj) throws SQLException {
		if (transformation != null) {
			for (int i = 0; i < geomObj.getNumElements(); i++) {
				double[] originalCoords = geomObj.getCoordinates(i);
				for (int j = 0; j < originalCoords.length; j += 3) {
					double[] vals = new double[]{originalCoords[j], originalCoords[j+1], originalCoords[j+2], 1};
					Matrix v = new Matrix(vals, 4);

					v = transformation.times(v);
					originalCoords[j] = v.get(0, 0) + refPointX;
					originalCoords[j+1] = v.get(1, 0) + refPointY;
					originalCoords[j+2] = v.get(2, 0) + refPointZ;
				}
			}
		}
		return geomObj;
	}

	protected GeometryObject convertToWGS84(GeometryObject geomObj) throws SQLException {
		return super.convertToWGS84(applyTransformationMatrix(geomObj));
	}

	public PlacemarkType createPlacemarkForColladaModel() throws SQLException {

		if (transformation == null) { // no implicit geometry
			// undo trick for very close coordinates
			double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {getOriginX()/100, getOriginY()/100, getOriginZ()/100});
			setLocationX(reducePrecisionForXorY(originInWGS84[0]));
			setLocationY(reducePrecisionForXorY(originInWGS84[1]));
			setLocationZ(reducePrecisionForZ(originInWGS84[2]));
			return super.createPlacemarkForColladaModel();
		}

		double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {0, 0, 0}); // will be turned into refPointX,Y,Z by convertToWGS84
		setLocationX(reducePrecisionForXorY(originInWGS84[0]));
		setLocationY(reducePrecisionForXorY(originInWGS84[1]));
		setLocationZ(reducePrecisionForZ(originInWGS84[2]));

		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(getGmlId());
		placemark.setId(DisplayForm.COLLADA_PLACEMARK_ID + placemark.getName());

		DisplayForm colladaDisplayForm = null;
		for (DisplayForm displayForm: getDisplayForms()) {
			if (displayForm.getForm() == DisplayForm.COLLADA) {
				colladaDisplayForm = displayForm;
				break;
			}
		}

		if (getBalloonSettings().isIncludeDescription() 
				&& !colladaDisplayForm.isHighlightingEnabled()) { // avoid double description

			ColladaOptions colladaOptions = getColladaOptions();
			if (!colladaOptions.isGroupObjects() || colladaOptions.getGroupSize() == 1) {
				addBalloonContents(placemark, getId());
			}
		}

		ModelType model = kmlFactory.createModelType();
		LocationType location = kmlFactory.createLocationType();

		switch (config.getProject().getKmlExporter().getAltitudeMode()) {
		case ABSOLUTE:
			model.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
			break;
		case RELATIVE:
			model.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
			break;
		}

		location.setLatitude(getLocationY());
		location.setLongitude(getLocationX());
		location.setAltitude(getLocationZ() + reducePrecisionForZ(getZOffset()));
		model.setLocation(location);

		// no heading value to correct

		LinkType link = kmlFactory.createLinkType();

		if (config.getProject().getKmlExporter().isOneFilePerObject() &&
				!config.getProject().getKmlExporter().isExportAsKmz() &&
				config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getActive().booleanValue())
		{
			link.setHref(getGmlId() + ".dae");
		}
		else {
			// File.separator would be wrong here, it MUST be "/"
			link.setHref(getGmlId() + "/" + getGmlId() + ".dae");
		}
		model.setLink(link);

		placemark.setAbstractGeometryGroup(kmlFactory.createModel(model));
		return placemark;
	}

	protected List<Point3d> setOrigins() {
		List<Point3d> coords = new ArrayList<Point3d>();
		
		if (transformation != null) { 
			// for implicit geometries, bugfix for the previous version (V1.6)
			// the local coordinates of the Origin Point must be converted from the local
			// Cartesian coordinate system to the world Coordinate reference System
			double[] originalCoords = new double[]{0, 0, 0, 1};
			Matrix v = new Matrix(originalCoords, 4);
			v = transformation.times(v);
			setOriginX ((v.get(0, 0) + refPointX)*100);
			setOriginY ((v.get(1, 0) + refPointY)*100);
			setOriginZ ((v.get(2, 0) + refPointZ)*100);
			// dummy
			Point3d point3d = new Point3d(getOriginX(), getOriginY(), getOriginZ());
			coords.add(point3d);			
		}
		else {
			coords = super.setOrigins();
		}

		return coords;
	}

	protected void fillGenericObjectForCollada(ResultSet rs) throws SQLException {

		if (transformation == null) { // no implicit geometry
			super.fillGenericObjectForCollada(rs);
			return;
		}

		String selectedTheme = config.getProject().getKmlExporter().getAppearanceTheme();
		int texImageCounter = 0;

		while (rs.next()) {
			long surfaceRootId = rs.getLong(1);
			for (String colladaQuery: Queries.COLLADA_IMPLICIT_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID) { // parent surfaces come first
				PreparedStatement psQuery = null;
				ResultSet rs2 = null;
				try {
					psQuery = connection.prepareStatement(colladaQuery);
					psQuery.setLong(1, surfaceRootId);
					//					psQuery.setString(2, selectedTheme);
					rs2 = psQuery.executeQuery();

					while (rs2.next()) {
						String theme = rs2.getString("theme");

						Object buildingGeometryObj = rs2.getObject(1); 
						// surfaceId is the key to all Hashmaps in building
						// for implicit geometries it must be randomized with
						// gmlId.hashCode() in order to properly group objects
						// otherwise surfaces with the same id would be overwritten
						long surfaceId = rs2.getLong("id") + getGmlId().hashCode();
						long textureImageId = rs2.getLong("tex_image_id");
						long parentId = rs2.getLong("parent_id");
						long rootId = rs2.getLong("root_id");

						if (buildingGeometryObj == null) { // root or parent
							if (selectedTheme.equalsIgnoreCase(theme)) {
								X3DMaterial x3dMaterial = new X3DMaterial();
								fillX3dMaterialValues(x3dMaterial, rs2);
								// x3dMaterial will only added if not all x3dMaterial members are null
								addX3dMaterial(surfaceId, x3dMaterial);
							}
							else if (theme == null) { // no theme for this parent surface
								if (getX3dMaterial(parentId) != null) { // material for parent's parent known
									addX3dMaterial(surfaceId, getX3dMaterial(parentId));
								}
							}
							continue;
						}

						// from hier on it is a surfaceMember
						eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

						String texImageUri = null;
						StringTokenizer texCoordsTokenized = null;

						if (selectedTheme.equals(KmlExporter.THEME_NONE))
							addX3dMaterial(surfaceId, defaultX3dMaterial);
						else {
							if (!selectedTheme.equalsIgnoreCase(theme)) { // no surface data for this surface and theme
								if (getX3dMaterial(parentId) != null) // material for parent surface known
									addX3dMaterial(surfaceId, getX3dMaterial(parentId));
								else if (getX3dMaterial(rootId) != null) // material for root surface known
									addX3dMaterial(surfaceId, getX3dMaterial(rootId));
								else
									addX3dMaterial(surfaceId, defaultX3dMaterial);
							}
							else {
								texImageUri = rs2.getString("tex_image_uri");
								
								StringBuffer sb =  new StringBuffer();
								Object texCoordsObject = rs2.getObject("texture_coordinates"); 
								if (texCoordsObject != null){
									GeometryObject texCoordsGeometryObject = geometryConverterAdapter.getGeometry(texCoordsObject);
									for (int i = 0; i < texCoordsGeometryObject.getNumElements(); i++) {
										double[] coordinates = texCoordsGeometryObject.getCoordinates(i);
										for (double coordinate : coordinates){
											sb.append(String.valueOf(coordinate));
											sb.append(" ");
										}									
									}									
								}
								String texCoords = sb.toString();
	
								if (texImageUri != null && texImageUri.trim().length() != 0
										&&  texCoords != null && texCoords.trim().length() != 0) {
	
									int fileSeparatorIndex = Math.max(texImageUri.lastIndexOf("\\"), texImageUri.lastIndexOf("/")); 
									texImageUri = ".." + File.separator + "_" + texImageUri.substring(fileSeparatorIndex + 1);
	
									addTexImageUri(surfaceId, texImageUri);
									if ((getUnsupportedTexImageId(texImageUri) == -1) && (getTexImage(texImageUri) == null)) { 
										// not already marked as wrapping texture && not already read in
										TextureImage texImage = null;
										try {
											byte[] imageBytes = textureExportAdapter.getInByteArray(textureImageId, "tex_image", texImageUri);
											if (imageBytes != null) {
												texImage = ImageReader.read(new ByteArrayInputStream(imageBytes));
											}																																
										} catch (IOException ioe) {}
										
										if (texImage != null) { // image in JPEG, PNG or another usual format
											addTexImage(texImageUri, texImage);
										}
										else {
											addUnsupportedTexImageId(texImageUri, textureImageId);
										}
	
										texImageCounter++;
										if (texImageCounter > 20) {
											eventDispatcher.triggerEvent(new CounterEvent(CounterType.TEXTURE_IMAGE, texImageCounter, this));
											texImageCounter = 0;
										}
									}
	
									texCoords = texCoords.replaceAll(";", " "); // substitute of ; for internal ring
									texCoordsTokenized = new StringTokenizer(texCoords, " ");
								}
								else {
									X3DMaterial x3dMaterial = new X3DMaterial();
									fillX3dMaterialValues(x3dMaterial, rs2);
									// x3dMaterial will only added if not all x3dMaterial members are null
									addX3dMaterial(surfaceId, x3dMaterial);
									if (getX3dMaterial(surfaceId) == null) {
										// untextured surface and no x3dMaterial -> default x3dMaterial (gray)
										addX3dMaterial(surfaceId, defaultX3dMaterial);
									}
								}
							}
	
							GeometryObject surface = geometryConverterAdapter.getPolygon(buildingGeometryObj);
							surface = applyTransformationMatrix(surface);
							GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
	
							int contourCount = surface.getNumElements();
							int[] stripCountArray = new int[contourCount];
							int[] countourCountArray = {contourCount};
	
							// last point of polygons in gml is identical to first and useless for GeometryInfo
							double[] giOrdinatesArray = new double[surface.getNumCoordinates() - (contourCount * 3)];
							int i = 0;
	
							for (int currentContour = 0; currentContour < surface.getNumElements(); currentContour++) {
								double[] ordinatesArray = surface.getCoordinates(currentContour);
								for (int j = 0; j < ordinatesArray.length - 3; j = j+3, i = i+3) {
	
									giOrdinatesArray[i] = ordinatesArray[j] * 100; // trick for very close coordinates
									giOrdinatesArray[i+1] = ordinatesArray[j+1] * 100;
									giOrdinatesArray[i+2] = ordinatesArray[j+2] * 100;
	
									TexCoords texCoordsForThisSurface = null;
									if (texCoordsTokenized != null && texCoordsTokenized.hasMoreTokens()) {
										double s = Double.parseDouble(texCoordsTokenized.nextToken());
										double t = Double.parseDouble(texCoordsTokenized.nextToken());
										texCoordsForThisSurface = new TexCoords(s, t);
									}
									setVertexInfoForXYZ(surfaceId,
											giOrdinatesArray[i],
											giOrdinatesArray[i+1],
											giOrdinatesArray[i+2],
											texCoordsForThisSurface);
								}
								stripCountArray[currentContour] = (ordinatesArray.length - 3) / 3;
								if (texCoordsTokenized != null && texCoordsTokenized.hasMoreTokens()) {
									texCoordsTokenized.nextToken(); // geometryInfo ignores last point in a polygon
									texCoordsTokenized.nextToken(); // keep texture coordinates in sync
								}
							}
							gi.setCoordinates(giOrdinatesArray);
							gi.setContourCounts(countourCountArray);
							gi.setStripCounts(stripCountArray);
							addGeometryInfo(surfaceId, gi);
						}
					}
				}
				catch (SQLException sqlEx) {
					Logger.getInstance().error("SQL error while querying city object: " + sqlEx.getMessage());
				}
				finally {
					if (rs2 != null)
						try { rs2.close(); } catch (SQLException e) {}
					if (psQuery != null)
						try { psQuery.close(); } catch (SQLException e) {}
				}
			}
		}

		// count rest images
		eventDispatcher.triggerEvent(new CounterEvent(CounterType.TEXTURE_IMAGE, texImageCounter, this));
	}

	protected List<PlacemarkType> createPlacemarksForHighlighting(KmlSplittingResult work) throws SQLException {
		if (transformation == null) { // no implicit geometry
			return super.createPlacemarksForHighlighting(work);
		}

		List<PlacemarkType> placemarkList= new ArrayList<PlacemarkType>();

		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setStyleUrl("#" + getStyleBasisName() + work.getDisplayForm().getName() + "Style");
		placemark.setName(work.getGmlId());
		placemark.setId(DisplayForm.GEOMETRY_HIGHLIGHTED_PLACEMARK_ID + placemark.getName());
		placemarkList.add(placemark);

		if (getBalloonSettings().isIncludeDescription()) {
			addBalloonContents(placemark, work.getId());
		}

		MultiGeometryType multiGeometry =  kmlFactory.createMultiGeometryType();
		placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));

		PreparedStatement getGeometriesStmt = null;
		ResultSet rs = null;

		double hlDistance = work.getDisplayForm().getHighlightingDistance();

		try {
			getGeometriesStmt = connection.prepareStatement(getHighlightingQuery(),
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			for (int i = 1; i <= getGeometriesStmt.getParameterMetaData().getParameterCount(); i++) {
				getGeometriesStmt.setLong(i, work.getId());
			}
			rs = getGeometriesStmt.executeQuery();

			double zOffset = getZOffsetFromConfigOrDB(work.getId());
			if (zOffset == Double.MAX_VALUE) {
				List<Point3d> anchorCandidates = new ArrayList<Point3d>();
				anchorCandidates.clear();
				anchorCandidates.add(new Point3d(0,0,0)); // will be turned into refPointX,Y,Z by convertToWGS84
				zOffset = getZOffsetFromGEService(work.getId(), anchorCandidates);
			}

			while (rs.next()) {
				Object unconvertedObj = rs.getObject(1);
				GeometryObject unconvertedSurface = geometryConverterAdapter.getPolygon(unconvertedObj);
				unconvertedSurface = applyTransformationMatrix(unconvertedSurface);
				if (unconvertedSurface == null || unconvertedSurface.getNumElements() == 0)
					return null;

				double[] ordinatesArray = unconvertedSurface.getCoordinates(0);
				double nx = 0;
				double ny = 0;
				double nz = 0;

				for (int current = 0; current < ordinatesArray.length - 3; current = current+3) {
					int next = current+3;
					if (next >= ordinatesArray.length - 3) next = 0;
					nx = nx + ((ordinatesArray[current+1] - ordinatesArray[next+1]) * (ordinatesArray[current+2] + ordinatesArray[next+2])); 
					ny = ny + ((ordinatesArray[current+2] - ordinatesArray[next+2]) * (ordinatesArray[current] + ordinatesArray[next])); 
					nz = nz + ((ordinatesArray[current] - ordinatesArray[next]) * (ordinatesArray[current+1] + ordinatesArray[next+1])); 
				}

				double value = Math.sqrt(nx * nx + ny * ny + nz * nz);
				if (value == 0) { // not a surface, but a line
					continue;
				}
				nx = nx / value;
				ny = ny / value;
				nz = nz / value;

				for (int i = 0; i < unconvertedSurface.getNumElements(); i++) {
					ordinatesArray = unconvertedSurface.getCoordinates(i);
					for (int j = 0; j < ordinatesArray.length; j = j + 3) {
						// coordinates = coordinates + hlDistance * (dot product of normal vector and unity vector)
						ordinatesArray[j] = ordinatesArray[j] + hlDistance * nx;
						ordinatesArray[j+1] = ordinatesArray[j+1] + hlDistance * ny;
						ordinatesArray[j+2] = ordinatesArray[j+2] + zOffset + hlDistance * nz;
					}
				}

				// now convert to WGS84 without applying transformation matrix (already done)
				GeometryObject surface = super.convertToWGS84(unconvertedSurface);
				unconvertedSurface = null;

				PolygonType polygon = kmlFactory.createPolygonType();
				switch (config.getProject().getKmlExporter().getAltitudeMode()) {
				case ABSOLUTE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
				}
				multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

				for (int i = 0; i < surface.getNumElements(); i++) {
					LinearRingType linearRing = kmlFactory.createLinearRingType();
					BoundaryType boundary = kmlFactory.createBoundaryType();
					boundary.setLinearRing(linearRing);

					if (i == 0)
						polygon.setOuterBoundaryIs(boundary);
					else
						polygon.getInnerBoundaryIs().add(boundary);

					// order points clockwise
					ordinatesArray = surface.getCoordinates(i);
					for (int j = 0; j < ordinatesArray.length; j = j+3)
						linearRing.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[j]) + "," 
								+ reducePrecisionForXorY(ordinatesArray[j+1]) + ","
								+ reducePrecisionForZ(ordinatesArray[j+2])));
				}
			}
		}
		catch (Exception e) {
			Logger.getInstance().warn("Exception when generating highlighting geometry of GenericCityObject " + work.getGmlId());
			e.printStackTrace();
		}
		finally {
			if (rs != null) rs.close();
			if (getGeometriesStmt != null) getGeometriesStmt.close();
		}

		return placemarkList;
	}
	
	protected List<PlacemarkType> createPlacemarksForPointOrCurve(ResultSet rs,
			  KmlSplittingResult work) throws SQLException {
		PointAndCurve pacSettings = config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve();
		List<PlacemarkType> placemarkList= new ArrayList<PlacemarkType>();

		double zOffset = getZOffsetFromConfigOrDB(work.getId());
		List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(rs, (zOffset == Double.MAX_VALUE));
		rs.beforeFirst(); // return cursor to beginning
		if (zOffset == Double.MAX_VALUE) {
			zOffset = getZOffsetFromGEService(work.getId(), lowestPointCandidates);
		}
		while (rs.next()) {

			PlacemarkType placemark = kmlFactory.createPlacemarkType();
			LineStringType lineString = kmlFactory.createLineStringType();

			Object buildingGeometryObj = rs.getObject(1); 
						
			GeometryObject pointOrCurveGeometry = geometryConverterAdapter.getGeometry(buildingGeometryObj);			
			
			eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

			if (pointOrCurveGeometry.getGeometryType() == GeometryObject.GeometryType.POINT) { // point
				isPoint = true; // dirty hack, don't try this at home
				double[] ordinatesArray = convertToWGS84(pointOrCurveGeometry).getCoordinates(0);

				double[] ordinatesArrayTopLeft = pointOrCurveGeometry.getCoordinates(0);
				ordinatesArrayTopLeft[0] = ordinatesArrayTopLeft[0] - 1; 
				ordinatesArrayTopLeft[1] = ordinatesArrayTopLeft[1] + 1; 
				ordinatesArrayTopLeft = super.convertPointCoordinatesToWGS84(ordinatesArrayTopLeft);

				double[] ordinatesArrayBottomRight = pointOrCurveGeometry.getCoordinates(0);
				ordinatesArrayBottomRight[0] = ordinatesArrayBottomRight[0] + 1; 
				ordinatesArrayBottomRight[1] = ordinatesArrayBottomRight[1] - 1; 
				ordinatesArrayBottomRight = super.convertPointCoordinatesToWGS84(ordinatesArrayBottomRight);

				// draw an X
				lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + "," 
						 									 + reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
						 									 + reducePrecisionForZ(ordinatesArray[2] + zOffset)));

				lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + "," 
						 									 + reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
						 									 + reducePrecisionForZ(ordinatesArray[2] + zOffset)));

				lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[0]) + "," 
						 									 + reducePrecisionForXorY(ordinatesArray[1]) + ","
						 									 + reducePrecisionForZ(ordinatesArray[2] + zOffset)));

				lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + "," 
						 									 + reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
						 									 + reducePrecisionForZ(ordinatesArray[2] + zOffset)));

				lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + "," 
						 									 + reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
						 									 + reducePrecisionForZ(ordinatesArray[2] + zOffset)));

				placemark.setName(work.getGmlId() + "_" + POINT);
				
				if (pacSettings.isPointHighlightingEnabled())
					placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.POINT + "Style");
				else
					placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.POINT + "Normal");
	
				// replace default BalloonTemplateHandler with a brand new one, this costs resources!
				if (pacSettings.getPointBalloon() != null && pacSettings.getPointBalloon().isIncludeDescription() &&
						pacSettings.getPointBalloon().getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
					String balloonTemplateFilename = pacSettings.getPointBalloon().getBalloonContentTemplateFile();
					if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
						setBalloonTemplateHandler(new BalloonTemplateHandlerImpl(new File(balloonTemplateFilename), connection));
					}
					addBalloonContents(placemark, work.getId());
				}
			}
			else { // curve
				pointOrCurveGeometry = convertToWGS84(pointOrCurveGeometry);
				double[] ordinatesArray = pointOrCurveGeometry.getCoordinates(0);
				for (int j = 0; j < ordinatesArray.length; j = j+3){
					lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[j]) + "," 
							 + reducePrecisionForXorY(ordinatesArray[j+1]) + ","
							 + reducePrecisionForZ(ordinatesArray[j+2] + zOffset)));
				}
				
				placemark.setName(work.getGmlId() + "_" + CURVE);
	
				// replace default BalloonTemplateHandler with a brand new one, this costs resources!
				if (pacSettings.isCurveHighlightingEnabled())
					placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.CURVE + "Style");
				else
					placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.CURVE + "Normal");
	
				if (pacSettings.getCurveBalloon() != null && pacSettings.getCurveBalloon().isIncludeDescription() &&
						pacSettings.getCurveBalloon().getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
					String balloonTemplateFilename = pacSettings.getCurveBalloon().getBalloonContentTemplateFile();
					if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
						setBalloonTemplateHandler(new BalloonTemplateHandlerImpl(new File(balloonTemplateFilename), connection));
					}
					// this is the reason for the isPoint dirty hack
					addBalloonContents(placemark, work.getId());
				}
			}

			switch (pacSettings.getCurveAltitudeMode()) {
			case ABSOLUTE:
				lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
				break;
			case RELATIVE:
				lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
				break;
			case CLAMP_TO_GROUND:
				lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));
				break;
			}

			placemark.setAbstractGeometryGroup(kmlFactory.createLineString(lineString));
			placemark.setId(/* DisplayForm.GEOMETRY_PLACEMARK_ID + */ placemark.getName());
			placemarkList.add(placemark);
		}

		return placemarkList;		
	}	

}
