package org.citydb.citygml.exporter.util;

import org.citydb.config.geometry.BoundingBox;

public class Metadata {
    private String datasetName;
    private String datasetDescription;
    private BoundingBox spatialExtent;

    public boolean isSetDatasetName() {
        return datasetName != null;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public boolean isSetDatasetDescription() {
        return datasetDescription != null;
    }

    public String getDatasetDescription() {
        return datasetDescription;
    }

    public void setDatasetDescription(String datasetDescription) {
        this.datasetDescription = datasetDescription;
    }

    public boolean isSetSpatialExtent() {
        return spatialExtent != null;
    }

    public BoundingBox getSpatialExtent() {
        return spatialExtent;
    }

    public void setSpatialExtent(BoundingBox extent) {
        this.spatialExtent = extent;
    }
}
