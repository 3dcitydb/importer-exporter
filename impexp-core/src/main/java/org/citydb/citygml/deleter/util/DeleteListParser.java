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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.citydb.config.project.deleter.DeleteList;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class DeleteListParser implements AutoCloseable {
    private final DeleteList deleteList;
    private final CSVParser parser;
    private final Iterator<CSVRecord> iterator;

    public DeleteListParser(DeleteList deleteList) throws IOException {
        this.deleteList = deleteList;

        try {
            BufferedReader reader = Files.newBufferedReader(Path.of(deleteList.getFile()),
                    Charset.forName(deleteList.getEncoding()));

            boolean withHeader = deleteList.hasHeader() || deleteList.getIdColumnName() != null;

            CSVFormat format = CSVFormat.DEFAULT
                    .withHeader(withHeader ? new String[0] : null)
                    .withDelimiter(deleteList.getDelimiter())
                    .withCommentMarker(deleteList.getCommentCharacter())
                    .withQuote(deleteList.getQuoteCharacter())
                    .withEscape(deleteList.getEscapeCharacter())
                    .withIgnoreHeaderCase();

            parser = new CSVParser(reader, format);
            iterator = parser.iterator();
        } catch (Exception e) {
            throw new IOException("Failed to open delete list file.", e);
        }
    }

    public DeleteList getDeleteList() {
        return deleteList;
    }

    public long getCurrentLineNumber() {
        return parser.getCurrentLineNumber();
    }

    public boolean hasNext() throws DeleteListException {
        return iterator.hasNext();
    }

    public String nextId() throws DeleteListException {
        CSVRecord record = iterator.next();

        if (deleteList.getIdColumnName() != null) {
            try {
                return record.get(deleteList.getIdColumnName());
            } catch (Exception e) {
                throw new DeleteListException("Failed to retrieve a column with name '" +
                        deleteList.getIdColumnName() + "'.");
            }
        } else {
            int index = deleteList.getIdColumnIndex() - 1;
            if (index >= record.size()) {
                throw new DeleteListException("The ID column index " + deleteList.getIdColumnIndex() + " is out of " +
                        "bounds. Only found " + record.size() + " columns in delete list.");
            }

            return record.get(index);
        }
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }
}
