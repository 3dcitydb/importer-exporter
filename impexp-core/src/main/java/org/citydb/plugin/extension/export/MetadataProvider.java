package org.citydb.plugin.extension.export;

import org.citydb.citygml.exporter.util.Metadata;
import org.citydb.config.Config;
import org.citydb.query.Query;

public interface MetadataProvider {
    Metadata getMetadata(Query query, Config config);
}
