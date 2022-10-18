/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2022
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

package org.citydb.core.operation.importer.concurrent;

import org.citydb.config.project.global.LogLevel;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.operation.importer.filter.CityGMLFilter;
import org.citydb.core.operation.importer.util.DuplicateLogger;
import org.citydb.core.operation.importer.util.DuplicateLogger.DuplicateLogEntry;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.Util;
import org.citydb.util.concurrent.Worker;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citydb.util.event.global.InterruptEvent;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DuplicateCheckerWorker extends Worker<CityGML> implements EventHandler {
    private final ReentrantLock runLock = new ReentrantLock();
    private volatile boolean shouldRun = true;
    private volatile boolean shouldWork = true;

    private final SchemaMapping schemaMapping;
    private final Connection connection;
    private final DuplicateLogger duplicateLogger;
    private final CityGMLFilter filter;
    private final EventDispatcher eventDispatcher;
    private final int placeHolders;
    private final PreparedStatement ps;
    private final Map<String, FileCandidate> candidates = new HashMap<>();

    private long duplicates;
    private String inputFile = "";

    public DuplicateCheckerWorker(DuplicateLogger duplicateLogger, CityGMLFilter filter) throws SQLException {
        this.duplicateLogger = duplicateLogger;
        this.filter = filter;

        schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
        connection = DatabaseConnectionPool.getInstance().getConnection();
        connection.setAutoCommit(false);

        AbstractDatabaseAdapter databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
        placeHolders = databaseAdapter.getSQLAdapter().getMaximumNumberOfItemsForInOperator();
        String schema = databaseAdapter.getConnectionDetails().getSchema();

        ps = connection.prepareStatement("select id, gmlid, objectclass_id from " + schema + ".cityobject " +
                "where gmlid in (" + String.join(",", Collections.nCopies(placeHolders, "?")) + ") " +
                "and termination_date is null");

        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
    }

    public long getNumberOfDuplicates() {
        return duplicates;
    }

    public void setInputFile(Path inputFile) {
        this.inputFile = inputFile != null ? inputFile.toAbsolutePath().toString() : "";
    }

    @Override
    public void interrupt() {
        shouldRun = false;
    }

    @Override
    public void run() {
        try {
            if (firstWork != null) {
                doWork(firstWork);
                firstWork = null;
            }

            while (shouldRun) {
                try {
                    CityGML work = workQueue.take();
                    doWork(work);
                } catch (InterruptedException ie) {
                    // re-check state
                }
            }

            if (shouldWork && !candidates.isEmpty()) {
                processDuplicates();
            }
        } finally {
            try {
                ps.close();
                connection.close();
            } catch (SQLException e) {
                //
            }

            eventDispatcher.removeEventHandler(this);
        }
    }

    public void doWork(CityGML work) {
        final ReentrantLock lock = this.runLock;
        lock.lock();

        try {
            if (!shouldWork) {
                return;
            }

            if (work instanceof AbstractFeature && work.getCityGMLClass() != CityGMLClass.APPEARANCE) {
                AbstractFeature feature = (AbstractFeature) work;
                if (feature.isSetId() && filter.getSelectionFilter().isSatisfiedBy(feature)) {
                    FeatureType featureType = schemaMapping.getFeatureType(Util.getObjectClassId(feature.getClass()));
                    String typeName = featureType != null ?
                            featureType.getPath() :
                            work.getCityGMLClass().toString();

                    candidates.put(feature.getId(), new FileCandidate(typeName, inputFile));
                    if (candidates.size() == placeHolders) {
                        processDuplicates();
                    }
                }
            }
        } catch (Throwable e) {
            eventDispatcher.triggerSyncEvent(new InterruptEvent(
                    "A fatal error occurred during checking of duplicates.",
                    LogLevel.ERROR, e, eventChannel));
        } finally {
            lock.unlock();
        }
    }

    private void processDuplicates() {
        try {
            int index = 1;
            for (String id : candidates.keySet()) {
                ps.setString(index++, id);
            }

            while (index <= placeHolders) {
                ps.setString(index++, null);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong(1);
                    String gmlId = rs.getString(2);
                    FeatureType featureType = schemaMapping.getFeatureType(rs.getInt(3));
                    FileCandidate candidate = candidates.get(gmlId);

                    duplicateLogger.write(DuplicateLogEntry.of(
                            id, gmlId, featureType.getPath(), candidate.typeName, candidate.fileName));
                    duplicates++;
                }
            }
        } catch (Throwable e) {
            eventDispatcher.triggerSyncEvent(new InterruptEvent(
                    "A fatal error occurred during checking of duplicates.",
                    LogLevel.ERROR, e, eventChannel));
        } finally {
            candidates.clear();
        }
    }

    private static class FileCandidate {
        private final String typeName;
        private final String fileName;

        FileCandidate(String typeName, String fileName) {
            this.typeName = typeName;
            this.fileName = fileName;
        }
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        shouldWork = false;
    }
}
