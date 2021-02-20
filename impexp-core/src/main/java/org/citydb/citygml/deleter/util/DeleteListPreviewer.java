package org.citydb.citygml.deleter.util;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.citydb.config.project.deleter.DeleteList;
import org.citydb.log.Logger;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeleteListPreviewer {
    private final Logger log = Logger.getInstance();
    private final DeleteList deleteList;
    private long numberOfRecords = 20;

    private DeleteListPreviewer(DeleteList deleteList) {
        this.deleteList = deleteList;
    }

    public static DeleteListPreviewer of(DeleteList deleteList) {
        return new DeleteListPreviewer(deleteList);
    }

    public DeleteListPreviewer withNumberOfRecords(long numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
        return this;
    }

    public long getNumberOfRecords() {
        return numberOfRecords;
    }

    public void printToConsole() throws Exception {
        log.info("Creating preview for the delete list file '" + deleteList.getFile() + "'.");
        log.info("Printing the first " + numberOfRecords + " records of the delete list based on the provided CSV settings.");

        List<List<String>> records = new ArrayList<>();
        List<Long> lineNumbers = new ArrayList<>();
        int lineNumberWidth = (int) (Math.log10(numberOfRecords) + 1);

        CsvParserSettings settings = DeleteListParser.defaultParserSettings(deleteList);
        settings.setNumberOfRecordsToRead(numberOfRecords);
        settings.setEmptyValue("");

        CsvParser parser = new CsvParser(settings);
        parser.beginParsing(Files.newBufferedReader(Path.of(deleteList.getFile()),
                Charset.forName(deleteList.getEncoding())));

        String[] row;
        while ((row = parser.parseNext()) != null) {
            lineNumbers.add(parser.getContext().currentLine());
            records.add(Arrays.stream(row)
                    .map(v -> v != null ? v : "")
                    .collect(Collectors.toList()));
        }

        // get header names
        List<String> headerNames;
        if (deleteList.getIdColumnName() != null) {
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
        records.forEach(record -> {
            IntStream.range(0, record.size())
                    .forEach(i -> columnWidths[i] = Math.max(columnWidths[i], record.get(i).length()));
        });

        // print header line
        log.printToConsole(" ".repeat(lineNumberWidth + 1) +
                IntStream.range(0, columnWidths.length).boxed()
                        .map(i -> String.format("%-" + columnWidths[i] + "s", headerNames.get(i)))
                        .collect(Collectors.joining(" | ")));

        // print header underline
        log.printToConsole(" ".repeat(lineNumberWidth + 1) +
                IntStream.range(0, columnWidths.length).boxed()
                        .map(i -> "-".repeat(columnWidths[i]))
                        .collect(Collectors.joining("-+-")));

        // print records
        for (int i = 0; i < records.size(); i++) {
            List<String> record = records.get(i);
            log.printToConsole(String.format("%" + lineNumberWidth + "d ", lineNumbers.get(i)) +
                    IntStream.range(0, columnWidths.length).boxed()
                            .map(j -> String.format("%-" + columnWidths[j] + "s", record.get(j)))
                            .collect(Collectors.joining(" | ")));
        }
    }
}
