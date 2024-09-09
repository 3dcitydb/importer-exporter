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
package org.citydb.core.operation.exporter.database.content;

import org.citydb.core.database.schema.mapping.AbstractObjectType;

public class DBSplittingResult {
    private final long id;
    private final AbstractObjectType<?> objectType;
    private final Object envelope;
    private final long sequenceId;

    public DBSplittingResult(long id, AbstractObjectType<?> objectType, Object envelope, long sequenceId) {
        this.id = id;
        this.objectType = objectType;
        this.envelope = envelope;
        this.sequenceId = sequenceId;
    }

    public DBSplittingResult(long id, AbstractObjectType<?> objectType, Object envelope) {
        this(id, objectType, envelope, -1);
    }

    public DBSplittingResult(long id, AbstractObjectType<?> objectType, long sequenceId) {
        this(id, objectType, null, sequenceId);
    }

    public DBSplittingResult(long id, AbstractObjectType<?> objectType) {
        this(id, objectType, null, -1);
    }

    public DBSplittingResult(DBSplittingResult other, long sequenceId) {
        this(other.id, other.objectType, other.envelope, sequenceId);
    }

    public long getId() {
        return id;
    }

    public AbstractObjectType<?> getObjectType() {
        return objectType;
    }

    public Object getEnvelope() {
        return envelope;
    }

    public long getSequenceId() {
        return sequenceId;
    }
}
