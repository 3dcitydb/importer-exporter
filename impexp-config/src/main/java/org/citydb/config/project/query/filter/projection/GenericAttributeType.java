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
package org.citydb.config.project.query.filter.projection;

import org.citygml4j.model.citygml.CityGMLClass;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="GenericAttributeTypeType")
@XmlEnum
public enum GenericAttributeType {
	@XmlEnumValue("stringAttribute")
	STRING_ATTRIBUTE("stringAttribute", CityGMLClass.STRING_ATTRIBUTE),
	@XmlEnumValue("intAttribute")
	INT_ATTRIBUTE("intAttribute", CityGMLClass.INT_ATTRIBUTE),
	@XmlEnumValue("doubleAttribute")
	DOUBLE_ATTRIBUTE("doubleAttribute", CityGMLClass.DOUBLE_ATTRIBUTE),
	@XmlEnumValue("dateAttribute")
	DATE_ATTRIBUTE("dateAttribute", CityGMLClass.DATE_ATTRIBUTE),
	@XmlEnumValue("uriAttribute")
	URI_ATTRIBUTE("uriAttribute", CityGMLClass.URI_ATTRIBUTE),
	@XmlEnumValue("measureAttribute")
	MEASURE_ATTRIBUTE("measureAttribute", CityGMLClass.MEASURE_ATTRIBUTE),
	@XmlEnumValue("genericAttributeSet")
	GENERIC_ATTRIBUTE_SET("genericAttributeSet", CityGMLClass.GENERIC_ATTRIBUTE_SET);
	
	private final String value;
	private final CityGMLClass featureClass;
	
	private GenericAttributeType(String v, CityGMLClass featureClass) {
		this.value = v;
		this.featureClass = featureClass;
	}
	
	public String value() {
        return value;
    }
	
	public CityGMLClass getCityGMLClass() {
		return featureClass;
	}
	
	public static GenericAttributeType fromValue(String v) {
        for (GenericAttributeType c: GenericAttributeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException();
    }
	
}