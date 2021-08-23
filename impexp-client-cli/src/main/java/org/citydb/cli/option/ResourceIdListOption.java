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

package org.citydb.cli.option;

import org.citydb.config.project.common.IdColumnType;
import org.citydb.config.project.common.IdList;
import picocli.CommandLine;

import java.nio.charset.Charset;
import java.util.function.Supplier;

public class ResourceIdListOption implements CliOption {
    @CommandLine.Option(names = {"-n", "--id-column-name"},
            description = "Name of the id column.")
    private String name;

    @CommandLine.Option(names = {"-I", "--id-column-index"},
            description = "Index of the id column (default: 1).")
    private Integer index;

    @CommandLine.Option(names = "--no-header", negatable = true, defaultValue = "true",
            description = "CSV file uses a header row (default: ${DEFAULT-VALUE}).")
    private boolean header = true;

    @CommandLine.Option(names = {"-D", "--delimiter"}, paramLabel = "<string>", defaultValue = ",",
            description = "Delimiter used for separating values (default: ${DEFAULT-VALUE}).")
    private String delimiter = ",";

    @CommandLine.Option(names = {"-Q", "--quote"}, paramLabel = "<char>", defaultValue = "\"",
            description = "Character used to enclose the column values (default: ${DEFAULT-VALUE}).")
    private Character quote = '"';

    @CommandLine.Option(names = "--quote-escape", paramLabel = "<char>", defaultValue = "\"",
            description = "Character used for escaping quotes (default: ${DEFAULT-VALUE}).")
    private Character escape = '"';

    @CommandLine.Option(names = {"-M", "--comment-marker"}, paramLabel = "<char>", defaultValue = "#",
            description = "Marker used to start a line comment (default: ${DEFAULT-VALUE}). " +
                    "Use 'none' to disable comments.")
    private String comment = "#";

    @CommandLine.Option(names = "--csv-encoding",
            description = "Encoding used for the CSV file (default: UTF-8).")
    private String encoding;

    public <T extends IdList> T toIdList(Supplier<T> idListSupplier) {
        T idList = idListSupplier.get();

        idList.setIdColumnName(name);
        idList.setIdColumnIndex(index != null ? index : 1);
        idList.setIdColumnType(IdColumnType.RESOURCE_ID);
        idList.setDelimiter(delimiter);
        idList.setHasHeader(header);
        idList.setQuoteCharacter(quote);
        idList.setQuoteEscapeCharacter(escape);
        idList.setCommentCharacter(comment != null && !comment.isEmpty() ? comment.charAt(0) : null);
        idList.setEncoding(encoding);

        return idList;
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

        if ("none".equals(comment)) {
            comment = null;
        } else if (comment != null && comment.length() > 1) {
            throw new CommandLine.ParameterException(commandLine,
                    "Invalid value for option '--comment-marker': '" + comment + "' is not a single character");
        }
    }
}
