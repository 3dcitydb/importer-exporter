package org.citydb.core.file.input;

import org.apache.tika.exception.TikaException;
import org.apache.tika.mime.MediaType;
import org.citydb.core.file.InputFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class RegularFileProcessor extends FileProcessor{

    private final FileTypeDetector fileTypeDetector;

    public RegularFileProcessor() throws TikaException, IOException {
        fileTypeDetector = new FileTypeDetector();
    }

    @Override
    public void process(Path file, List<InputFile> files, boolean force, Pattern contentFilePattern) {
        MediaType mediaType = fileTypeDetector.getMediaType(file);
        if (isSupportedContentType(mediaType)) {
            files.add(new RegularInputFile(file, mediaType));
        }
    }
}
