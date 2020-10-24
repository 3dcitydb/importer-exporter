package org.citydb.plugin.extension.export;

import org.citydb.citygml.exporter.util.Metadata;
import org.citydb.config.project.exporter.ExportConfig;
import org.citydb.plugin.PluginException;
import org.citydb.query.Query;

public interface MetadataProvider {
    void setMetadata(Metadata metadata, Query query, ExportConfig exportConfig) throws PluginException;
}
