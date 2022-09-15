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

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.citydb.config.project.common.IdList;
import org.citydb.util.log.Logger;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IdListPreviewer {
    private final Logger log = Logger.getInstance();
    private final String file;
    private final IdList idList;
    private long numberOfRecords = 20;

    private IdListPreviewer(String file, IdList idList) {
        this.file = file;
        this.idList = idList;
    }

    public static IdListPreviewer of(String file, IdList idList) {
        return new IdListPreviewer(file, idList);
    }

    public IdListPreviewer withNumberOfRecords(long numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
        return this;
    }

    public long getNumberOfRecords() {
        return numberOfRecords;
    }

    public void printToConsole() throws Exception {
        log.info("Generating preview for the CSV file '" + file + "'.");
        log.info("Printing the first " + numberOfRecords + " records based on the provided CSV settings.");

        List<List<String>> records = new ArrayList<>();
        List<Long> lineNumbers = new ArrayList<>();
        int lineNumberWidth = (int) (Math.log10(numberOfRecords) + 1);

        CsvParserSettings settings = IdListParser.defaultParserSettings(idList);
        settings.setNumberOfRecordsToRead(numberOfRecords);
        settings.setEmptyValue("");

        CsvParser parser = new CsvParser(settings);
        parser.beginParsing(Files.newBufferedReader(Paths.get(file), Charset.forName(idList.getEncoding())));

        String[] row;
        while ((row = parser.parseNext()) != null) {
            lineNumbers.add(parser.getContext().currentLine());
            records.add(Arrays.stream(row)
                    .map(v -> v != null ? v : "")
                    .collect(Collectors.toList()));
        }

        if (records.isEmpty() && parser.getContext().headers() == null) {
            log.warn("No records read from CSV file.");
            return;
        }

        log.printToConsole("");

        // get header names
        List<String> headerNames;
        if (idList.getIdColumnName() != null) {
            headerNames = Arrays.asList(parser.getContext().headers());
        } else {
            headerNames = new ArrayList<>();
            if (!records.isEmpty()) {
                IntStream.range(1, records.get(0).size() + 1).boxed()
                        .map(i -> "Column " + i)
                        .forEach(headerNames::add);
            }
        }

        // adapt records to number of header names
        for (int i = 0; i < records.size(); i++) {
            List<String> record = records.get(i);
            while (record.size() < headerNames.size()) {
                record.add("<missing>");
            }

            if (record.size() > headerNames.size()) {
                records.set(i, record.subList(0, headerNames.size()));
            }
        }

        // calculate column widths
        int[] columnWidths = new int[headerNames.size()];
        IntStream.range(0, headerNames.size()).forEach(i -> columnWidths[i] = headerNames.get(i).length());
        records.forEach(record -> IntStream.range(0, record.size())
                .forEach(i -> columnWidths[i] = Math.max(columnWidths[i], record.get(i).length())));

        // print header line
        log.printToConsole(repeat(' ', lineNumberWidth + 1) +
                IntStream.range(0, columnWidths.length).boxed()
                        .map(i -> String.format("%-" + columnWidths[i] + "s", headerNames.get(i)))
                        .collect(Collectors.joining(" | ")));

        // print header underline
        log.printToConsole(repeat(' ', lineNumberWidth + 1) +
                IntStream.range(0, columnWidths.length).boxed()
                        .map(i -> repeat('-', columnWidths[i]))
                        .collect(Collectors.joining("-+-")));

        // print records
        for (int i = 0; i < records.size(); i++) {
            List<String> record = records.get(i);
            log.printToConsole(String.format("%" + lineNumberWidth + "d ", lineNumbers.get(i)) +
                    IntStream.range(0, columnWidths.length).boxed()
                            .map(j -> String.format("%-" + columnWidths[j] + "s", record.get(j)))
                            .collect(Collectors.joining(" | ")));
        }

        log.printToConsole("");
    }

    private String repeat(char c, int count) {
        char[] value = new char[count];
        Arrays.fill(value, c);
        return new String(value);
    }
}
