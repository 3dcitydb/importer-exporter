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

package org.citydb.cli.operation.deleter;

import org.citydb.cli.option.CliOption;
import org.citydb.config.project.deleter.Continuation;
import org.citydb.config.project.global.UpdatingPersonMode;
import picocli.CommandLine;

public class MetadataOption implements CliOption {
    @CommandLine.Option(names = "--lineage",
            description = "Lineage to use for the city objects.")
    private String lineage;

    @CommandLine.Option(names = "--updating-person", paramLabel = "<name>",
            description = "Name of the user responsible for the delete (default: database user).")
    private String updatingPerson;

    @CommandLine.Option(names = "--reason-for-update", paramLabel = "<reason>",
            description = "Reason for deleting the data.")
    private String reasonForUpdate;

    private Continuation continuation;

    public Continuation toContinuation() {
        Continuation continuation = new Continuation();

        if (lineage != null) {
            continuation.setLineage(lineage);
        }

        if (updatingPerson != null) {
            continuation.setUpdatingPersonMode(UpdatingPersonMode.USER);
            continuation.setUpdatingPerson(updatingPerson);
        } else {
            continuation.setUpdatingPersonMode(UpdatingPersonMode.DATABASE);
        }

        if (reasonForUpdate != null) {
            continuation.setReasonForUpdate(reasonForUpdate);
        }

        return continuation;
    }
}
