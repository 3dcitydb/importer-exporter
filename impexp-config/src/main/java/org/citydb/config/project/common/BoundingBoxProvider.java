package org.citydb.config.project.common;

import org.citydb.config.geometry.BoundingBox;

public interface BoundingBoxProvider {
    boolean isSetExtent();
    BoundingBox getExtent();
    void setExtent(BoundingBox extent);
}
