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

package org.citydb.cli.options.deleter;

import org.citydb.config.project.deleter.DeleteList;
import org.citydb.config.project.deleter.DeleteListIdType;
import org.citydb.plugin.cli.CliOption;
import picocli.CommandLine;

import java.nio.charset.Charset;
import java.nio.file.Path;

public class DeleteListOption implements CliOption {
    enum Type {resource, db}

    @CommandLine.Option(names = {"-f", "--delete-list"}, required = true,
            description = "Name of the CSV file containing the delete list.")
    private Path file;

    @CommandLine.Option(names = "--delete-list-encoding",
            description = "Encoding used for the CSV file (default: UTF-8).")
    private String encoding;

    @CommandLine.Option(names = {"-n", "--id-column-name"},
            description = "Name of the id column.")
    private String name;

    @CommandLine.Option(names = {"-I", "--id-column-index"},
            description = "Index of the id column (default: 1).")
    private Integer index;

    @CommandLine.Option(names = {"-C", "--id-column-type"}, paramLabel = "<type>", defaultValue = "resource",
            description = "Type of id column value: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Type type;

    @CommandLine.Option(names = {"-D", "--delimiter"}, paramLabel = "<char>", defaultValue = ",",
            description = "Delimiter used for separating values (default: ${DEFAULT-VALUE}).")
    private Character delimiter;

    @CommandLine.Option(names = "--no-header", negatable = true, defaultValue = "true",
            description = "CSV file uses a header row (default: ${DEFAULT-VALUE}).")
    private boolean header;

    @CommandLine.Option(names = "--quote", paramLabel = "<char>", defaultValue = "\"",
            description = "Character used to enclose the column values (default: ${DEFAULT-VALUE}).")
    private Character quote;

    @CommandLine.Option(names = "--comment-marker", paramLabel = "<char>",
            description = "Marker used to start a line comment.")
    private Character comment;

    @CommandLine.Option(names = "--escape", paramLabel = "<char>",
            description = "Character used for escaping the delimiter.")
    private Character escape;

    public DeleteList toDeleteList() {
        DeleteList deleteList = new DeleteList();

        deleteList.setFile(file.toAbsolutePath().toString());
        deleteList.setEncoding(encoding);
        deleteList.setIdColumnName(name);
        deleteList.setIdColumnIndex(index != null ? index : 1);
        deleteList.setIdType(type == Type.db ? DeleteListIdType.DATABASE_ID : DeleteListIdType.RESOURCE_ID);
        deleteList.setDelimiter(delimiter);
        deleteList.setHasHeader(header);
        deleteList.setQuoteCharacter(quote);
        deleteList.setCommentCharacter(comment);
        deleteList.setEscapeCharacter(escape);

        return deleteList;
    }

    @Override
    public void preprocess(CommandLine commandLine) {
        if (name != null) {
            if (index != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: --id-column-name and --id-column-index are mutually exclusive (specify only one)");
            } else if (!header) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: --id-column-name and --no-header are mutually exclusive (specify only one)");
            }
        }

        if (encoding != null && !Charset.isSupported(encoding)) {
            throw new CommandLine.ParameterException(commandLine,
                    "Error: The file encoding " + encoding + " is not supported");
        }
    }
}
