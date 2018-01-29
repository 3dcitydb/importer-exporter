/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.citydb.database.schema.TableEnum;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.citygml.generics.DateAttribute;
import org.citygml4j.model.citygml.generics.DoubleAttribute;
import org.citygml4j.model.citygml.generics.GenericAttributeSet;
import org.citygml4j.model.citygml.generics.IntAttribute;
import org.citygml4j.model.citygml.generics.MeasureAttribute;
import org.citygml4j.model.citygml.generics.StringAttribute;
import org.citygml4j.model.citygml.generics.UriAttribute;
import org.citygml4j.model.gml.basicTypes.Measure;

import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;

public class DBCityObjectGenericAttrib implements DBExporter {
	private PreparedStatement ps;
	private HashMap<Long, GenericAttributeSet> attributeSets;

	public DBCityObjectGenericAttrib(Connection connection, CityGMLExportManager exporter) throws SQLException {
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		Table table = new Table(TableEnum.CITYOBJECT_GENERICATTRIB.getName(), schema);
		Select select = new Select();

		select.addProjection(table.getColumn("id"), table.getColumn("parent_genattrib_id"),
				table.getColumn("attrname"), table.getColumn("datatype"), table.getColumn("strval"), table.getColumn("intval"), table.getColumn("realval"),
				table.getColumn("urival"), table.getColumn("dateval"), table.getColumn("unit"), table.getColumn("genattribset_codespace"))
		.addSelection(ComparisonFactory.equalTo(table.getColumn("cityobject_id"), new PlaceHolder<>()));
		ps = connection.prepareStatement(select.toString());

		attributeSets = new HashMap<Long, GenericAttributeSet>();
	}

	protected void doExport(AbstractCityObject cityObject, long cityObjectId, ProjectionFilter projectionFilter) throws SQLException {
		ps.setLong(1, cityObjectId);

		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				long id = rs.getLong(1);
				long parentId = rs.getLong(2);
				String attrName = rs.getString(3);
				CityGMLClass attrType = Util.genericAttributeType2cityGMLClass(rs.getInt(4));

				// skip attribute if it is not covered by the projection filter
				if (!projectionFilter.containsGenericAttribute(attrName, attrType))
					continue;

				AbstractGenericAttribute genericAttribute = null;
				GenericAttributeSet parentAttributeSet = null;

				if (parentId != 0) {
					parentAttributeSet = attributeSets.get(parentId);
					if (parentAttributeSet == null) {
						parentAttributeSet = new GenericAttributeSet();
						attributeSets.put(parentId, parentAttributeSet);
					}
				}

				switch (attrType) {
				case STRING_ATTRIBUTE:
					String strVal = rs.getString(5);
					if (!rs.wasNull()) {
						genericAttribute = new StringAttribute();
						((StringAttribute)genericAttribute).setValue(strVal);
					}
					break;
				case INT_ATTRIBUTE:
					int intVal = rs.getInt(6);
					if (!rs.wasNull()) {
						genericAttribute = new IntAttribute();
						((IntAttribute)genericAttribute).setValue(intVal);
					}
					break;
				case DOUBLE_ATTRIBUTE:
					double realVal = rs.getDouble(7);
					if (!rs.wasNull()) {							
						genericAttribute = new DoubleAttribute();
						((DoubleAttribute)genericAttribute).setValue(realVal);
					}
					break;
				case URI_ATTRIBUTE:
					String uriVal = rs.getString(8);
					if (!rs.wasNull()) {
						genericAttribute = new UriAttribute();
						((UriAttribute)genericAttribute).setValue(uriVal);
					}
					break;
				case DATE_ATTRIBUTE:
					Timestamp dateVal = rs.getTimestamp(9);
					if (!rs.wasNull()) {
						genericAttribute = new DateAttribute();
						GregorianCalendar calendar = new GregorianCalendar();
						calendar.setTime(dateVal);	
						((DateAttribute)genericAttribute).setValue(calendar);
					}
					break;
				case MEASURE_ATTRIBUTE:
					Double measureVal = rs.getDouble(7);
					if (!rs.wasNull()) {
						genericAttribute = new MeasureAttribute();
						Measure measure = new Measure();
						measure.setValue(measureVal);
						measure.setUom(rs.getString(10));
						((MeasureAttribute)genericAttribute).setValue(measure);
					}
					break;
				case GENERIC_ATTRIBUTE_SET:
					genericAttribute = attributeSets.get(id);
					if (genericAttribute == null) {
						genericAttribute = new GenericAttributeSet();
						attributeSets.put(id, (GenericAttributeSet)genericAttribute);
					}

					((GenericAttributeSet)genericAttribute).setCodeSpace(rs.getString(11));
					break;
				default:
					continue;
				}

				if (genericAttribute != null) {
					genericAttribute.setName(attrName);

					// assign generic attribute to city object or parent attribute set
					if (parentAttributeSet == null)
						cityObject.addGenericAttribute(genericAttribute);
					else
						parentAttributeSet.addGenericAttribute(genericAttribute);
				}
			}

		} finally {
			attributeSets.clear();
		}
	}

	@Override
	public void close() throws SQLException {
		ps.close();
	}

}
