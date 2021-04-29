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
package org.citydb.config.project.importer;

import org.citydb.config.project.common.AffineTransformation;
import org.citydb.config.project.common.Path;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "import")
@XmlType(name = "ImportType", propOrder = {
        "filter",
        "continuation",
        "path",
        "generalOptions",
        "resourceId",
        "appearances",
        "affineTransformation",
        "cityGMLOptions",
        "cityJSONOptions",
        "indexes",
        "importLog",
        "resources"
})
public class ImportConfig {
    private ImportFilter filter;
    private Continuation continuation;
    private Path path;
    @XmlElement(name = "general")
    private GeneralOptions generalOptions;
    private ImportResourceId resourceId;
    private ImportAppearance appearances;
    private AffineTransformation affineTransformation;
    private CityGMLOptions cityGMLOptions;
    private CityJSONOptions cityJSONOptions;
    private Index indexes;
    private ImportLog importLog;
    private ImportResources resources;

    public ImportConfig() {
        continuation = new Continuation();
        path = new Path();
        generalOptions = new GeneralOptions();
        resourceId = new ImportResourceId();
        appearances = new ImportAppearance();
        filter = new ImportFilter();
        affineTransformation = new AffineTransformation();
        cityGMLOptions = new CityGMLOptions();
        cityJSONOptions = new CityJSONOptions();
        indexes = new Index();
        importLog = new ImportLog();
        resources = new ImportResources();
    }

    public Continuation getContinuation() {
        return continuation;
    }

    public void setContinuation(Continuation continuation) {
        if (continuation != null) {
            this.continuation = continuation;
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

    public ImportResourceId getResourceId() {
        return resourceId;
    }

    public void setResourceId(ImportResourceId resourceId) {
        if (resourceId != null) {
            this.resourceId = resourceId;
        }
    }

    public ImportAppearance getAppearances() {
        return appearances;
    }

    public void setAppearances(ImportAppearance appearances) {
        if (appearances != null) {
            this.appearances = appearances;
        }
    }

    public ImportFilter getFilter() {
        return filter;
    }

    public void setFilter(ImportFilter filter) {
        if (filter != null) {
            this.filter = filter;
        }
    }

    public Index getIndexes() {
        return indexes;
    }

    public void setIndexes(Index indexes) {
        if (indexes != null) {
            this.indexes = indexes;
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

    public ImportLog getImportLog() {
        return importLog;
    }

    public void setImportLog(ImportLog importLog) {
        if (importLog != null) {
            this.importLog = importLog;
        }
    }

    public ImportResources getResources() {
        return resources;
    }

    public void setResources(ImportResources resources) {
        if (resources != null) {
            this.resources = resources;
        }
    }

}
