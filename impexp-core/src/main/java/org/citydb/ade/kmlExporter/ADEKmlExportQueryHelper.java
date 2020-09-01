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
package org.citydb.ade.kmlExporter;

import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.Lod0FootprintMode;

public interface ADEKmlExportQueryHelper {
	String getImplicitGeometryNullColumns();
	String getBuildingPartAggregateGeometries(double tolerance, int srid2D, int lodToExportFrom, double groupBy1, double groupBy2, double groupBy3, int objectClassId);
	String getBuildingPartQuery(int lodToExportFrom, Lod0FootprintMode lod0FootprintMode, DisplayForm displayForm, boolean lodCheckOnly, int objectClassId);
	String getBridgePartAggregateGeometries(double tolerance, int srid2D, int lodToExportFrom, double groupBy1, double groupBy2, double groupBy3, int objectClassId);
	String getBridgePartQuery(int lodToExportFrom, DisplayForm displayForm, boolean lodCheckOnly, int objectClassId);
	String getTunnelPartAggregateGeometries(double tolerance, int srid2D, int lodToExportFrom, double groupBy1, double groupBy2, double groupBy3, int objectClassId);
	String getTunnelPartQuery(int lodToExportFrom, DisplayForm displayForm, boolean lodCheckOnly, int objectClassId);
	String getCityObjectGroupFootprint(int objectClassId);
	String getSolitaryVegetationObjectQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId);
	String getPlantCoverQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId);
	String getGenericCityObjectQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId);
	String getGenericCityObjectPointAndCurveQuery(int lodToExportFrom, int objectClassId);
	String getCityFurnitureQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId);
	String getWaterBodyQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId);
	String getLandUseQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId);
	String getTransportationQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId);
	String getReliefQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId);
}
