package org.citydb.query.serialize;

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.common.database.cache.model.CacheTableModel;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.sql.SQLQueryBuilder;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.Select;

import java.sql.Connection;
import java.sql.SQLException;

public class QueryResultSerializer {
    private final AbstractDatabaseAdapter databaseAdapter;
    private final SQLQueryBuilder queryBuilder;

    public QueryResultSerializer(SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
        queryBuilder = new SQLQueryBuilder(schemaMapping, databaseAdapter);
    }

    public CacheTable serialize(Query query) throws QueryBuildException, QuerySerializeException {
        try {
            // build select and make sure we only query the ID column
            Select select = queryBuilder.buildQuery(query);
            for (ProjectionToken token : select.getProjection()) {
                if (token instanceof Column && !((Column) token).getName().equals(MappingConstants.ID))
                    select.removeProjection(token);
            }

            Connection connection = DatabaseConnectionPool.getInstance().getConnection();
            connection.setAutoCommit(false);

            // create cache table and populate with result from query
            CacheTable cacheTable = new CacheTable(CacheTableModel.ID_LIST, connection, databaseAdapter.getSQLAdapter());
            cacheTable.createAsSelect(select.toString());
            cacheTable.createIndexes();

            return cacheTable;
        } catch (SQLException e) {
            throw new QuerySerializeException("Failed to create cache table from query.", e);
        }
    }
}
