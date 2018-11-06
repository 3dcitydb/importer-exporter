/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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

import net.opengis.kml._2.PlacemarkType;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.KmlExporter;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;
import org.citydb.modules.kml.util.AffineTransformer;
import org.citydb.modules.kml.util.BalloonTemplateHandler;
import org.citydb.modules.kml.util.ElevationServiceHandler;
import org.citydb.query.Query;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.geometry.Point;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CityFurniture extends KmlGenericObject{
	private final Logger log = Logger.getInstance();

	public static final String STYLE_BASIS_NAME = "Furniture";

	private AffineTransformer transformer;

	public CityFurniture(Connection connection,
			Query query,
			KmlExporterManager kmlExporterManager,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			AbstractDatabaseAdapter databaseAdapter,
			BlobExportAdapter textureExportAdapter,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandler balloonTemplateHandler,
			EventDispatcher eventDispatcher,
			Config config) {

		super(connection,
				query,
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
		return config.getProject().getKmlExporter().getCityFurnitureDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return config.getProject().getKmlExporter().getCityFurnitureColladaOptions();
	}

	public Balloon getBalloonSettings() {
		return config.getProject().getKmlExporter().getCityFurnitureBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	public void read(KmlSplittingResult work) {
		PreparedStatement psQuery = null;
		ResultSet rs = null;

		try {
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 1: lodToExportFrom;

			while (currentLod >= minLod) {
				if (!work.getDisplayForm().isAchievableFromLoD(currentLod)) 
					break;

				try {
					String query = queries.getCityFurnitureBasisData(currentLod);
					psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					for (int i = 1; i <= getParameterCount(query); i++)
						psQuery.setLong(i, work.getId());

					rs = psQuery.executeQuery();
					if (rs.isBeforeFirst()) {
						rs.next();
						if (rs.getLong(4) != 0 || rs.getLong(1) != 0)
							break; // result set not empty
					}

					try { rs.close(); } catch (SQLException sqle) {} 
					try { psQuery.close(); } catch (SQLException sqle) {}
					rs = null;
				} catch (Exception e) {
					log.error("SQL error while querying the highest available LOD: " + e.getMessage());
					try { if (rs != null) rs.close(); } catch (SQLException sqle) {} 
					try { if (psQuery != null) psQuery.close(); } catch (SQLException sqle) {}
					try { connection.commit(); } catch (SQLException sqle) {}
					rs = null;
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
				log.info("Could not display object " + work.getGmlId() + " as " + work.getDisplayForm().getName() + fromMessage + ".");
			}

			else { // result not empty
				// decide whether explicit or implicit geometry
				long sgRootId = rs.getLong(4);
				if (sgRootId == 0) {
					sgRootId = rs.getLong(1);
					if (sgRootId != 0) {
						GeometryObject point = geometryConverterAdapter.getPoint(rs.getObject(2));
						String transformationString = rs.getString(3);
						if (point != null && transformationString != null) {
							double[] ordinatesArray = point.getCoordinates(0);
							Point referencePoint = new Point(ordinatesArray[0], ordinatesArray[1], ordinatesArray[2]);						
							List<Double> m = Util.string2double(transformationString, "\\s+");
							if (m != null && m.size() >= 16)
								transformer = new AffineTransformer(new Matrix(m.subList(0, 16), 4), referencePoint, databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid());
						}
					}
				}

				try { rs.close(); } catch (SQLException sqle) {} 
				try { psQuery.close(); } catch (SQLException sqle) {}
				rs = null;

				String query = queries.getCityFurnitureQuery(currentLod, 
						work.getDisplayForm(),
						transformer != null, 
						work.getDisplayForm().getForm() == DisplayForm.COLLADA && config.getProject().getKmlExporter().getAppearanceTheme() != KmlExporter.THEME_NONE);
				psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				psQuery.setLong(1, sgRootId);
				rs = psQuery.executeQuery();
				
				kmlExporterManager.updateFeatureTracker(work);

				// get the proper displayForm (for highlighting)
				int indexOfDf = getDisplayForms().indexOf(work.getDisplayForm());
				if (indexOfDf != -1)
					work.setDisplayForm(getDisplayForms().get(indexOfDf));

				switch (work.getDisplayForm().getForm()) {
				case DisplayForm.FOOTPRINT:
					kmlExporterManager.print(createPlacemarksForFootprint(rs, work, transformer),
							work,
							getBalloonSettings().isBalloonContentInSeparateFile());
					break;

				case DisplayForm.EXTRUDED:
					PreparedStatement psQuery2 = null;
					ResultSet rs2 = null;

					try {
						query = queries.getExtrusionHeight();
						psQuery2 = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
						for (int i = 1; i <= getParameterCount(query); i++)
							psQuery2.setLong(i, work.getId());

						rs2 = psQuery2.executeQuery();
						rs2.next();

						double measuredHeight = rs2.getDouble("envelope_measured_height");
						kmlExporterManager.print(createPlacemarksForExtruded(rs, work, measuredHeight, false, transformer),
								work, getBalloonSettings().isBalloonContentInSeparateFile());
						break;
					} finally {
						try { if (rs2 != null) rs2.close(); } catch (SQLException e) {}
						try { if (psQuery2 != null) psQuery2.close(); } catch (SQLException e) {}
					}

				case DisplayForm.GEOMETRY:
					setGmlId(work.getGmlId());
					setId(work.getId());
					if (this.query.isSetTiling()) { // region
						if (work.getDisplayForm().isHighlightingEnabled())
							kmlExporterManager.print(createPlacemarksForHighlighting(rs, work, transformer), work, getBalloonSettings().isBalloonContentInSeparateFile());

						kmlExporterManager.print(createPlacemarksForGeometry(rs, work, transformer), work, getBalloonSettings().isBalloonContentInSeparateFile());
					} else { // reverse order for single objects
						kmlExporterManager.print(createPlacemarksForGeometry(rs, work, transformer), work, getBalloonSettings().isBalloonContentInSeparateFile());
						if (work.getDisplayForm().isHighlightingEnabled())
							kmlExporterManager.print(createPlacemarksForHighlighting(rs, work, transformer), work, getBalloonSettings().isBalloonContentInSeparateFile());
					}
					break;

				case DisplayForm.COLLADA:
					String currentgmlId = getGmlId();
					setGmlId(work.getGmlId());
					setId(work.getId());
					fillGenericObjectForCollada(rs, config.getProject().getKmlExporter().getCityFurnitureColladaOptions().isGenerateTextureAtlases(),  transformer);

					if (currentgmlId != work.getGmlId() && getGeometryAmount() > GEOMETRY_AMOUNT_WARNING)
						log.info("Object " + work.getGmlId() + " has more than " + GEOMETRY_AMOUNT_WARNING + " geometries. This may take a while to process...");

					List<Point3d> anchorCandidates = getOrigins();
					double zOffset = getZOffsetFromConfigOrDB(work.getId());
					if (zOffset == Double.MAX_VALUE) {
						zOffset = getZOffsetFromGEService(work.getId(), anchorCandidates);
					}
					setZOffset(zOffset);

					ColladaOptions colladaOptions = getColladaOptions();
					setIgnoreSurfaceOrientation(colladaOptions.isIgnoreSurfaceOrientation());
					try {
						if (work.getDisplayForm().isHighlightingEnabled()) 
							kmlExporterManager.print(createPlacemarksForHighlighting(rs, work, transformer), work, getBalloonSettings().isBalloonContentInSeparateFile());
					} catch (Exception ioe) {
						log.logStackTrace(ioe);
					}

					break;
				}
			}
		} catch (SQLException sqlEx) {
			log.error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
			return;
		} catch (JAXBException jaxbEx) {
			log.error("XML error while working on city object " + work.getGmlId() + ": " + jaxbEx.getMessage());
			return;
		} finally {
			if (rs != null)
				try { rs.close(); } catch (SQLException e) {}
			if (psQuery != null)
				try { psQuery.close(); } catch (SQLException e) {}
		}
	}

	public PlacemarkType createPlacemarkForColladaModel() throws SQLException {
		double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {getOrigin().x,
				getOrigin().y,
				getOrigin().z});
		setLocation(reducePrecisionForXorY(originInWGS84[0]),
				reducePrecisionForXorY(originInWGS84[1]),
				reducePrecisionForZ(originInWGS84[2]));

		return super.createPlacemarkForColladaModel();
	}

}
