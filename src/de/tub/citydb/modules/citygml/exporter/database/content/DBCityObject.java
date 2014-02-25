/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashSet;

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

import de.tub.citydb.api.geometry.BoundingBox;
import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.Tiling;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.BoundingBoxFilter;
import de.tub.citydb.util.Util;

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
	private boolean useInternalBBoxFilter;
	private boolean useTiling;
	private boolean setTileInfoAsGenericAttribute;
	private BoundingBoxFilter boundingBoxFilter;
	private BoundingBox activeTile;
	private Tiling tiling;

	private HashSet<Long> generalizesToSet;
	private HashSet<Long> externalReferenceSet;

	public DBCityObject(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.config = config;
		this.connection = connection;
		this.boundingBoxFilter = exportFilter.getBoundingBoxFilter();

		init();
	}

	private void init() throws SQLException {
		exportAppearance = config.getProject().getExporter().getAppearances().isSetExportAppearance();
		useInternalBBoxFilter = config.getInternal().isUseInternalBBoxFilter();

		tiling = config.getProject().getExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling();
		useTiling = boundingBoxFilter.isActive() && tiling.getMode() != TilingMode.NO_TILING;
		setTileInfoAsGenericAttribute = useTiling && tiling.isIncludeTileAsGenericAttribute();
		if (setTileInfoAsGenericAttribute)
			activeTile = boundingBoxFilter.getFilterState();

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
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("geodb_util.transform_or_null");

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
		return read(cityObject, parentId, false);
	}

	public boolean read(AbstractCityObject cityObject, long parentId, boolean isTopLevelObject) throws SQLException {
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
				if (isTopLevelObject && (useInternalBBoxFilter || useTiling)) {
					if (!cityObject.isSetBoundedBy() ||
							!cityObject.getBoundedBy().isSetEnvelope() ||
							boundingBoxFilter.filter(cityObject.getBoundedBy().getEnvelope()))
						return false;
				}

				// gml:id
				String gmlId = rs.getString(1);
				if (gmlId != null)
					cityObject.setId(gmlId);

				// gml:name
				String gmlName = rs.getString(2);
				String gmlNameCodespace = rs.getString(3);
				if (gmlName != null)
					cityObject.setName(Util.string2codeList(gmlName, gmlNameCodespace));
				
				// gml:description
				String description = rs.getString(4);
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRef();
					stringOrRef.setValue(description);
					cityObject.setDescription(stringOrRef);
				}

				// creationDate
				Date creationDate = rs.getDate(6);
				if (creationDate != null) {
					GregorianCalendar gregDate = new GregorianCalendar();
					gregDate.setTime(creationDate);
					cityObject.setCreationDate(gregDate);
				}

				// terminationDate
				Date terminationDate = rs.getDate(7);
				if (terminationDate != null) {
					GregorianCalendar gregDate = new GregorianCalendar();
					gregDate.setTime(terminationDate);
					cityObject.setTerminationDate(gregDate);
				}
				
				// relativeToTerrain
				String relativeToTerrain = rs.getString(8);
				if (relativeToTerrain != null)
					cityObject.setRelativeToTerrain(RelativeToTerrain.fromValue(relativeToTerrain));

				// relativeToWater
				String relativeToWater = rs.getString(9);
				if (relativeToWater != null)
					cityObject.setRelativeToWater(RelativeToWater.fromValue(relativeToWater));
				
				do {
					// generalizesTo
					long generalizesTo = rs.getLong(14);
					if (!rs.wasNull())
						generalizesToSet.add(generalizesTo);

					// externalReference
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

				} while (rs.next());
				
				generalizesToSet.clear();
				externalReferenceSet.clear();

				if (isTopLevelObject && setTileInfoAsGenericAttribute) {
					String value;

					double minX = activeTile.getLowerLeftCorner().getX();
					double minY = activeTile.getLowerLeftCorner().getY();
					double maxX = activeTile.getUpperRightCorner().getX();
					double maxY = activeTile.getUpperRightCorner().getY();

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
				genericAttributeExporter.read(cityObject, parentId);
				
				// get appearance information associated with the cityobject
				if (exportAppearance) {
					if (isTopLevelObject)
						appearanceExporter.clearLocalCache();

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
