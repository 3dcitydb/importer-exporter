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
package org.citydb.config.project.exporter;

import org.citydb.config.project.common.Path;
import org.citydb.config.project.common.XSLTransformation;
import org.citydb.config.project.query.QueryConfig;
import org.citydb.config.project.resources.Resources;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "export")
@XmlType(name = "ExportType", propOrder = {
        "query",
        "simpleQuery",
        "path",
        "continuation",
        "cityObjectGroup",
        "address",
        "appearances",
        "xlink",
        "xslTransformation",
        "metadataProvider",
        "cityGMLOptions",
        "resources"
})
public class ExportConfig {
    @XmlAttribute
    private boolean useSimpleQuery;
    private QueryConfig query;
    private SimpleQuery simpleQuery;
    private Path path;
    private Continuation continuation;
    private ExportCityObjectGroup cityObjectGroup;
    private ExportAddress address;
    private ExportAppearance appearances;
    private XLink xlink;
    private XSLTransformation xslTransformation;
    private String metadataProvider;
    private CityGMLOptions cityGMLOptions;
    private Resources resources;

    public ExportConfig() {
        query = new QueryConfig();
        simpleQuery = new SimpleQuery();
        path = new Path();
        continuation = new Continuation();
        cityObjectGroup = new ExportCityObjectGroup();
        address = new ExportAddress();
        appearances = new ExportAppearance();
        xlink = new XLink();
        xslTransformation = new XSLTransformation();
        cityGMLOptions = new CityGMLOptions();
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
        if (query != null)
            this.query = query;
    }

    public SimpleQuery getSimpleQuery() {
        return simpleQuery;
    }

    public void setSimpleQuery(SimpleQuery query) {
        if (query != null)
            this.simpleQuery = query;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        if (path != null)
            this.path = path;
    }

    public Continuation getContinuation() {
        return continuation;
    }

    public void setContinuation(Continuation continuation) {
        if (continuation != null)
            this.continuation = continuation;
    }

    public ExportAddress getAddress() {
        return address;
    }

    public void setAddress(ExportAddress address) {
        if (address != null)
            this.address = address;
    }

    public ExportAppearance getAppearances() {
        return appearances;
    }

    public void setAppearances(ExportAppearance appearances) {
        if (appearances != null)
            this.appearances = appearances;
    }

    public ExportCityObjectGroup getCityObjectGroup() {
        return cityObjectGroup;
    }

    public void setCityObjectGroup(ExportCityObjectGroup cityObjectGroup) {
        if (cityObjectGroup != null)
            this.cityObjectGroup = cityObjectGroup;
    }

    public XLink getXlink() {
        return xlink;
    }

    public void setXlink(XLink xlink) {
        if (xlink != null)
            this.xlink = xlink;
    }

    public XSLTransformation getXSLTransformation() {
        return xslTransformation;
    }

    public void setXSLTransformation(XSLTransformation xslTransformation) {
        if (xslTransformation != null)
            this.xslTransformation = xslTransformation;
    }

    public boolean isSetMetadataProvider() {
        return metadataProvider != null;
    }

    public String getMetadataProvider() {
        return metadataProvider;
    }

    public void setMetadataProvider(String metadataProvider) {
        this.metadataProvider = metadataProvider;
    }

    public CityGMLOptions getCityGMLOptions() {
        return cityGMLOptions;
    }

    public void setCityGMLOptions(CityGMLOptions cityGMLOptions) {
        if (cityGMLOptions != null)
            this.cityGMLOptions = cityGMLOptions;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources system) {
        if (system != null)
            this.resources = system;
    }

}
