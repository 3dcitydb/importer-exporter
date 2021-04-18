package org.citydb.cli.options.importer;

import org.citydb.config.project.global.UpdatingPersonMode;
import org.citydb.config.project.importer.Continuation;
import org.citydb.config.project.importer.CreationDateMode;
import org.citydb.config.project.importer.TerminationDateMode;
import org.citydb.plugin.cli.CliOption;
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
