package org.citydb.citygml.importer.reader.citygml;

import org.citydb.citygml.importer.reader.FeatureReadException;
import org.citydb.citygml.importer.reader.FeatureReader;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.file.InputFile;
import org.citydb.log.Logger;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.citygml4j.xml.io.reader.UnmarshalException;
import org.citygml4j.xml.io.reader.XMLChunk;

import java.io.IOException;

public class CityGMLReader implements FeatureReader, EventHandler {
	private final Logger LOG = Logger.getInstance();
    private final CityGMLInputFilter typeFilter;
    private final CounterFilter counterFilter;
    private final ValidationErrorHandler validationHandler;
    private final CityGMLInputFactory factory;
    private final Config config;
    private final EventDispatcher eventDispatcher;

    private volatile boolean shouldRun = true;

    CityGMLReader(CityGMLInputFilter typeFilter, CounterFilter counterFilter, ValidationErrorHandler validationHandler, CityGMLInputFactory factory, Config config) {
        this.typeFilter = typeFilter;
        this.counterFilter = counterFilter;
        this.validationHandler = validationHandler;
        this.factory = factory;
        this.config = config;

        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT,this);
    }

    @Override
    public long getValidationErrors() {
        return validationHandler != null ? validationHandler.getValidationErrors() : 0;
    }

    @Override
    public long read(InputFile inputFile, WorkerPool<CityGML> workerPool, long counter) throws FeatureReadException {
        if (validationHandler != null)
            validationHandler.reset();

        org.citygml4j.xml.io.reader.CityGMLReader reader;

        try {
            reader = factory.createFilteredCityGMLReader(factory.createCityGMLReader(inputFile.getFile().toString(), inputFile.openStream()), typeFilter);
            boolean useValidation = config.getProject().getImporter().getXMLValidation().isSetUseXMLValidation();
            boolean breakTopLevelFeatureImport = false;
            
            while (shouldRun && reader.hasNext()) {
                XMLChunk chunk = reader.nextChunk();
                CityGML cityGML = null;
                try {
    				cityGML = chunk.unmarshal();
    			} catch (UnmarshalException e) {
    				if (!useValidation || chunk.hasPassedXMLValidation()) {
    					StringBuilder msg = new StringBuilder();				
    					msg.append("Failed to unmarshal XML chunk: ").append(e.getMessage());			
    					LOG.error(msg.toString());
    				}
    			} catch (MissingADESchemaException e) {
    				throw new FeatureReadException("Failed to read an ADE XML Schema.", e);
    			} catch (Exception e) {
    				// this is to catch general exceptions that may occur during the import
    				throw new FeatureReadException("Aborting due to an unexpected " + e.getClass().getName() + " error.", e);
    			}
                
				if (!useValidation || chunk.hasPassedXMLValidation()) {
					if (counterFilter != null && !(cityGML instanceof Appearance)) {
                        counter++;

                        if (counter < counterFilter.getLowerLimit())
                            continue;

                        if (counter > counterFilter.getUpperLimit())
                        	breakTopLevelFeatureImport = true;
                    }
					
					if (!breakTopLevelFeatureImport || (cityGML instanceof Appearance && breakTopLevelFeatureImport))
						workerPool.addWork(cityGML);
				}
            }
        } catch (CityGMLReadException | IOException e) {
            throw new FeatureReadException("Failed to read CityGML input file.", e);
        }

        try {
            reader.close();
        } catch (CityGMLReadException e) {
            throw new FeatureReadException("Failed to close CityGML reader.", e);
        }

        return counter;      
    }

    @Override
    public void close() throws FeatureReadException {
        eventDispatcher.removeEventHandler(this);
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        shouldRun = false;
    }
}
