package org.citydb.cli.options.deleter;

import org.citydb.config.project.deleter.Continuation;
import org.citydb.config.project.global.UpdatingPersonMode;
import org.citydb.plugin.cli.CliOption;
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
