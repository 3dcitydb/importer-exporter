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
package org.citydb.config.project.database;

import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Date;

@XmlType(name = "WorkspaceType", propOrder = {
        "name",
        "timestamp"
})
public class Workspace {
    private String name;
    @XmlSchemaType(name = "date")
    private XMLGregorianCalendar timestamp;

    public Workspace() {
    }

    public Workspace(String name) {
        setName(name);
    }

    public Workspace(String name, Date timestamp) {
        this(name);
        setTimestamp(timestamp);
    }

    public Workspace(Workspace other) {
        name = other.name;
        timestamp = other.timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name != null && !name.trim().isEmpty()) ? name : null;
    }

    public boolean isSetName() {
        return name != null && !name.trim().isEmpty();
    }

    public Date getTimestamp() {
        return timestamp != null ? timestamp.toGregorianCalendar().getTime() : null;
    }

    public boolean isSetTimestamp() {
        return timestamp != null;
    }

    public void setTimestamp(Date timestamp) {
        if (timestamp != null) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                this.timestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(format.format(timestamp));
            } catch (DatatypeConfigurationException e) {
                this.timestamp = null;
            }
        } else {
            this.timestamp = null;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(isSetName() ? name : "");
        if (timestamp != null) {
            builder.append(" at timestamp ").append(timestamp.toXMLFormat());
        }

        return builder.toString();
    }

}
