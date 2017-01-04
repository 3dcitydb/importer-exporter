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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.HashSet;

import org.citydb.api.geometry.BoundingBox;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.config.project.filter.Tiling;
import org.citydb.config.project.filter.TilingMode;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.feature.BoundingBoxFilter;
import org.citydb.modules.common.filter.feature.ProjectionPropertyFilter;
import org.citydb.util.Util;
import org.citygml4j.geometry.Point;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.RelativeToTerrain;
import org.citygml4j.model.citygml.core.RelativeToWater;
import org.citygml4j.model.citygml.generics.StringAttribute;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.feature.BoundingShape;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.gml.GMLModuleType;

public class DBCityObject implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psCityObject;

	private DBAppearance appearanceExporter;
	private DBGeneralization generalizesToExporter;
	private DBCityObjectGenericAttrib genericAttributeExporter;
	private String gmlSrsName;
	private boolean exportAppearance;
	private boolean useTiling;
	private boolean setTileInfoAsGenericAttribute;
	private BoundingBoxFilter boundingBoxFilter;
	private BoundingBox activeTile;
	private Tiling tiling;
	private ProjectionPropertyFilter dummyFilter;

	private HashSet<Long> generalizesToSet;
	private HashSet<Long> externalReferenceSet;

	public DBCityObject(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.config = config;
		this.connection = connection;
		boundingBoxFilter = exportFilter.getBoundingBoxFilter();

		init();
	}

	private void init() throws SQLException {
		exportAppearance = config.getProject().getExporter().getAppearances().isSetExportAppearance();

		tiling = config.getProject().getExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling();
		useTiling = boundingBoxFilter != null && boundingBoxFilter.isActive() && tiling.getMode() != TilingMode.NO_TILING;
		setTileInfoAsGenericAttribute = useTiling && tiling.isIncludeTileAsGenericAttribute();
		if (setTileInfoAsGenericAttribute)
			activeTile = boundingBoxFilter.getFilterState();

		dummyFilter = new ProjectionPropertyFilter(null);
		generalizesToSet = new HashSet<Long>();
		externalReferenceSet = new HashSet<Long>();

		gmlSrsName = config.getInternal().getExportTargetSRS().getGMLSrsName();
		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
			.append("select co.GMLID, co.NAME, co.NAME_CODESPACE, co.DESCRIPTION, co.ENVELOPE, co.CREATION_DATE, co.TERMINATION_DATE, co.RELATIVE_TO_TERRAIN, co.RELATIVE_TO_WATER, ")
			.append("ex.ID as EXID, ex.INFOSYS, ex.NAME, ex.URI, ge.GENERALIZES_TO_ID ")
			.append("from CITYOBJECT co left join EXTERNAL_REFERENCE ex on co.ID = ex.CITYOBJECT_ID ")
			.append("left join GENERALIZATION ge on ge.CITYOBJECT_ID=co.ID where co.ID = ?");
			psCityObject = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select co.GMLID, co.NAME, co.NAME_CODESPACE, co.DESCRIPTION, ")
			.append(transformOrNull).append("(co.ENVELOPE, ").append(srid).append(") AS ENVELOPE, ")
			.append("co.CREATION_DATE, co.TERMINATION_DATE, co.RELATIVE_TO_TERRAIN, co.RELATIVE_TO_WATER, ")
			.append("ex.ID as EXID, ex.INFOSYS, ex.NAME, ex.URI, ge.GENERALIZES_TO_ID ")
			.append("from CITYOBJECT co left join EXTERNAL_REFERENCE ex on co.ID = ex.CITYOBJECT_ID ")
			.append("left join GENERALIZATION ge on ge.CITYOBJECT_ID=co.ID where co.ID = ?");
			psCityObject = connection.prepareStatement(query.toString());
		}

		generalizesToExporter = (DBGeneralization)dbExporterManager.getDBExporter(DBExporterEnum.GENERALIZATION);
		genericAttributeExporter = (DBCityObjectGenericAttrib)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT_GENERICATTRIB);
		if (exportAppearance)
			appearanceExporter = (DBAppearance)dbExporterManager.getDBExporter(DBExporterEnum.LOCAL_APPEARANCE);
	}


	public boolean read(AbstractCityObject cityObject, long parentId) throws SQLException {
		return read(cityObject, parentId, false, dummyFilter);
	}

	public boolean read(AbstractCityObject cityObject, long parentId, boolean isTopLevelObject, ProjectionPropertyFilter projectionFilter) throws SQLException {
		ResultSet rs = null;

		try {
			psCityObject.setLong(1, parentId);
			rs = psCityObject.executeQuery();

			if (rs.next()) {
				// boundedBy
				Object object = rs.getObject(5);
				if (!rs.wasNull() && object != null) {
					GeometryObject geomObj = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getEnvelope(object);
					double[] coordinates = geomObj.getCoordinates(0);

					Envelope envelope = new Envelope();
					envelope.setLowerCorner(new Point(coordinates[0], coordinates[1], coordinates[2]));
					envelope.setUpperCorner(new Point(coordinates[3], coordinates[4], coordinates[5]));
					envelope.setSrsDimension(3);
					envelope.setSrsName(gmlSrsName);

					BoundingShape boundedBy = new BoundingShape();
					boundedBy.setEnvelope(envelope);
					cityObject.setBoundedBy(boundedBy);
				}

				// check bounding volume filter
				if (isTopLevelObject && useTiling) {
					if (!cityObject.isSetBoundedBy() ||
							!cityObject.getBoundedBy().isSetEnvelope() ||
							boundingBoxFilter.filter(cityObject.getBoundedBy().getEnvelope()))
						return false;
				}

				if (projectionFilter.filter(GMLModuleType.CORE, "boundedBy"))
					cityObject.unsetBoundedBy();

				// gml:id
				String gmlId = rs.getString(1);
				if (gmlId != null)
					cityObject.setId(gmlId);

				// gml:name
				if (projectionFilter.pass(GMLModuleType.CORE, "name")) {
					String gmlName = rs.getString(2);
					String gmlNameCodespace = rs.getString(3);
					if (gmlName != null)
						cityObject.setName(Util.string2codeList(gmlName, gmlNameCodespace));
				}

				// gml:description
				if (projectionFilter.pass(GMLModuleType.CORE, "description")) {
					String description = rs.getString(4);
					if (description != null) {
						StringOrRef stringOrRef = new StringOrRef();
						stringOrRef.setValue(description);
						cityObject.setDescription(stringOrRef);
					}
				}

				// creationDate
				if (projectionFilter.pass(CityGMLModuleType.CORE, "creationDate")) {
					Timestamp creationDate = rs.getTimestamp(6);
					if (creationDate != null) {
						GregorianCalendar gregDate = new GregorianCalendar();
						gregDate.setTime(creationDate);
						cityObject.setCreationDate(gregDate);
					}
				}

				// terminationDate
				if (projectionFilter.pass(CityGMLModuleType.CORE, "terminationDate")) {
					Timestamp terminationDate = rs.getTimestamp(7);
					if (terminationDate != null) {
						GregorianCalendar gregDate = new GregorianCalendar();
						gregDate.setTime(terminationDate);
						cityObject.setTerminationDate(gregDate);
					}
				}

				// relativeToTerrain
				if (projectionFilter.pass(CityGMLModuleType.CORE, "relativeToTerrain")) {
					String relativeToTerrain = rs.getString(8);
					if (relativeToTerrain != null)
						cityObject.setRelativeToTerrain(RelativeToTerrain.fromValue(relativeToTerrain));
				}

				// relativeToWater
				if (projectionFilter.pass(CityGMLModuleType.CORE, "relativeToWater")) {
					String relativeToWater = rs.getString(9);
					if (relativeToWater != null)
						cityObject.setRelativeToWater(RelativeToWater.fromValue(relativeToWater));
				}

				do {
					// generalizesTo
					if (projectionFilter.pass(CityGMLModuleType.CORE, "generalizesTo")) {
						long generalizesTo = rs.getLong(14);
						if (!rs.wasNull())
							generalizesToSet.add(generalizesTo);
					}

					// externalReference
					if (projectionFilter.pass(CityGMLModuleType.CORE, "externalReference")) {
						long externalReferenceId = rs.getLong(10);
						if (!rs.wasNull() && !externalReferenceSet.contains(externalReferenceId)) {
							externalReferenceSet.add(externalReferenceId);

							ExternalReference externalReference = new ExternalReference();
							ExternalObject externalObject = new ExternalObject();

							String infoSys = rs.getString(11);
							if (infoSys != null)
								externalReference.setInformationSystem(infoSys);

							String name = rs.getString(12);
							String uri = rs.getString(13);

							if (name != null || uri != null) {
								if (name != null)
									externalObject.setName(name);

								if (uri != null)
									externalObject.setUri(uri);
							} else if (name == null && uri == null) {
								externalObject.setUri("");
							}

							externalReference.setExternalObject(externalObject);
							cityObject.addExternalReference(externalReference);
						}
					}

				} while (rs.next());

				generalizesToSet.clear();
				externalReferenceSet.clear();

				if (isTopLevelObject && setTileInfoAsGenericAttribute) {
					String value;

					double minX = activeTile.getLowerCorner().getX();
					double minY = activeTile.getLowerCorner().getY();
					double maxX = activeTile.getUpperCorner().getX();
					double maxY = activeTile.getUpperCorner().getY();

					switch (tiling.getGenericAttributeValue()) {
					case XMIN_YMIN:
						value = String.valueOf(minX) + ' ' + String.valueOf(minY);
						break;
					case XMAX_YMIN:
						value = String.valueOf(maxX) + ' ' + String.valueOf(minY);
						break;
					case XMIN_YMAX:
						value = String.valueOf(minX) + ' ' + String.valueOf(maxY);
						break;
					case XMAX_YMAX:
						value = String.valueOf(maxX) + ' ' + String.valueOf(maxY);
						break;
					case XMIN_YMIN_XMAX_YMAX:
						value = String.valueOf(minX) + ' ' + String.valueOf(minY) + ' ' + String.valueOf(maxX) + ' ' + String.valueOf(maxY);
						break;
					default:
						value = String.valueOf(boundingBoxFilter.getTileRow()) + ' ' + String.valueOf(boundingBoxFilter.getTileColumn());
					} 

					StringAttribute genericStringAttrib = new StringAttribute();
					genericStringAttrib.setName("TILE");
					genericStringAttrib.setValue(value);
					cityObject.addGenericAttribute(genericStringAttrib);
				}

				// generalizesTo relation
				if (!generalizesToSet.isEmpty())
					generalizesToExporter.read(cityObject, parentId, generalizesToSet);

				// generic attributes
				genericAttributeExporter.read(cityObject, parentId, projectionFilter);

				// get appearance information associated with the cityobject
				if (exportAppearance) {
					if (isTopLevelObject)
						appearanceExporter.clearLocalCache();

					if (projectionFilter.pass(CityGMLModuleType.APPEARANCE, "appearance"))
						appearanceExporter.read(cityObject, parentId);
				}

				// update feature counter
				dbExporterManager.updateFeatureCounter(cityObject.getCityGMLClass());
			}

			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psCityObject.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.CITYOBJECT;
	}
}
