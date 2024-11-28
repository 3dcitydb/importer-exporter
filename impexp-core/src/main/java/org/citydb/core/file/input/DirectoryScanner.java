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

import org.apache.tika.exception.TikaException;
import org.citydb.core.file.InputFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DirectoryScanner {
    private final Matcher matcher;

    private volatile boolean shouldRun = true;
    private boolean recursive = false;

    public DirectoryScanner() throws TikaException, IOException {
        Pattern contentFile = Pattern.compile("(?i).+\\.((gml)|(xml)|(json)|(cityjson)|(gz)|(gzip))$");
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
        return new String[]{"gml", "xml", "json", "cityjson", "gz", "gzip", "zip"};
    }

    public List<InputFile> listFiles(List<Path> bases, String... fileEndings) throws IOException, TikaException {
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

    public List<InputFile> listFiles(Path base, String... fileEndings) throws IOException, TikaException {
        return listFiles(Collections.singletonList(base), fileEndings);
    }

    private void listFiles(Path base, List<InputFile> files, Pattern filePattern) throws IOException, TikaException {
        Pattern contentFile = Pattern.compile("(?i).+\\.((gml)|(xml)|(json)|(cityjson)|(gz)|(gzip))$");
        if (Files.isDirectory(base)) {
            Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    matcher.reset(file.getFileName().toString()).usePattern(filePattern);
                    if (matcher.matches()) {
                        FileProcessor fileProcessor = null;
                        try {
                            fileProcessor = FileProcessor.getProcessor(file);
                        } catch (TikaException e) {
                            throw new RuntimeException(e);
                        }
                        fileProcessor.process(file, files, false, contentFile);
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
            FileProcessor fileProcessor = FileProcessor.getProcessor(base);
            fileProcessor.process(base, files, true, contentFile);
        }
    }
}