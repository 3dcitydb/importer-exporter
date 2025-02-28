/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

import net.opengis.kml._2.MultiGeometryType;
import net.opengis.kml._2.PlacemarkType;
import org.citydb.config.Config;
import org.citydb.config.project.visExporter.Balloon;
import org.citydb.config.project.visExporter.ColladaOptions;
import org.citydb.config.project.visExporter.DisplayFormType;
import org.citydb.config.project.visExporter.Styles;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.adapter.BlobExportAdapter;
import org.citydb.core.database.connection.DatabaseConnectionPool;
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
import java.util.ArrayList;
import java.util.List;

public class Bridge extends AbstractVisObject {
    private final Logger log = Logger.getInstance();

    public static final String STYLE_BASIS_NAME = "Bridge"; // "Bridge"

    public Bridge(Connection connection,
                  Query query,
                  VisExporterManager visExporterManager,
                  net.opengis.kml._2.ObjectFactory kmlFactory,
                  AbstractDatabaseAdapter databaseAdapter,
                  BlobExportAdapter textureExportAdapter,
                  ElevationServiceHandler elevationServiceHandler,
                  BalloonTemplateHandler balloonTemplateHandler,
                  EventDispatcher eventDispatcher,
                  Config config) {

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
    }

    protected Styles getStyles() {
        return config.getVisExportConfig().getBridgeStyles();
    }

    public Balloon getBalloonSettings() {
        return config.getVisExportConfig().getBridgeBalloon();
    }

    public String getStyleBasisName() {
        return STYLE_BASIS_NAME;
    }

    public void read(DBSplittingResult work) {
        List<PlacemarkType> placemarks = new ArrayList<>();
        PreparedStatement psQuery = null;
        ResultSet rs = null;

        try {
            String query = queries.getBridgePartsFromBridge();
            psQuery = connection.prepareStatement(query);
            for (int i = 1; i <= getParameterCount(query); i++)
                psQuery.setLong(i, work.getId());

            rs = psQuery.executeQuery();

            while (rs.next()) {
                long bridgePartId = rs.getLong(1);
                List<PlacemarkType> placemarkBPart = readBridgePart(bridgePartId, work);
                if (placemarkBPart != null)
                    placemarks.addAll(placemarkBPart);
            }
        } catch (SQLException sqlEx) {
            log.error("SQL error while getting bridge parts for bridge " + work.getGmlId() + ": " + sqlEx.getMessage());
            return;
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException sqle) {
            }
            try {
                if (psQuery != null) psQuery.close();
            } catch (SQLException sqle) {
            }
        }

