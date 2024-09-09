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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ImportBatchingType", propOrder = {
        "featureBatchSize",
        "gmlIdCacheBatchSize",
        "tempBatchSize"
})
public class ImportBatching {
    public static final int MAX_BATCH_SIZE = 65535;

    @XmlElement(defaultValue = "20")
    @XmlSchemaType(name = "positiveInteger")
    private int featureBatchSize = 20;
    @XmlElement(defaultValue = "1000")
    @XmlSchemaType(name = "positiveInteger")
    private int gmlIdCacheBatchSize = 1000;
    @XmlElement(defaultValue = "1000")
    @XmlSchemaType(name = "positiveInteger")
    private int tempBatchSize = 1000;

    public int getFeatureBatchSize() {
        return featureBatchSize > 0 ? featureBatchSize : 20;
    }

    public void setFeatureBatchSize(int featureBatchSize) {
        if (featureBatchSize > 0 && featureBatchSize <= MAX_BATCH_SIZE)
            this.featureBatchSize = featureBatchSize;
    }

    public int getGmlIdCacheBatchSize() {
        return gmlIdCacheBatchSize > 0 ? gmlIdCacheBatchSize : 1000;
    }

    public void setGmlIdCacheBatchSize(int gmlIdCacheBatchSize) {
        if (gmlIdCacheBatchSize > 0 && gmlIdCacheBatchSize <= MAX_BATCH_SIZE)
            this.gmlIdCacheBatchSize = gmlIdCacheBatchSize;
    }

    public int getTempBatchSize() {
        return tempBatchSize > 0 ? tempBatchSize : 1000;
    }

    public void setTempBatchSize(int tempBatchSize) {
        if (tempBatchSize > 0 && tempBatchSize <= MAX_BATCH_SIZE)
            this.tempBatchSize = tempBatchSize;
    }

}
