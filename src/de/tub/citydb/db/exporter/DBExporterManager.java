/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.impl.jaxb.ObjectFactory;
import org.citygml4j.jaxb.gml._3_1_1.AbstractFeatureType;
import org.citygml4j.jaxb.gml._3_1_1.FeaturePropertyType;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.core.CityGMLBase;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.gml.GMLClass;

import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.db.gmlId.DBGmlIdLookupServerManager;
import de.tub.citydb.db.gmlId.GmlIdLookupServer;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.sax.SAXBuffer;

public class DBExporterManager {
	private final JAXBContext jaxbContext;
	private final Connection connection;
	private final WorkerPool<SAXBuffer> ioWriterPool;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final CityGMLFactory cityGMLFactory;
	private final ExportFilter exportFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private EnumMap<DBExporterEnum, DBExporter> dbExporterMap;
	private HashMap<CityGMLClass, Long> featureCounterMap;
	private HashMap<GMLClass, Long> geometryCounterMap;
	private CoreModule coreFactory;

	public DBExporterManager(JAXBContext jaxbContext,
			Connection connection,
			WorkerPool<SAXBuffer> ioWriterPool,
			WorkerPool<DBXlink> xlinkExporterPool,
			DBGmlIdLookupServerManager lookupServerManager,
			CityGMLFactory cityGMLFactory,
			ExportFilter exportFilter,
			Config config,
			EventDispatcher eventDispatcher) {
		this.jaxbContext = jaxbContext;
		this.connection = connection;
		this.ioWriterPool = ioWriterPool;
		this.xlinkExporterPool = xlinkExporterPool;
		this.lookupServerManager = lookupServerManager;
		this.cityGMLFactory = cityGMLFactory;
		this.exportFilter = exportFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		dbExporterMap = new EnumMap<DBExporterEnum, DBExporter>(DBExporterEnum.class);
		featureCounterMap = new HashMap<CityGMLClass, Long>();
		geometryCounterMap = new HashMap<GMLClass, Long>();
		coreFactory = config.getProject().getExporter().getModuleVersion().getCore().getModule();
	}

	public DBExporter getDBExporter(DBExporterEnum dbExporterType) throws SQLException {
		DBExporter dbExporter = dbExporterMap.get(dbExporterType);

		if (dbExporter == null) {
			switch (dbExporterType) {
			case SURFACE_GEOMETRY:
				dbExporter = new DBSurfaceGeometry(connection, config, this);
				break;
			case IMPLICIT_GEOMETRY:
				dbExporter = new DBImplicitGeometry(connection, cityGMLFactory, this);
				break;
			case CITYOBJECT:
				dbExporter = new DBCityObject(connection, cityGMLFactory, exportFilter, config, this);
				break;
			case BUILDING:
				dbExporter = new DBBuilding(connection, cityGMLFactory, exportFilter, config, this);
				break;
			case ROOM:
				dbExporter = new DBRoom(connection, cityGMLFactory, this);
				break;
			case BUILDING_FURNITURE:
				dbExporter = new DBBuildingFurniture(connection, cityGMLFactory, config, this);
				break;
			case BUILDING_INSTALLATION:
				dbExporter = new DBBuildingInstallation(connection, cityGMLFactory, this);
				break;
			case THEMATIC_SURFACE:
				dbExporter = new DBThematicSurface(connection, cityGMLFactory, config, this);
				break;
			case CITY_FURNITURE:
				dbExporter = new DBCityFurniture(connection, cityGMLFactory, exportFilter, config, this);
				break;
			case LAND_USE:
				dbExporter = new DBLandUse(connection, cityGMLFactory, exportFilter, config, this);
				break;
			case WATERBODY:
				dbExporter = new DBWaterBody(connection, cityGMLFactory, exportFilter, config, this);
				break;
			case PLANT_COVER:
				dbExporter = new DBPlantCover(connection, cityGMLFactory, exportFilter, config, this);
				break;
			case SOLITARY_VEGETAT_OBJECT:
				dbExporter = new DBSolitaryVegetatObject(connection, cityGMLFactory, exportFilter, config, this);
				break;
			case TRANSPORTATION_COMPLEX:
				dbExporter = new DBTransportationComplex(connection, cityGMLFactory, exportFilter, config, this);
				break;
			case RELIEF_FEATURE:
				dbExporter = new DBReliefFeature(connection, cityGMLFactory, exportFilter, config, this);
				break;
			case APPEARANCE:
				dbExporter = new DBAppearance(connection, cityGMLFactory, config, this);
				break;
			case TEXTUREPARAM:
				dbExporter = new DBTextureParam(connection, cityGMLFactory);
				break;
			case GENERIC_CITYOBJECT:
				dbExporter = new DBGenericCityObject(connection, cityGMLFactory, exportFilter, config, this);
				break;
			case CITYOBJECTGROUP:
				dbExporter = new DBCityObjectGroup(connection, cityGMLFactory, config, this);
				break;
			case GENERALIZATION:
				dbExporter = new DBGeneralization(connection, cityGMLFactory, exportFilter, config);
				break;
			case SDO_GEOMETRY:
				dbExporter = new DBSdoGeometry(config);
				break;
			}

			if (dbExporter != null)
				dbExporterMap.put(dbExporterType, dbExporter);
		}

		return dbExporter;
	}

