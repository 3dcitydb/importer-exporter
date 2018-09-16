/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.citygml4j.model.module.citygml.CityGMLVersion;

@XmlEnum
@XmlType(name = "cityGMLContext")
public enum CityGMLContext {

	@XmlEnumValue("citygml-2.0")
    CITYGML_2_0(CityGMLVersion.v2_0_0, "citygml-2.0"),
    @XmlEnumValue("citygml-1.0")
    CITYGML_1_0(CityGMLVersion.v1_0_0, "citygml-1.0");
    
    private final CityGMLVersion version;
    private final String value;

    CityGMLContext(CityGMLVersion version, String value) {
        this.version = version;
        this.value = value;
    }

    public CityGMLVersion getCityGMLVersion() {
        return version;
    }

    public static CityGMLContext fromCityGMLVersion(CityGMLVersion version) {
		if (version == CityGMLVersion.v1_0_0)
			return CITYGML_1_0;
		else
			return CITYGML_2_0;
	}

	@Override
	public String toString() {
		return value;
	}

}
