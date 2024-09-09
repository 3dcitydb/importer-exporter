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
package org.citydb.core.query.filter.lod;

import java.util.NoSuchElementException;

public class LodIterator {
    private final LodFilter filter;
    private final int fromLod;
    private final int toLod;

    private final int limit;
    private final int update;
    private int next;
    private int current;

    LodIterator(LodFilter filter, int fromLod, int toLod, boolean reverse) {
        this.filter = filter;
        this.fromLod = fromLod;
        this.toLod = toLod;

        limit = reverse ? fromLod - 1 : toLod + 1;
        update = reverse ? -1 : 1;

        reset();
    }

    public void reset() {
        current = update == -1 ? toLod + 1 : fromLod - 1;
        next = -1;
    }

    public boolean hasNext() {
        if (next == -1) {
            try {
                next = next();
            } catch (NoSuchElementException e) {
                //
            }
        }

        return next != -1;
    }

    public int next() {
        if (next == -1) {
            boolean hasFound = false;

            for (int lod = current + update; lod != limit; lod += update) {
                if (filter.isEnabled(lod)) {
                    current = lod;
                    hasFound = true;
                    break;
                }
            }

            if (!hasFound)
                throw new NoSuchElementException();

        } else {
            current = next;
            next = -1;
        }

        return current;
    }

}