	public boolean lookupAndPutGmlId(String gmlId, long id, CityGMLClass type) {
		GmlIdLookupServer lookupServer = lookupServerManager.getLookupServer(type);

		if (lookupServer != null)
			return lookupServer.lookupAndPut(gmlId, id, type);
		else
			return false;
	}

	public void putGmlId(String gmlId, long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		GmlIdLookupServer lookupServer = lookupServerManager.getLookupServer(type);

		if (lookupServer != null)
			lookupServer.put(gmlId, id, rootId, reverse, mapping, type);
	}

	public void putGmlId(String gmlId, long id, CityGMLClass type) {
		putGmlId(gmlId, id, -1, false, null, type);
	}

	public String getGmlId(long id, CityGMLClass type) {
		GmlIdLookupServer lookupServer = lookupServerManager.getLookupServer(type);

		if (lookupServer != null)
			return lookupServer.get(id, type);
		else
			return null;
	}

	public void propagateEvent(Event event) {
		eventDispatcher.triggerEvent(event);
	}

	public void propagateXlink(DBXlink xlink) {
		xlinkExporterPool.addWork(xlink);
	}

	public void updateFeatureCounter(CityGMLClass featureType) {
		Long counter = featureCounterMap.get(featureType);
		if (counter == null)
			featureCounterMap.put(featureType, new Long(1));
		else
			featureCounterMap.put(featureType, counter + 1);
	}

	public HashMap<CityGMLClass, Long> getFeatureCounter() {
		return featureCounterMap;
	}

	public void updateGeometryCounter(GMLClass geometryType) {
		Long counter = geometryCounterMap.get(geometryType);
		if (counter == null)
			geometryCounterMap.put(geometryType, new Long(1));
		else
			geometryCounterMap.put(geometryType, counter + 1);
	}

	public HashMap<GMLClass, Long> getGeometryCounter() {
		return geometryCounterMap;
	}

	public void close() throws SQLException {
		for (DBExporter exporter : dbExporterMap.values())
			exporter.close();
	}
	
	@SuppressWarnings("unchecked")
	public void print(CityGMLBase cityObject) throws JAXBException {
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		JAXBElement<?> featureProperty = null;

		if (cityObject.getCityGMLClass() == CityGMLClass.APPEARANCE) {
			Appearance appearance = (Appearance)cityObject;
			
			switch (appearance.getCityGMLModule().getModuleVersion()) {
			case v0_4_0:
				org.citygml4j.jaxb.citygml._0_4.AppearancePropertyType property040 = new org.citygml4j.jaxb.citygml._0_4.AppearancePropertyType();
				property040.setAppearance(((org.citygml4j.impl.jaxb.citygml.appearance._0_4.AppearanceImpl)appearance).getJAXBObject());
				featureProperty = ObjectFactory.CITYGML_0_4.createAppearanceMember(property040);
				break;
			case v1_0_0:
				org.citygml4j.jaxb.citygml.app._1.AppearancePropertyType property100 = new org.citygml4j.jaxb.citygml.app._1.AppearancePropertyType();
				property100.setAppearance(((org.citygml4j.impl.jaxb.citygml.appearance._1.AppearanceImpl)appearance).getJAXBObject());
				featureProperty = ObjectFactory.APP_1.createAppearanceMember(property100);
				break;
			}
		} else if (cityObject.getCityGMLClass().childOrSelf(CityGMLClass.CITYOBJECT)) {
			JAXBElement<?> jaxbElem = cityGMLFactory.cityGML2jaxb(cityObject);
			
			if (jaxbElem != null && jaxbElem.getValue() != null && jaxbElem.getValue() instanceof AbstractFeatureType) {
				FeaturePropertyType featureProp = new FeaturePropertyType();
				featureProp.set_Feature((JAXBElement<? extends AbstractFeatureType>)jaxbElem);
				switch (coreFactory.getModuleVersion()) {
				case v0_4_0:
					featureProperty =  ObjectFactory.CITYGML_0_4.createCityObjectMember(featureProp);
					break;
				case v1_0_0:
					featureProperty =  ObjectFactory.CORE_1.createCityObjectMember(featureProp);
					break;
				}
			}
		}
			
		if (featureProperty != null) {
			SAXBuffer buffer = new SAXBuffer();
			marshaller.marshal(featureProperty, buffer);
			ioWriterPool.addWork(buffer);
		}
	}
}
