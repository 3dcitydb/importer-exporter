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

package org.citydb.config.project.query.simple;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.comparison.GreaterThanOperator;
import org.citydb.config.project.query.filter.selection.comparison.LessThanOrEqualToOperator;
import org.citydb.config.project.query.filter.selection.comparison.NullOperator;
import org.citydb.config.project.query.filter.selection.logical.AndOperator;
import org.citydb.config.project.query.filter.selection.logical.NotOperator;
import org.citydb.config.project.query.filter.selection.logical.OrOperator;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlType(name = "SimpleFeatureVersionFilterType", propOrder = {
        "startDate",
        "endDate"
})
public class SimpleFeatureVersionFilter {
    @XmlAttribute(required = true)
    private SimpleFeatureVersionFilterMode mode = SimpleFeatureVersionFilterMode.LATEST;
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar startDate;
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar endDate;

    public SimpleFeatureVersionFilterMode getMode() {
        return mode != null ? mode : SimpleFeatureVersionFilterMode.LATEST;
    }

    public void setMode(SimpleFeatureVersionFilterMode mode) {
        if (mode != null) {
            this.mode = mode;
        }
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

    public AbstractPredicate toPredicate() {
        if (mode == SimpleFeatureVersionFilterMode.LATEST) {
            return new NullOperator("core:terminationDate");
        } else if (mode == SimpleFeatureVersionFilterMode.TERMINATED) {
            return new NotOperator(new NullOperator("core:terminationDate"));
        } else if (startDate != null) {
            if (mode == SimpleFeatureVersionFilterMode.TERMINATED_AT) {
                return new LessThanOrEqualToOperator("core:terminationDate", startDate.toXMLFormat());
            } else if (mode == SimpleFeatureVersionFilterMode.AT
                    || (mode == SimpleFeatureVersionFilterMode.BETWEEN && endDate != null)) {
                XMLGregorianCalendar creationDate = mode == SimpleFeatureVersionFilterMode.AT ?
                        startDate :
                        endDate;

                return new AndOperator(
                        new LessThanOrEqualToOperator("core:creationDate", creationDate.toXMLFormat()),
                        new OrOperator(
                                new GreaterThanOperator("core:terminationDate", startDate.toXMLFormat()),
                                new NullOperator("core:terminationDate")
                        )
                );
            }
        }

        return null;
    }
}
