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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.File;
import java.util.Locale;

@XmlType(name = "GltfOptionsType", propOrder = {
        "pathToConverter",
        "gltfVersion",
        "removeColladaFiles",
        "embedTextures",
        "useBinaryGltf",
        "useDracoCompression"
})
public class GltfOptions {
    @XmlAttribute
    private boolean createGltfModel;
    private String pathToConverter;
    private GltfVersion gltfVersion;
    private Boolean removeColladaFiles;
    private Boolean embedTextures;
    private Boolean useBinaryGltf;
    private Boolean useDracoCompression;

    public GltfOptions() {
        gltfVersion = GltfVersion.v2_0;
        embedTextures = true;
        useDracoCompression = true;

        pathToConverter = "contribs" + File.separator + "collada2gltf";
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (osName.contains("windows"))
            pathToConverter += File.separator + "COLLADA2GLTF-v2.1.3-windows-Release-x64" + File.separator + "COLLADA2GLTF-bin.exe";
        else if (osName.contains("mac"))
            pathToConverter += File.separator + "COLLADA2GLTF-v2.1.3-osx" + File.separator + "COLLADA2GLTF-bin";
        else if (osName.contains("nux"))
            pathToConverter += File.separator + "COLLADA2GLTF-v2.1.3-linux" + File.separator + "COLLADA2GLTF-bin";
    }

    public boolean isCreateGltfModel() {
        return createGltfModel;
    }

    public void setCreateGltfModel(boolean createGltfModel) {
        this.createGltfModel = createGltfModel;
    }

    public String getPathToConverter() {
        return pathToConverter;
    }

    public void setPathToConverter(String pathToConverter) {
        if (pathToConverter != null) {
            this.pathToConverter = pathToConverter;
        }
    }

    public GltfVersion getGltfVersion() {
        return gltfVersion;
    }

    public void setGltfVersion(GltfVersion gltfVersion) {
        if (gltfVersion != null) {
            this.gltfVersion = gltfVersion;
        }
    }

    public boolean isRemoveColladaFiles() {
        return removeColladaFiles != null ? removeColladaFiles : false;
    }

    public void setRemoveColladaFiles(boolean removeColladaFiles) {
        this.removeColladaFiles = removeColladaFiles;
    }

    public boolean isEmbedTextures() {
        return embedTextures != null ? embedTextures : true;
    }

    public void setEmbedTextures(boolean embedTextures) {
        this.embedTextures = embedTextures;
    }

    public boolean isUseBinaryGltf() {
        return useBinaryGltf != null ? useBinaryGltf : false;
    }

    public void setUseBinaryGltf(boolean useBinaryGltf) {
        this.useBinaryGltf = useBinaryGltf;
    }

    public boolean isUseDracoCompression() {
        return useDracoCompression != null ? useDracoCompression : true;
    }

    public void setUseDracoCompression(boolean useDracoCompression) {
        this.useDracoCompression = useDracoCompression;
    }
}
