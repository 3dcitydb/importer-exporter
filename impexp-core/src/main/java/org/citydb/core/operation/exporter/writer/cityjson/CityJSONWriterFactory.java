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

package org.citydb.core.operation.exporter.writer.cityjson;

import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.exporter.CityJSONOptions;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.core.operation.exporter.writer.FeatureWriter;
import org.citydb.core.operation.exporter.writer.FeatureWriterFactory;
import org.citydb.core.query.Query;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.cityjson.CityJSONBuilder;
import org.citygml4j.builder.cityjson.CityJSONBuilderException;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONChunkWriter;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONOutputFactory;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONWriteException;
import org.citygml4j.builder.cityjson.marshal.util.DefaultTextureVerticesBuilder;
import org.citygml4j.builder.cityjson.marshal.util.DefaultVerticesBuilder;
import org.citygml4j.builder.cityjson.marshal.util.DefaultVerticesTransformer;

import java.io.OutputStream;

public class CityJSONWriterFactory implements FeatureWriterFactory {
    private final CityJSONOutputFactory factory;
    private final Config config;
    private final DatabaseSrs targetSrs;
    private final boolean useSequentialWriting;

    public CityJSONWriterFactory(Query query, Config config) throws FeatureWriteException {
        this.config = config;

        try {
            CityJSONBuilder builder = CityGMLContext.getInstance().createCityJSONBuilder();
            factory = builder.createCityJSONOutputFactory();
            factory.setRemoveDuplicateChildGeometries(config.getExportConfig().getCityJSONOptions().isRemoveDuplicateChildGeometries());
        } catch (CityJSONBuilderException e) {
            throw new FeatureWriteException("Failed to initialize CityJSON output factory.", e);
        }

        targetSrs = query.getTargetSrs();
        useSequentialWriting = query.isSetSorting();
    }

    @Override
    public FeatureWriter createFeatureWriter(OutputStream outputStream) throws FeatureWriteException {
        CityJSONChunkWriter chunkWriter;
        try {
            chunkWriter = factory.createCityJSONChunkWriter(outputStream, config.getExportConfig().getGeneralOptions().getFileEncoding());
        } catch (CityJSONWriteException e) {
            throw new FeatureWriteException("Failed to create CityJSON writer.", e);
        }

        chunkWriter.setCalcBoundingBox(config.getExportConfig().getGeneralOptions().getEnvelope().isUseEnvelopeOnCityModel());

        CityJSONOptions cityJSONOptions = config.getExportConfig().getCityJSONOptions();
        chunkWriter.setIndent(cityJSONOptions.isPrettyPrint() ? " " : "");
        chunkWriter.setVerticesBuilder(new DefaultVerticesBuilder()
                .withSignificantDigits(cityJSONOptions.getSignificantDigits()));
        chunkWriter.setTextureVerticesBuilder(new DefaultTextureVerticesBuilder()
                .withSignificantDigits(cityJSONOptions.getSignificantTextureDigits()));

        if (cityJSONOptions.isUseGeometryCompression()) {
            chunkWriter.setVerticesTransformer(new DefaultVerticesTransformer());
        }

        return new CityJSONWriter(chunkWriter, config, targetSrs, useSequentialWriting);
    }
}
