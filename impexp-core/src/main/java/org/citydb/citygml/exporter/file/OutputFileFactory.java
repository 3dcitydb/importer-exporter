package org.citydb.citygml.exporter.file;

import org.citydb.config.internal.OutputFile;
import org.citydb.util.Util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class OutputFileFactory {

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
                return createZipOutputFile(file);
            case "gzip":
            case "gz":
                return new GZipOutputFile(file);
            default:
                return new XMLOutputFile(file);
        }
    }

    private ZipOutputFile createZipOutputFile(Path file) throws IOException {
        URI uri = URI.create("jar:" + file.toUri());

        try {
            FileSystem fileSystem = FileSystems.getFileSystem(uri);
            fileSystem.close();
        } catch (Throwable ignored) {
            //
        }

        Files.deleteIfExists(file);

        Map<String, Object> env = new HashMap<>();
        env.put("create", "true");
        env.put("useTempFile", true);
        FileSystem fileSystem = FileSystems.newFileSystem(uri, env);
        String contentFile = "/" + Util.stripFileExtension(file.getFileName().toString()) + ".gml";

        return new ZipOutputFile(contentFile, file, fileSystem);
    }
}
