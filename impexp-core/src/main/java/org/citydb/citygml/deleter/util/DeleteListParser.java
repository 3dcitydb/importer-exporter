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

import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DeleteListParser implements AutoCloseable {
    private final Path file;

    private String name;
    private int index = 1;
    private IdType idType = IdType.GML_ID;
    private String delimiter = ",";
    private String commentStart = "#";
    private char quoteChar = '"';
    private boolean header = false;
    private Charset encoding = StandardCharsets.UTF_8;

    private long currentLineNumber;
    private BufferedReader reader;
    private String id;

    public enum IdType {
        GML_ID,
        DATABASE_ID
    }

    public DeleteListParser(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    public long getCurrentLineNumber() {
        return currentLineNumber;
    }

    public DeleteListParser withIdColumn(String name) {
        this.name = name;
        header = true;
        return this;
    }

    public DeleteListParser withIdColumn(int index) {
        this.index = index;
        return this;
    }

    public DeleteListParser withIdType(IdType idType) {
        this.idType = idType;
        return this;
    }

    public IdType getIdType() {
        return idType;
    }

    public DeleteListParser withDelimiter(String delimiter) {
        if (delimiter != null) {
            this.delimiter = delimiter;
        }

        return this;
    }

    public DeleteListParser withCommentStart(String commentStart) {
        this.commentStart = commentStart;
        return this;
    }

    public DeleteListParser withQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }

    public DeleteListParser withHeader(boolean header) {
        this.header = header;
        return this;
    }

    public DeleteListParser withEncoding(Charset encoding) {
        this.encoding = encoding;
        return this;
    }

    public boolean hasNext() throws IOException {
        if (id == null) {
            try {
                id = nextId();
            } catch (NoSuchElementException e) {
                return false;
            }
        }

        return id != null;
    }

    public String nextId() throws IOException {
        try {
            if (id == null) {
                String line = readLine();
                if (line == null) {
                    throw new NoSuchElementException();
                }

                String[] columns = line.split(delimiter);
                if (columns.length < index) {
                    throw new IOException("Found " + columns.length + " columns in delete list but expected " +
                            "the id value in column " + index + ".");
                }

                id = columns[index - 1];
                if (quoteChar != Character.MIN_VALUE
                        && id.length() > 1
                        && id.charAt(0) == quoteChar
                        && id.charAt(id.length() - 1) == quoteChar) {
                    id = id.substring(1, id.length() - 1);
                }
            }

            return id;
        } finally {
            id = null;
        }
    }

    private String readLine() throws IOException {
        if (reader == null) {
            reader = Files.newBufferedReader(file, encoding);
        }

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
    }

    public Iterator<Query> queryIterator(SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter) {
        return new QueryIterator(this, schemaMapping, databaseAdapter);
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
