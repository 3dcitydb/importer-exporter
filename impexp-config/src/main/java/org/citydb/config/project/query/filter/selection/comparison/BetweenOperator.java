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
package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "propertyIsBetween")
@XmlType(name = "BetweenOperatorType", propOrder = {
        "lowerBoundary",
        "upperBoundary"
})
public class BetweenOperator extends AbstractComparisonOperator {
    @XmlElement(required = true)
    private String lowerBoundary;
    @XmlElement(required = true)
    private String upperBoundary;

    public BetweenOperator() {
    }

    public BetweenOperator(String valueReference, String lowerBoundary, String upperBoundary) {
        super(valueReference);
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
    }

    public boolean isSetLowerBoundary() {
        return lowerBoundary != null;
    }

    public String getLowerBoundary() {
        return lowerBoundary;
    }

    public void setLowerBoundary(String lowerBoundary) {
        this.lowerBoundary = lowerBoundary;
    }

    public boolean isSetUpperBoundary() {
        return upperBoundary != null;
    }

    public String getUpperBoundary() {
        return upperBoundary;
    }

    public void setUpperBoundary(String upperBoundary) {
        this.upperBoundary = upperBoundary;
    }

    @Override
    public void reset() {
        lowerBoundary = null;
        upperBoundary = null;
        super.reset();
    }

    @Override
    public ComparisonOperatorName getOperatorName() {
        return ComparisonOperatorName.BETWEEN;
    }

}
