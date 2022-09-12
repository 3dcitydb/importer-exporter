/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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

package org.citydb.core.operation.common.csv;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.ResultIterator;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.citydb.config.project.common.IdList;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IdListParser implements AutoCloseable {
    private final IdList idList;
    private final CsvParser parser;
    private final ResultIterator<Record, ParsingContext> iterator;

    public IdListParser(IdList idList) throws IOException {
        this.idList = idList;

        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(idList.getFile()),
                    Charset.forName(idList.getEncoding()));

            parser = new CsvParser(defaultParserSettings(idList));
            iterator = parser.iterateRecords(reader).iterator();
        } catch (Exception e) {
            throw new IOException("Failed to open ID list file.", e);
        }
    }

    public static CsvParserSettings defaultParserSettings(IdList idList) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(idList.hasHeader() || idList.getIdColumnName() != null);
        settings.setSkipEmptyLines(true);
        settings.setLineSeparatorDetectionEnabled(true);

        CsvFormat format = settings.getFormat();
        format.setDelimiter(idList.getDelimiter());
        format.setQuote(idList.getQuoteCharacter());
        format.setComment(idList.getCommentCharacter() != null ? idList.getCommentCharacter() : '\0');
        format.setQuoteEscape(idList.getQuoteEscapeCharacter());

        return settings;
    }

    public IdList getIdList() {
        return idList;
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

    public String nextId() throws IdListException {
        try {
            long lineNumber = parser.getContext().currentLine();
            Record record = iterator.next();

            if (idList.getIdColumnName() != null) {
                if (!record.getMetaData().containsColumn(idList.getIdColumnName())) {
                    throw new IdListException("Column name '" + idList.getIdColumnName() + "' not found " +
                            "[line " + lineNumber + "]. Available columns are " +
                            String.join(", ", record.getMetaData().headers()) + ".");
                }

                return record.getString(idList.getIdColumnName());
            } else {
                int index = idList.getIdColumnIndex() - 1;
                int length = record.getValues().length;

                if (index >= length) {
                    throw new IdListException("Invalid column index " + idList.getIdColumnIndex() +
                            " [line " + lineNumber + "]. Number of available columns is " + length + ".");
                }

                return record.getString(index);
            }
        } catch (IdListException e) {
            throw e;
        } catch (Exception e) {
            throw new IdListException("An error occurred while parsing the ID list.", e);
        }
    }

    @Override
    public void close() throws IOException {
        parser.stopParsing();
    }
}
