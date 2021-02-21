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

import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(name = "SimpleAttributeFilterType", propOrder = {
        "resourceIdFilter",
        "nameFilter",
        "lineageFilter"
})
public class SimpleAttributeFilter {
    @XmlElement(name = "resourceIds")
    private ResourceIdOperator resourceIdFilter;
    @XmlElement(name = "name")
    private LikeOperator nameFilter;
    @XmlElement(name = "lineage")
    private LikeOperator lineageFilter;

    public SimpleAttributeFilter() {
        resourceIdFilter = new ResourceIdOperator();
        nameFilter = new LikeOperator();
        lineageFilter = new LikeOperator();
    }

    public ResourceIdOperator getResourceIdFilter() {
        return resourceIdFilter;
    }

    public boolean isSetResourceIdFilter() {
        return resourceIdFilter != null;
    }

    public void setResourceIdFilter(ResourceIdOperator resourceIdFilter) {
        this.resourceIdFilter = resourceIdFilter;
    }

    public LikeOperator getNameFilter() {
        return nameFilter;
    }

    public boolean isSetNameFilter() {
        return nameFilter != null;
    }

    public void setNameFilter(LikeOperator nameFilter) {
        this.nameFilter = nameFilter;
    }

    public LikeOperator getLineageFilter() {
        return lineageFilter;
    }

    public boolean isSetLineageFilter() {
        return lineageFilter != null;
    }

    public void setLineageFilter(LikeOperator lineageFilter) {
        this.lineageFilter = lineageFilter;
    }

    public List<AbstractPredicate> toPredicates() {
        List<AbstractPredicate> predicates = new ArrayList<>();

        // gml:id filter
        if (resourceIdFilter != null && resourceIdFilter.isSetResourceIds()) {
            predicates.add(resourceIdFilter);
        }

        // gml:name filter
        if (nameFilter != null && nameFilter.isSetLiteral()) {
            nameFilter.setValueReference("gml:name");
            predicates.add(nameFilter);
        }

        // citydb:lineage filter
        if (lineageFilter != null && lineageFilter.isSetLiteral()) {
            lineageFilter.setValueReference("citydb:lineage");
            predicates.add(lineageFilter);
        }

        return predicates;
    }
}
