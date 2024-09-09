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
package org.citydb.config.project.database;

import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;
import org.citygml4j.model.module.citygml.CoreModule;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlType(name = "DatabaseOperationType", propOrder = {
        "lastUsed",
        "boundingBoxTypeName",
        "boundingBoxSrs",
        "spatialIndex",
        "normalIndex",
        "featureVersionFilter"
})
public class DatabaseOperation {
    private DatabaseOperationType lastUsed = DatabaseOperationType.REPORT;
    private QName boundingBoxTypeName = new QName(CoreModule.v2_0_0.getNamespaceURI(), "_CityObject");
    @XmlIDREF
    private DatabaseSrs boundingBoxSrs = DatabaseSrs.createDefaultSrs();
    private boolean spatialIndex;
    private boolean normalIndex;
    @XmlAttribute
    private boolean useFeatureVersionFilter = true;
    @XmlElement(name = "featureVersion")
    private SimpleFeatureVersionFilter featureVersionFilter;

    public DatabaseOperation() {
        featureVersionFilter = new SimpleFeatureVersionFilter();
    }

    public DatabaseOperationType lastUsed() {
        return lastUsed;
    }

    public void setLastUsed(DatabaseOperationType mode) {
        this.lastUsed = mode;
    }

    public QName getBoundingBoxTypeName() {
        return boundingBoxTypeName;
    }

    public void setBoundingBoxTypeName(QName boundingBoxTypeName) {
        this.boundingBoxTypeName = boundingBoxTypeName;
    }

    public DatabaseSrs getBoundingBoxSrs() {
        return boundingBoxSrs;
    }

    public void setBoundingBoxSrs(DatabaseSrs boundingBoxSrs) {
        this.boundingBoxSrs = boundingBoxSrs;
    }

    public boolean isSetSpatialIndex() {
        return spatialIndex;
    }

    public void setSpatialIndex(boolean spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    public boolean isSetNormalIndex() {
        return normalIndex;
    }

    public void setNormalIndex(boolean normalIndex) {
        this.normalIndex = normalIndex;
    }

    public boolean isUseFeatureVersionFilter() {
        return useFeatureVersionFilter;
    }

    public void setUseFeatureVersionFilter(boolean useFeatureVersionFilter) {
        this.useFeatureVersionFilter = useFeatureVersionFilter;
    }

    public SimpleFeatureVersionFilter getFeatureVersionFilter() {
        return featureVersionFilter;
    }

    public boolean isSetFeatureVersionFilter() {
        return featureVersionFilter != null;
    }

    public void setFeatureVersionFilter(SimpleFeatureVersionFilter featureVersionFilter) {
        if (featureVersionFilter != null) {
            this.featureVersionFilter = featureVersionFilter;
        }
    }
}
