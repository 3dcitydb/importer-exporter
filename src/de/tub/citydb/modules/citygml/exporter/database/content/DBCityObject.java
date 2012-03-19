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
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashSet;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.geometry.Point;
import org.citygml4j.impl.citygml.core.ExternalObjectImpl;
import org.citygml4j.impl.citygml.core.ExternalReferenceImpl;
import org.citygml4j.impl.citygml.generics.DateAttributeImpl;
import org.citygml4j.impl.citygml.generics.DoubleAttributeImpl;
import org.citygml4j.impl.citygml.generics.IntAttributeImpl;
import org.citygml4j.impl.citygml.generics.StringAttributeImpl;
import org.citygml4j.impl.citygml.generics.UriAttributeImpl;
import org.citygml4j.impl.gml.feature.BoundingShapeImpl;
import org.citygml4j.impl.gml.geometry.primitives.EnvelopeImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.citygml.generics.DateAttribute;
import org.citygml4j.model.citygml.generics.DoubleAttribute;
import org.citygml4j.model.citygml.generics.IntAttribute;
import org.citygml4j.model.citygml.generics.StringAttribute;
import org.citygml4j.model.citygml.generics.UriAttribute;
import org.citygml4j.model.gml.feature.BoundingShape;
import org.citygml4j.model.gml.geometry.primitives.Envelope;

import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.Tiling;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.BoundingBoxFilter;

