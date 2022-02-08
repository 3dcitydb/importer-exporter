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
package org.citydb.vis.database;

import net.opengis.kml._2.PlacemarkType;
import org.citydb.config.Config;
import org.citydb.config.project.visExporter.*;
import org.citydb.core.ade.visExporter.ADEVisExportException;
import org.citydb.core.ade.visExporter.ADEVisExportExtensionManager;
import org.citydb.core.ade.visExporter.ADEVisExportManager;
import org.citydb.core.ade.visExporter.ADEVisExporter;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.adapter.BlobExportAdapter;
import org.citydb.core.query.Query;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.log.Logger;
import org.citydb.vis.util.BalloonTemplateHandler;
import org.citydb.vis.util.ElevationServiceHandler;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ADEObject extends AbstractVisObject {
	private final Logger log = Logger.getInstance();
	private final int adeObjectClassId;

	public ADEObject(Connection connection,
	                 Query query,
	                 VisExporterManager visExporterManager,
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
                visExporterManager,
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
		return ADEVisExportExtensionManager.getInstance().getPreference(config, adeObjectClassId);
	}

	protected Styles getStyles() {
		return getPreference().getStyles();
	}

	public PointAndCurve getPointAndCurve() {
		return getPreference().getPointAndCurve();
	}

	public Balloon getBalloonSettings() {
		return getPreference().getBalloon();
	}

	public String getStyleBasisName() {
		return ADEVisExportExtensionManager.getInstance().getPreference(config, adeObjectClassId).getTarget();
	}

	public void read(DBSplittingResult work) {
		PreparedStatement pointAndCurveQueryPs = null;
		ResultSet pointAndCurveQueryRs = null;
		boolean hasPointAndCurve = false;
		PreparedStatement brepIdsQueryPs = null;
		ResultSet brepIdsQueryRs = null;
		boolean hasBrep = false;
		PreparedStatement brepGeometriesQueryPs = null;
		ResultSet brepGeometriesQueryRs = null;

		try {
			ADEVisExportManager adeVisExportManager = visExporterManager.getADEVisExportManager(adeObjectClassId);
			ADEVisExporter adeVisExporter = adeVisExportManager.getVisExporter(adeObjectClassId);

			int lodToExportFrom = config.getVisExportConfig().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 0: lodToExportFrom;

			while (currentLod >= minLod) {
				if (!work.getDisplayForm().isAchievableFromLoD(currentLod)) 
					break;

				try {
					String query = adeVisExporter.getSurfaceGeometryQuery(currentLod);
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
					query = adeVisExporter.getPointAndCurveQuery(currentLod);
					if (query != null) {
						pointAndCurveQueryPs = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
						for (int i = 1; i <= getParameterCount(query); i++)
							pointAndCurveQueryPs.setLong(i, work.getId());

						pointAndCurveQueryRs = pointAndCurveQueryPs.executeQuery();
						if (pointAndCurveQueryRs.next())
							hasPointAndCurve = true;
					}
				} catch (Exception e) {
					log.error("SQL error while querying the highest available LOD.", e);
				}

				if (hasBrep || hasPointAndCurve)
					break;

				currentLod--;
			}

			if (!hasBrep && !hasPointAndCurve) {
				String fromMessage = " from LoD" + lodToExportFrom;
				if (lodToExportFrom == 5) {
					if (work.getDisplayForm().getType() == DisplayFormType.COLLADA)
						fromMessage = ". LoD1 or higher required";
					else
						fromMessage = " from any LoD";
				}
				log.info("Could not display object " + work.getGmlId() + " as " + work.getDisplayForm().getName() + fromMessage + ".");
			}
			else { // result not empty
				visExporterManager.updateFeatureTracker(work);

				if (hasPointAndCurve) {
					// export point and curve geometries for all display forms
					visExporterManager.print(createPlacemarksForPointOrCurve(pointAndCurveQueryRs, work, getPointAndCurve()),
							work,
							getBalloonSettings().isBalloonContentInSeparateFile());
				}

				if (hasBrep) {
					String query;
					if (work.getDisplayForm().getType() == DisplayFormType.FOOTPRINT || work.getDisplayForm().getType() == DisplayFormType.EXTRUDED) {
						query = adeVisExporter.getSurfaceGeometryQuery(currentLod);
					} else {
						query = adeVisExporter.getSurfaceGeometryRefIdsQuery(currentLod);
					}
					brepGeometriesQueryPs = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					for (int i = 1; i <= getParameterCount(query); i++)
						brepGeometriesQueryPs.setLong(i, work.getId());
					brepGeometriesQueryRs = brepGeometriesQueryPs.executeQuery();

					switch (work.getDisplayForm().getType()) {
						case FOOTPRINT:
							visExporterManager.print(createPlacemarksForFootprint(brepGeometriesQueryRs, work),
									work,
									getBalloonSettings().isBalloonContentInSeparateFile());
							break;

						case EXTRUDED:
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
								visExporterManager.print(createPlacemarksForExtruded(brepGeometriesQueryRs, work, measuredHeight, false),
										work, getBalloonSettings().isBalloonContentInSeparateFile());
								break;
							} finally {
								try { if (psQuery != null) psQuery.close(); } catch (SQLException e) {}
							}

						case GEOMETRY:
							setGmlId(work.getGmlId());
							setId(work.getId());

							visExporterManager.print(createPlacemarksForGeometry(brepGeometriesQueryRs, work), work, getBalloonSettings().isBalloonContentInSeparateFile());
							if (getStyle(work.getDisplayForm().getType()).isHighlightingEnabled())
								visExporterManager.print(createPlacemarksForHighlighting(brepGeometriesQueryRs, work), work, getBalloonSettings().isBalloonContentInSeparateFile());

							break;

						case COLLADA:
							ColladaOptions colladaOptions = config.getVisExportConfig().getColladaOptions();

							String currentgmlId = getGmlId();
							setGmlId(work.getGmlId());
							setId(work.getId());
							fillGenericObjectForCollada(brepGeometriesQueryRs, colladaOptions.isGenerateTextureAtlases());

							if (currentgmlId != null && !currentgmlId.equals(work.getGmlId()) && getGeometryAmount() > GEOMETRY_AMOUNT_WARNING)
								log.info("Object " + work.getGmlId() + " has more than " + GEOMETRY_AMOUNT_WARNING + " geometries. This may take a while to process...");

							List<Point3d> anchorCandidates = getOrigins();
							double zOffset = getZOffsetFromConfigOrDB(work.getId());
							if (zOffset == Double.MAX_VALUE) {
								zOffset = getZOffsetFromGEService(work.getId(), anchorCandidates);
							}
							setZOffset(zOffset);

							setIgnoreSurfaceOrientation(colladaOptions.isIgnoreSurfaceOrientation());
							try {
								if (getStyle(work.getDisplayForm().getType()).isHighlightingEnabled())
									visExporterManager.print(createPlacemarksForHighlighting(brepGeometriesQueryRs, work), work, getBalloonSettings().isBalloonContentInSeparateFile());
							} catch (Exception ioe) {
								log.logStackTrace(ioe);
							}

							break;
					}
				}
			}
		} catch (SQLException e) {
			log.error("SQL error while querying city object " + work.getGmlId() + ".", e);
		} catch (JAXBException e) {
			log.error("XML error while working on city object " + work.getGmlId() + ".", e);
		} catch (ADEVisExportException e) {
			log.error("ADE VIS export error while working on city object " + work.getGmlId() + ".", e);
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
