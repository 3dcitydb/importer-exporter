package org.citydb.plugin.extension.export;

import org.citydb.citygml.exporter.util.Metadata;
import org.citydb.config.project.exporter.Exporter;
import org.citydb.plugin.PluginException;
import org.citydb.query.Query;

public interface MetadataProvider {
    void setMetadata(Metadata metadata, Query query, Exporter exportConfig) throws PluginException;
}