        if (placemarks.size() == 0) {
            int lodToExportFrom = config.getVisExportConfig().getLodToExportFrom();
            String fromMessage = " from LoD" + lodToExportFrom;
            if (lodToExportFrom == 5) {
                if (work.getDisplayForm().getType() == DisplayFormType.COLLADA)
                    fromMessage = ". LoD1 or higher required";
                else
                    fromMessage = " from any LoD";
            }
            log.info("Could not display object " + work.getGmlId() + " as " + work.getDisplayForm().getName() + fromMessage + ".");
        } else {
            try {
                // compact list before exporting
                for (int i = 0; i < placemarks.size(); i++) {
                    PlacemarkType placemark1 = placemarks.get(i);
                    if (placemark1 == null) continue;
                    MultiGeometryType multiGeometry1 = (MultiGeometryType) placemark1.getAbstractGeometryGroup().getValue();
                    for (int j = i + 1; j < placemarks.size(); j++) {
                        PlacemarkType placemark2 = placemarks.get(j);
                        if (placemark2 == null || !placemark1.getId().equals(placemark2.getId())) continue;
                        // compact since ids are identical
                        MultiGeometryType multiGeometry2 = (MultiGeometryType) placemark2.getAbstractGeometryGroup().getValue();
                        multiGeometry1.getAbstractGeometryGroup().addAll(multiGeometry2.getAbstractGeometryGroup());
                        placemarks.set(j, null); // polygons transfered, placemark exhausted
                    }
                }

                visExporterManager.updateFeatureTracker(work);
                visExporterManager.print(placemarks,
                        work,
                        getBalloonSettings().isBalloonContentInSeparateFile());
            } catch (JAXBException jaxbEx) {
                //
            }
        }
    }

    private List<PlacemarkType> readBridgePart(long bridgePartId, DBSplittingResult work) {
        PreparedStatement psQuery = null;
        ResultSet rs = null;
        boolean reversePointOrder = false;

        try {
            currentLod = config.getVisExportConfig().getLodToExportFrom();

            // we handle FOOTPRINT/EXTRUDED differently than GEOMETRY/COLLADA
            if (work.getDisplayForm().getType() == DisplayFormType.GEOMETRY
                    || work.getDisplayForm().getType() == DisplayFormType.COLLADA) {

                if (currentLod == 5) {
                    // find the highest available LOD to export from. to increase performance,
                    // this is just a light-weight query that only checks for the main exterior
                    // bridge shell without appearances
                    while (--currentLod > 0) {
                        if (!work.getDisplayForm().isAchievableFromLoD(currentLod))
                            break;

                        try {
                            String query = queries.getBridgePartQuery(currentLod, work.getDisplayForm(), true, work.getObjectClassId());
                            psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                            for (int i = 1; i <= getParameterCount(query); i++)
                                psQuery.setLong(i, bridgePartId);

                            rs = psQuery.executeQuery();
                            if (rs.isBeforeFirst())
                                break;
                        } catch (SQLException e) {
                            log.error("SQL error while querying the highest available LOD.", e);
                            try {
                                connection.commit();
                            } catch (SQLException sqle) {
                            }
                        } finally {
                            try {
                                if (rs != null) rs.close();
                            } catch (SQLException sqle) {
                            }
                            try {
                                if (psQuery != null) psQuery.close();
                            } catch (SQLException sqle) {
                            }
                            rs = null;
                        }
                    }
                }

                // ok, if we have an LOD to export from, we issue a heavy-weight query to get
                // the building geometry including sub-features and appearances
                if (currentLod > 0 && work.getDisplayForm().isAchievableFromLoD(currentLod)) {
                    try {
                        String query = queries.getBridgePartQuery(currentLod, work.getDisplayForm(), false, work.getObjectClassId());
                        psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        for (int i = 1; i <= getParameterCount(query); i++)
                            psQuery.setLong(i, bridgePartId);

                        rs = psQuery.executeQuery();
                    } catch (SQLException e) {
                        log.error("SQL error while querying geometries in LOD " + currentLod + ".", e);
                        try {
                            if (psQuery != null) psQuery.close();
                        } catch (SQLException sqle) {
                        }
                        try {
                            connection.commit();
                        } catch (SQLException sqle) {
                        }
                        rs = null;
                    }
                }
            } else {
                int minLod = currentLod;
                if (currentLod == 5) {
                    currentLod = 4;
                    minLod = 1;
                }

                while (currentLod >= minLod) {
                    if (!work.getDisplayForm().isAchievableFromLoD(currentLod))
                        break;

                    try {
                        // first, check whether we have an LOD1 geometry or a GroundSurface
                        String query = queries.getBridgePartQuery(currentLod, work.getDisplayForm(), false, work.getObjectClassId());
                        psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        for (int i = 1; i <= getParameterCount(query); i++)
                            psQuery.setLong(i, bridgePartId);

                        rs = psQuery.executeQuery();
                        if (rs.isBeforeFirst())
                            break;

                        try {
                            rs.close();
                        } catch (SQLException sqle) {
                        }
                        try {
                            psQuery.close();
                        } catch (SQLException sqle) {
                        }
                    } catch (SQLException e) {
                        log.error("SQL error while querying geometries in LOD " + currentLod + ".", e);
                        try {
                            if (rs != null) rs.close();
                        } catch (SQLException sqle) {
                        }
                        try {
                            if (psQuery != null) psQuery.close();
                        } catch (SQLException sqle) {
                        }
                        try {
                            connection.commit();
                        } catch (SQLException sqle) {
                        }
                    }

                    // second, try and generate a footprint by aggregating geometries
                    reversePointOrder = true;
                    int groupBasis = 4;

                    try {
                        String query = queries.getBridgePartAggregateGeometries(0.001,
                                DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getUtil().get2DSrid(dbSrs),
                                currentLod,
                                Math.pow(groupBasis, 4),
                                Math.pow(groupBasis, 3),
                                Math.pow(groupBasis, 2), work.getObjectClassId());

                        psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        for (int i = 1; i <= getParameterCount(query); i++)
                            psQuery.setLong(i, bridgePartId);

                        rs = psQuery.executeQuery();
                        if (rs.isBeforeFirst()) {
                            rs.next();
                            if (rs.getObject(1) != null) {
                                rs.beforeFirst();
                                break;
                            }
                        }

                        try {
                            rs.close();
                        } catch (SQLException sqle) {
                        }
                        try {
                            psQuery.close();
                        } catch (SQLException sqle) {
                        }
                        rs = null;
                    } catch (SQLException e) {
                        log.error("SQL error while aggregating geometries in LOD " + currentLod + ".", e);
                        try {
                            if (rs != null) rs.close();
                        } catch (SQLException sqle) {
                        }
                        try {
                            if (psQuery != null) psQuery.close();
                        } catch (SQLException sqle) {
                        }
                        try {
                            connection.commit();
                        } catch (SQLException sqle) {
                        }
                        rs = null;
                    }

                    currentLod--;
                    reversePointOrder = false;
                }
            }

            if (rs != null && rs.isBeforeFirst()) { // result not empty
                switch (work.getDisplayForm().getType()) {
                    case FOOTPRINT:
                        return createPlacemarksForFootprint(rs, work);

                    case EXTRUDED:
                        PreparedStatement psQuery2 = null;
                        ResultSet rs2 = null;

                        try {
                            String query = queries.getExtrusionHeight();
                            psQuery2 = connection.prepareStatement(query);
                            for (int i = 1; i <= getParameterCount(query); i++)
                                psQuery2.setLong(i, bridgePartId);

                            rs2 = psQuery2.executeQuery();
                            rs2.next();

                            double measuredHeight = rs2.getDouble("envelope_measured_height");
                            return createPlacemarksForExtruded(rs, work, measuredHeight, reversePointOrder);
                        } finally {
                            try {
                                if (rs2 != null) rs2.close();
                            } catch (SQLException e) {
                            }
                            try {
                                if (psQuery2 != null) psQuery2.close();
                            } catch (SQLException e) {
                            }
                        }

                    case GEOMETRY:
                        setGmlId(work.getGmlId());
                        setId(work.getId());
                        List<PlacemarkType> placemarks = createPlacemarksForGeometry(rs, work);
                        if (getStyle(work.getDisplayForm().getType()).isHighlightingEnabled()) {
                            placemarks.addAll(createPlacemarksForHighlighting(rs, work));
                        }
                        return placemarks;

                    case COLLADA:
                        ColladaOptions colladaOptions = config.getVisExportConfig().getColladaOptions();

                        fillGenericObjectForCollada(rs, colladaOptions.isGenerateTextureAtlases()); // fill and refill
                        String currentgmlId = getGmlId();
                        setGmlId(work.getGmlId());
                        setId(work.getId());

                        if (currentgmlId != null && !currentgmlId.equals(work.getGmlId()) && getGeometryAmount() > GEOMETRY_AMOUNT_WARNING)
                            log.info("Object " + work.getGmlId() + " has more than " + GEOMETRY_AMOUNT_WARNING + " geometries. This may take a while to process...");

                        List<Point3d> anchorCandidates = getOrigins(); // setOrigins() called mainly for the side-effect
                        double zOffset = getZOffsetFromConfigOrDB(work.getId());
                        if (zOffset == Double.MAX_VALUE) {
                            zOffset = getZOffsetFromGEService(work.getId(), anchorCandidates);
                        }
                        setZOffset(zOffset);

                        setIgnoreSurfaceOrientation(colladaOptions.isIgnoreSurfaceOrientation());
                        try {
                            if (getStyle(work.getDisplayForm().getType()).isHighlightingEnabled()) {
                                return createPlacemarksForHighlighting(rs, work);
                            }
                            // just COLLADA, no KML
                            List<PlacemarkType> dummy = new ArrayList<>();
                            dummy.add(null);
                            return dummy;
                        } catch (Exception ioe) {
                            log.logStackTrace(ioe);
                        }
                }
            }
        } catch (SQLException sqlEx) {
            log.error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
            return null;
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            if (psQuery != null)
                try {
                    psQuery.close();
                } catch (SQLException e) {
                }
        }

        return null; // nothing found
    }

    public PlacemarkType createPlacemarkForColladaModel() throws SQLException {
        double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[]{getOrigin().x,
                getOrigin().y,
                getOrigin().z});
        setLocation(reducePrecisionForXorY(originInWGS84[0]),
                reducePrecisionForXorY(originInWGS84[1]),
                reducePrecisionForZ(originInWGS84[2]));

        return super.createPlacemarkForColladaModel();
    }

}
