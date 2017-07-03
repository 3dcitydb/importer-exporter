/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.kml.database;

import java.io.ByteArrayInputStream;
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

import org.citydb.api.database.BalloonTemplateHandler;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.KmlExporter;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.log.Logger;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;
import org.citydb.modules.common.event.GeometryCounterEvent;
import org.citydb.textureAtlas.model.TextureImage;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.appearance.X3DMaterial;

import net.opengis.kml._2.AltitudeModeEnumType;
import net.opengis.kml._2.BoundaryType;
import net.opengis.kml._2.LinearRingType;
import net.opengis.kml._2.LinkType;
import net.opengis.kml._2.LocationType;
import net.opengis.kml._2.ModelType;
import net.opengis.kml._2.MultiGeometryType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PolygonType;

public class SolitaryVegetationObject extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "Vegetation";

	private long sgRootId;
	private Matrix transformation;

	private double refPointX;
	private double refPointY;
	private double refPointZ;

	public SolitaryVegetationObject(Connection connection,
			KmlExporterManager kmlExporterManager,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			AbstractDatabaseAdapter databaseAdapter,
			BlobExportAdapter textureExportAdapter,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandler balloonTemplateHandler,
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
		return config.getProject().getKmlExporter().getVegetationDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return config.getProject().getKmlExporter().getVegetationColladaOptions();
	}

	public Balloon getBalloonSettings() {
		return config.getProject().getKmlExporter().getVegetationBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	protected String getHighlightingQuery() {
		if (transformation == null)
			return Queries.getSolitaryVegetationObjectHighlightingQuery(currentLod,false);
		return Queries.getSolitaryVegetationObjectHighlightingQuery(currentLod,true);		
	}

	public void read(KmlSplittingResult work) {
		PreparedStatement psQuery = null;
		ResultSet rs = null;

		try {
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 1: lodToExportFrom;

			while (currentLod >= minLod) {
				if(!work.getDisplayForm().isAchievableFromLoD(currentLod)) break;

				try {
					psQuery = connection.prepareStatement(Queries.getSolitaryVegetationObjectBasisData(currentLod));

					for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
						psQuery.setLong(i, work.getId());
					}

					rs = psQuery.executeQuery();
					if (rs.isBeforeFirst()) {
						rs.next();
						if (rs.getLong(4)!= 0 || rs.getLong(1)!= 0)
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

			if (rs == null) { // result empty, give up
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

				psQuery = connection.prepareStatement(Queries.getSolitaryVegetationObjectGeometryContents(work.getDisplayForm(), databaseAdapter.getSQLAdapter(),isImplcitGeometry),
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				psQuery.setLong(1, sgRootId);
				rs = psQuery.executeQuery();
				
				kmlExporterManager.updateFeatureTracker(work);

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
						if (work.getDisplayForm().isHighlightingEnabled()) {
							kmlExporterManager.print(createPlacemarksForHighlighting(work),
									work,
									getBalloonSettings().isBalloonContentInSeparateFile());
						}
					}
					break;
				case DisplayForm.COLLADA:
					String currentgmlId = getGmlId();
					setGmlId(work.getGmlId()); // must be set before fillGenericObjectForCollada
					setId(work.getId());	   // due to implicit geometries randomized with gmlId.hashCode()
					fillGenericObjectForCollada(rs, config.getProject().getKmlExporter().getVegetationColladaOptions().isGenerateTextureAtlases());

					if (currentgmlId != work.getGmlId() && getGeometryAmount() > GEOMETRY_AMOUNT_WARNING) {
						Logger.getInstance().info("Object " + work.getGmlId() + " has more than " + GEOMETRY_AMOUNT_WARNING + " geometries. This may take a while to process...");
					}

					List<Point3d> anchorCandidates = getOrigins(); // setOrigins() called mainly for the side-effect
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
			
			// implicit geometries are not associated with a crs (srid = 0)
			// after transformation into world coordinates, we therefore have to assign the database crs
			geomObj.setSrid(databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid());
		}
		
		return geomObj;
	}

	// this function will be called prior
	protected GeometryObject convertToWGS84(GeometryObject geomObj) throws SQLException {
		return super.convertToWGS84(applyTransformationMatrix(geomObj));
	}

	
	protected double[] convertPointWorldCoordinatesToWGS84(double[] coords) throws SQLException {
		double[] pointCoords = null;
		GeometryObject convertedPointGeom = null;
		// this is a nasty hack for Oracle. In Oracle, transforming a single point to WGS84 does not change
		// its z-value, whereas transforming a series of vertices does affect their z-value
		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			convertedPointGeom = super.convertToWGS84(GeometryObject.createCurve(coords, coords.length, dbSrs.getSrid()));
			break;
		case POSTGIS:
			convertedPointGeom = super.convertToWGS84(GeometryObject.createPoint(coords, coords.length, dbSrs.getSrid()));
			break;
		}
		if (convertedPointGeom != null)
			pointCoords = convertedPointGeom.getCoordinates(0);
		return pointCoords;
	}
	
	public PlacemarkType createPlacemarkForColladaModel() throws SQLException {

		if (transformation == null) { // no implicit geometry
			// undo trick for very close coordinates
			double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {getOrigin().x,
					getOrigin().y,
					getOrigin().z});
			setLocation(reducePrecisionForXorY(originInWGS84[0]),
					reducePrecisionForXorY(originInWGS84[1]),
					reducePrecisionForZ(originInWGS84[2]));
			return super.createPlacemarkForColladaModel();
		}

	//	double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {0, 0, 0}); // will be turned into refPointX,Y,Z by convertToWGS84
		double[] originInWGS84 = convertPointWorldCoordinatesToWGS84(new double[] {getOrigin().x,
				getOrigin().y,
				getOrigin().z});
		setLocation(reducePrecisionForXorY(originInWGS84[0]),
				reducePrecisionForXorY(originInWGS84[1]),
				reducePrecisionForZ(originInWGS84[2]));

		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(getGmlId());
		placemark.setId(config.getProject().getKmlExporter().getIdPrefixes().getPlacemarkCollada() + placemark.getName());

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
		default:
			break;
		}

		location.setLatitude(getLocation().y);
		location.setLongitude(getLocation().x);
		location.setAltitude(getLocation().z + reducePrecisionForZ(getZOffset()));
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
			link.setHref(getId() + "/" + getGmlId() + ".dae");
		}
		model.setLink(link);

		placemark.setAbstractGeometryGroup(kmlFactory.createModel(model));
		return placemark;
	}

