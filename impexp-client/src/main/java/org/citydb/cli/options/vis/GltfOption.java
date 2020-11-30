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

package org.citydb.cli.options.vis;

import org.citydb.plugin.cli.CliOption;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GltfOption implements CliOption {
    @CommandLine.Option(names = {"-G", "--gltf"}, required = true,
            description = "Convert COLLADA output to glTF.")
    private boolean exportGltf;

    @CommandLine.Option(names = "--gltf-converter", paramLabel = "<file>",
            description = "Path to the COLLADA2GLTF converter executable.")
    private Path file;

    @CommandLine.Option(names = {"-O", "--gltf-option"}, split = ",", paramLabel = "'<option>'",
            description = "CLI option to be passed to the COLLADA2GLTF converter (embrace with single quotes)")
    private String[] options;

    @CommandLine.Option(names = {"-s", "--suppress-collada"},
            description = "Only keep glTF and remove the COLLADA output.")
    private boolean suppressCollada;

    private List<String> parsedOptions;

    public Path getConverterPath() {
        return file;
    }

    public List<String> getOptions() {
        return parsedOptions;
    }

    public boolean isSuppressCollada() {
        return suppressCollada;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        if (options != null) {
            parsedOptions = new ArrayList<>();
            Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
            Matcher matcher = pattern.matcher("");

            for (String option : options) {
                if (option.charAt(0) == '\'' && option.charAt(option.length() - 1) == '\'') {
                    option = option.substring(1, option.length() - 1);
                }

                matcher.reset(option);
                while (matcher.find()) {
                    parsedOptions.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
                }
            }
        }
    }
}
