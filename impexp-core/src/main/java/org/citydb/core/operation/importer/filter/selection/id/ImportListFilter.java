package org.citydb.core.operation.importer.filter.selection.id;

import org.citydb.config.project.importer.ImportList;
import org.citydb.config.project.importer.ImportListMode;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.query.filter.FilterException;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ImportListFilter {
    private final CacheTable importListCacheTable;
    private final ImportListMode mode;

    public ImportListFilter(ImportList importList, CacheTable importListCacheTable) throws FilterException {
        if (importList == null) {
            throw new FilterException("Import list config must not be null.");
        }

        this.importListCacheTable = importListCacheTable;
        mode = importList.getMode();
    }

    public boolean isSatisfiedBy(AbstractFeature feature) throws FilterException {
        if (feature.isSetId()) {
            try (PreparedStatement ps = importListCacheTable.getConnection().prepareStatement("select 1 from " +
                    importListCacheTable.getTableName() + " where gmlid = ?")) {
                ps.setString(1, feature.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    return (mode == ImportListMode.IMPORT) == rs.next();
                }
            } catch (SQLException e) {
                throw new FilterException("Failed to query import list.", e);
            }
        }

        return true;
    }
}
