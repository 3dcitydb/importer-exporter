package org.citydb.citygml.deleter.util;

import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.util.CoreConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DeleteLogger {
    private final LocalDateTime date = LocalDateTime.now();
    private final Path logFile;
    private final BufferedWriter writer;
    private final DeleteMode mode;

    public DeleteLogger(Path logFile, DeleteMode mode, DatabaseConnection connection) throws IOException {
        Path defaultDir = CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.DELETE_LOG_DIR);
        if (logFile.toAbsolutePath().normalize().startsWith(defaultDir)) {
            Files.createDirectories(defaultDir);
        }

        if (Files.exists(logFile) && Files.isDirectory(logFile)) {
            logFile = logFile.resolve(getDefaultLogFileName());
        } else if (!Files.exists(logFile.getParent())) {
            Files.createDirectories(logFile.getParent());
        }

        this.logFile = logFile;
        this.mode = mode;
        writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8);
        writeHeader(connection);
    }

    public Path getLogFilePath() {
        return logFile;
    }

    private void writeHeader(DatabaseConnection connection) throws IOException {
        writer.write('#' + getClass().getPackage().getImplementationTitle() +
                ", version \"" + getClass().getPackage().getImplementationVersion() + "\"");
        writer.newLine();
        writer.write("#Delete mode: " + (mode == DeleteMode.TERMINATE ? "Terminate" : "Delete"));
        writer.newLine();
        writer.write("#Database connection: ");
        writer.write(connection.toConnectString());
        writer.newLine();
        writer.write("#Timestamp: ");
        writer.write(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        writer.newLine();
        writer.write("FEATURE_TYPE,CITYOBJECT_ID,GMLID");
        writer.newLine();
    }

    private void writeFooter(boolean success) throws IOException {
        writer.write("#" + (mode == DeleteMode.TERMINATE ? "Terminate" : "Delete"));
        writer.write(success ? " successfully finished." : "aborted.");
    }

    public void write(String type, long id, String gmlId) throws IOException {
        writer.write(type + "," + id + "," + (gmlId != null ? gmlId : "") + System.lineSeparator());
    }

    public String getDefaultLogFileName() {
        return "deleted-features-" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")) + ".log";
    }

    public void close(boolean success) throws IOException {
        writeFooter(success);
        writer.close();
    }
}