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

import java.util.concurrent.atomic.AtomicBoolean;

public class IdCacheEntry {
    private final long id;
    private final long rootId;
    private final boolean reverse;
    private final String mapping;
    private final int objectClassId;
    private final AtomicBoolean isRegistered = new AtomicBoolean(false);
    private final AtomicBoolean isRequested = new AtomicBoolean(false);

    public IdCacheEntry(long id, long rootId, boolean reverse, String mapping, int objectClassId) {
        this.id = id;
        this.rootId = rootId;
        this.reverse = reverse;
        this.mapping = mapping;
        this.objectClassId = objectClassId;
    }

    public IdCacheEntry(long id, long rootId, boolean reverse, String mapping) {
        this(id, rootId, reverse, mapping, 0);
    }

    public long getId() {
        return id;
    }

    public long getRootId() {
        return rootId;
    }

    public boolean isReverse() {
        return reverse;
    }

    public String getMapping() {
        return mapping;
    }

    public boolean isRequested() {
        return isRequested.get();
    }

    protected boolean getAndSetRequested(boolean value) {
        return isRequested.getAndSet(value);
    }

    protected boolean isRegistered() {
        return isRegistered.get();
    }

    protected boolean getAndSetRegistered(boolean value) {
        return isRegistered.getAndSet(value);
    }

    public int getObjectClassId() {
        return objectClassId;
    }

}
