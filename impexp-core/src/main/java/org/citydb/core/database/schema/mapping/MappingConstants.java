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
package org.citydb.core.database.schema.mapping;

public class MappingConstants {
    public static final String CITYDB_SCHEMA_NAMESPACE_URI = "http://www.3dcitydb.org/database/schema/1.0";

    public static final String TARGET_TABLE_TOKEN = "${target.table}";
    public static final String TARGET_OBJECTCLASS_ID_TOKEN = "${target.objectclass_id}";
    public static final String TARGET_ID_TOKEN = "${target.id}";

    public static final String ID = "id";
    public static final String ROOT_ID = "root_id";
    public static final String GMLID = "gmlid";
    public static final String ENVELOPE = "envelope";
    public static final String TERMINATION_DATE = "termination_date";
    public static final String LAST_MODIFICATION_DATE = "last_modification_date";
    public static final String UPDATING_PERSON = "updating_person";
    public static final String REASON_FOR_UPDATE = "reason_for_update";
    public static final String LINEAGE = "lineage";
    public static final String IS_XLINK = "is_xlink";
    public static final String OBJECTCLASS_ID = "objectclass_id";

    public static final String SURFACE_GEOMETRY = "surface_geometry";
    public static final String CITYOBJECT = "cityobject";
    public static final String GEOMETRY = "geometry";
    public static final String ADE_DEFAULT_XML_PREFIX = "ade";

    public static final String IMPLICIT_GEOMETRY_PATH = "ImplicitGeometry";
    public static final String IMPLICIT_GEOMETRY_TABLE = "implicit_geometry";
    public static final int IMPLICIT_GEOMETRY_OBJECTCLASS_ID = 59;
    public static final int SURFACE_GEOMETRY_OBJECTCLASS_ID = 0;
    public static final int APPEARANCE_OBJECTCLASS_ID = 50;
}
