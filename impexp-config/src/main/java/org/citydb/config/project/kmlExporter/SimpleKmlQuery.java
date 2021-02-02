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
package org.citydb.config.project.kmlExporter;

import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "SimpleKmlExportQueryType", propOrder = {
        "featureTypeFilter",
        "resourceIdFilter",
        "spatialFilter"
})
public class SimpleKmlQuery {
    @XmlAttribute
    private boolean useTypeNames;
    @XmlAttribute
    private boolean useResourceIdFilter;
    @XmlAttribute
    private boolean useBboxFilter;

    @XmlElement(name = "typeNames")
    protected FeatureTypeFilter featureTypeFilter;
    @XmlElement(name = "resourceIds")
    private ResourceIdOperator resourceIdFilter;
    @XmlElement(name = "bbox", required = true)
    private KmlTiling spatialFilter;

    public SimpleKmlQuery() {
        featureTypeFilter = new FeatureTypeFilter();
        resourceIdFilter = new ResourceIdOperator();
        spatialFilter = new KmlTiling();
    }

    public boolean isUseTypeNames() {
        return useTypeNames;
    }

    public void setUseTypeNames(boolean useTypeNames) {
        this.useTypeNames = useTypeNames;
    }

    public FeatureTypeFilter getFeatureTypeFilter() {
        return featureTypeFilter;
    }

    public boolean isSetFeatureTypeFilter() {
        return featureTypeFilter != null;
    }

    public void setFeatureTypeFilter(FeatureTypeFilter featureTypeFilter) {
        this.featureTypeFilter = featureTypeFilter;
    }

    public boolean isUseResourceIdFilter() {
        return useResourceIdFilter;
    }

    public void setUseResourceIdFilter(boolean useResourceIdFilter) {
        this.useResourceIdFilter = useResourceIdFilter;
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

    public boolean isUseBboxFilter() {
        return useBboxFilter;
    }

    public void setUseBboxFilter(boolean useBboxFilter) {
        this.useBboxFilter = useBboxFilter;
    }

    public KmlTiling getSpatialFilter() {
        return spatialFilter;
    }

    public boolean isSetBboxFilter() {
        return spatialFilter != null;
    }

    public void setSpatialFilter(KmlTiling spatialFilter) {
        this.spatialFilter = spatialFilter;
    }
}
