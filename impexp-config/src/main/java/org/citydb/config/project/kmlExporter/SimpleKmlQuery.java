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

import org.citydb.config.project.query.filter.selection.sql.SelectOperator;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.project.query.simple.SimpleAttributeFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "SimpleKmlExportQueryType", propOrder = {
        "featureTypeFilter",
        "featureVersionFilter",
        "attributeFilter",
        "sqlFilter",
        "bboxFilter"
})
public class SimpleKmlQuery {
    @XmlAttribute
    private boolean useTypeNames;
    @XmlAttribute
    private boolean useFeatureVersionFilter;
    @XmlAttribute
    private boolean useAttributeFilter;
    @XmlAttribute
    private boolean useSQLFilter;
    @XmlAttribute
    private boolean useBboxFilter;

    @XmlElement(name = "typeNames")
    protected FeatureTypeFilter featureTypeFilter;
    @XmlElement(name = "featureVersion")
    private SimpleFeatureVersionFilter featureVersionFilter;
    @XmlElement(name = "attributes")
    private SimpleAttributeFilter attributeFilter;
    @XmlElement(name = "sql")
    private SelectOperator sqlFilter;
    @XmlElement(name = "bbox", required = true)
    private KmlTiling bboxFilter;

    public SimpleKmlQuery() {
        featureTypeFilter = new FeatureTypeFilter();
        featureVersionFilter = new SimpleFeatureVersionFilter();
        attributeFilter = new SimpleAttributeFilter();
        sqlFilter = new SelectOperator();
        bboxFilter = new KmlTiling();
    }

    public boolean isUseTypeNames() {
        return useTypeNames;
    }

    public void setUseTypeNames(boolean useTypeNames) {
        this.useTypeNames = useTypeNames;
    }

    public boolean isUseFeatureVersionFilter() {
        return useFeatureVersionFilter;
    }

    public void setUseFeatureVersionFilter(boolean useFeatureVersionFilter) {
        this.useFeatureVersionFilter = useFeatureVersionFilter;
    }

    public boolean isUseAttributeFilter() {
        return useAttributeFilter;
    }

    public void setUseAttributeFilter(boolean useAttributeFilter) {
        this.useAttributeFilter = useAttributeFilter;
    }

    public boolean isUseSQLFilter() {
        return useSQLFilter;
    }

    public void setUseSQLFilter(boolean useSQLFilter) {
        this.useSQLFilter = useSQLFilter;
    }

    public boolean isUseBboxFilter() {
        return useBboxFilter;
    }

    public void setUseBboxFilter(boolean useBboxFilter) {
        this.useBboxFilter = useBboxFilter;
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

    public SimpleFeatureVersionFilter getFeatureVersionFilter() {
        return featureVersionFilter;
    }

    public boolean isSetFeatureVersionFilter() {
        return featureVersionFilter != null;
    }

    public void setFeatureVersionFilter(SimpleFeatureVersionFilter featureVersionFilter) {
        this.featureVersionFilter = featureVersionFilter;
    }

    public SimpleAttributeFilter getAttributeFilter() {
        return attributeFilter;
    }

    public boolean isSetAttributeFilter() {
        return attributeFilter != null;
    }

    public void setAttributeFilter(SimpleAttributeFilter attributeFilter) {
        this.attributeFilter = attributeFilter;
    }

    public SelectOperator getSQLFilter() {
        return sqlFilter;
    }

    public boolean isSetSQLFilter() {
        return sqlFilter != null;
    }

    public void setSQLFilter(SelectOperator sqlFilter) {
        this.sqlFilter = sqlFilter;
    }

    public KmlTiling getBboxFilter() {
        return bboxFilter;
    }

    public boolean isSetBboxFilter() {
        return bboxFilter != null;
    }

    public void setBboxFilter(KmlTiling bboxFilter) {
        this.bboxFilter = bboxFilter;
    }
}
