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
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.core.operation.exporter.util.Metadata;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.core.operation.exporter.writer.FeatureWriter;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.writer.CityJSONWriterWorkerFactory;
import org.citydb.core.writer.SequentialWriter;
import org.citydb.util.concurrent.SingleWorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONChunkWriter;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONWriteException;
import org.citygml4j.builder.cityjson.marshal.CityJSONMarshaller;
import org.citygml4j.cityjson.CityJSON;
import org.citygml4j.cityjson.feature.AbstractCityObjectType;
import org.citygml4j.cityjson.metadata.MetadataType;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.util.Arrays;

public class CityJSONWriter implements FeatureWriter, EventHandler {
    private final CityJSONChunkWriter writer;
    private final DatabaseSrs targetSrs;
    private final boolean useSequentialWriting;
    private final boolean addSequenceId;
    private final CityJSONMarshaller marshaller;
    private final SingleWorkerPool<AbstractCityObjectType> writerPool;
    private final EventDispatcher eventDispatcher;

    private Metadata metadata;
    private SequentialWriter<AbstractCityObjectType> sequentialWriter;
    private boolean hasContent;

    CityJSONWriter(CityJSONChunkWriter writer, Config config, DatabaseSrs targetSrs, boolean useSequentialWriting) {
        this.writer = writer;
        this.targetSrs = targetSrs;
        this.useSequentialWriting = useSequentialWriting;

        addSequenceId = config.getExportConfig().getCityJSONOptions().isAddSequenceIdWhenSorting();
        marshaller = writer.getCityJSONMarshaller();
        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

        writerPool = new SingleWorkerPool<>(
                "cityjson_writer_pool",
                new CityJSONWriterWorkerFactory(writer, ObjectRegistry.getInstance().getEventDispatcher()),
                config.getExportConfig().getResources().getThreadPool().getMaxThreads() * 2,
                false);

        writerPool.prestartCoreWorkers();

        if (useSequentialWriting) {
            sequentialWriter = new SequentialWriter<>(writerPool);
        }
    }

    @Override
    public void writeHeader() throws FeatureWriteException {
        // nothing to do
    }

    @Override
    public void write(AbstractFeature feature, long sequenceId) throws FeatureWriteException {
        if (feature instanceof AbstractCityObject) {
            CityJSON cityJSON = new CityJSON();

            AbstractCityObjectType cityObject = marshaller.marshal((AbstractCityObject) feature, cityJSON);
            for (AbstractCityObjectType child : cityJSON.getCityObjects()) {
                writerPool.addWork(child);
            }

            if (cityJSON.isSetExtensionProperties()) {
                cityJSON.getExtensionProperties().forEach(writer::addRootExtensionProperty);
            }

            if (cityObject != null) {
                if (!useSequentialWriting) {
                    writerPool.addWork(cityObject);
                } else {
                    try {
                        if (addSequenceId && sequenceId >= 0) {
                            cityObject.getAttributes().addExtensionAttribute("sequenceId", sequenceId);
                        }

                        sequentialWriter.write(cityObject, sequenceId);
                    } catch (InterruptedException e) {
                        throw new FeatureWriteException("Failed to write city object with gml:id '" + feature.getId() + "'.", e);
                    }
                }
            }

            hasContent = cityObject != null || cityJSON.hasCityObjects();
        }
    }

    @Override
    public void updateSequenceId(long sequenceId) throws FeatureWriteException {
        if (useSequentialWriting) {
            try {
                sequentialWriter.updateSequenceId(sequenceId);
            } catch (InterruptedException e) {
                throw new FeatureWriteException("Failed to update sequence id.", e);
            }
        }
    }

    @Override
    public void useIndentation(boolean useIndentation) {
        writer.setIndent(useIndentation ? " " : "");
    }

    @Override
    public Metadata getMetadata() {
        if (metadata == null) {
            metadata = new Metadata();
        }

        return metadata;
    }

    @Override
    public void close() throws FeatureWriteException {
        try {
            writerPool.shutdownAndWait();

            // add metadata
            if (hasContent) {
                setMetadata();
            }

            writer.writeEndDocument();
            writer.close();
        } catch (InterruptedException | CityJSONWriteException e) {
            throw new FeatureWriteException("Failed to close CityJSON writer.", e);
        } finally {
            if (!writerPool.isTerminated()) {
                writerPool.shutdownNow();
            }

            eventDispatcher.removeEventHandler(this);
        }
    }

    private void setMetadata() {
        MetadataType metadata = new MetadataType();
        if (targetSrs != null) {
            metadata.setReferenceSystem(targetSrs.getSrid());
        }

        if (this.metadata != null) {
            if (this.metadata.isSetDatasetName()) {
                metadata.setDatasetTitle(this.metadata.getDatasetName());
            }

            if (this.metadata.isSetDatasetDescription()) {
                metadata.setAbstract(this.metadata.getDatasetDescription());
            }

            if (this.metadata.isSetSpatialExtent()
                    && this.metadata.getSpatialExtent().is3D()) {
                BoundingBox bbox = this.metadata.getSpatialExtent();
                metadata.setGeographicalExtent(Arrays.asList(
                        bbox.getLowerCorner().getX(), bbox.getLowerCorner().getY(), bbox.getLowerCorner().getZ(),
                        bbox.getUpperCorner().getX(), bbox.getUpperCorner().getY(), bbox.getUpperCorner().getZ()));
            }
        }

        writer.setMetadata(metadata);
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        if (useSequentialWriting) {
            sequentialWriter.interrupt();
        }
    }
}
