/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.exporter.database.xlink;

import org.citydb.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.config.Config;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.file.FileType;
import org.citydb.file.OutputFile;
import org.citydb.log.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public class DBXlinkExporterTextureImage implements DBXlinkExporter {
    private final Logger log = Logger.getInstance();
    private final DBXlinkExporterManager exporterManager;
    private final OutputFile outputFile;
    private final BlobExportAdapter blobExporter;
    private final String textureURI;
    private final boolean isAbsoluteTextureURI;
    private final String separator;
    private final boolean overwriteTextureImage;
    private final boolean useBuckets;

    private boolean[] buckets;

    public DBXlinkExporterTextureImage(Connection connection, Config config, DBXlinkExporterManager exporterManager) {
        this.exporterManager = exporterManager;

        outputFile = exporterManager.getOutputFile();
        textureURI = config.getInternal().getExportTextureURI();
        isAbsoluteTextureURI = new File(textureURI).isAbsolute();
        separator = isAbsoluteTextureURI ? File.separator : "/";
        overwriteTextureImage = config.getProject().getExporter().getAppearances().isSetOverwriteTextureFiles();
        useBuckets = config.getProject().getExporter().getAppearances().getTexturePath().isUseBuckets()
                && config.getProject().getExporter().getAppearances().getTexturePath().getNoOfBuckets() > 0;

        if (useBuckets)
            buckets = new boolean[config.getProject().getExporter().getAppearances().getTexturePath().getNoOfBuckets()];

        blobExporter = exporterManager.getDatabaseAdapter().getSQLAdapter()
                .getBlobExportAdapter(connection, BlobType.TEXTURE_IMAGE)
                .withBatchSize(exporterManager.getBlobBatchSize());
    }

    public boolean export(DBXlinkTextureFile xlink) throws SQLException {
        String fileURI = xlink.getFileURI();

        if (fileURI == null || fileURI.isEmpty()) {
            log.error("Database error while exporting a texture file: Attribute TEX_IMAGE_URI is empty.");
            return false;
        }

        Path file;
        try {
            if (isAbsoluteTextureURI)
                file = Paths.get(textureURI, fileURI);
            else if (outputFile.getType() != FileType.ARCHIVE)
                file = Paths.get(outputFile.resolve(textureURI, fileURI));
            else
                file = null;
        } catch (InvalidPathException e) {
            log.error("Failed to export a texture file: '" + fileURI + "' is invalid.");
            return false;
        }

        if (useBuckets) {
            try {
                int bucket = Integer.parseInt(fileURI.substring(0, fileURI.indexOf(separator))) - 1;
                if (!buckets[bucket]) {
                    buckets[bucket] = true;

                    if (file != null)
                        Files.createDirectories(file.getParent());
                    else
                        outputFile.createDirectories(textureURI + separator + bucket);
                }
            } catch (IOException | NumberFormatException e) {
                log.error("Failed to create texture bucket for '" + fileURI + "'.");
                return false;
            }
        }

        try {
            int exported = blobExporter.addBatch(xlink.getId(), new BlobExportAdapter.BatchEntry(
                    () -> file != null ?
                            Files.newOutputStream(file) :
                            outputFile.newOutputStream(outputFile.resolve(textureURI, fileURI)),
                    () -> file == null || overwriteTextureImage || !Files.exists(file)));

            if (exported > 0)
                exporterManager.propagateEvent(new CounterEvent(CounterType.TEXTURE_IMAGE, exported, this));

            return true;
        } catch (IOException e) {
            log.error("Failed to batch export texture files.", e);
            return false;
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            int exported = blobExporter.executeBatch();
            if (exported > 0)
                exporterManager.propagateEvent(new CounterEvent(CounterType.TEXTURE_IMAGE, exported, this));
        } catch (IOException e) {
            log.error("Failed to batch export texture files.", e);
        }

        blobExporter.close();
    }

    @Override
    public DBXlinkExporterEnum getDBXlinkExporterType() {
        return DBXlinkExporterEnum.TEXTURE_IMAGE;
    }

}
