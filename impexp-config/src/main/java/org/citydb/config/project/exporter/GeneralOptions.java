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

import org.citydb.config.project.common.ComputeNumberMatched;

import javax.xml.bind.annotation.XmlType;
import java.nio.charset.StandardCharsets;

@XmlType(name = "GeneralExportOptionsType", propOrder = {})
public class GeneralOptions {
    private Boolean failFastOnErrors = true;
    private ComputeNumberMatched computeNumberMatched;
    private String fileEncoding;
    private OutputFormat compressedOutputFormat = OutputFormat.CITYGML;
    private TileTokenValue datasetName;
    private TileTokenValue datasetDescription;
    private ExportEnvelope envelope;

    public GeneralOptions() {
        computeNumberMatched = new ComputeNumberMatched();
        datasetName = new TileTokenValue();
        datasetDescription = new TileTokenValue();
        envelope = new ExportEnvelope();
    }

    public boolean isFailFastOnErrors() {
        return failFastOnErrors != null ? failFastOnErrors : true;
    }

    public void setFailFastOnErrors(boolean failFastOnErrors) {
        this.failFastOnErrors = failFastOnErrors;
    }

    public ComputeNumberMatched getComputeNumberMatched() {
        return computeNumberMatched;
    }

    public void setComputeNumberMatched(ComputeNumberMatched computeNumberMatched) {
        if (computeNumberMatched != null) {
            this.computeNumberMatched = computeNumberMatched;
        }
    }

    public boolean isSetFileEncoding() {
        return fileEncoding != null;
    }

    public String getFileEncoding() {
        return fileEncoding != null ? fileEncoding : StandardCharsets.UTF_8.name();
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    public OutputFormat getCompressedOutputFormat() {
        return compressedOutputFormat != null ? compressedOutputFormat : OutputFormat.CITYGML;
    }

    public void setCompressedOutputFormat(OutputFormat compressedOutputFormat) {
        this.compressedOutputFormat = compressedOutputFormat;
    }

    public TileTokenValue getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(TileTokenValue datasetName) {
        if (datasetName != null) {
            this.datasetName = datasetName;
        }
    }

    public TileTokenValue getDatasetDescription() {
        return datasetDescription;
    }

    public void setDatasetDescription(TileTokenValue datasetDescription) {
        if (datasetDescription != null) {
            this.datasetDescription = datasetDescription;
        }
    }

    public ExportEnvelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(ExportEnvelope envelope) {
        if (envelope != null) {
            this.envelope = envelope;
        }
    }
}
