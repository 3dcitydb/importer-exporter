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

package org.citydb.core.operation.exporter.util;

import org.citydb.config.project.exporter.OutputFormat;
import org.citydb.core.file.OutputFile;

public class InternalConfig {
    private OutputFile outputFile;
    private OutputFormat outputFormat;
    private String exportTextureURI;
    private boolean transformCoordinates = false;
    private boolean exportGlobalAppearances = false;
    private boolean registerGmlIdInCache = false;
    private boolean exportFeatureReferences = true;
    private boolean exportGeometryReferences = true;

    public OutputFile getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(OutputFile outputFile) {
        this.outputFile = outputFile;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat != null ? outputFormat : OutputFormat.CITYGML;
    }

    public void setOutputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getExportTextureURI() {
        return exportTextureURI;
    }

    public void setExportTextureURI(String exportTextureURI) {
        this.exportTextureURI = exportTextureURI;
    }

    public boolean isTransformCoordinates() {
        return transformCoordinates;
    }

    public void setTransformCoordinates(boolean transformCoordinates) {
        this.transformCoordinates = transformCoordinates;
    }

    public boolean isExportGlobalAppearances() {
        return exportGlobalAppearances;
    }

    public void setExportGlobalAppearances(boolean exportGlobalAppearances) {
        this.exportGlobalAppearances = exportGlobalAppearances;
    }

    public boolean isRegisterGmlIdInCache() {
        return registerGmlIdInCache;
    }

    public void setRegisterGmlIdInCache(boolean registerGmlIdInCache) {
        this.registerGmlIdInCache = registerGmlIdInCache;
    }

    public boolean isExportFeatureReferences() {
        return exportFeatureReferences;
    }

    public void setExportFeatureReferences(boolean exportFeatureReferences) {
        this.exportFeatureReferences = exportFeatureReferences;
    }

    public boolean isExportGeometryReferences() {
        return exportGeometryReferences;
    }

    public void setExportGeometryReferences(boolean exportGeometryReferences) {
        this.exportGeometryReferences = exportGeometryReferences;
    }
}
