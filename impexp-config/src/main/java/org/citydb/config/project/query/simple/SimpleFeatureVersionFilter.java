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

package org.citydb.config.project.query.simple;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlType(name="SimpleFeatureVersionFilterType", propOrder={
        "featureState",
        "startDate",
        "endDate"
})
public class SimpleFeatureVersionFilter {
    @XmlAttribute(required = true)
    private SimpleFeatureVersionFilterMode mode = SimpleFeatureVersionFilterMode.STATE;
    @XmlElement(name = "state")
    private FeatureState featureState = FeatureState.LATEST;
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar startDate;
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar endDate;

    public SimpleFeatureVersionFilterMode getMode() {
        return mode;
    }

    public void setMode(SimpleFeatureVersionFilterMode mode) {
        this.mode = mode;
    }

    public FeatureState getFeatureState() {
        return featureState;
    }

    public boolean isSetFeatureState() {
        return featureState != null;
    }

    public void setFeatureState(FeatureState featureState) {
        this.featureState = featureState;
    }

    public XMLGregorianCalendar getStartDate() {
        return startDate;
    }

    public boolean isSetStartDate() {
        return startDate != null;
    }

    public void setStartDate(XMLGregorianCalendar startDate) {
        this.startDate = startDate;
    }

    public XMLGregorianCalendar getEndDate() {
        return endDate;
    }

    public boolean isSetEndDate() {
        return endDate != null;
    }

    public void setEndDate(XMLGregorianCalendar endDate) {
        this.endDate = endDate;
    }
}
