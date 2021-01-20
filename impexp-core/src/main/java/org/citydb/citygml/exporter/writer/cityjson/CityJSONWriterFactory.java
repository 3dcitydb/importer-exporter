package org.citydb.citygml.exporter.writer.cityjson;

import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.citygml.exporter.writer.FeatureWriter;
import org.citydb.citygml.exporter.writer.FeatureWriterFactory;
import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.exporter.CityJSONOptions;
import org.citydb.file.FileType;
import org.citydb.query.Query;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.cityjson.CityJSONBuilder;
import org.citygml4j.builder.cityjson.CityJSONBuilderException;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONChunkWriter;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONOutputFactory;
import org.citygml4j.builder.cityjson.marshal.util.DefaultTextureVerticesBuilder;
import org.citygml4j.builder.cityjson.marshal.util.DefaultVerticesBuilder;
import org.citygml4j.builder.cityjson.marshal.util.DefaultVerticesTransformer;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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
            factory.setGenerateCityGMLMetadata(config.getExportConfig().getCityJSONOptions().isGenerateCityGMLMetadata());
        } catch (CityJSONBuilderException e) {
            throw new FeatureWriteException("Failed to initialize CityJSON output factory.", e);
        }

        targetSrs = query.getTargetSrs();
        useSequentialWriting = query.isSetSorting();
    }

    @Override
    public FeatureWriter createFeatureWriter(OutputStream outputStream, FileType fileType) throws FeatureWriteException {
        CityJSONChunkWriter chunkWriter = factory.createCityJSONChunkWriter(new BufferedWriter(new OutputStreamWriter(outputStream)));

        CityJSONOptions cityJSONOptions = config.getExportConfig().getCityJSONOptions();

        chunkWriter.setIndent(cityJSONOptions.isPrettyPrint() ? " " : "");
        chunkWriter.setVerticesBuilder(new DefaultVerticesBuilder()
                .withSignificantDigits(cityJSONOptions.getSignificantDigits()));
        chunkWriter.setTextureVerticesBuilder(new DefaultTextureVerticesBuilder()
                .withSignificantDigits(cityJSONOptions.getSignificantTextureDigits()));

        if (cityJSONOptions.isTransformVertices()) {
            chunkWriter.setVerticesTransformer(new DefaultVerticesTransformer());
        }

        return new CityJSONWriter(chunkWriter, config, targetSrs, useSequentialWriting);
    }
}
