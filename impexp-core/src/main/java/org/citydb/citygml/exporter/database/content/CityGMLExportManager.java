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
package org.citydb.citygml.exporter.database.content;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.ade.exporter.ADEExportManager;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.common.database.cache.CacheTableManager;
import org.citydb.citygml.common.database.cache.model.CacheTableModel;
import org.citydb.citygml.common.database.uid.UIDCache;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.uid.UIDCacheType;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AppearanceRemover;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.ExportCounter;
import org.citydb.citygml.exporter.util.LodGeometryChecker;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.citygml.exporter.writer.FeatureWriter;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.exporter.Exporter;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.AppSchema;
import org.citydb.database.schema.mapping.FeatureProperty;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.InjectedProperty;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.ObjectType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.log.Logger;
import org.citydb.query.Query;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citydb.util.CoreConstants;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.builder.jaxb.unmarshal.JAXBUnmarshaller;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElement;
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.bridge.BridgeInstallation;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.transportation.AbstractTransportationObject;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.TunnelFurniture;
import org.citygml4j.model.citygml.tunnel.TunnelInstallation;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CityGMLExportManager implements CityGMLExportHelper {
	private final Logger log = Logger.getInstance();
	private final IdentityHashMap<Class<? extends DBExporter>, DBExporter> exporters = new IdentityHashMap<>();
	private final IdentityHashMap<ADEExtension, ADEExportManager> adeExporters = new IdentityHashMap<>();

	private final Connection connection;
	private final Query query;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SchemaMapping schemaMapping;
	private final CityGMLBuilder cityGMLBuilder;
	private final ADEExtensionManager adeManager;
	private final FeatureWriter featureWriter;
	private final WorkerPool<DBXlink> xlinkPool;
	private final UIDCacheManager uidCacheManager;
	private final CacheTableManager cacheTableManager;
	private final Config config;

	private final AttributeValueSplitter attributeValueSplitter;
	private final LodGeometryChecker lodGeometryChecker;
	private final ExportCounter exportCounter;
	private final JAXBUnmarshaller jaxbUnmarshaller;
	private GMLConverter gmlConverter;
	private AppearanceRemover appearanceRemover;
	private Document document;

	private boolean failOnError = false;
	private boolean hasADESupport = false;

	public CityGMLExportManager(Connection connection,
			Query query,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			FeatureWriter featureWriter,
			WorkerPool<DBXlink> xlinkPool,
			UIDCacheManager uidCacheManager,
			CacheTableManager cacheTableManager,
			Config config) throws CityGMLExportException {
		this.connection = connection;
		this.query = query;
		this.databaseAdapter = databaseAdapter;
		this.schemaMapping = schemaMapping;
		this.cityGMLBuilder = cityGMLBuilder;
		this.featureWriter = featureWriter;
		this.xlinkPool = xlinkPool;
		this.uidCacheManager = uidCacheManager;
		this.cacheTableManager = cacheTableManager;
		this.config = config;

		adeManager = ADEExtensionManager.getInstance();
		hasADESupport = !adeManager.getEnabledExtensions().isEmpty();

		attributeValueSplitter = new AttributeValueSplitter();
		lodGeometryChecker = new LodGeometryChecker(schemaMapping);
		exportCounter = new ExportCounter(schemaMapping);

		if (config.getProject().getExporter().getAppearances().isSetExportAppearance())
			appearanceRemover = new AppearanceRemover(xlinkPool);

		try {
			jaxbUnmarshaller = cityGMLBuilder.createJAXBUnmarshaller();
			jaxbUnmarshaller.setThrowMissingADESchema(false);
			jaxbUnmarshaller.setParseSchema(false);
		} catch (CityGMLBuilderException e) {
			throw new CityGMLExportException("Failed to build JAXB unmarshaller.", e);
		}
	}

	@Override
	public <T extends AbstractGML> T createObject(long objectId, int objectClassId, Class<T> type) throws CityGMLExportException, SQLException {
		AbstractObjectType<?> objectType = getAbstractObjectType(objectClassId);
		if (objectType == null)
			throw new CityGMLExportException("Failed to determine object type for " + getObjectSignature(objectClassId, objectId) + ". Skipping export.");

		AbstractGML object = exportObject(objectId, objectType, true);
		return type.isInstance(object) ? type.cast(object) : null;
	}

	public AbstractGML exportObject(long objectId, AbstractObjectType<?> objectType, boolean exportStub) throws CityGMLExportException, SQLException {
		AbstractGML object = Util.createObject(objectType.getObjectClassId(), query.getTargetVersion());
		if (object == null)
			throw new CityGMLExportException("Failed to instantiate citygml4j object for " + getObjectSignature(objectType, objectId) + ". Skipping export.");

		if (object instanceof ADEModelObject) {
			ADEExtension extension = adeManager.getExtensionByObjectClassId(objectType.getObjectClassId());
			if (extension != null && !extension.isEnabled())
				throw new CityGMLExportException("ADE extension for object " + getObjectSignature(objectType, objectId) + " is disabled. Skipping export.");
		
			if (exportStub)
				object.setLocalProperty(CoreConstants.EXPORT_STUB, true);
		}

		boolean success;

		// top-level feature types
		if (object instanceof AbstractBuilding)
			success = getExporter(DBBuilding.class).doExport((AbstractBuilding)object, objectId, (FeatureType)objectType);
		else if (object instanceof AbstractBridge)
			success = getExporter(DBBridge.class).doExport((AbstractBridge)object, objectId, (FeatureType)objectType);
		else if (object instanceof AbstractTunnel)
			success = getExporter(DBTunnel.class).doExport((AbstractTunnel)object, objectId, (FeatureType)objectType);
		else if (object instanceof CityFurniture)
			success = getExporter(DBCityFurniture.class).doExport((CityFurniture)object, objectId, (FeatureType)objectType);
		else if (object instanceof CityObjectGroup)
			success = getExporter(DBCityObjectGroup.class).doExport((CityObjectGroup)object, objectId, (FeatureType)objectType);
		else if (object instanceof GenericCityObject)
			success = getExporter(DBGenericCityObject.class).doExport((GenericCityObject)object, objectId, (FeatureType)objectType);
		else if (object instanceof LandUse)
			success = getExporter(DBLandUse.class).doExport((LandUse)object, objectId, (FeatureType)objectType);
		else if (object instanceof PlantCover)
			success = getExporter(DBPlantCover.class).doExport((PlantCover)object, objectId, (FeatureType)objectType);
		else if (object instanceof SolitaryVegetationObject)
			success = getExporter(DBSolitaryVegetatObject.class).doExport((SolitaryVegetationObject)object, objectId, (FeatureType)objectType);
		else if (object instanceof ReliefFeature)
			success = getExporter(DBReliefFeature.class).doExport((ReliefFeature)object, objectId, (FeatureType)objectType);
		else if (object instanceof TransportationComplex)
			success = getExporter(DBTransportationComplex.class).doExport((TransportationComplex)object, objectId, (FeatureType)objectType);
		else if (object instanceof WaterBody)
			success = getExporter(DBWaterBody.class).doExport((WaterBody)object, objectId, (FeatureType)objectType);

		// nested feature types
		else if (object instanceof AbstractBoundarySurface)
			success = getExporter(DBThematicSurface.class).doExport((AbstractBoundarySurface)object, objectId, (FeatureType)objectType);
		else if (object instanceof AbstractOpening)
			success = getExporter(DBOpening.class).doExport((AbstractOpening)object, objectId, (FeatureType)objectType);
		else if (object instanceof BuildingInstallation)
			success = getExporter(DBBuildingInstallation.class).doExport((BuildingInstallation)object, objectId, (FeatureType)objectType);
		else if (object instanceof IntBuildingInstallation)
			success = getExporter(DBBuildingInstallation.class).doExport((IntBuildingInstallation)object, objectId, (FeatureType)objectType);
		else if (object instanceof Room)
			success = getExporter(DBRoom.class).doExport((Room)object, objectId, (FeatureType)objectType);
		else if (object instanceof BuildingFurniture)
			success = getExporter(DBBuildingFurniture.class).doExport((BuildingFurniture)object, objectId, (FeatureType)objectType);
		else if (object instanceof org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)
			success = getExporter(DBBridgeThematicSurface.class).doExport((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)object, objectId, (FeatureType)objectType);
		else if (object instanceof org.citygml4j.model.citygml.bridge.AbstractOpening)
			success = getExporter(DBBridgeOpening.class).doExport((org.citygml4j.model.citygml.bridge.AbstractOpening)object, objectId, (FeatureType)objectType);		
		else if (object instanceof BridgeConstructionElement)
			success = getExporter(DBBridgeConstrElement.class).doExport((BridgeConstructionElement)object, objectId, (FeatureType)objectType);
		else if (object instanceof BridgeInstallation)
			success = getExporter(DBBridgeInstallation.class).doExport((BridgeInstallation)object, objectId, (FeatureType)objectType);
		else if (object instanceof IntBridgeInstallation)
			success = getExporter(DBBridgeInstallation.class).doExport((IntBridgeInstallation)object, objectId, (FeatureType)objectType);
		else if (object instanceof BridgeRoom)
			success = getExporter(DBBridgeRoom.class).doExport((BridgeRoom)object, objectId, (FeatureType)objectType);		
		else if (object instanceof BridgeFurniture)
			success = getExporter(DBBridgeFurniture.class).doExport((BridgeFurniture)object, objectId, (FeatureType)objectType);
		else if (object instanceof org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)
			success = getExporter(DBTunnelThematicSurface.class).doExport((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)object, objectId, (FeatureType)objectType);
		else if (object instanceof org.citygml4j.model.citygml.tunnel.AbstractOpening)
			success = getExporter(DBTunnelOpening.class).doExport((org.citygml4j.model.citygml.tunnel.AbstractOpening)object, objectId, (FeatureType)objectType);		
		else if (object instanceof TunnelInstallation)
			success = getExporter(DBTunnelInstallation.class).doExport((TunnelInstallation)object, objectId, (FeatureType)objectType);
		else if (object instanceof IntTunnelInstallation)
			success = getExporter(DBTunnelInstallation.class).doExport((IntTunnelInstallation)object, objectId, (FeatureType)objectType);
		else if (object instanceof HollowSpace)
			success = getExporter(DBTunnelHollowSpace.class).doExport((HollowSpace)object, objectId, (FeatureType)objectType);
		else if (object instanceof TunnelFurniture)
			success = getExporter(DBTunnelFurniture.class).doExport((TunnelFurniture)object, objectId, (FeatureType)objectType);
		else if (object instanceof AbstractReliefComponent)
			success = getExporter(DBReliefComponent.class).doExport((AbstractReliefComponent)object, objectId, (FeatureType)objectType);
		else if (object instanceof AbstractTransportationObject)
			success = getExporter(DBTrafficArea.class).doExport((AbstractTransportationObject)object, objectId, (FeatureType)objectType);
		else if (object instanceof AbstractWaterBoundarySurface)
			success = getExporter(DBWaterBoundarySurface.class).doExport((AbstractWaterBoundarySurface)object, objectId, (FeatureType)objectType);
		else if (object instanceof Address)
			success = getExporter(DBAddress.class).doExport((Address)object, objectId, (FeatureType)objectType);

		// generic fallback for any ADE object
		else
			success = getExporter(DBCityObject.class).doExport(object, objectId, objectType);

		return success ? object : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractFeature> Collection<T> exportNestedCityGMLObjects(FeatureProperty featureProperty, long parentId, Class<T> featureClass) throws CityGMLExportException, SQLException {
		Collection<? extends AbstractFeature> features;
		FeatureType featureType = featureProperty.getType();

		// building module
		if (featureType.isEqualToOrSubTypeOf(getFeatureType(AbstractBuilding.class)))
			features = getExporter(DBBuilding.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(AbstractBoundarySurface.class)))
			features = getExporter(DBThematicSurface.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(AbstractOpening.class)))
			features = getExporter(DBOpening.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(BuildingInstallation.class)))
			features = getExporter(DBBuildingInstallation.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(IntBuildingInstallation.class)))
			features = getExporter(DBBuildingInstallation.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(Room.class)))
			features = getExporter(DBRoom.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(BuildingFurniture.class)))
			features = getExporter(DBBuildingFurniture.class).doExport(featureProperty, parentId);

		// bridge module
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(AbstractBridge.class)))
			features = getExporter(DBBridge.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(org.citygml4j.model.citygml.bridge.AbstractBoundarySurface.class)))
			features = getExporter(DBBridgeThematicSurface.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(org.citygml4j.model.citygml.bridge.AbstractOpening.class)))
			features = getExporter(DBBridgeOpening.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(BridgeConstructionElement.class)))
			features = getExporter(DBBridgeConstrElement.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(BridgeInstallation.class)))
			features = getExporter(DBBridgeInstallation.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(IntBridgeInstallation.class)))
			features = getExporter(DBBridgeInstallation.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(BridgeRoom.class)))
			features = getExporter(DBBridgeRoom.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(BridgeFurniture.class)))
			features = getExporter(DBBridgeFurniture.class).doExport(featureProperty, parentId);

		// city furniture module
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(CityFurniture.class)))
			features = getExporter(DBCityFurniture.class).doExport(featureProperty, parentId);

		// generics module
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(GenericCityObject.class)))
			features = getExporter(DBGenericCityObject.class).doExport(featureProperty, parentId);

		// land use module
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(LandUse.class)))
			features = getExporter(DBLandUse.class).doExport(featureProperty, parentId);

		// vegetation module
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(PlantCover.class)))
			features = getExporter(DBPlantCover.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(SolitaryVegetationObject.class)))
			features = getExporter(DBSolitaryVegetatObject.class).doExport(featureProperty, parentId);

		// relief module
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(ReliefFeature.class)))
			features = getExporter(DBReliefFeature.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(AbstractReliefComponent.class)))
			features = getExporter(DBReliefComponent.class).doExport(featureProperty, parentId);

		// transportation module
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(TransportationComplex.class)))
			features = getExporter(DBTransportationComplex.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(AbstractTransportationObject.class)))
			features = getExporter(DBTrafficArea.class).doExport(featureProperty, parentId);

		// tunnel module
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(AbstractTunnel.class)))
			features = getExporter(DBTunnel.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface.class)))
			features = getExporter(DBTunnelThematicSurface.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(org.citygml4j.model.citygml.tunnel.AbstractOpening.class)))
			features = getExporter(DBTunnelOpening.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(TunnelInstallation.class)))
			features = getExporter(DBTunnelInstallation.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(IntTunnelInstallation.class)))
			features = getExporter(DBTunnelInstallation.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(HollowSpace.class)))
			features = getExporter(DBTunnelHollowSpace.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(TunnelFurniture.class)))
			features = getExporter(DBTunnelFurniture.class).doExport(featureProperty, parentId);

		// water body module
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(WaterBody.class)))
			features = getExporter(DBWaterBody.class).doExport(featureProperty, parentId);
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(AbstractWaterBoundarySurface.class)))
			features = getExporter(DBWaterBoundarySurface.class).doExport(featureProperty, parentId);

		// core module
		else if (featureType.isEqualToOrSubTypeOf(getFeatureType(Address.class)))
			features = getExporter(DBAddress.class).doExport(featureProperty, parentId);

		else
			features = Collections.emptyList();

		// filter instances according to the provided feature class
		if (!features.isEmpty())
			features.removeIf(abstractFeature -> !featureClass.isInstance(abstractFeature));

		return (Collection<T>)features;
	}

	protected void delegateToADEExporter(AbstractGML object, long objectId, AbstractObjectType<?> objectType, ProjectionFilter projectionFilter) throws CityGMLExportException, SQLException {
		// delegate export of ADE object to an ADE exporter
		if (object instanceof ADEModelObject && !object.hasLocalProperty(CoreConstants.EXPORT_STUB))
			getADEExportManager(objectType.getSchema()).exportObject((ADEModelObject)object, objectId, objectType, projectionFilter);
	}

	protected void delegateToADEExporter(List<String> adeHookTables, AbstractFeature parent, long parentId, FeatureType parentType, ProjectionFilter projectionFilter) throws CityGMLExportException, SQLException {
		// delegate export of ADE generic application properties to an ADE exporter
		for (String adeHookTable : adeHookTables)
			getADEExportManager(adeHookTable).exportGenericApplicationProperties(adeHookTable, parent, parentId, parentType, projectionFilter);
	}

	@Override
	public SurfaceGeometry exportSurfaceGeometry(long surfaceGeometryId) throws CityGMLExportException, SQLException {
		return getExporter(DBSurfaceGeometry.class).doExport(surfaceGeometryId);
	}

	@Override
	public ImplicitGeometry exportImplicitGeometry(long id, GeometryObject referencePoint, String transformationMatrix) throws CityGMLExportException, SQLException {
		return getExporter(DBImplicitGeometry.class).doExport(id, referencePoint, transformationMatrix);
	}

	public Appearance exportGlobalAppearance(long appearanceId) throws CityGMLExportException, SQLException {
		return getExporter(DBGlobalAppearance.class).doExport(appearanceId);
	}

	@Override
	public boolean exportAsGlobalFeature(AbstractFeature feature) throws CityGMLExportException {
		if (featureWriter.supportsFlatHierarchies()) {
			if (!query.getFeatureTypeFilter().containsFeatureType(getFeatureType(feature)))
				feature.setLocalProperty(CoreConstants.EXPORT_AS_ADDITIONAL_OBJECT, true);

			try {
				featureWriter.write(feature, -1);
			} catch (FeatureWriteException e) {
				throw new CityGMLExportException("Failed to write global feature with gml:id '" + feature.getId() + "'.", e);
			}

			updateExportCounter(feature);
			return true;
		}

		return false;
	}

	@Override
	public boolean supportsExportOfGlobalFeatures() {
		return featureWriter.supportsFlatHierarchies();
	}

	@Override
	public GMLConverter getGMLConverter() {
		if (gmlConverter == null) {		
			gmlConverter = new GMLConverter(query.isSetTargetSrs() ? query.getTargetSrs().getGMLSrsName() :
				databaseAdapter.getConnectionMetaData().getReferenceSystem().getGMLSrsName());
		}

		return gmlConverter;
	}

	@Override
	public AbstractDatabaseAdapter getDatabaseAdapter() {
		return databaseAdapter;
	}

	@Override
	public CityGMLVersion getTargetCityGMLVersion() {
		return query.getTargetVersion();
	}

	@Override
	public ProjectionFilter getProjectionFilter(AbstractObjectType<?> objectType) {
		return query.getProjectionFilter(objectType);
	}

	@Override
	public CombinedProjectionFilter getCombinedProjectionFilter(String tableName) {
		List<ProjectionFilter> filters = new ArrayList<>();			
		schemaMapping.listAbstractObjectTypesByTable(tableName, true).forEach(type -> filters.add(query.getProjectionFilter(type)));

		return new CombinedProjectionFilter(filters);
	}

	@Override
	public LodFilter getLodFilter() {
		return query.getLodFilter();
	}

	@Override
	public AttributeValueSplitter getAttributeValueSplitter() {
		return attributeValueSplitter;
	}

	@Override
	public boolean isFailOnError() {
		return failOnError;
	}

	@Override
	public Exporter getExportConfig() {
		return config.getProject().getExporter();
	}

	@Override
	public String getTableNameWithSchema(String tableName) {
		return databaseAdapter.getConnectionDetails().getSchema() + '.' + tableName;
	}

	@Override
	public ProjectionToken getGeometryColumn(Column column) {
		return (!config.getInternal().isTransformCoordinates()) ? 
				column : new Function(databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null"),
						column.getName(), column, new IntegerLiteral(query.getTargetSrs().getSrid()));
	}

	@Override
	public ProjectionToken getGeometryColumn(Column column, String asName) {
		return (!config.getInternal().isTransformCoordinates()) ? 
				column : new Function(databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null"),
						asName, column, new IntegerLiteral(query.getTargetSrs().getSrid()));
	}

	@Override
	public String getGeometryColumn(String columnName) {
		return (!config.getInternal().isTransformCoordinates()) ? 
				columnName : databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null") +
				"(" + columnName + ", " + query.getTargetSrs().getSrid() + ") as " + columnName.replaceFirst(".*?\\.", "");
	}

	@Override
	public String getGeometryColumn(String columnName, String asName) {
		return (!config.getInternal().isTransformCoordinates()) ? 
				columnName : databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null") +
				"(" + columnName + ", " + query.getTargetSrs().getSrid() + ") as " + asName;
	}

	@Override
	public void logOrThrowErrorMessage(String message) throws CityGMLExportException {
		if (!failOnError)
			log.error(message);
		else
			throw new CityGMLExportException(message);
	}

	@Override
	public String getObjectSignature(int objectClassId, long id) {
		AbstractObjectType<?> objectType = schemaMapping.getAbstractObjectType(objectClassId);
		return objectType != null ? getObjectSignature(objectType, id) : "city object (id : " + id + " )";
	}

	@Override
	public String getObjectSignature(AbstractObjectType<?> objectType, long id) {
		return objectType.getSchema().getXMLPrefix() + ":" + objectType.getPath() + " (id: " + id + ")";
	}

	@Override
	public FeatureType getFeatureType(AbstractFeature feature) {
		return schemaMapping.getFeatureType(Util.getObjectClassId(feature.getClass()));
	}

	@Override
	public ObjectType getObjectType(AbstractGML object) {
		return schemaMapping.getObjectType(Util.getObjectClassId(object.getClass()));
	}

	@Override
	public AbstractObjectType<?> getAbstractObjectType(AbstractGML object) {
		return schemaMapping.getAbstractObjectType(Util.getObjectClassId(object.getClass()));
	}

	@Override
	public FeatureType getFeatureType(int objectClassId) {
		return schemaMapping.getFeatureType(objectClassId);
	}

	@Override
	public ObjectType getObjectType(int objectClassId) {
		return schemaMapping.getObjectType(objectClassId);
	}

	@Override
	public AbstractObjectType<?> getAbstractObjectType(int objectClassId) {
		return schemaMapping.getAbstractObjectType(objectClassId);
	}

	@Override
	public boolean satisfiesLodFilter(AbstractCityObject cityObject) {
		return query.getLodFilter().preservesGeometry() || lodGeometryChecker.satisfiesLodFilter(cityObject);
	}

	protected <T extends AbstractGML> T createObject(int objectClassId, Class<T> type) {
		AbstractGML object = Util.createObject(objectClassId, query.getTargetVersion());
		return type.isInstance(object) ? type.cast(object) : null;
	}

	public String generateNewGmlId(AbstractFeature feature) {
		String gmlId = DefaultGMLIdManager.getInstance().generateUUID(config.getProject().getExporter().getXlink().getFeature().getIdPrefix());

		if (feature.isSetId()) {
			if (config.getProject().getExporter().getXlink().getFeature().isSetAppendId())
				gmlId = gmlId + "-" + feature.getId();

			if (config.getProject().getExporter().getXlink().getFeature().isSetKeepGmlIdAsExternalReference()
					&& feature instanceof AbstractCityObject) {
				ExternalReference externalReference = new ExternalReference();
				externalReference.setInformationSystem(config.getInternal().getCurrentExportFile().getFile().toString());

				ExternalObject externalObject = new ExternalObject();
				externalObject.setName(feature.getId());
				externalReference.setExternalObject(externalObject);

				((AbstractCityObject)feature).addExternalReference(externalReference);
			}
		}

		return gmlId;
	}

	public void propagateXlink(DBXlink xlink) {
		xlinkPool.addWork(xlink);
	}

	@Override
	public boolean lookupAndPutObjectUID(String gmlId, long id, int objectClassId) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.OBJECT);
		if (cache != null)
			return cache.lookupAndPut(gmlId, id, objectClassId);
		else
			return false;
	}

	@Override
	public boolean lookupObjectUID(String gmlId) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.OBJECT);
		if (cache != null)
			return cache.get(gmlId) != null;
		else
			return false;
	}

	public void putObjectUID(String gmlId, long id, int objectClassId) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.OBJECT);
		if (cache != null)
			cache.put(gmlId, id, -1, false, null, objectClassId);
	}	

	public boolean lookupAndPutGeometryUID(String gmlId, long id) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.GEOMETRY);
		if (cache != null)
			return cache.lookupAndPut(gmlId, id, MappingConstants.SURFACE_GEOMETRY_OBJECTCLASS_ID);
		else 
			return false;
	}

	public String getGeometrySignature(AbstractGeometry geometry, long id) {
		return getGeometrySignature(geometry.getGMLClass(), id);
	}

	public String getGeometrySignature(GMLClass geometryClass, long id) {
		return "gml:" + geometryClass + " (ID: " + id + ")";
	}

	protected String getPropertyName(AbstractProperty property) {
		return property.getSchema().getXMLPrefix() + ":" + property.getPath();
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	protected boolean hasADESupport() {
		return hasADESupport;
	}

	protected FeatureType getFeatureType(Class<? extends AbstractFeature> featureClass) {
		return schemaMapping.getFeatureType(Util.getObjectClassId(featureClass));
	}

	protected AbstractObjectType<?> getAbstractObjectType(Class<? extends AbstractGML> objectClass) {
		return schemaMapping.getAbstractObjectType(Util.getObjectClassId(objectClass));
	}

	public void cleanupAppearances(AbstractGML object) {
		if (appearanceRemover != null 
				&& !query.getLodFilter().preservesGeometry()
				&& object instanceof AbstractCityObject)
			appearanceRemover.cleanupAppearance((AbstractCityObject)object);
	}

	public void updateExportCounter(AbstractGML object) {
		exportCounter.updateExportCounter(object);
	}

	public Map<Integer, Long> getAndResetObjectCounter() {
		return exportCounter.getAndResetObjectCounter();
	}

	public Map<GMLClass, Long> getAndResetGeometryCounter() {
		return exportCounter.getAndResetGeometryCounter();
	}

	public CityGMLBuilder getCityGMLBuilder() {
		return cityGMLBuilder;
	}

	protected SchemaMapping getSchemaMapping() {
		return schemaMapping;
	}	

	protected ADEGenericElement createADEGenericElement(String uri, String localName) throws ParserConfigurationException {
		if (document == null)
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		ADEGenericElement adeElement = new ADEGenericElement();
		adeElement.setContent(document.createElementNS(uri, localName));

		return adeElement;
	}

	protected Object unmarshal(Reader reader) {
		Object object;

		try {
			Unmarshaller unmarshaller = cityGMLBuilder.getJAXBContext().createUnmarshaller();
			object = unmarshaller.unmarshal(reader);
			if (object != null)
				object = jaxbUnmarshaller.unmarshal(object);
		} catch (JAXBException | MissingADESchemaException e) {
			object = null;
		}

		return object;
	}

	public void close() throws CityGMLExportException, SQLException {
		for (DBExporter exporter : exporters.values())
			exporter.close();

		for (ADEExportManager adeExporter : adeExporters.values())			
			adeExporter.close();

		exporters.clear();
	}

	protected Set<String> getADEHookTables(TableEnum table) {
		Set<String> adeHookTables = null;

		for (FeatureType featureType : schemaMapping.listFeatureTypesByTable(table.getName(), true)) {
			// skip ADE features - we do not support ADEs of ADEs
			if (adeManager.getExtensionByObjectClassId(featureType.getObjectClassId()) != null)
				continue;

			for (AbstractProperty property : featureType.listProperties(false, true)) {
				if (property instanceof InjectedProperty) {
					String adeHookTable = ((InjectedProperty)property).getBaseJoin().getTable();
					if (adeHookTables == null)
						adeHookTables = new HashSet<>();

					adeHookTables.add(adeHookTable);						
				}
			}
		}

		return adeHookTables;
	}

	private ADEExportManager getADEExportManager(AppSchema schema) throws CityGMLExportException, SQLException {		
		ADEExtension adeExtension = adeManager.getExtensionBySchema(schema);
		if (adeExtension == null || !adeExtension.isEnabled()) {
			throw new CityGMLExportException("ADE extension for schema " +
					schema.getNamespace(query.getTargetVersion()).getURI() +
					" is disabled. Skipping export.");
		}

		return getADEExportManager(adeExtension);
	}

	private ADEExportManager getADEExportManager(String tableName) throws CityGMLExportException, SQLException {		
		ADEExtension adeExtension = adeManager.getExtensionByTableName(tableName);
		if (adeExtension == null || !adeExtension.isEnabled()) {
			throw new CityGMLExportException("ADE extension for table '" +
					tableName + "' is disabled. Skipping export.");
		}

		return getADEExportManager(adeExtension);
	}

	private ADEExportManager getADEExportManager(ADEExtension extension) throws CityGMLExportException, SQLException {
		ADEExportManager adeExporter = adeExporters.get(extension);
		if (adeExporter == null) {
			adeExporter = extension.createADEExportManager();
			if (adeExporter == null)
				throw new CityGMLExportException("Failed to create ADE exporter for '" +
						extension.getMetadata().getIdentifier() + "'");

			adeExporter.init(connection, this);
			adeExporters.put(extension, adeExporter);
		}

		return adeExporter;
	}

	protected <T extends DBExporter> T getExporter(Class<T> type) throws CityGMLExportException, SQLException {
		DBExporter exporter = exporters.get(type);

		if (exporter == null) {
			// core module
			if (type == DBSurfaceGeometry.class) {
				CacheTable cacheTable = null;
				if (config.getInternal().isExportGlobalAppearances()) {
					cacheTable = cacheTableManager.getCacheTable(CacheTableModel.GLOBAL_APPEARANCE);
					if (cacheTable == null)
						logOrThrowErrorMessage("Failed to access temporary table for global appearances.");
				}

				exporter = new DBSurfaceGeometry(connection, cacheTable, this, config);
			} else if (type == DBCityObject.class)
				exporter = new DBCityObject(connection, query, this);
			else if (type == DBGeneralization.class)
				exporter = new DBGeneralization(connection, query, this);
			else if (type == DBCityObjectGenericAttrib.class)
				exporter = new DBCityObjectGenericAttrib(connection, this);
			else if (type == DBAddress.class)
				exporter = new DBAddress(connection, this);
			else if (type == DBImplicitGeometry.class)
				exporter = new DBImplicitGeometry(connection, this);

			// building module
			else if (type == DBBuilding.class)
				exporter = new DBBuilding(connection, this);
			else if (type == DBThematicSurface.class)
				exporter = new DBThematicSurface(connection, this);
			else if (type == DBOpening.class)
				exporter = new DBOpening(connection, this);
			else if (type == DBBuildingInstallation.class)
				exporter = new DBBuildingInstallation(connection, this);
			else if (type == DBRoom.class)
				exporter = new DBRoom(connection, this);
			else if (type == DBBuildingFurniture.class)
				exporter = new DBBuildingFurniture(connection, this);

			// bridge module
			else if (type == DBBridge.class)
				exporter = new DBBridge(connection, this);
			else if (type == DBBridgeThematicSurface.class)
				exporter = new DBBridgeThematicSurface(connection, this);
			else if (type == DBBridgeOpening.class)
				exporter = new DBBridgeOpening(connection, this);
			else if (type == DBBridgeConstrElement.class)
				exporter = new DBBridgeConstrElement(connection, this);
			else if (type == DBBridgeInstallation.class)
				exporter = new DBBridgeInstallation(connection, this);
			else if (type == DBBridgeRoom.class)
				exporter = new DBBridgeRoom(connection, this);
			else if (type == DBBridgeFurniture.class)
				exporter = new DBBridgeFurniture(connection, this);

			// city furniture module
			else if (type == DBCityFurniture.class)
				exporter = new DBCityFurniture(connection, this);

			// city object group module
			else if (type == DBCityObjectGroup.class)
				exporter = new DBCityObjectGroup(connection, this);

			// generics module
			else if (type == DBGenericCityObject.class)
				exporter = new DBGenericCityObject(connection, this);

			// land use module
			else if (type == DBLandUse.class)
				exporter = new DBLandUse(connection, this);

			// vegetation module
			else if (type == DBPlantCover.class)
				exporter = new DBPlantCover(connection, this);
			else if (type == DBSolitaryVegetatObject.class)
				exporter = new DBSolitaryVegetatObject(connection, this);

			// relief module
			else if (type == DBReliefFeature.class)
				exporter = new DBReliefFeature(connection, this);
			else if (type == DBReliefComponent.class)
				exporter = new DBReliefComponent(connection, this);

			// transportation module
			else if (type == DBTransportationComplex.class)
				exporter = new DBTransportationComplex(connection, this);
			else if (type == DBTrafficArea.class)
				exporter = new DBTrafficArea(connection, this);

			// tunnel module
			else if (type == DBTunnel.class)
				exporter = new DBTunnel(connection, this);
			else if (type == DBTunnelThematicSurface.class)
				exporter = new DBTunnelThematicSurface(connection, this);
			else if (type == DBTunnelOpening.class)
				exporter = new DBTunnelOpening(connection, this);
			else if (type == DBTunnelInstallation.class)
				exporter = new DBTunnelInstallation(connection, this);
			else if (type == DBTunnelHollowSpace.class)
				exporter = new DBTunnelHollowSpace(connection, this);
			else if (type == DBTunnelFurniture.class)
				exporter = new DBTunnelFurniture(connection, this);

			// water body module
			else if (type == DBWaterBody.class)
				exporter = new DBWaterBody(connection, this);
			else if (type == DBWaterBoundarySurface.class)
				exporter = new DBWaterBoundarySurface(connection, this);

			// appearance module
			else if (type == DBGlobalAppearance.class) {
				CacheTable cacheTable = null;
				if (config.getInternal().isExportGlobalAppearances()) {
					cacheTable = cacheTableManager.getCacheTable(CacheTableModel.GLOBAL_APPEARANCE);
					if (cacheTable == null)
						logOrThrowErrorMessage("Failed to access temporary table for global appearances.");
				}

				exporter = new DBGlobalAppearance(connection, query, cacheTable, this, config);
			} else if (type == DBLocalAppearance.class)
				exporter = new DBLocalAppearance(connection, query, this, config);

			if (exporter == null)
				throw new CityGMLExportException("Failed to build database exporter of type " + type.getName() + ".");

			exporters.put(type, exporter);
		}

		return type.cast(exporter);
	}

}
