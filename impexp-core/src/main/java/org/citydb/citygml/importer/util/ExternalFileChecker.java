package org.citydb.citygml.importer.util;

import org.citydb.config.internal.InputFile;
import org.citydb.config.internal.FileType;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;

public class ExternalFileChecker {
    private final InputFile inputFile;
    private final boolean replaceSeparator;

    public ExternalFileChecker(InputFile inputFile) {
        this.inputFile = inputFile;
        replaceSeparator = inputFile.getSeparator().equals("/");
    }

    public Map.Entry<String, String> getFileInfo(String imageURI) throws IOException {
        try {
            new URL(imageURI);
            return new AbstractMap.SimpleEntry<>(imageURI, imageURI);
        } catch (MalformedURLException ignored) {
            //
        }

        String path = null;
        Path file = null;
        if (replaceSeparator)
            imageURI = imageURI.replace("\\", "/");

        try {
            file = inputFile.resolve(imageURI);
            if (Files.exists(file))
                path = imageURI;
        } catch (InvalidPathException e) {
            //
        }

        if (path == null && inputFile.getType() == FileType.ARCHIVE) {
            try {
                file = inputFile.getFile().getParent().resolve(imageURI);
                if (Files.exists(file))
                    path = file.toString();
            } catch (InvalidPathException e) {
                //
            }
        }

        if (path == null)
            throw new IOException("Failed to find file.");

        if (Files.size(file) == 0)
            throw new IOException("Zero byte file.");

        return new AbstractMap.SimpleEntry<>(path, file.getFileName().toString());
    }

    public InputFile getInputFile() {
        return inputFile;
    }
}
