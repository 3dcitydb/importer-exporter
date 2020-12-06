/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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

import org.citydb.config.Config;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.Styles;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;
import org.citydb.modules.kml.util.BalloonTemplateHandler;
import org.citydb.modules.kml.util.ElevationServiceHandler;
import org.citydb.query.Query;

import javax.xml.bind.JAXBException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CityObjectGroup extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "Group";

	public CityObjectGroup(Connection connection,
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

	protected Styles getStyles() {
		return config.getKmlExportConfig().getCityObjectGroupStyles();
	}

	public ColladaOptions getColladaOptions() {
		return null; // no COLLADA display form for CityObjectGroups
	}

	public Balloon getBalloonSettings() {
		return config.getKmlExportConfig().getCityObjectGroupBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	public void read(KmlSplittingResult work) {
		PreparedStatement psQuery = null;
		ResultSet rs = null;

		try {
			String query = queries.getCityObjectGroupFootprint(work.getObjectClassId());
			psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			for (int i = 1; i <= getParameterCount(query); i++)
				psQuery.setLong(i, work.getId());

			rs = psQuery.executeQuery();
			if (!rs.isBeforeFirst()) {
				try { rs.close(); } catch (SQLException sqle) {} 
				try { psQuery.close(); } catch (SQLException sqle) {}
				rs = null;
			}

			if (rs == null) { // result empty, give up
				Logger.getInstance().info("Could not display CityObjectGroup " + work.getGmlId());
			}
			else { // result not empty
				// hard-coded for groups
				kmlExporterManager.updateFeatureTracker(work);
				kmlExporterManager.print(createPlacemarksForFootprint(rs, work),
						work,
						getBalloonSettings().isBalloonContentInSeparateFile());				
			}
		} catch (SQLException sqlEx) {
			Logger.getInstance().error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
		} catch (JAXBException jaxbEx) {
			Logger.getInstance().error("XML error while working on city object " + work.getGmlId() + ": " + jaxbEx.getMessage());
		} finally {
			if (rs != null)
				try { rs.close(); } catch (SQLException e) {}
			if (psQuery != null)
				try { psQuery.close(); } catch (SQLException e) {}
		}
	}

}
