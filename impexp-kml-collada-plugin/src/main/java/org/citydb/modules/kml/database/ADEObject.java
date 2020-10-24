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
package org.citydb.modules.kml.database;

import net.opengis.kml._2.PlacemarkType;
import org.citydb.config.Config;
import org.citydb.config.project.kmlExporter.ADEPreference;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.PointAndCurve;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;
import org.citydb.ade.kmlExporter.ADEKmlExportException;
import org.citydb.ade.kmlExporter.ADEKmlExportExtensionManager;
import org.citydb.ade.kmlExporter.ADEKmlExportManager;
import org.citydb.ade.kmlExporter.ADEKmlExporter;
import org.citydb.modules.kml.util.BalloonTemplateHandler;
import org.citydb.modules.kml.util.ElevationServiceHandler;
import org.citydb.query.Query;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ADEObject extends KmlGenericObject{
	private final Logger log = Logger.getInstance();

	public static final String POINT = "Point";
	public static final String CURVE = "Curve";

	private final int adeObjectClassId;

	public ADEObject(Connection connection,
	                 Query query,
	                 KmlExporterManager kmlExporterManager,
	                 net.opengis.kml._2.ObjectFactory kmlFactory,
	                 AbstractDatabaseAdapter databaseAdapter,
	                 BlobExportAdapter textureExportAdapter,
	                 ElevationServiceHandler elevationServiceHandler,
	                 BalloonTemplateHandler balloonTemplateHandler,
	                 EventDispatcher eventDispatcher,
	                 Config config,
	                 int adeObjectClassId) {

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

		this.adeObjectClassId = adeObjectClassId;
	}

	private ADEPreference getPreference() {
		return ADEKmlExportExtensionManager.getInstance().getPreference(config, adeObjectClassId);
	}
	protected List<DisplayForm> getDisplayForms() {
		return getPreference().getDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return getPreference().getColladaOptions();
	}

	public PointAndCurve getPointAndCurve() {
		return getPreference().getPointAndCurve();
	}

	public Balloon getBalloonSettings() {
		return getPreference().getBalloon();
	}

	public String getStyleBasisName() {
		return ADEKmlExportExtensionManager.getInstance().getPreference(config, adeObjectClassId).getTarget();
	}

	public void read(KmlSplittingResult work) {
		PreparedStatement pointAndCurveQueryPs = null;
		ResultSet pointAndCurveQueryRs = null;
		boolean hasPointAndCurve = false;
		PreparedStatement brepIdsQueryPs = null;
		ResultSet brepIdsQueryRs = null;
		boolean hasBrep = false;
		PreparedStatement brepGeometriesQueryPs = null;
		ResultSet brepGeometriesQueryRs = null;

		try {
			ADEKmlExportManager adeKmlExportManager = kmlExporterManager.getADEKmlExportManager(adeObjectClassId);
			ADEKmlExporter adeKmlExporter = adeKmlExportManager.getKmlExporter(adeObjectClassId);

			int lodToExportFrom = config.getKmlExportConfig().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 0: lodToExportFrom;

			while (currentLod >= minLod) {
				if (!work.getDisplayForm().isAchievableFromLoD(currentLod)) 
					break;

				try {
					String query = adeKmlExporter.getSurfaceGeometryQuery(currentLod);
					if (query != null) {
						brepIdsQueryPs = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
						for (int i = 1; i <= getParameterCount(query); i++)
							brepIdsQueryPs.setLong(i, work.getId());

						brepIdsQueryRs = brepIdsQueryPs.executeQuery();

						if (brepIdsQueryRs.isBeforeFirst()) {
							hasBrep = true; // result set not empty
						}
					}

					// check for point or curve
					query = adeKmlExporter.getPointAndCurveQuery(currentLod);
					if (query != null) {
						pointAndCurveQueryPs = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
						for (int i = 1; i <= getParameterCount(query); i++)
							pointAndCurveQueryPs.setLong(i, work.getId());

						pointAndCurveQueryRs = pointAndCurveQueryPs.executeQuery();
						if (pointAndCurveQueryRs.next())
							hasPointAndCurve = true;
					}
				} catch (Exception e) {
					log.error("SQL error while querying the highest available LOD: " + e.getMessage());
				}

				if (hasBrep || hasPointAndCurve)
					break;

				currentLod--;
			}

			if (!hasBrep && !hasPointAndCurve) {
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
				kmlExporterManager.updateFeatureTracker(work);

				if (hasPointAndCurve) {
					// export point and curve geometries for all display forms
					kmlExporterManager.print(createPlacemarksForPointOrCurve(pointAndCurveQueryRs, work, getPointAndCurve()),
							work,
							getBalloonSettings().isBalloonContentInSeparateFile());
				}

				if (hasBrep) {
					String query;
					if (work.getDisplayForm().getForm() == DisplayForm.FOOTPRINT || work.getDisplayForm().getForm() == DisplayForm.EXTRUDED) {
						query = adeKmlExporter.getSurfaceGeometryQuery(currentLod);
					} else {
						query = adeKmlExporter.getSurfaceGeometryRefIdsQuery(currentLod);
					}
					brepGeometriesQueryPs = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					for (int i = 1; i <= getParameterCount(query); i++)
						brepGeometriesQueryPs.setLong(i, work.getId());
					brepGeometriesQueryRs = brepGeometriesQueryPs.executeQuery();

					// get the proper displayForm (for highlighting)
					int indexOfDf = getDisplayForms().indexOf(work.getDisplayForm());
					if (indexOfDf != -1)
						work.setDisplayForm(getDisplayForms().get(indexOfDf));

					switch (work.getDisplayForm().getForm()) {
						case DisplayForm.FOOTPRINT:
							kmlExporterManager.print(createPlacemarksForFootprint(brepGeometriesQueryRs, work),
									work,
									getBalloonSettings().isBalloonContentInSeparateFile());
							break;

						case DisplayForm.EXTRUDED:
							PreparedStatement psQuery = null;
							ResultSet rs = null;

							try {
								query = queries.getExtrusionHeight();
								psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
								for (int i = 1; i <= getParameterCount(query); i++)
									psQuery.setLong(i, work.getId());

								rs = psQuery.executeQuery();
								rs.next();

								double measuredHeight = rs.getDouble("envelope_measured_height");
								kmlExporterManager.print(createPlacemarksForExtruded(brepGeometriesQueryRs, work, measuredHeight, false),
										work, getBalloonSettings().isBalloonContentInSeparateFile());
								break;
							} finally {
								try { if (psQuery != null) psQuery.close(); } catch (SQLException e) {}
							}

						case DisplayForm.GEOMETRY:
							setGmlId(work.getGmlId());
							setId(work.getId());

							kmlExporterManager.print(createPlacemarksForGeometry(brepGeometriesQueryRs, work), work, getBalloonSettings().isBalloonContentInSeparateFile());
							if (work.getDisplayForm().isHighlightingEnabled())
								kmlExporterManager.print(createPlacemarksForHighlighting(brepGeometriesQueryRs, work), work, getBalloonSettings().isBalloonContentInSeparateFile());

							break;

						case DisplayForm.COLLADA:
							String currentgmlId = getGmlId();
							setGmlId(work.getGmlId());
							setId(work.getId());
							fillGenericObjectForCollada(brepGeometriesQueryRs, getColladaOptions().isGenerateTextureAtlases());

							if (currentgmlId != null && !currentgmlId.equals(work.getGmlId()) && getGeometryAmount() > GEOMETRY_AMOUNT_WARNING)
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
									kmlExporterManager.print(createPlacemarksForHighlighting(brepGeometriesQueryRs, work), work, getBalloonSettings().isBalloonContentInSeparateFile());
							} catch (Exception ioe) {
								log.logStackTrace(ioe);
							}

							break;
					}
				}
			}
		} catch (SQLException sqlEx) {
			log.error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
		} catch (JAXBException jaxbEx) {
			log.error("XML error while working on city object " + work.getGmlId() + ": " + jaxbEx.getMessage());
		} catch (ADEKmlExportException e) {
			log.error("ADE Kml-Export error while working on city object " + work.getGmlId() + ": " + e.getMessage());
		} finally {
			if (brepGeometriesQueryPs != null)
				try { brepGeometriesQueryPs.close(); } catch (SQLException e) {}
			if (brepGeometriesQueryPs != null)
				try { brepGeometriesQueryPs.close(); } catch (SQLException e) {}
			if (pointAndCurveQueryPs != null)
				try { pointAndCurveQueryPs.close(); } catch (SQLException e) {}
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
