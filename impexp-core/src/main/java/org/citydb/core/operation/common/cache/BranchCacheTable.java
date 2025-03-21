/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

import org.citydb.core.database.adapter.AbstractSQLAdapter;
import org.citydb.core.operation.common.cache.model.CacheTableModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class BranchCacheTable extends AbstractCacheTable {
    private final CacheTable main;
    private final CacheTableModel model;
    private final ReentrantLock mainLock = new ReentrantLock();

    private volatile boolean isCreated = false;
    private List<CacheTable> branches;

    protected BranchCacheTable(CacheTableModel model, Connection connection, AbstractSQLAdapter sqlAdapter) {
        super(connection, sqlAdapter);
        this.model = model;

        main = new CacheTable(model, connection, sqlAdapter, false);
        branches = new ArrayList<>();
    }

    @Override
    protected void create() throws SQLException {
        if (isCreated)
            return;

        final ReentrantLock lock = this.mainLock;
        lock.lock();

        try {
            if (!isCreated) {
                main.create();
                isCreated = true;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void createAndIndex() throws SQLException {
        if (isCreated)
            return;

        final ReentrantLock lock = this.mainLock;
        lock.lock();

        try {
            if (!isCreated) {
                main.createAndIndex();
                isCreated = true;
            }
        } finally {
            lock.unlock();
        }
    }

    public CacheTable branch() throws SQLException {
        if (!isCreated)
            return null;

        final ReentrantLock lock = this.mainLock;
        lock.lock();

        try {
            if (isCreated) {
                CacheTable branch = new CacheTable(model, connection, sqlAdapter, false);
                branch.create();
                branches.add(branch);

                return branch;
            } else
                return null;
        } finally {
            lock.unlock();
        }
    }

    public CacheTable branchAndIndex() throws SQLException {
        if (!isCreated)
            return null;

        final ReentrantLock lock = this.mainLock;
        lock.lock();

        try {
            if (isCreated) {
                CacheTable branch = new CacheTable(model, connection, sqlAdapter, false);
                branch.createAndIndex();
                branches.add(branch);

                return branch;
            } else
                return null;
        } finally {
            lock.unlock();
        }
    }

    public CacheTable getMainTable() {
        return main;
    }

    public List<CacheTable> getBranchTables() {
        return new ArrayList<>(branches);
    }

    @Override
    public boolean isCreated() {
        return isCreated;
    }

    @Override
    protected void drop() throws SQLException {
        if (!isCreated)
            return;

        final ReentrantLock lock = this.mainLock;
        lock.lock();

        try {
            if (isCreated) {
                main.dropInternal();
                for (CacheTable branch : branches)
                    branch.dropInternal();

                isCreated = false;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public CacheTableModel getModelType() {
        return model;
    }

}
