package org.citydb.citygml.exporter.writer.cityjson;

import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.citygml.exporter.writer.FeatureWriter;
import org.citydb.citygml.exporter.writer.FeatureWriterFactory;
import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.exporter.CityJSONOptions;
import org.citydb.query.Query;
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
