package org.citydb.citygml.importer.file;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.citydb.config.internal.InputFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class DirectoryScanner {
    private final TikaConfig tikaConfig;
    private final Pattern contentFile;
    private final Matcher matcher;

    private volatile boolean shouldRun = true;
    private boolean recursive = false;

    public DirectoryScanner() throws TikaException, IOException {
        tikaConfig = new TikaConfig();
        contentFile = Pattern.compile("(?i).+\\.((gml)|(xml)|(gz)|(gzip))$");
        matcher = Pattern.compile("").matcher("");
    }

    public DirectoryScanner(boolean recursive) throws TikaException, IOException {
        this();
        this.recursive = recursive;
    }

    public void cancel() {
        shouldRun = false;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public String[] getDefaultFileEndings() {
        return new String[]{".gml", ".xml", ".gz", ".gzip", ".zip"};
    }

    public List<InputFile> listFiles(List<Path> bases, String... fileEndings) throws IOException {
        if (fileEndings.length == 0)
            fileEndings = getDefaultFileEndings();

        Pattern pattern = Pattern.compile("(?i).+((" + Arrays.stream(fileEndings)
                .map(Pattern::quote).collect(Collectors.joining(")|(")) + "))$");

        List<InputFile> files = new ArrayList<>();
        for (Path base : bases)
            listFiles(base, files, pattern);

        return files;
    }

    public List<InputFile> listFiles(Path base, String... fileEndings) throws IOException {
        return listFiles(Collections.singletonList(base), fileEndings);
    }

    private void listFiles(Path base, List<InputFile> files, Pattern filePattern) throws IOException {
        if (Files.isDirectory(base)) {
            Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    matcher.reset(file.getFileName().toString()).usePattern(filePattern);
                    if (matcher.matches())
                        processFile(file, files, false);

                    return shouldRun ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (shouldRun)
                        return recursive || base.equals(dir) ? super.preVisitDirectory(dir, attrs) : FileVisitResult.SKIP_SUBTREE;
                    else
                        return FileVisitResult.TERMINATE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    return shouldRun ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.TERMINATE;
                }
            });
        } else
            processFile(base, files, true);
    }

    private void processFile(Path file, List<InputFile> files, boolean force) {
        if (!shouldRun)
            return;

        MediaType mediaType = getMediaType(file);

        if (mediaType == MediaType.APPLICATION_ZIP)
            processZipFile(file, files);
        else if ("application/gzip".equalsIgnoreCase(mediaType.toString()))
            processGZipFile(file, files);
        else if (isSupportedContentType(mediaType)) {
            files.add(new XMLInputFile(file));
        } else if (force)
            files.add(new XMLInputFile(file));
    }

    private void processGZipFile(Path gzipFile, List<InputFile> files) {
        try {
            MediaType mediaType = getMediaType(new GZIPInputStream(new FileInputStream(gzipFile.toFile())));
            if (isSupportedContentType(mediaType))
                files.add(new GZipInputFile(gzipFile));
        } catch (IOException ignored) {
            //
        }
    }

    private void processZipFile(Path zipFile, List<InputFile> files) {
        URI uri = URI.create("jar:" + zipFile.toAbsolutePath().toUri());

        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, new HashMap<>())) {
            Files.walk(fileSystem.getPath("/")).filter(Files::isRegularFile).forEach(path -> {
                matcher.reset(path.getFileName().toString()).usePattern(contentFile);
                if (matcher.matches()) {
                    MediaType mediaType = getMediaType(path);
                    if (isSupportedContentType(mediaType)) {
                        ZipInputFile zipInputFile = new ZipInputFile(path.toString(), zipFile, uri);
                        files.add(zipInputFile);
                    }
                }
            });
        } catch (Throwable ignored) {
            //
        }
    }

    private MediaType getMediaType(Path file) {
        try {
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, file.toString());
            return tikaConfig.getDetector().detect(TikaInputStream.get(file), metadata);
        } catch (IOException e) {
            return MediaType.EMPTY;
        }
    }

    private MediaType getMediaType(InputStream stream) {
        try {
            return tikaConfig.getDetector().detect(TikaInputStream.get(stream), new Metadata());
        } catch (IOException e) {
            return MediaType.EMPTY;
        }
    }

    private boolean isSupportedContentType(MediaType mediaType) {
        return mediaType == MediaType.APPLICATION_XML
                || mediaType == MediaType.TEXT_PLAIN;
    }

}
