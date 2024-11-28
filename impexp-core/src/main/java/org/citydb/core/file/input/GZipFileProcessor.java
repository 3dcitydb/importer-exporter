package org.citydb.core.file.input;

import org.apache.tika.exception.TikaException;
import org.apache.tika.mime.MediaType;
import org.citydb.core.file.InputFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class GZipFileProcessor extends FileProcessor{

    private final FileTypeDetector fileTypeDetector;

    public GZipFileProcessor() throws TikaException, IOException {
        fileTypeDetector = new FileTypeDetector();
    }

    @Override
    public void process(Path file, List<InputFile> files, boolean force, Pattern contentFilePattern) {
        try (InputStream stream = new GZIPInputStream(new FileInputStream(file.toFile()))) {
            // pass file name without gzip extension as hint for content detection
            String fileName = file.getFileName().toString();
            fileName = deriveFileNameWithoutGzipExtension(fileName);
            MediaType mediaType = fileTypeDetector.getMediaType(stream, fileName);
            if (isSupportedContentType(mediaType)) {
                files.add(new GZipInputFile(file, mediaType));
            }
        } catch (IOException e) {
            //
        }
    }

    private String deriveFileNameWithoutGzipExtension(String fileName) {
        if (fileName.endsWith(".gz")) {
            return fileName.substring(0, fileName.length() - 3);
        } else if (fileName.endsWith(".gzip")) {
            return fileName.substring(0, fileName.length() - 5);
        }
        return fileName;
    }

}
