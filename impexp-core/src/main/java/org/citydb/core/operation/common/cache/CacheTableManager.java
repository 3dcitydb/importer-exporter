/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.core.operation.common.cache;

import org.citydb.config.project.global.CacheMode;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.adapter.AbstractSQLAdapter;
import org.citydb.core.database.adapter.h2.H2Adapter;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.operation.common.cache.model.CacheTableModel;
import org.citydb.util.log.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CacheTableManager {
    private final String cacheDir;
    private final Cache primaryCache;

    private final Map<CacheMode, Cache> caches = new ConcurrentHashMap<>();
    private final Map<CacheTableModel, CacheTable> cacheTables = new ConcurrentHashMap<>();
    private final Map<CacheTableModel, BranchCacheTable> branchCacheTables = new ConcurrentHashMap<>();

    public CacheTableManager(org.citydb.config.project.global.Cache cacheConfig) throws SQLException, IOException {
        cacheDir = cacheConfig.getLocalCachePath();
        primaryCache = getOrCreateCache(cacheConfig.getCacheMode());
    }

    public AbstractDatabaseAdapter getCacheAdapter() {
        return primaryCache.adapter;
    }

    public CacheTable createCacheTable(CacheTableModel model) throws SQLException {
        return getOrCreateCacheTable(primaryCache, model, false, CacheTable.class);
    }

    public CacheTable createCacheTable(CacheTableModel model, CacheMode mode) throws SQLException {
        return getOrCreateCacheTable(getOrCreateCache(mode), model, false, CacheTable.class);
    }

    public CacheTable createAndIndexCacheTable(CacheTableModel model) throws SQLException {
        return getOrCreateCacheTable(primaryCache, model, true, CacheTable.class);
    }

    public CacheTable createAndIndexCacheTable(CacheTableModel model, CacheMode mode) throws SQLException {
        return getOrCreateCacheTable(getOrCreateCache(mode), model, true, CacheTable.class);
    }

    public CacheTable getCacheTable(CacheTableModel model) {
        return cacheTables.get(model);
    }

    public boolean existsCacheTable(CacheTableModel model) {
        return cacheTables.containsKey(model);
    }

    public BranchCacheTable createBranchCacheTable(CacheTableModel model) throws SQLException {
        return getOrCreateCacheTable(primaryCache, model, false, BranchCacheTable.class);
    }

    public BranchCacheTable createBranchCacheTable(CacheTableModel model, CacheMode mode) throws SQLException {
        return getOrCreateCacheTable(getOrCreateCache(mode), model, false, BranchCacheTable.class);
    }

    public BranchCacheTable createAndIndexBranchCacheTable(CacheTableModel model) throws SQLException {
        return getOrCreateCacheTable(primaryCache, model, true, BranchCacheTable.class);
    }

    public BranchCacheTable createAndIndexBranchCacheTable(CacheTableModel model, CacheMode mode) throws SQLException {
        return getOrCreateCacheTable(getOrCreateCache(mode), model, true, BranchCacheTable.class);
    }

    public BranchCacheTable getBranchCacheTable(CacheTableModel model) {
        return branchCacheTables.get(model);
    }

    public boolean existsBranchCacheTable(CacheTableModel model) {
        return branchCacheTables.containsKey(model);
    }

    public synchronized void drop(AbstractCacheTable cacheTable) throws SQLException {
        cacheTable.drop();

        if (cacheTable instanceof BranchCacheTable) {
            branchCacheTables.remove(cacheTable.getModelType());
        } else {
            cacheTables.remove(cacheTable.getModelType());
        }
    }

    public synchronized void dropIf(Predicate<AbstractCacheTable> filter) throws SQLException {
        dropIf(filter, cacheTables);
        dropIf(filter, branchCacheTables);
    }

    private void dropIf(Predicate<AbstractCacheTable> filter, Map<CacheTableModel, ? extends AbstractCacheTable> cacheTables) throws SQLException {
        for (AbstractCacheTable cacheTable : cacheTables.values()) {
            if (filter.test(cacheTable)) {
                cacheTable.drop();
                cacheTables.remove(cacheTable.getModelType());
            }
        }
    }

    public synchronized void dropAll() throws SQLException {
        // drop cache and branch tables in case they have not
        // been deleted automatically by the previous rollback
        for (CacheTable cacheTable : cacheTables.values()) {
            try {
                cacheTable.drop();
                cacheTable.connection.commit();
            } catch (SQLException e) {
                //
            }
        }

        for (BranchCacheTable branchCacheTable : branchCacheTables.values()) {
            try {
                branchCacheTable.drop();
                branchCacheTable.connection.commit();
            } catch (SQLException e) {
                //
            }
        }

        cacheTables.clear();
        branchCacheTables.clear();
    }

    public synchronized void close() throws SQLException {
        dropAll();

        for (Cache cache : caches.values()) {
            cache.connection.close();

            try {
                cache.deleteCacheDir();
            } catch (IOException e) {
                throw new SQLException("Failed to delete local cache directory.", e);
            }
        }

        caches.clear();
    }

    private <T extends AbstractCacheTable> T getOrCreateCacheTable(Cache cache, CacheTableModel model, boolean createIndexes, Class<T> type) throws SQLException {
        Connection connection = cache.connection;
        AbstractSQLAdapter sqlAdapter = cache.adapter.getSQLAdapter();

        AbstractCacheTable cacheTable = type == CacheTable.class ?
                cacheTables.computeIfAbsent(model, v -> new CacheTable(model, connection, sqlAdapter)) :
                branchCacheTables.computeIfAbsent(model, v -> new BranchCacheTable(model, connection, sqlAdapter));

        if (sqlAdapter != cacheTable.sqlAdapter) {
            throw new SQLException("A cache table for '" + model + "' already exists and may not be created twice " +
                    "for the cache mode '" + cache.mode.value() + "'.");
        }

        if (!cacheTable.isCreated()) {
            if (createIndexes) {
                cacheTable.createAndIndex();
            } else {
                cacheTable.create();
            }
        }

        return type.cast(cacheTable);
    }

    private synchronized Cache getOrCreateCache(CacheMode mode) throws SQLException {
        Cache cache = caches.get(mode);
        if (cache == null) {
            AbstractDatabaseAdapter adapter;
            Connection connection;
            Path cacheDir = null;

            if (mode == CacheMode.LOCAL) {
                try {
                    adapter = new H2Adapter();
                    Class.forName(adapter.getConnectionFactoryClassName());
                    cacheDir = checkAndGetCacheDir().resolve(UUID.randomUUID().toString());
                    connection = DriverManager.getConnection(adapter.getJDBCUrl(cacheDir.resolve("tmp").toString(), -1, null), "sa", "");
                    Logger.getInstance().debug("Created local cache at directory '" + cacheDir + "'.");
                } catch (IOException e) {
                    throw new SQLException("Failed to create local cache directory.", e);
                } catch (ClassNotFoundException e) {
                    throw new SQLException("Failed to load local cache driver.", e);
                }
            } else {
                adapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
                connection = DatabaseConnectionPool.getInstance().getConnection();
            }

            connection.setAutoCommit(false);
            cache = new Cache(connection, adapter, mode, cacheDir);
            caches.put(mode, cache);
        }

        return cache;
    }

    private Path checkAndGetCacheDir() throws IOException {
        if (cacheDir == null || cacheDir.trim().length() == 0) {
            throw new IOException("No local cache directory provided.");
        }

        Path cacheDir = Paths.get(this.cacheDir).toAbsolutePath();
        if (!Files.exists(cacheDir)) {
            try {
                Files.createDirectories(cacheDir);
            } catch (IOException e) {
                throw new IOException("Failed to create local cache directory '" + cacheDir + "'.", e);
            }
        }

        if (!Files.isDirectory(cacheDir)) {
            throw new IOException("The local cache setting '" + cacheDir + "' is not a directory.");
        }

        if (!Files.isReadable(cacheDir)) {
            throw new IOException("Lacking read permissions on local cache directory '" + cacheDir + "'.");
        }

        if (!Files.isWritable(cacheDir)) {
            throw new IOException("Lacking write permissions on local cache directory '" + cacheDir + "'.");
        }

        return cacheDir;
    }

    private static class Cache {
        private final Connection connection;
        private final AbstractDatabaseAdapter adapter;
        private final CacheMode mode;
        private final Path cacheDir;

        Cache(Connection connection, AbstractDatabaseAdapter adapter, CacheMode mode, Path cacheDir) {
            this.connection = connection;
            this.adapter = adapter;
            this.mode = mode;
            this.cacheDir = cacheDir;
        }

        void deleteCacheDir() throws IOException {
            if (cacheDir != null) {
                try (Stream<Path> stream = Files.walk(cacheDir)) {
                    for (Path path : stream
                            .sorted(Comparator.reverseOrder())
                            .collect(Collectors.toList())) {
                        Files.delete(path);
                    }
                }
            }
        }
    }
}
