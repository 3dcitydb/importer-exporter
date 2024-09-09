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
package org.citydb.config.project.visExporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ColladaOptionsType", propOrder = {
        "ignoreSurfaceOrientation",
        "generateSurfaceNormals",
        "cropImages",
        "generateTextureAtlases",
        "packingAlgorithm",
        "textureAtlasPots",
        "scaleImages",
        "imageScaleFactor",
        "groupObjects",
        "groupSize"
})
public class ColladaOptions {
    private Boolean ignoreSurfaceOrientation;
    private Boolean generateSurfaceNormals;
    private Boolean cropImages;
    private Boolean generateTextureAtlases;
    private Integer packingAlgorithm;
    private Boolean textureAtlasPots;
    private Boolean scaleImages;
    private Double imageScaleFactor;
    private Boolean groupObjects;
    private Integer groupSize;

    public ColladaOptions() {
        generateSurfaceNormals = true;
        generateTextureAtlases = true;
        packingAlgorithm = 1; // TextureAtlasGenerator.TPIM
        textureAtlasPots = true;
        imageScaleFactor = 1.0;
        groupSize = 1;
    }

    public void setIgnoreSurfaceOrientation(boolean ignoreSurfaceOrientation) {
        this.ignoreSurfaceOrientation = ignoreSurfaceOrientation;
    }

    public boolean isIgnoreSurfaceOrientation() {
        return ignoreSurfaceOrientation != null ? ignoreSurfaceOrientation : false;
    }

    public void setGenerateSurfaceNormals(boolean generateSurfaceNormals) {
        this.generateSurfaceNormals = generateSurfaceNormals;
    }

    public boolean isGenerateSurfaceNormals() {
        return generateSurfaceNormals != null ? generateSurfaceNormals : true;
    }

    public void setCropImages(boolean cropImages) {
        this.cropImages = cropImages;
    }

    public boolean isCropImages() {
        return cropImages != null ? cropImages : false;
    }

    public void setGenerateTextureAtlases(boolean generateTextureAtlases) {
        this.generateTextureAtlases = generateTextureAtlases;
    }

    public boolean isGenerateTextureAtlases() {
        return generateTextureAtlases != null ? generateTextureAtlases : true;
    }

    public void setPackingAlgorithm(int packingAlgorithm) {
        this.packingAlgorithm = packingAlgorithm;
    }

    public int getPackingAlgorithm() {
        return packingAlgorithm != null ? packingAlgorithm : 1;
    }

    public void setTextureAtlasPots(boolean textureAtlasPots) {
        this.textureAtlasPots = textureAtlasPots;
    }

    public boolean isTextureAtlasPots() {
        return textureAtlasPots != null ? textureAtlasPots : true;
    }

    public void setScaleImages(boolean scaleImages) {
        this.scaleImages = scaleImages;
    }

    public boolean isScaleImages() {
        return scaleImages != null ? scaleImages : false;
    }

    public void setImageScaleFactor(double imageScaleFactor) {
        if (imageScaleFactor >= 0 && imageScaleFactor <= 1) {
            this.imageScaleFactor = imageScaleFactor;
        }
    }

    public double getImageScaleFactor() {
        return imageScaleFactor != null ? imageScaleFactor : 1.0;
    }

    public void setGroupObjects(boolean groupObjects) {
        this.groupObjects = groupObjects;
    }

    public boolean isGroupObjects() {
        return groupObjects != null ? groupObjects : false;
    }

    public void setGroupSize(int groupSize) {
        if (groupSize > 0) {
            this.groupSize = groupSize;
        }
    }

    public int getGroupSize() {
        return groupSize != null ? groupSize : 1;
    }
}