public class DBCityObject implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psCityObject;

	private DBAppearance appearanceExporter;
	private DBGeneralization generalizesToExporter;
	private String gmlSrsName;
	private boolean exportAppearance;
	private boolean useInternalBBoxFilter;
	private boolean useTiling;
	private boolean setTileInfoAsGenericAttribute;
	private boolean transformCoords;
	private BoundingBoxFilter boundingBoxFilter;
	private BoundingBox activeTile;
	private Tiling tiling;

	private HashSet<Long> generalizesToSet;
	private HashSet<Long> externalReferenceSet;
	private HashSet<Long> genericAttributeSet;

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
		genericAttributeSet = new HashSet<Long>();

		transformCoords = config.getInternal().isTransformCoordinates();
		gmlSrsName = config.getInternal().getExportTargetSRS().getGMLSrsName();
		if (!transformCoords) {		
			psCityObject = connection.prepareStatement("select co.GMLID, co.ENVELOPE, co.CREATION_DATE, co.TERMINATION_DATE, ex.ID as EXID, ex.INFOSYS, ex.NAME, ex.URI, " +
					"ga.ID as GAID, ga.ATTRNAME, ga.DATATYPE, ga.STRVAL, ga.INTVAL, ga.REALVAL, ga.URIVAL, ga.DATEVAL, ge.GENERALIZES_TO_ID " +
					"from CITYOBJECT co left join EXTERNAL_REFERENCE ex on co.ID = ex.CITYOBJECT_ID " +
					"left join CITYOBJECT_GENERICATTRIB ga on co.ID = ga.CITYOBJECT_ID " +
			"left join GENERALIZATION ge on ge.CITYOBJECT_ID=co.ID where co.ID = ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();

			psCityObject = connection.prepareStatement("select co.GMLID, " +
					"geodb_util.transform_or_null(co.ENVELOPE, " + srid + ") AS ENVELOPE, " +
					"co.CREATION_DATE, co.TERMINATION_DATE, ex.ID as EXID, ex.INFOSYS, ex.NAME, ex.URI, " +
					"ga.ID as GAID, ga.ATTRNAME, ga.DATATYPE, ga.STRVAL, ga.INTVAL, ga.REALVAL, ga.URIVAL, ga.DATEVAL, ge.GENERALIZES_TO_ID " +
					"from CITYOBJECT co left join EXTERNAL_REFERENCE ex on co.ID = ex.CITYOBJECT_ID " +
					"left join CITYOBJECT_GENERICATTRIB ga on co.ID = ga.CITYOBJECT_ID " +
			"left join GENERALIZATION ge on ge.CITYOBJECT_ID=co.ID where co.ID = ?");
		}

		appearanceExporter = (DBAppearance)dbExporterManager.getDBExporter(DBExporterEnum.APPEARANCE);
		generalizesToExporter = (DBGeneralization)dbExporterManager.getDBExporter(DBExporterEnum.GENERALIZATION);
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
				generalizesToSet.clear();
				externalReferenceSet.clear();
				genericAttributeSet.clear();

				// boundedBy
				STRUCT struct = (STRUCT)rs.getObject("ENVELOPE");
				if (!rs.wasNull() && struct != null) {
					JGeometry jGeom = JGeometry.load(struct);
					double[] points = jGeom.getMBR();
					
					Envelope env = new EnvelopeImpl();
					Point lower = null;
					Point upper = null;

					if (jGeom.getDimensions() == 2) {
						lower = new Point(points[0], points[1], 0);
						upper = new Point(points[2], points[3], 0);
					} else {					
						lower = new Point(points[0], points[1], points[2]);
						upper = new Point(points[3], points[4], points[5]);
					}

					env.setLowerCorner(lower);
					env.setUpperCorner(upper);
					env.setSrsDimension(3);
					env.setSrsName(gmlSrsName);

					BoundingShape boundedBy = new BoundingShapeImpl();
					boundedBy.setEnvelope(env);
					cityObject.setBoundedBy(boundedBy);
				}

				// check bounding volume filter
				if (isTopLevelObject && (useInternalBBoxFilter || useTiling)) {
					if (!cityObject.isSetBoundedBy() ||
							!cityObject.getBoundedBy().isSetEnvelope() ||
							boundingBoxFilter.filter(cityObject.getBoundedBy().getEnvelope()))
						return false;
				}

				String gmlId = rs.getString("GMLID");
				if (gmlId != null)
					cityObject.setId(gmlId);

				// creationDate
				Date creationDate = rs.getDate("CREATION_DATE");
				if (creationDate != null) {
					GregorianCalendar gregDate = new GregorianCalendar();
					gregDate.setTime(creationDate);
					cityObject.setCreationDate(gregDate);
				}

				// terminationDate
				Date terminationDate = rs.getDate("TERMINATION_DATE");
				if (terminationDate != null) {
					GregorianCalendar gregDate = new GregorianCalendar();
					gregDate.setTime(terminationDate);
					cityObject.setTerminationDate(gregDate);
				}

				do {
					// generalizesTo
					long generalizesTo = rs.getLong("GENERALIZES_TO_ID");
					if (!rs.wasNull())
						generalizesToSet.add(generalizesTo);

					// externalReference
					long externalReferenceId = rs.getLong("EXID");
					if (!rs.wasNull() && !externalReferenceSet.contains(externalReferenceId)) {
						externalReferenceSet.add(externalReferenceId);

						ExternalReference externalReference = new ExternalReferenceImpl();
						ExternalObject externalObject = new ExternalObjectImpl();

						String infoSys = rs.getString("INFOSYS");
						if (infoSys != null)
							externalReference.setInformationSystem(infoSys);

						String name = rs.getString("NAME");
						String uri = rs.getString("URI");

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

					// generic attributes
					long genericAttribId = rs.getLong("GAID");
					if (!rs.wasNull() && !genericAttributeSet.contains(genericAttribId)) {
						genericAttributeSet.add(genericAttribId);

						AbstractGenericAttribute genericAttrib = null;
						String attrName = rs.getString("ATTRNAME");
						int dataType = rs.getInt("DATATYPE");

						switch (dataType) {
						case 1:
							String strVal = rs.getString("STRVAL");
							genericAttrib = new StringAttributeImpl();
							((StringAttribute)genericAttrib).setValue(strVal);
							break;
						case 2:
							Integer intVal = rs.getInt("INTVAL");
							if (!rs.wasNull()) {
								genericAttrib = new IntAttributeImpl();
								((IntAttribute)genericAttrib).setValue(intVal);
							}
							break;
						case 3:
							Double realVal = rs.getDouble("REALVAL");
							if (!rs.wasNull()) {							
								genericAttrib = new DoubleAttributeImpl();
								((DoubleAttribute)genericAttrib).setValue(realVal);
							}
							break;
						case 4:
							String uriVal = rs.getString("URIVAL");
							genericAttrib = new UriAttributeImpl();
							((UriAttribute)genericAttrib).setValue(uriVal);
							break;
						case 5:
							Date dateVal = rs.getDate("DATEVAL");
							if (dateVal != null) {
								genericAttrib = new DateAttributeImpl();
								GregorianCalendar gregDate = new GregorianCalendar();
								gregDate.setTime(dateVal);	
								((DateAttribute)genericAttrib).setValue(gregDate);
							}
							break;
						}

						if (genericAttrib != null) {
							genericAttrib.setName(attrName);
							cityObject.addGenericAttribute(genericAttrib);
						}
					}

				} while (rs.next());

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

					StringAttribute genericStringAttrib = new StringAttributeImpl();
					genericStringAttrib.setName("TILE");
					genericStringAttrib.setValue(value);
					cityObject.addGenericAttribute(genericStringAttrib);
				}

				// generalizesTo relation
				if (!generalizesToSet.isEmpty())
					generalizesToExporter.read(cityObject, parentId, generalizesToSet);

				// get appearance information associated with the cityobject
				if (exportAppearance)
					appearanceExporter.read(cityObject, parentId);

				if (cityObject.getCityGMLClass() != CityGMLClass.CITY_OBJECT_GROUP)
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
