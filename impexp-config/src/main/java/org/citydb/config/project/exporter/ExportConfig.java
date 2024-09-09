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
package org.citydb.config.project.exporter;

import org.citydb.config.project.common.AffineTransformation;
import org.citydb.config.project.common.Path;
import org.citydb.config.project.query.QueryConfig;
import org.citydb.config.project.resources.Resources;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "export")
@XmlType(name = "ExportType", propOrder = {
        "query",
        "simpleQuery",
        "path",
        "generalOptions",
        "resourceId",
        "continuation",
        "cityObjectGroup",
        "appearances",
        "affineTransformation",
        "cityGMLOptions",
        "cityJSONOptions",
        "resources"
})
public class ExportConfig {
    @XmlAttribute
    private boolean useSimpleQuery = true;
    private QueryConfig query;
    private SimpleQuery simpleQuery;
    private Path path;
    @XmlElement(name = "general")
    private GeneralOptions generalOptions;
    private ExportResourceId resourceId;
    private Continuation continuation;
    private ExportCityObjectGroup cityObjectGroup;
    private ExportAppearance appearances;
    private AffineTransformation affineTransformation;
    private CityGMLOptions cityGMLOptions;
    private CityJSONOptions cityJSONOptions;
    private Resources resources;

    public ExportConfig() {
        query = new QueryConfig();
        simpleQuery = new SimpleQuery();
        path = new Path();
        generalOptions = new GeneralOptions();
        resourceId = new ExportResourceId();
        continuation = new Continuation();
        cityObjectGroup = new ExportCityObjectGroup();
        appearances = new ExportAppearance();
        affineTransformation = new AffineTransformation();
        cityGMLOptions = new CityGMLOptions();
        cityJSONOptions = new CityJSONOptions();
        resources = new Resources();
    }

    public boolean isUseSimpleQuery() {
        return useSimpleQuery;
    }

    public void setUseSimpleQuery(boolean useSimpleQuery) {
        this.useSimpleQuery = useSimpleQuery;
    }

    public QueryConfig getQuery() {
        return query;
    }

    public void setQuery(QueryConfig query) {
        if (query != null) {
            this.query = query;
        }
    }

    public SimpleQuery getSimpleQuery() {
        return simpleQuery;
    }

    public void setSimpleQuery(SimpleQuery query) {
        if (query != null) {
            this.simpleQuery = query;
        }
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        if (path != null) {
            this.path = path;
        }
    }

    public GeneralOptions getGeneralOptions() {
        return generalOptions;
    }

    public void setGeneralOptions(GeneralOptions generalOptions) {
        if (generalOptions != null) {
            this.generalOptions = generalOptions;
        }
    }

    public ExportResourceId getResourceId() {
        return resourceId;
    }

    public void setResourceId(ExportResourceId resourceId) {
        if (resourceId != null) {
            this.resourceId = resourceId;
        }
    }

    public Continuation getContinuation() {
        return continuation;
    }

    public void setContinuation(Continuation continuation) {
        if (continuation != null) {
            this.continuation = continuation;
        }
    }

    public ExportAppearance getAppearances() {
        return appearances;
    }

    public void setAppearances(ExportAppearance appearances) {
        if (appearances != null) {
            this.appearances = appearances;
        }
    }

    public ExportCityObjectGroup getCityObjectGroup() {
        return cityObjectGroup;
    }

    public void setCityObjectGroup(ExportCityObjectGroup cityObjectGroup) {
        if (cityObjectGroup != null) {
            this.cityObjectGroup = cityObjectGroup;
        }
    }

    public AffineTransformation getAffineTransformation() {
        return affineTransformation;
    }

    public void setAffineTransformation(AffineTransformation affineTransformation) {
        if (affineTransformation != null) {
            this.affineTransformation = affineTransformation;
        }
    }

    public CityGMLOptions getCityGMLOptions() {
        return cityGMLOptions;
    }

    public void setCityGMLOptions(CityGMLOptions cityGMLOptions) {
        if (cityGMLOptions != null) {
            this.cityGMLOptions = cityGMLOptions;
        }
    }

    public CityJSONOptions getCityJSONOptions() {
        return cityJSONOptions;
    }

    public void setCityJSONOptions(CityJSONOptions cityJSONOptions) {
        if (cityJSONOptions != null) {
            this.cityJSONOptions = cityJSONOptions;
        }
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources system) {
        if (system != null) {
            this.resources = system;
        }
    }

}
