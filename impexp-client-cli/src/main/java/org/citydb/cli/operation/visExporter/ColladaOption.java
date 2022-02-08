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

package org.citydb.cli.operation.visExporter;

import org.citydb.cli.option.CliOption;
import org.citydb.config.project.visExporter.ColladaOptions;
import org.citydb.textureAtlas.TextureAtlasCreator;
import picocli.CommandLine;

public class ColladaOption implements CliOption {
    enum Mode {none, basic, tpim, tpim_wo_rotation}

    @CommandLine.Option(names = {"-B", "--double-sided"},
            description = "Force all surfaces to be double sided.")
    private boolean doubleSided;

    @CommandLine.Option(names = "--no-surface-normals", defaultValue = "true",
            description = "Do not generate surface normals.")
    private boolean surfaceNormals;

    @CommandLine.Option(names = {"-C", "--crop-textures"},
            description = "Crop texture images.")
    private boolean cropTextures;

    @CommandLine.Option(names = {"-f", "--texture-scale-factor"}, paramLabel = "<0..1>", defaultValue = "1.0",
            description = "Scale texture images by the given factor (default: ${DEFAULT-VALUE}).")
    private double scaleFactor;

    @CommandLine.Option(names = {"-x", "--texture-atlas"}, paramLabel = "<mode>", defaultValue = "basic",
            description = "Texture atlas mode: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Mode textureAtlas;

    @CommandLine.Option(names = "--no-pot-atlases", defaultValue = "true",
            description = "Do not create power-of-two sized texture atlases.")
    private boolean requirePot;

    public ColladaOptions toColladaOptions() {
        ColladaOptions colladaOptions = new ColladaOptions();
        colladaOptions.setIgnoreSurfaceOrientation(doubleSided);
        colladaOptions.setGenerateSurfaceNormals(surfaceNormals);
        colladaOptions.setCropImages(cropTextures);

        colladaOptions.setScaleImages(scaleFactor != 1);
        if (scaleFactor != 1) {
            colladaOptions.setImageScaleFactor(scaleFactor);
        }

        colladaOptions.setGenerateTextureAtlases(textureAtlas != Mode.none);
        if (textureAtlas != Mode.none) {
            switch (textureAtlas) {
                case tpim:
                    colladaOptions.setPackingAlgorithm(TextureAtlasCreator.TPIM);
                    break;
                case tpim_wo_rotation:
                    colladaOptions.setPackingAlgorithm(TextureAtlasCreator.TPIM_WO_ROTATION);
                    break;
                default:
                    colladaOptions.setPackingAlgorithm(TextureAtlasCreator.BASIC);
                    break;
            }

            colladaOptions.setTextureAtlasPots(requirePot);
        }

        return colladaOptions;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        if (scaleFactor < 0 || scaleFactor > 1) {
            throw new CommandLine.ParameterException(commandLine, "Error: The texture scale factor " +
                    "must be a number within 0 and 1 but was '" + scaleFactor + "'");
        }
    }
}
