package org.citydb.core.operation.importer.filter.selection.id;

import org.citydb.config.project.importer.ImportIdList;
import org.citydb.config.project.importer.ImportIdListMode;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.query.filter.FilterException;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IdListFilter {
    private final CacheTable idListCacheTable;
    private final ImportIdListMode mode;

    public IdListFilter(ImportIdList importIdList, CacheTable idListCacheTable) throws FilterException {
        if (importIdList == null) {
            throw new FilterException("Identifier list config must not be null.");
        }

        this.idListCacheTable = idListCacheTable;
        mode = importIdList.getMode();
    }

    public boolean isSatisfiedBy(AbstractFeature feature) throws FilterException {
        if (feature.isSetId()) {
            try (PreparedStatement ps = idListCacheTable.getConnection().prepareStatement("select 1 from " +
                    idListCacheTable.getTableName() + " where gmlid = ?")) {
                ps.setString(1, feature.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    return (mode == ImportIdListMode.IMPORT) == rs.next();
                }
            } catch (SQLException e) {
                throw new FilterException("Failed to query identifier list.", e);
            }
        }

        return true;
    }
}
