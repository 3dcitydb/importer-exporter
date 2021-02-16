/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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

package org.citydb.citygml.deleter.util;

import org.citydb.config.project.deleter.DeleteList;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

public class DeleteListParser implements AutoCloseable {
    private final DeleteList deleteList;
    private final BufferedReader reader;

    private final String name;
    private final String delimiter;
    private final String commentStart;
    private final char quoteCharacter;

    private int index;
    private boolean header;
    private long currentLineNumber;
    private String id;

    public DeleteListParser(DeleteList deleteList) throws IOException {
        this.deleteList = deleteList;

        try {
            reader = Files.newBufferedReader(Path.of(deleteList.getFile()), Charset.forName(deleteList.getEncoding()));
        } catch (Exception e) {
            throw new IOException("Failed to open delete list file.", e);
        }

        if (deleteList.getName() != null) {
            name = deleteList.getName();
            index = 1;
            header = true;
        } else {
            name = null;
            index = deleteList.getIndex();
            header = deleteList.hasHeader();
        }

        delimiter = deleteList.getDelimiter();
        commentStart = deleteList.getCommentStart();
        String quoteCharacter = deleteList.getQuoteCharacter().trim();
        this.quoteCharacter = !quoteCharacter.isEmpty() ? quoteCharacter.charAt(0) : '"';
    }

    public DeleteList getDeleteList() {
        return deleteList;
    }

    public long getCurrentLineNumber() {
        return currentLineNumber;
    }

    public boolean hasNext() throws DeleteListException {
        if (id == null) {
            try {
                id = nextId();
            } catch (NoSuchElementException e) {
                return false;
            }
        }

        return id != null;
    }

    public String nextId() throws DeleteListException {
        try {
            if (id == null) {
                String line = readLine();
                if (line == null) {
                    throw new NoSuchElementException();
                }

                String[] columns = line.split(delimiter);
                if (columns.length < index) {
                    throw new DeleteListException("Found " + columns.length + " columns in delete list but expected " +
                            "the id value in column " + index + ".");
                }

                id = columns[index - 1].trim();
                if (quoteCharacter != Character.MIN_VALUE
                        && id.length() > 1
                        && id.charAt(0) == quoteCharacter
                        && id.charAt(id.length() - 1) == quoteCharacter) {
                    id = id.substring(1, id.length() - 1);
                }
            }

            return id;
        } finally {
            id = null;
        }
    }

    private String readLine() throws DeleteListException {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                ++currentLineNumber;

                if (commentStart == null || !line.startsWith(commentStart)) {
                    if (header) {
                        if (name != null) {
                            boolean found = false;
                            String[] columns = line.split(delimiter);
                            for (int i = 0; i < columns.length; i++) {
                                if (name.equalsIgnoreCase(columns[i])) {
                                    index = i + 1;
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                throw new DeleteListException("Failed to find a column named '" + name + "' " +
                                        "in delete list (line " + currentLineNumber + ").");
                            }
                        }

                        header = false;
                    } else {
                        return line;
                    }
                }
            }

            return null;
        } catch (IOException e) {
            throw new DeleteListException("Failed to parse the delete list.", e);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
