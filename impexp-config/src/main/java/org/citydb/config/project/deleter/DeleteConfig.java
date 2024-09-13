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

package org.citydb.config.project.deleter;

import org.citydb.config.project.common.ComputeNumberMatched;
import org.citydb.config.project.common.IdList;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.query.QueryConfig;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "delete")
@XmlType(name = "DeleteType", propOrder = {
        "mode",
        "query",
        "simpleQuery",
        "deleteList",
        "cleanupGlobalAppearances",
        "autoCommit",
        "computeNumberMatched",
        "continuation",
        "deleteLog"
})
public class DeleteConfig {
    @XmlAttribute
    private boolean useSimpleQuery = true;
    @XmlAttribute
    private boolean useDeleteList;
    @XmlElement(required = true)
    private DeleteMode mode = DeleteMode.DELETE;
    private QueryConfig query;
    private SimpleQuery simpleQuery;
    private IdList deleteList;
    private boolean cleanupGlobalAppearances;
    private boolean autoCommit;
    private ComputeNumberMatched computeNumberMatched;
    private Continuation continuation;
    private DeleteLog deleteLog;

    public DeleteConfig() {
        query = new QueryConfig();
        simpleQuery = new SimpleQuery();
        computeNumberMatched = new ComputeNumberMatched();
        continuation = new Continuation();
        deleteLog = new DeleteLog();
    }

    public boolean isUseSimpleQuery() {
        return useSimpleQuery;
    }

    public void setUseSimpleQuery(boolean useSimpleQuery) {
        this.useSimpleQuery = useSimpleQuery;
    }

    public boolean isUseDeleteList() {
        return useDeleteList;
    }

    public void setUseDeleteList(boolean useDeleteList) {
        this.useDeleteList = useDeleteList;
    }

    public DeleteMode getMode() {
        return mode != null ? mode : DeleteMode.DELETE;
    }

    public void setMode(DeleteMode mode) {
        this.mode = mode;
    }

    public QueryConfig getQuery() {
        return query;
    }

    public void setQuery(QueryConfig query) {
        if (query != null)
            this.query = query;
    }

    public SimpleQuery getSimpleQuery() {
        return simpleQuery;
    }

    public void setSimpleQuery(SimpleQuery query) {
        if (query != null)
            this.simpleQuery = query;
    }

    public boolean isSetDeleteList() {
        return deleteList != null;
    }

    public IdList getDeleteList() {
        return deleteList;
    }

    public void setDeleteList(IdList deleteList) {
        this.deleteList = deleteList;
    }

    public boolean isCleanupGlobalAppearances() {
        return cleanupGlobalAppearances;
    }

    public void setCleanupGlobalAppearances(boolean cleanupGlobalAppearances) {
        this.cleanupGlobalAppearances = cleanupGlobalAppearances;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public ComputeNumberMatched getComputeNumberMatched() {
        return computeNumberMatched;
    }

    public void setComputeNumberMatched(ComputeNumberMatched computeNumberMatched) {
        if (computeNumberMatched != null) {
            this.computeNumberMatched = computeNumberMatched;
        }
    }

    public Continuation getContinuation() {
        return continuation;
    }

    public void setContinuation(Continuation continuation) {
        if (continuation != null)
            this.continuation = continuation;
    }

    public DeleteLog getDeleteLog() {
        return deleteLog;
    }

    public void setDeleteLog(DeleteLog deleteLog) {
        if (deleteLog != null) {
            this.deleteLog = deleteLog;
        }
    }
}
