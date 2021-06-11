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

package org.citydb.cli.options.vis;

import org.citydb.config.project.visExporter.GltfOptions;
import org.citydb.config.project.visExporter.GltfVersion;
import org.citydb.plugin.cli.CliOption;
import picocli.CommandLine;

import java.nio.file.Path;

public class GltfOption implements CliOption {
    @CommandLine.Option(names = {"-G", "--gltf"}, required = true,
            description = "Convert COLLADA output to glTF.")
    private boolean exportGltf;

    @CommandLine.Option(names = "--gltf-version", paramLabel = "<version>", defaultValue = "2.0",
            description = "glTF version: 1.0, 2.0 (default: ${DEFAULT-VALUE}).")
    private String version;

    @CommandLine.Option(names = "--gltf-converter", paramLabel = "<file>",
            description = "Path to the COLLADA2GLTF converter executable.")
    private Path file;

    @CommandLine.Option(names = "--gltf-embed-textures",
            description = "Embed textures in glTF files.")
    private boolean embedTextures;

    @CommandLine.Option(names = "--gltf-binary",
            description = "Output binary glTF.")
    private boolean binaryGltf;

    @CommandLine.Option(names = "--gltf-draco-compression",
            description = "Output meshes using Draco compression (requires glTF version 2.0).")
    private boolean dracoCompression;

    @CommandLine.Option(names = {"-m", "--remove-collada"},
            description = "Only keep glTF and remove the COLLADA output.")
    private boolean removeCollada;

    private GltfVersion gltfVersion;

    public GltfOptions toGltfOptions() {
        GltfOptions gltfOptions = new GltfOptions();
        gltfOptions.setCreateGltfModel(exportGltf);
        gltfOptions.setGltfVersion(gltfVersion);
        gltfOptions.setEmbedTextures(embedTextures);
        gltfOptions.setUseBinaryGltf(binaryGltf);
        gltfOptions.setUseDracoCompression(dracoCompression);
        gltfOptions.setRemoveColladaFiles(removeCollada);

        if (file != null) {
            gltfOptions.setPathToConverter(file.toAbsolutePath().toString());
        }

        return gltfOptions;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        if (version != null) {
            switch (version) {
                case "1.0":
                    gltfVersion = GltfVersion.v1_0;
                    break;
                case "2.0":
                    gltfVersion = GltfVersion.v2_0;
                    break;
                default:
                    throw new CommandLine.ParameterException(commandLine, "Invalid value for option '--gltf-version': " +
                            "expected one of [1.0, 2.0] but was '" + version + "'");
            }
        }

        if (dracoCompression && gltfVersion == GltfVersion.v1_0) {
            throw new CommandLine.ParameterException(commandLine,
                    "Error: --gltf-draco-compression can only be used with glTF version 2.0");
        }
    }
}
