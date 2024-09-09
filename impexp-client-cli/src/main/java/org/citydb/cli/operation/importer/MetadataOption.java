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

package org.citydb.cli.operation.importer;

import org.citydb.cli.option.CliOption;
import org.citydb.config.project.global.UpdatingPersonMode;
import org.citydb.config.project.importer.Continuation;
import org.citydb.config.project.importer.CreationDateMode;
import org.citydb.config.project.importer.TerminationDateMode;
import picocli.CommandLine;

public class MetadataOption implements CliOption {
    enum DateMode {replace, complement, inherit}

    @CommandLine.Option(names = "--creation-date", paramLabel = "<mode>", defaultValue = "replace",
            description = "Creation date mode: ${COMPLETION-CANDIDATES}. Replace creation dates " +
                    "with the current timestamp, complement missing values with the current timestamp, " +
                    "or inherit missing values from the parent feature (default: ${DEFAULT-VALUE}).")
    private DateMode creationDate;

    @CommandLine.Option(names = "--termination-date", paramLabel = "<mode>", defaultValue = "replace",
            description = "Termination date mode: ${COMPLETION-CANDIDATES}. Replace termination dates " +
                    "with NULL, complement missing values with NULL, or inherit missing values from the " +
                    "parent feature (default: ${DEFAULT-VALUE}).")
    private DateMode terminationDate;

    @CommandLine.Option(names = "--lineage",
            description = "Lineage to use for the city objects.")
    private String lineage;

    @CommandLine.Option(names = "--updating-person", paramLabel = "<name>",
            description = "Name of the user responsible for the import (default: database user).")
    private String updatingPerson;

    @CommandLine.Option(names = "--reason-for-update", paramLabel = "<reason>",
            description = "Reason for importing the data.")
    private String reasonForUpdate;

    @CommandLine.Option(names = "--use-metadata-from-file",
            description = "Use lineage, updating person and reason for update from input file if available.")
    private boolean useMetadataFromFile;

    private Continuation continuation;

    public Continuation toContinuation() {
        Continuation continuation = new Continuation();

        switch (creationDate) {
            case complement:
                continuation.setCreationDateMode(CreationDateMode.COMPLEMENT);
                break;
            case inherit:
                continuation.setCreationDateMode(CreationDateMode.INHERIT);
                break;
            default:
                continuation.setCreationDateMode(CreationDateMode.REPLACE);
        }

        switch (terminationDate) {
            case complement:
                continuation.setTerminationDateMode(TerminationDateMode.COMPLEMENT);
                break;
            case inherit:
                continuation.setTerminationDateMode(TerminationDateMode.INHERIT);
                break;
            default:
                continuation.setTerminationDateMode(TerminationDateMode.REPLACE);
        }

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

        continuation.setImportCityDBMetadata(useMetadataFromFile);

        return continuation;
    }
}
