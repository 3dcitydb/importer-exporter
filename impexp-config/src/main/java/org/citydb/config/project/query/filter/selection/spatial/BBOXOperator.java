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
package org.citydb.config.project.query.filter.selection.spatial;

import org.citydb.config.geometry.BoundingBox;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "bbox")
@XmlType(name = "BBOXOperatorType", propOrder = {
        "operand"
})
public class BBOXOperator extends AbstractSpatialOperator {
    @XmlElements({
            @XmlElement(name = "operand", type = BoundingBox.class),
            @XmlElement(name = "envelope", type = BoundingBox.class)
    })
    private BoundingBox operand;

    public boolean isSetEnvelope() {
        return operand != null;
    }

    public BoundingBox getEnvelope() {
        return operand;
    }

    public void setEnvelope(BoundingBox operand) {
        this.operand = operand;
    }

    @Override
    public void reset() {
        operand = null;
        super.reset();
    }

    @Override
    public SpatialOperatorName getOperatorName() {
        return SpatialOperatorName.BBOX;
    }

}
