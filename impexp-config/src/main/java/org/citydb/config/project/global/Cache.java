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
package org.citydb.config.project.global;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.nio.file.Path;
import java.nio.file.Paths;


@XmlType(name = "CacheType", propOrder = {
        "mode",
        "localPath"
})
public class Cache {
    public static final Path DEFAULT_LOCAL_CACHE_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "3dcitydb").toAbsolutePath();

    @XmlElement(required = true)
    private CacheMode mode = CacheMode.DATABASE;
    private String localPath;

    public Cache() {
        localPath = DEFAULT_LOCAL_CACHE_DIR.toString();
    }

    public boolean isUseDatabase() {
        return mode == null || mode == CacheMode.DATABASE;
    }

    public boolean isUseLocal() {
        return mode == CacheMode.LOCAL;
    }

    public CacheMode getCacheMode() {
        return mode != null ? mode : CacheMode.DATABASE;
    }

    public void setCacheMode(CacheMode mode) {
        this.mode = mode;
    }

    public boolean isSetLocalCachePath() {
        return localPath != null && !localPath.isEmpty();
    }

    public String getLocalCachePath() {
        return localPath;
    }

    public void setLocalCachePath(String localPath) {
        if (localPath != null && !localPath.isEmpty()) {
            this.localPath = localPath;
        }
    }

}
