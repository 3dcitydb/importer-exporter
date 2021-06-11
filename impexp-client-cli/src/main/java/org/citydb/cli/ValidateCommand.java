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

package org.citydb.cli;

import org.citydb.citygml.validator.ValidationException;
import org.citydb.citygml.validator.controller.Validator;
import org.citydb.log.Logger;
import org.citydb.plugin.CliCommand;
import org.citydb.plugin.cli.CliOptionBuilder;
import org.citydb.util.CoreConstants;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@CommandLine.Command(
        name = "validate",
        description = "Validates input files against their schemas.",
        versionProvider = ImpExpCli.class
)
public class ValidateCommand extends CliCommand {
    @CommandLine.Parameters(paramLabel = "<file>", arity = "1",
            description = "Files or directories to validate (glob patterns allowed).")
    private String[] files;

    private final Logger log = Logger.getInstance();

    @Override
    public Integer call() throws Exception {
        List<Path> inputFiles;
        try {
            log.debug("Parsing and resolving input file parameters.");
            inputFiles = CliOptionBuilder.inputFiles(files, CoreConstants.WORKING_DIR);

            if (inputFiles.isEmpty()) {
                log.error("Failed to find input files for the provided parameters: " + String.join(", ", files));
                log.warn("Data validation aborted.");
                return 1;
            }
        } catch (IOException e) {
            throw new ImpExpException("Failed to parse input file parameters.", e);
        }

        try {
            Validator validator = new Validator();
            validator.doValidate(inputFiles);
            log.info("Data validation successfully finished.");
            return validator.getNumberOfInvalidFiles() == 0 ? 0 : 3;
        } catch (ValidationException e) {
            log.error(e.getMessage(), e.getCause());
            log.warn("Data validation aborted.");
            return 1;
        }
    }
}
