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
    private final AbstractDatabaseAdapter databaseAdapter;
    private final SQLQueryBuilder queryBuilder;

    public QueryMaterializer(SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
        queryBuilder = new SQLQueryBuilder(schemaMapping, databaseAdapter);
    }

    public CacheTable materialize(Query query) throws QueryBuildException, QueryMaterializeException {
        try {
            Connection connection = DatabaseConnectionPool.getInstance().getConnection();
            connection.setAutoCommit(false);

            // build select and make sure we only query the ID column
            Select select = queryBuilder.buildQuery(query);
            for (ProjectionToken token : select.getProjection()) {
                if (token instanceof Column && !((Column) token).getName().equals(MappingConstants.ID))
                    select.removeProjection(token);
            }

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
}
