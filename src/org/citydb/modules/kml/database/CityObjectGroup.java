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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.citydb.api.database.BalloonTemplateHandler;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.log.Logger;

public class CityObjectGroup extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "Group";

	public CityObjectGroup(Connection connection,
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
		return config.getProject().getKmlExporter().getCityObjectGroupDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return null; // no COLLADA display form for CityObjectGroups
	}

	public Balloon getBalloonSettings() {
		return config.getProject().getKmlExporter().getCityObjectGroupBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	protected String getHighlightingQuery() {
		return null;  // no COLLADA or Geometry display form for CityObjectGroups
	}

	public void read(KmlSplittingResult work) {

		PreparedStatement psQuery = null;
		ResultSet rs = null;

		try {
//			psQuery = getQueryForObjectType(work);
			psQuery = connection.prepareStatement(Queries.CITYOBJECTGROUP_FOOTPRINT);

			for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
				psQuery.setLong(i, work.getId());
			}
				
			rs = psQuery.executeQuery();
			if (!rs.isBeforeFirst()) {
				try { rs.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
				rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
				try { psQuery.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
			}

			if (rs == null) { // result empty, give up
				Logger.getInstance().info("Could not display CityObjectGroup " + work.getGmlId());
			}
			else { // result not empty
				// get the proper displayForm (colors, highlighting) when not building
				DisplayForm displayForm = new DisplayForm(DisplayForm.FOOTPRINT, -1, -1);
				int indexOfDf = getDisplayForms().indexOf(displayForm);
				if (indexOfDf != -1) {
					work.setDisplayForm(getDisplayForms().get(indexOfDf));
				}

				// hard-coded for groups
				kmlExporterManager.updateFeatureTracker(work);
				kmlExporterManager.print(createPlacemarksForFootprint(rs, work),
										 work,
										 getBalloonSettings().isBalloonContentInSeparateFile());				
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
	
}
