/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import oracle.jdbc.OracleResultSet;

import org.citygml4j.factory.CityGMLFactory;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.kmlExporter.Balloon;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;

public class CityObjectGroup extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "Group";

	public CityObjectGroup(Connection connection,
			KmlExporterManager kmlExporterManager,
			CityGMLFactory cityGMLFactory,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandlerImpl balloonTemplateHandler,
			EventDispatcher eventDispatcher,
			DatabaseSrs dbSrs,
			Config config) {

		super(connection,
			  kmlExporterManager,
			  cityGMLFactory,
			  kmlFactory,
			  elevationServiceHandler,
			  balloonTemplateHandler,
			  eventDispatcher,
			  dbSrs,
			  config);
	}

	protected Balloon getBalloonSettings() {
		return config.getProject().getKmlExporter().getCityObjectGroupBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	public void read(KmlSplittingResult work) {

		PreparedStatement psQuery = null;
		OracleResultSet rs = null;

		try {
//			psQuery = getQueryForObjectType(work);
			psQuery = connection.prepareStatement(Queries.CITYOBJECTGROUP_FOOTPRINT);

			for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
				psQuery.setString(i, work.getGmlId());
			}
				
			rs = (OracleResultSet)psQuery.executeQuery();
			if (!rs.isBeforeFirst()) {
				try { rs.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
				rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
				try { psQuery.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
			}

			if (rs == null) { // result empty, give up
				Logger.getInstance().info("Could not display CityObjectGroup " + work.getGmlId());
			}
			else { // result not empty
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, 1, this));
/*
				// get the proper displayForm (colors, highlighting) when not building
				DisplayForm displayForm = new DisplayForm(DisplayForm.FOOTPRINT, -1, -1);
				int indexOfDf = config.getProject().getKmlExporter().getCityObjectGroupDisplayForms().indexOf(displayForm);
				if (indexOfDf != -1) {
					work.setDisplayForm(config.getProject().getKmlExporter().getCityObjectGroupDisplayForms().get(indexOfDf));
				}
*/
				// hard-coded for groups
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
