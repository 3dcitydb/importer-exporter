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

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ElevationType", propOrder = {
        "altitudeMode",
        "altitudeOffsetMode",
        "altitudeOffsetValue",
        "callGElevationService",
        "useOriginalZCoords",
})
public class Elevation {
    private AltitudeMode altitudeMode;
    private AltitudeOffsetMode altitudeOffsetMode;
    private Double altitudeOffsetValue;
    private Boolean callGElevationService;
    private Boolean useOriginalZCoords;

    public Elevation() {
        altitudeMode = AltitudeMode.ABSOLUTE;
        altitudeOffsetMode = AltitudeOffsetMode.NO_OFFSET;
        useOriginalZCoords = true;
    }

    public AltitudeMode getAltitudeMode() {
        return altitudeMode;
    }

    public void setAltitudeMode(AltitudeMode altitudeMode) {
        if (altitudeMode != null) {
            this.altitudeMode = altitudeMode;
        }
    }

    public AltitudeOffsetMode getAltitudeOffsetMode() {
        return altitudeOffsetMode;
    }

    public void setAltitudeOffsetMode(AltitudeOffsetMode altitudeOffsetMode) {
        if (altitudeOffsetMode != null) {
            this.altitudeOffsetMode = altitudeOffsetMode;
        }
    }

    public Double getAltitudeOffsetValue() {
        return altitudeOffsetValue != null ? altitudeOffsetValue : 0;
    }

    public void setAltitudeOffsetValue(Double altitudeOffsetValue) {
        this.altitudeOffsetValue = altitudeOffsetValue;
    }

    public boolean isCallGElevationService() {
        return callGElevationService != null ? callGElevationService : false;
    }

    public void setCallGElevationService(boolean callGElevationService) {
        this.callGElevationService = callGElevationService;
    }

    public boolean isUseOriginalZCoords() {
        return useOriginalZCoords != null ? useOriginalZCoords : true;
    }

    public void setUseOriginalZCoords(boolean useOriginalZCoords) {
        this.useOriginalZCoords = useOriginalZCoords;
    }
}
