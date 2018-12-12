package org.citydb.citygml.exporter.file;

import org.citydb.config.Config;
import org.citydb.config.internal.FileType;
import org.citydb.config.internal.OutputFile;
import org.citydb.event.EventDispatcher;
import org.citydb.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputFileFactory {
    private final Config config;
    private final EventDispatcher eventDispatcher;
    private final Object eventChannel;

    public OutputFileFactory(Config config, EventDispatcher eventDispatcher, Object eventChannel) {
        this.config = config;
        this.eventDispatcher = eventDispatcher;
        this.eventChannel = eventChannel;
    }

    public OutputFileFactory(Config config, EventDispatcher eventDispatcher) {
        this(config, eventDispatcher, null);
    }

    public OutputFile createOutputFile(Path file) throws IOException {
        file = file.toAbsolutePath().normalize();
        Files.createDirectories(file.getParent());

        String extension = Util.getFileExtension(file.getFileName().toString());
        if (extension.isEmpty()) {
            extension = "gml";
            file = file.resolveSibling(file.getFileName() + ".gml");
        }

        switch (extension) {
            case "zip":
                return new ZipOutputFile(Util.stripFileExtension(file.getFileName().toString()) + ".gml",
                        file,
                        file.getParent(),
                        config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(),
                        eventDispatcher,
                        eventChannel);
            case "gzip":
            case "gz":
                return new GZipOutputFile(file);
            default:
                return new XMLOutputFile(file);
        }
    }

    public FileType getFileType(Path file) {
        switch (Util.getFileExtension(file.getFileName().toString())) {
            case "zip":
                return FileType.ARCHIVE;
            case "gzip":
            case "gz":
                return FileType.COMPRESSED;
            default:
                return FileType.REGULAR;
        }
    }
}
