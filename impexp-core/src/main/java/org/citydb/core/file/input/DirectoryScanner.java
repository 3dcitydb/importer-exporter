/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citydb.core.file.input;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.citydb.core.file.InputFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class DirectoryScanner {
    private final TikaConfig tikaConfig;
    private final Pattern contentFile;
    private final Matcher matcher;

    private volatile boolean shouldRun = true;
    private boolean recursive = false;

    public DirectoryScanner() throws TikaException, IOException {
        tikaConfig = new TikaConfig();
        contentFile = Pattern.compile("(?i).+\\.((gml)|(xml)|(json)|(cityjson)|(gz)|(gzip))$");
        matcher = Pattern.compile("").matcher("");

        // map additional file extensions to mime types
        tikaConfig.getMimeRepository().addPattern(MimeTypes.getDefaultMimeTypes().forName("application/json"), "*.cityjson");
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
        return new String[]{"gml", "xml", "json", "cityjson", "gz", "gzip", "zip"};
    }

    public List<InputFile> listFiles(List<Path> bases, String... fileEndings) throws IOException {
        if (fileEndings.length == 0) {
            fileEndings = getDefaultFileEndings();
        }

        Pattern pattern = Pattern.compile("(?i).+\\.((" + Arrays.stream(fileEndings)
                .map(Pattern::quote)
                .collect(Collectors.joining(")|(")) + "))$");

        List<InputFile> files = new ArrayList<>();
        for (Path base : bases) {
            if (!Files.exists(base)) {
                throw new FileNotFoundException("Failed to find resource '" + base.toString() + "'.");
            }

            listFiles(base, files, pattern);
        }

        return files;
    }

    public List<InputFile> listFiles(Path base, String... fileEndings) throws IOException {
        return listFiles(Collections.singletonList(base), fileEndings);
    }

    private void listFiles(Path base, List<InputFile> files, Pattern filePattern) throws IOException {
        if (Files.isDirectory(base)) {
            Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    matcher.reset(file.getFileName().toString()).usePattern(filePattern);
                    if (matcher.matches()) {
                        processFile(file, files, false);
                    }

                    return shouldRun ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (shouldRun) {
                        return recursive || base.equals(dir) ? super.preVisitDirectory(dir, attrs) : FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.TERMINATE;
                    }
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    return shouldRun ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.TERMINATE;
                }
            });
        } else {
            processFile(base, files, true);
        }
    }

    private void processFile(Path file, List<InputFile> files, boolean force) throws IOException {
        if (!shouldRun)
            return;

        MediaType mediaType = getMediaType(file);
        if (mediaType.equals(InputFile.APPLICATION_ZIP)) {
            processZipFile(file, files);
        } else if (mediaType.equals(InputFile.APPLICATION_GZIP)) {
            processGZipFile(file, files);
        } else if (isSupportedContentType(mediaType) || force) {
            files.add(new RegularInputFile(file, mediaType));
        }
    }

    private void processGZipFile(Path gzipFile, List<InputFile> files) {
        try (InputStream stream = new GZIPInputStream(new FileInputStream(gzipFile.toFile()))) {
            // pass file name without gzip extension as hint for content detection
            String fileName = gzipFile.getFileName().toString();
            if (fileName.endsWith(".gz")) {
                fileName = fileName.substring(0, fileName.length() - 3);
            } else if (fileName.endsWith(".gzip")) {
                fileName = fileName.substring(0, fileName.length() - 5);
            }

            MediaType mediaType = getMediaType(stream, fileName);
            if (isSupportedContentType(mediaType)) {
                files.add(new GZipInputFile(gzipFile, mediaType));
            }
        } catch (IOException e) {
            //
        }
    }

    private void processZipFile(Path zipFile, List<InputFile> files) throws IOException {
        URI uri = URI.create("jar:" + zipFile.toAbsolutePath().toUri());

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
                matcher.reset(path.getFileName().toString()).usePattern(contentFile);
                if (matcher.matches()) {
                    MediaType mediaType = getMediaType(path);
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

    private MediaType getMediaType(Path file) {
        try (InputStream stream = TikaInputStream.get(file)) {
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.toString());
            return tikaConfig.getDetector().detect(stream, metadata);
        } catch (IOException e) {
            return MediaType.EMPTY;
        }
    }

    private MediaType getMediaType(InputStream stream, String fileName) {
        try {
            Metadata metadata = new Metadata();
            if (fileName != null) {
                metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
            }

            return tikaConfig.getDetector().detect(TikaInputStream.get(stream), metadata);
        } catch (IOException e) {
            return MediaType.EMPTY;
        }
    }

    private boolean isSupportedContentType(MediaType mediaType) {
        return mediaType.equals(InputFile.APPLICATION_XML)
                || mediaType.equals(InputFile.APPLICATION_JSON);
    }
}
