package org.citydb.citygml.deleter.database;

import org.citydb.citygml.common.cache.CacheTable;
import org.citydb.citygml.deleter.util.DeleteListException;
import org.citydb.citygml.deleter.util.DeleteListParser;
import org.citydb.config.project.deleter.DeleteListIdType;
import org.citydb.database.schema.mapping.MappingConstants;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteListImporter {
    private final CacheTable cacheTable;
    private final int maxBatchSize;

    private int batchCounter;

    public DeleteListImporter(CacheTable cacheTable, int maxBatchSize) {
        this.cacheTable = cacheTable;
        this.maxBatchSize = maxBatchSize;
    }

    public void doImport(DeleteListParser parser) throws DeleteListException, SQLException {
        DeleteListIdType idType = parser.getDeleteList().getIdType();
        String sql = "insert into " + cacheTable.getTableName() + " " +
                (idType == DeleteListIdType.DATABASE_ID ?
                        "(" + MappingConstants.ID + ") values (?)" :
                        "(" + MappingConstants.GMLID + ") values (?)");

        try (PreparedStatement ps = cacheTable.getConnection().prepareStatement(sql)) {
            while (parser.hasNext()) {
                String id = parser.nextId();

                if (idType == DeleteListIdType.DATABASE_ID) {
                    try {
                        ps.setLong(1, Long.parseLong(id));
                    } catch (NumberFormatException e) {
                        throw new DeleteListException("Invalid database id in delete list: '" + id + "' " +
                                "(line " + parser.getCurrentLineNumber() + ").", e);
                    }
                } else {
                    ps.setString(1, id);
                }

                ps.addBatch();
                if (++batchCounter == maxBatchSize) {
                    ps.executeBatch();
                    batchCounter = 0;
                }
            }

            if (batchCounter > 0) {
                ps.executeBatch();
            }
        }
    }
}
