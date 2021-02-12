package org.citydb.config.project.common;

import org.citydb.config.geometry.BoundingBox;

public interface BoundingBoxFilter {
    boolean isSetExtent();
    BoundingBox getExtent();
    void setExtent(BoundingBox extent);
}
