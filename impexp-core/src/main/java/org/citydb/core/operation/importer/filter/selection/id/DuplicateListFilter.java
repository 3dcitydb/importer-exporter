package org.citydb.core.operation.importer.filter.selection.id;

import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.query.filter.FilterException;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DuplicateListFilter {
    private final CacheTable duplicateListCacheTable;

    public DuplicateListFilter(CacheTable duplicateListCacheTable) {
        this.duplicateListCacheTable = duplicateListCacheTable;
    }

    public boolean isSatisfiedBy(AbstractFeature feature) throws FilterException {
        if (feature.isSetId()) {
            try (PreparedStatement ps = duplicateListCacheTable.getConnection().prepareStatement("select 1 from " +
                    duplicateListCacheTable.getTableName() + " where gmlid = ?")) {
                ps.setString(1, feature.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    return !rs.next();
                }
            } catch (SQLException e) {
                throw new FilterException("Failed to query duplicate list.", e);
            }
        }

        return true;
    }
}
