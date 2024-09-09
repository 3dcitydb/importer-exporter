/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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
package org.citydb.core.database.schema;

public enum SequenceEnum {
    ADDRESS_ID_SEQ("ADDRESS_SEQ"),
    APPEARANCE_ID_SEQ("APPEARANCE_SEQ"),
    CITYOBJECT_ID_SEQ("CITYOBJECT_SEQ"),
    SURFACE_GEOMETRY_ID_SEQ("SURFACE_GEOMETRY_SEQ"),
    IMPLICIT_GEOMETRY_ID_SEQ("IMPLICIT_GEOMETRY_SEQ"),
    SURFACE_DATA_ID_SEQ("SURFACE_DATA_SEQ"),
    TEX_IMAGE_ID_SEQ("TEX_IMAGE_SEQ"),
    CITYOBJECT_GENERICATTRIB_ID_SEQ("CITYOBJECT_GENERICATT_SEQ"),
    EXTERNAL_REFERENCE_ID_SEQ("EXTERNAL_REF_SEQ"),
    RASTER_REL_GEORASTER_ID_SEQ("RASTER_REL_GEORASTER_SEQ");

    private String name;

    private SequenceEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
