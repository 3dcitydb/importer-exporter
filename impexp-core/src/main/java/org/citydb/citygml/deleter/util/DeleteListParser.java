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

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.ResultIterator;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.citydb.config.project.deleter.DeleteList;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DeleteListParser implements AutoCloseable {
    private final DeleteList deleteList;
    private final CsvParser parser;
    private final ResultIterator<Record, ParsingContext> iterator;

    public DeleteListParser(DeleteList deleteList) throws IOException {
        this.deleteList = deleteList;

        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(deleteList.getFile()),
                    Charset.forName(deleteList.getEncoding()));

            parser = new CsvParser(defaultParserSettings(deleteList));
            iterator = parser.iterateRecords(reader).iterator();
        } catch (Exception e) {
            throw new IOException("Failed to open delete list file.", e);
        }
    }

    public static CsvParserSettings defaultParserSettings(DeleteList deleteList) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(deleteList.hasHeader() || deleteList.getIdColumnName() != null);
        settings.setSkipEmptyLines(true);

        CsvFormat format = settings.getFormat();
        format.setDelimiter(deleteList.getDelimiter());
        format.setQuote(deleteList.getQuoteCharacter());
        format.setComment(deleteList.getCommentCharacter() != null ? deleteList.getCommentCharacter() : '\0');
        format.setQuoteEscape(deleteList.getQuoteEscapeCharacter());

        return settings;
    }

    public DeleteList getDeleteList() {
        return deleteList;
    }

    public CsvParser getCsvParser() {
        return parser;
    }

    public long getCurrentLineNumber() {
        return parser.getContext() != null ? parser.getContext().currentLine() : 0;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public String nextId() throws DeleteListException {
        try {
            long lineNumber = parser.getContext().currentLine();
            Record record = iterator.next();

            if (deleteList.getIdColumnName() != null) {
                if (!record.getMetaData().containsColumn(deleteList.getIdColumnName())) {
                    throw new DeleteListException("Column name '" + deleteList.getIdColumnName() + "' not found " +
                            "[line " + lineNumber + "]. Available columns are " +
                            String.join(", ", record.getMetaData().headers()) + ".");
                }

                return record.getString(deleteList.getIdColumnName());
            } else {
                int index = deleteList.getIdColumnIndex() - 1;
                int length = record.getValues().length;

                if (index >= length) {
                    throw new DeleteListException("Invalid column index " + deleteList.getIdColumnIndex() +
                            " [line " + lineNumber + "]. Number of available columns is " + length + ".");
                }

                return record.getString(index);
            }
        } catch (DeleteListException e) {
            throw e;
        } catch (Exception e) {
            throw new DeleteListException("An error occurred while parsing the delete list.", e);
        }
    }

    @Override
    public void close() throws IOException {
        parser.stopParsing();
    }
}
