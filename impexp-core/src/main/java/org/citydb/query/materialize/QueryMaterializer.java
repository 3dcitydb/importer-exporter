package org.citydb.query.materialize;

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

public class QueryMaterializer {
    private final SchemaMapping schemaMapping;
    private final AbstractDatabaseAdapter databaseAdapter;

    public QueryMaterializer(SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter) {
        this.schemaMapping = schemaMapping;
        this.databaseAdapter = databaseAdapter;
    }

    public CacheTable materializeQuery(Select select) throws QueryMaterializeException {
        try {
            // make sure we only query the ID column
            boolean hasIdColumn = false;
            for (ProjectionToken token : select.getProjection()) {
                if (token instanceof Column && ((Column) token).getName().equalsIgnoreCase(MappingConstants.ID))
                    hasIdColumn = true;
                else
                    select.removeProjection(token);
            }

            if (!hasIdColumn)
                throw new QueryMaterializeException("Failed to materialize query due to missing ID column.");

            Connection connection = DatabaseConnectionPool.getInstance().getConnection();
            connection.setAutoCommit(false);

            // convert select statement to string
            String stmt = databaseAdapter.getSQLAdapter().serializeStatement(select);

            // create cache table and populate with result from query
            CacheTable cacheTable = new CacheTable(CacheTableModel.ID_LIST, connection, databaseAdapter.getSQLAdapter());
            cacheTable.createAsSelect(stmt);
            cacheTable.createIndexes();

            return cacheTable;
        } catch (SQLException e) {
            throw new QueryMaterializeException("Failed to materialize query in cache table.", e);
        }
    }

    public CacheTable materializeQuery(Query query) throws QueryBuildException, QueryMaterializeException {
        SQLQueryBuilder queryBuilder = new SQLQueryBuilder(schemaMapping, databaseAdapter);
        return materializeQuery(queryBuilder.buildQuery(query));
    }
}
