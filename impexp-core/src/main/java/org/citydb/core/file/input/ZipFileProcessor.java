package org.citydb.core.file.input;

import org.apache.tika.exception.TikaException;
import org.apache.tika.mime.MediaType;
import org.citydb.core.file.InputFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ZipFileProcessor extends FileProcessor{

    private final FileTypeDetector fileTypeDetector;

    public ZipFileProcessor() throws TikaException, IOException {
        fileTypeDetector = new FileTypeDetector();
    }

    @Override
    public void process(Path file, List<InputFile> files, boolean force, Pattern contentFilePattern) throws IOException {
        processZipFile(file, files, contentFilePattern);
    }

    private void processZipFile(Path zipFile, List<InputFile> files, Pattern contentFilePattern) throws IOException {
        URI uri = URI.create("jar:" + zipFile.toAbsolutePath().toUri());
        Matcher matcher = Pattern.compile("").matcher("");
        FileSystem fileSystem = null;
        try {
            fileSystem = FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            //
        } catch (Throwable e) {
            throw new IOException("Failed to open zip file system '" + uri + "'.", e);
        }
        if (fileSystem == null) {
            try {
                fileSystem = FileSystems.newFileSystem(uri, new HashMap<>());
            } catch (Throwable e) {
                throw new IOException("Failed to open zip file system '" + uri + "'.", e);
            }
        }
        try (Stream<Path> stream = Files.walk(fileSystem.getPath("/")).filter(Files::isRegularFile)) {
            stream.forEach(path -> {
                matcher.reset(path.getFileName().toString()).usePattern(contentFilePattern);
                if (matcher.matches()) {
                    MediaType mediaType = this.fileTypeDetector.getMediaType(path);
                    if (isSupportedContentType(mediaType)) {
                        ZipInputFile zipInputFile = new ZipInputFile(path.toString(), zipFile, uri, mediaType);
                        files.add(zipInputFile);
                    }
                }
            });
        } catch (Throwable e) {
            throw new IOException("Failed to read zip file entries.", e);
        } finally {
            fileSystem.close();
        }
    }
}
