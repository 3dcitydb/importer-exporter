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
package org.citydb.operation.exporter.database.content;

import org.citydb.database.schema.TableEnum;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class DBCityObjectGenericAttrib implements DBExporter {
    private final Connection connection;
    private final CityGMLExportManager exporter;
    private PreparedStatement ps;

    public DBCityObjectGenericAttrib(Connection connection, CityGMLExportManager exporter) {
        this.connection = connection;
        this.exporter = exporter;
    }

    protected Select addProjection(Select select, Table table, String prefix) {
        select.addProjection(table.getColumn("id", prefix + "id"), table.getColumn("parent_genattrib_id", prefix + "parent_genattrib_id"),
                table.getColumn("attrname", prefix + "attrname"), table.getColumn("datatype", prefix + "datatype"),
                table.getColumn("strval", prefix + "strval"), table.getColumn("intval", prefix + "intval"),
                table.getColumn("realval", prefix + "realval"), table.getColumn("urival", prefix + "urival"),
                table.getColumn("dateval", prefix + "dateval"), table.getColumn("unit", prefix + "unit"),
                table.getColumn("genattribset_codespace", prefix + "genattribset_codespace"));

        return select;
    }

    protected void doExport(AbstractCityObject cityObject, long cityObjectId, ProjectionFilter projectionFilter) throws SQLException {
        if (ps == null) {
            String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
            Table table = new Table(TableEnum.CITYOBJECT_GENERICATTRIB.getName(), schema);
            Select select = addProjection(new Select(), table, "");
            ps = connection.prepareStatement(select.toString());
        }

        ps.setLong(1, cityObjectId);

        try (ResultSet rs = ps.executeQuery()) {
            Map<Long, GenericAttributeSet> attributeSets = new HashMap<>();
            while (rs.next())
                doExport(rs.getLong(1), cityObject, projectionFilter, "", attributeSets, rs);
        }
    }

    protected void doExport(long id, AbstractCityObject cityObject, ProjectionFilter projectionFilter, String prefix, Map<Long, GenericAttributeSet> attributeSets, ResultSet rs) throws SQLException {
        long parentId = rs.getLong(prefix + "parent_genattrib_id");
        String name = rs.getString(prefix + "attrname");
        CityGMLClass type = Util.genericAttributeType2cityGMLClass(rs.getInt(prefix + "datatype"));

        // skip attribute if it is not covered by the projection filter
        if (!projectionFilter.containsGenericAttribute(name, type))
            return;

        AbstractGenericAttribute genericAttribute = null;
        GenericAttributeSet parentAttributeSet = null;

        if (parentId != 0) {
            parentAttributeSet = attributeSets.get(parentId);
            if (parentAttributeSet == null) {
                parentAttributeSet = new GenericAttributeSet();
                attributeSets.put(parentId, parentAttributeSet);
            }
        }

        switch (type) {
            case STRING_ATTRIBUTE:
                String strVal = rs.getString(prefix + "strval");
                if (!rs.wasNull()) {
                    genericAttribute = new StringAttribute();
                    ((StringAttribute) genericAttribute).setValue(strVal);
                }
                break;
            case INT_ATTRIBUTE:
                int intVal = rs.getInt(prefix + "intval");
                if (!rs.wasNull()) {
                    genericAttribute = new IntAttribute();
                    ((IntAttribute) genericAttribute).setValue(intVal);
                }
                break;
            case DOUBLE_ATTRIBUTE:
                double realVal = rs.getDouble(prefix + "realval");
                if (!rs.wasNull()) {
                    genericAttribute = new DoubleAttribute();
                    ((DoubleAttribute) genericAttribute).setValue(realVal);
                }
                break;
            case URI_ATTRIBUTE:
                String uriVal = rs.getString(prefix + "urival");
                if (!rs.wasNull()) {
                    genericAttribute = new UriAttribute();
                    ((UriAttribute) genericAttribute).setValue(uriVal);
                }
                break;
            case DATE_ATTRIBUTE:
                Timestamp dateVal = rs.getTimestamp(prefix + "dateval");
                if (!rs.wasNull()) {
                    genericAttribute = new DateAttribute();
                    ((DateAttribute) genericAttribute).setValue(dateVal.toLocalDateTime().toLocalDate());
                }
                break;
            case MEASURE_ATTRIBUTE:
                double measureVal = rs.getDouble(prefix + "realval");
                if (!rs.wasNull()) {
                    genericAttribute = new MeasureAttribute();
                    Measure measure = new Measure();
                    measure.setValue(measureVal);
                    measure.setUom(rs.getString(prefix + "unit"));
                    ((MeasureAttribute) genericAttribute).setValue(measure);
                }
                break;
            case GENERIC_ATTRIBUTE_SET:
                genericAttribute = attributeSets.get(id);
                if (genericAttribute == null) {
                    genericAttribute = new GenericAttributeSet();
                    attributeSets.put(id, (GenericAttributeSet) genericAttribute);
                }

                ((GenericAttributeSet) genericAttribute).setCodeSpace(rs.getString(prefix + "genattribset_codespace"));
                break;
        }

        if (genericAttribute != null) {
            genericAttribute.setName(name);

            // assign generic attribute to city object or parent attribute set
            if (parentAttributeSet == null)
                cityObject.addGenericAttribute(genericAttribute);
            else
                parentAttributeSet.addGenericAttribute(genericAttribute);
        }
    }

    @Override
    public void close() throws SQLException {
        if (ps != null)
            ps.close();
    }

}
