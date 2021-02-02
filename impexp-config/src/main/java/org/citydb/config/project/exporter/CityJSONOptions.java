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

package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CityJSONExportOptionsType", propOrder = {})
public class CityJSONOptions {
    @XmlElement(defaultValue = "false")
    private boolean prettyPrint;
    @XmlElement(defaultValue = "3")
    @XmlSchemaType(name = "positiveInteger")
    private int significantDigits = 3;
    @XmlElement(defaultValue = "7")
    @XmlSchemaType(name = "positiveInteger")
    private int significantTextureDigits = 7;
    @XmlElement(defaultValue = "true")
    private boolean useGeometryCompression = true;
    @XmlElement(defaultValue = "false")
    private boolean addSequenceIdWhenSorting;
    @XmlElement(defaultValue = "false")
    private boolean removeDuplicateChildGeometries;

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public int getSignificantDigits() {
        return significantDigits;
    }

    public void setSignificantDigits(int significantDigits) {
        if (significantDigits > 0) {
            this.significantDigits = significantDigits;
        }
    }

    public int getSignificantTextureDigits() {
        return significantTextureDigits;
    }

    public void setSignificantTextureDigits(int significantTextureDigits) {
        if (significantTextureDigits > 0) {
            this.significantTextureDigits = significantTextureDigits;
        }
    }

    public boolean isUseGeometryCompression() {
        return useGeometryCompression;
    }

    public void setUseGeometryCompression(boolean useGeometryCompression) {
        this.useGeometryCompression = useGeometryCompression;
    }

    public boolean isAddSequenceIdWhenSorting() {
        return addSequenceIdWhenSorting;
    }

    public void setAddSequenceIdWhenSorting(boolean addSequenceIdWhenSorting) {
        this.addSequenceIdWhenSorting = addSequenceIdWhenSorting;
    }

    public boolean isRemoveDuplicateChildGeometries() {
        return removeDuplicateChildGeometries;
    }

    public void setRemoveDuplicateChildGeometries(boolean removeDuplicateChildGeometries) {
        this.removeDuplicateChildGeometries = removeDuplicateChildGeometries;
    }
}
