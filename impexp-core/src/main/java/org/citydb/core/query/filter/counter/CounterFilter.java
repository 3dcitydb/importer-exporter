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
package org.citydb.core.query.filter.counter;

import org.citydb.core.query.filter.selection.operator.comparison.ComparisonOperatorName;

public class CounterFilter {
    private long count = -1;
    private long startIndex = -1;
    private long startId = -1;
    private ComparisonOperatorName comparisonOperator;

    public CounterFilter() {
    }

    public boolean isSetCount() {
        return count != -1;
    }

    public long getCount() {
        return isSetCount() ? count : 0;
    }

    public void setCount(long count) {
        this.count = Math.max(count, -1);
    }

    public boolean isSetStartIndex() {
        return startIndex != -1;
    }

    public long getStartIndex() {
        return isSetStartIndex() ? startIndex : 0;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = Math.max(startIndex, -1);
        startId = -1;
    }

    public boolean isSetStartId() {
        return startId != -1;
    }

    public long getStartId() {
        return isSetStartId() ? startId : 0;
    }

    public void setStartId(long startId) {
        this.startId = Math.max(startId, -1);
        startIndex = -1;
    }

    public void setStartId(long startId, ComparisonOperatorName comparisonOperator) {
        setStartId(startId);
        this.comparisonOperator = comparisonOperator;
    }

    public ComparisonOperatorName getStartIdComparisonOperator() {
        return comparisonOperator != null ? comparisonOperator : ComparisonOperatorName.GREATER_THAN;
    }
}