/*	// Origin for Collada scene
	protected List<Point3d> setOrigins() {
		List<Point3d> coords = new ArrayList<Point3d>();

		if (transformation != null) { 
			// for implicit geometries, bugfix for the previous version (V1.6)
			// the local coordinates of the Origin Point must be converted from the local
			// Cartesian coordinate system to the world Coordinate reference System
			double[] originalCoords = new double[]{0, 0, 0, 1};
			Matrix v = new Matrix(originalCoords, 4);
			v = transformation.times(v);
			setOriginX ((v.get(0, 0) + refPointX)*CLOSE_COORDS_FACTOR);
			setOriginY ((v.get(1, 0) + refPointY)*CLOSE_COORDS_FACTOR);
			setOriginZ ((v.get(2, 0) + refPointZ));
			// dummy
			Point3d point3d = new Point3d(getOriginX(), getOriginY(), getOriginZ());
			coords.add(point3d);	
		}
		else {
			coords = super.setOrigins();
		}
		return coords;
	}*/

	protected void fillGenericObjectForCollada(ResultSet rs, boolean generateTextureAtlas) throws SQLException {

		if (transformation == null) { // no implicit geometry
			super.fillGenericObjectForCollada(rs, generateTextureAtlas);
			return;
		}

		String selectedTheme = config.getProject().getKmlExporter().getAppearanceTheme();
		int texImageCounter = 0;
		
		DisplayForm colladaDisplayForm = null;
		for (DisplayForm displayForm: getDisplayForms()) {
			if (displayForm.getForm() == DisplayForm.COLLADA) {
				colladaDisplayForm = displayForm;
				break;
			}
		}
		
		X3DMaterial x3dSurfaceMaterial =  super.getX3dMaterialFromIntColor(colladaDisplayForm.getRgba0());

		while (rs.next()) {
			long surfaceRootId = rs.getLong(1);
			for (String colladaQuery: Queries.COLLADA_IMPLICIT_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID) { // parent surfaces come first
				PreparedStatement psQuery = null;
				ResultSet rs2 = null;
				try {
					psQuery = connection.prepareStatement(colladaQuery);
					psQuery.setLong(1, surfaceRootId);
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
								if (getX3dMaterial(parentId) != null && getX3dMaterial(surfaceId) == null) { // material for parent's parent known
									addX3dMaterial(surfaceId, getX3dMaterial(parentId));
								}
							}
							continue;
						}

						// from hier on it is a surfaceMember
						eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

						String texImageUri = null;
						StringTokenizer texCoordsTokenized = null;

						if (selectedTheme.equals(KmlExporter.THEME_NONE)) {
							if (getX3dMaterial(surfaceId) == null)
								addX3dMaterial(surfaceId, x3dSurfaceMaterial);
						}
						else {
							if (!selectedTheme.equalsIgnoreCase(theme) && !selectedTheme.equalsIgnoreCase("<unknown>")) { // no surface data for this surface and theme
								if (getX3dMaterial(surfaceId) == null) {
									if (getX3dMaterial(parentId) != null) // material for parent surface known
										addX3dMaterial(surfaceId, getX3dMaterial(parentId));
									else if (getX3dMaterial(rootId) != null) // material for root surface known
										addX3dMaterial(surfaceId, getX3dMaterial(rootId));
									else
										addX3dMaterial(surfaceId, x3dSurfaceMaterial);
								}
							}
							else {
								texImageUri = rs2.getString("tex_image_uri");

								StringBuilder sb =  new StringBuilder();
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
									texImageUri = "_" + texImageUri.substring(fileSeparatorIndex + 1); 
									texImageUri = texImageUri.replaceAll(" ", "_"); //replace spaces with underscores

									addTexImageUri(surfaceId, texImageUri);
									if ((getUnsupportedTexImageId(texImageUri) == -1) && (getTexImage(texImageUri) == null)) { 
										// not already marked as wrapping texture && not already read in
										TextureImage texImage = null;
										try {
											byte[] imageBytes = textureExportAdapter.getInByteArray(textureImageId, texImageUri);
											if (imageBytes != null) {
												imageReader.setSupportRGB(generateTextureAtlas);
												texImage = imageReader.read(new ByteArrayInputStream(imageBytes));
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
										if (getX3dMaterial(parentId) != null) // material for parent surface known
											addX3dMaterial(surfaceId, getX3dMaterial(parentId));
										else if (getX3dMaterial(rootId) != null) // material for root surface known
											addX3dMaterial(surfaceId, getX3dMaterial(rootId));
										else // untextured surface and no x3dMaterial -> default x3dMaterial (gray)
											addX3dMaterial(surfaceId, x3dSurfaceMaterial);
									}
								}
							}
						}

						GeometryObject surface = applyTransformationMatrix(geometryConverterAdapter.getPolygon(buildingGeometryObj));
						List<VertexInfo> vertexInfos = new ArrayList<VertexInfo>();
						
						int ringCount = surface.getNumElements();
						int[] vertexCount = new int[ringCount];

						for (int i = 0; i < surface.getNumElements(); i++) {
							double[] ordinatesArray = surface.getCoordinates(i);
							int vertices = 0;

							for (int j = 0; j < ordinatesArray.length - 3; j = j+3) {

								// calculate origin and list of lowest points
								updateOrigins(ordinatesArray[j], ordinatesArray[j + 1], ordinatesArray[j + 2]);

								// get or create node in vertex info tree
								VertexInfo vertexInfo = setVertexInfoForXYZ(surfaceId,
										ordinatesArray[j],
										ordinatesArray[j+1],
										ordinatesArray[j+2]);

								if (texCoordsTokenized != null && texCoordsTokenized.hasMoreTokens()) {
									double s = Double.parseDouble(texCoordsTokenized.nextToken());
									double t = Double.parseDouble(texCoordsTokenized.nextToken());
									vertexInfo.addTexCoords(surfaceId, new TexCoords(s, t));
								}
		
								vertexInfos.add(vertexInfo);
								vertices++;
							}
							
							vertexCount[i] = vertices;

							if (texCoordsTokenized != null && texCoordsTokenized.hasMoreTokens()) {
								texCoordsTokenized.nextToken(); // geometryInfo ignores last point in a polygon
								texCoordsTokenized.nextToken(); // keep texture coordinates in sync
							}
						}

						addSurfaceInfo(surfaceId, new SurfaceInfo(ringCount, vertexCount, vertexInfos));
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
		placemark.setId(config.getProject().getKmlExporter().getIdPrefixes().getPlacemarkHighlight() + placemark.getName());
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
				default:
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
			Logger.getInstance().warn("Exception when generating highlighting geometry of vegetation object " + work.getGmlId());
			e.printStackTrace();
		}
		finally {
			if (rs != null) rs.close();
			if (getGeometriesStmt != null) getGeometriesStmt.close();
		}

		return placemarkList;
	}

}
