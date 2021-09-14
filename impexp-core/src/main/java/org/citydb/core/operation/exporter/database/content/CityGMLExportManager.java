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
package org.citydb.core.operation.exporter.database.content;

import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.exporter.ExportConfig;
import org.citydb.config.project.exporter.OutputFormat;
import org.citydb.config.project.exporter.XLinkConfig;
import org.citydb.config.project.exporter.XLinkFeatureConfig;
import org.citydb.core.ade.ADEExtension;
import org.citydb.core.ade.ADEExtensionManager;
import org.citydb.core.ade.exporter.ADEExportManager;
import org.citydb.core.ade.exporter.CityGMLExportHelper;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.*;
import org.citydb.core.operation.common.cache.*;
import org.citydb.core.operation.common.cache.model.CacheTableModel;
import org.citydb.core.operation.common.util.AffineTransformer;
import org.citydb.core.operation.common.xlink.DBXlink;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.util.*;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.core.operation.exporter.writer.FeatureWriter;
import org.citydb.core.plugin.PluginException;
import org.citydb.core.plugin.PluginManager;
import org.citydb.core.plugin.extension.exporter.FeatureExportExtension;
import org.citydb.core.query.Query;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.core.util.CoreConstants;
import org.citydb.core.util.Util;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.builder.jaxb.unmarshal.JAXBUnmarshaller;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.bridge.*;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.*;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.core.*;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.transportation.AbstractTransportationObject;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.tunnel.*;
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
import java.util.*;

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
	private final List<FeatureExportExtension> plugins;
	private final FeatureWriter featureWriter;
	private final WorkerPool<DBXlink> xlinkPool;
	private final IdCacheManager idCacheManager;
	private final CacheTableManager cacheTableManager;
	private final InternalConfig internalConfig;
	private final Config config;

	private final boolean failOnError;
	private final Set<String> localGeometryCache;
	private final AttributeValueSplitter attributeValueSplitter;
	private final ExportCounter exportCounter;
	private final JAXBUnmarshaller jaxbUnmarshaller;
	private final boolean hasADESupport;

	private GMLConverter gmlConverter;
	private LodGeometryChecker lodGeometryChecker;
	private AppearanceRemover appearanceRemover;
	private AffineTransformer affineTransformer;
	private Document document;

	public CityGMLExportManager(Connection connection,
			Query query,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			FeatureWriter featureWriter,
			WorkerPool<DBXlink> xlinkPool,
			IdCacheManager idCacheManager,
			CacheTableManager cacheTableManager,
			AffineTransformer affineTransformer,
			InternalConfig internalConfig,
			Config config) throws CityGMLExportException {
		this.connection = connection;
		this.query = query;
		this.databaseAdapter = databaseAdapter;
		this.schemaMapping = schemaMapping;
		this.cityGMLBuilder = cityGMLBuilder;
		this.featureWriter = featureWriter;
		this.xlinkPool = xlinkPool;
		this.idCacheManager = idCacheManager;
		this.cacheTableManager = cacheTableManager;
		this.internalConfig = internalConfig;
		this.config = config;

		adeManager = ADEExtensionManager.getInstance();
		hasADESupport = !adeManager.getEnabledExtensions().isEmpty();
		plugins = PluginManager.getInstance().getEnabledExternalPlugins(FeatureExportExtension.class);

		failOnError = config.getExportConfig().getGeneralOptions().isFailFastOnErrors();
		localGeometryCache = new HashSet<>();
		attributeValueSplitter = new AttributeValueSplitter();
		exportCounter = new ExportCounter(schemaMapping);

		if (!query.getLodFilter().preservesGeometry()) {
			lodGeometryChecker = new LodGeometryChecker(this, schemaMapping);
			if (config.getExportConfig().getAppearances().isSetExportAppearance())
				appearanceRemover = new AppearanceRemover();
		}

		if (config.getExportConfig().getAffineTransformation().isEnabled()) {
			this.affineTransformer = affineTransformer;
		}

		try {
			jaxbUnmarshaller = cityGMLBuilder.createJAXBUnmarshaller();
			jaxbUnmarshaller.setThrowMissingADESchema(false);
			jaxbUnmarshaller.setParseSchema(false);
		} catch (CityGMLBuilderException e) {
			throw new CityGMLExportException("Failed to build JAXB unmarshaller.", e);
		}
	}

	public AbstractGML exportObject(long objectId, AbstractObjectType<?> objectType) throws CityGMLExportException, SQLException {
		AbstractGML object = exportObject(objectId, objectType, false);
		return object != null ? processObject(object) : null;
	}

	private AbstractGML processObject(AbstractGML object) throws CityGMLExportException, SQLException {
		try {
			// execute batch export
			executeBatch();

			// remove empty city objects in case we filter LoDs
			if (lodGeometryChecker != null)
				lodGeometryChecker.cleanupCityObjects(object);

			// remove local appearances in case we filter LoDs
			if (appearanceRemover != null)
				appearanceRemover.cleanupAppearances(object);

			// cache geometry ids in case we export global appearances
			if (internalConfig.isExportGlobalAppearances())
				getExporter(DBGlobalAppearance.class).cacheGeometryIds(object);

			if (object instanceof AbstractFeature) {
				AbstractFeature feature = (AbstractFeature) object;

				// invoke export plugins
				if (!plugins.isEmpty()) {
					for (FeatureExportExtension plugin : plugins) {
						try {
							feature = plugin.postprocess(feature);
							if (feature == null)
								return null;
						} catch (PluginException e) {
							throw new CityGMLExportException("Export plugin " + plugin.getClass().getName() + " threw an exception.", e);
						}
					}
				}

				// trigger export of textures if required
				if (isLazyTextureExport() && config.getExportConfig().getAppearances().isSetExportAppearance())
					getExporter(DBLocalAppearance.class).triggerLazyTextureExport(feature);
			}

			return object;
		} finally {
			// clear local geometry cache
			localGeometryCache.clear();
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

	protected <T extends AbstractGML> T createObject(int objectClassId, Class<T> type) {
		AbstractGML object = Util.createObject(objectClassId, query.getTargetVersion());
		return type.isInstance(object) ? type.cast(object) : null;
	}

	private AbstractGML exportObject(long objectId, AbstractObjectType<?> objectType, boolean exportStub) throws CityGMLExportException, SQLException {
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
		else {
			getExporter(DBCityObject.class).addBatch(object, objectId, objectType, query.getProjectionFilter(objectType));
			success = true;
		}

		return success ? object : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractFeature> Collection<T> exportNestedFeatures(FeatureProperty featureProperty, long parentId, Class<T> featureClass) throws CityGMLExportException, SQLException {
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

	protected int getFeatureBatchSize() {
		return getBatchSize(config.getDatabaseConfig().getExportBatching().getFeatureBatchSize());
	}

	protected int getGeometryBatchSize() {
		return getBatchSize(config.getDatabaseConfig().getExportBatching().getGeometryBatchSize());
	}

	private int getBatchSize(int batchSize) {
		return Math.min(batchSize, databaseAdapter.getSQLAdapter().getMaximumNumberOfItemsForInOperator());
	}

	@Override
	public void executeBatch() throws CityGMLExportException, SQLException {
		getExporter(DBCityObject.class).executeBatch();
		getExporter(DBSurfaceGeometry.class).executeBatch();
	}

	@Override
	public SurfaceGeometryExporter getSurfaceGeometryExporter() throws CityGMLExportException, SQLException {
		return getExporter(DBSurfaceGeometry.class);
	}

	@Override
	public ImplicitGeometry createImplicitGeometry(long id, GeometryObject referencePoint, String transformationMatrix) throws CityGMLExportException, SQLException {
		return getExporter(DBImplicitGeometry.class).doExport(id, referencePoint, transformationMatrix);
	}

	public Appearance exportGlobalAppearance(long appearanceId) throws CityGMLExportException, SQLException {
		return getExporter(DBGlobalAppearance.class).doExport(appearanceId);
	}

	@Override
	public boolean exportAsGlobalFeature(AbstractFeature feature) throws CityGMLExportException, SQLException {
		if (featureWriter.supportsFlatHierarchies()) {
			if (!query.getFeatureTypeFilter().containsFeatureType(getFeatureType(feature)))
				feature.setLocalProperty(CoreConstants.EXPORT_AS_ADDITIONAL_OBJECT, true);

			AbstractGML object = processObject(feature);
			if (object instanceof AbstractFeature) {
				feature = (AbstractFeature) object;

				try {
					featureWriter.write(feature, -1);
				} catch (FeatureWriteException e) {
					throw new CityGMLExportException("Failed to write global feature with gml:id '" + feature.getId() + "'.", e);
				}

				updateExportCounter(feature);
				return true;
			}
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
			gmlConverter = new GMLConverter(query.isSetTargetSrs() ?
					query.getTargetSrs().getGMLSrsName() :
					databaseAdapter.getConnectionMetaData().getReferenceSystem().getGMLSrsName(),
					affineTransformer, config);
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
	public ExportConfig getExportConfig() {
		return config.getExportConfig();
	}

	public AffineTransformer getAffineTransformer() {
		return affineTransformer;
	}

	public InternalConfig getInternalConfig() {
		return internalConfig;
	}

	@Override
	public String getTableNameWithSchema(String tableName) {
		return databaseAdapter.getConnectionDetails().getSchema() + '.' + tableName;
	}

	@Override
	public ProjectionToken getGeometryColumn(Column column) {
		return (!internalConfig.isTransformCoordinates()) ?
				column :
				new Function(databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null"),
						column.getName(), column, new IntegerLiteral(query.getTargetSrs().getSrid()));
	}

	@Override
	public ProjectionToken getGeometryColumn(Column column, String asName) {
		return (!internalConfig.isTransformCoordinates()) ?
				new Column(column.getTable(), column.getName(), asName) :
				new Function(databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null"),
						asName, column, new IntegerLiteral(query.getTargetSrs().getSrid()));
	}

	@Override
	public String getGeometryColumn(String columnName) {
		return (!internalConfig.isTransformCoordinates()) ?
				columnName :
				databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null") +
						"(" + columnName + ", " + query.getTargetSrs().getSrid() + ") as " + columnName.replaceFirst(".*?\\.", "");
	}

	@Override
	public String getGeometryColumn(String columnName, String asName) {
		return (!internalConfig.isTransformCoordinates()) ?
				columnName + " as " + asName :
				databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null") +
						"(" + columnName + ", " + query.getTargetSrs().getSrid() + ") as " + asName;
	}

	@Override
	public void logOrThrowErrorMessage(String message) throws CityGMLExportException {
		logOrThrowErrorMessage(message, null);
	}

	@Override
	public void logOrThrowErrorMessage(String message, Throwable cause) throws CityGMLExportException {
		if (!failOnError) {
			log.error(message, cause);
		} else {
			throw new CityGMLExportException(message, cause);
		}
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

	public String generateFeatureGmlId(AbstractFeature feature) {
		return generateFeatureGmlId(feature, feature.getId());
	}

	public String generateFeatureGmlId(AbstractFeature feature, String oldGmlId) {
		if (internalConfig.getOutputFormat() == OutputFormat.CITYJSON) {
			return DefaultGMLIdManager.getInstance().generateUUID();
		} else {
			XLinkFeatureConfig xlinkOptions = config.getExportConfig().getCityGMLOptions().getXlink().getFeature();
			String gmlId = DefaultGMLIdManager.getInstance().generateUUID(xlinkOptions.getIdPrefix());
			if (oldGmlId != null) {
				if (xlinkOptions.isSetAppendId())
					gmlId = gmlId + "-" + oldGmlId;

				if (xlinkOptions.isSetKeepGmlIdAsExternalReference() && feature instanceof AbstractCityObject) {
					ExternalReference externalReference = new ExternalReference();
					if (internalConfig.getOutputFile() != null)
						externalReference.setInformationSystem(internalConfig.getOutputFile().getFile().toString());

					ExternalObject externalObject = new ExternalObject();
					externalObject.setName(oldGmlId);
					externalReference.setExternalObject(externalObject);

					((AbstractCityObject) feature).addExternalReference(externalReference);
				}
			}

			return gmlId;
		}
	}

	public String generateGeometryGmlId(AbstractGeometry geometry) {
		return generateGeometryGmlId(geometry.getId());
	}

	public String generateGeometryGmlId(String oldGmlId) {
		XLinkConfig xlinkOptions = config.getExportConfig().getCityGMLOptions().getXlink().getGeometry();
		String gmlId = DefaultGMLIdManager.getInstance().generateUUID(xlinkOptions.getIdPrefix());
		if (xlinkOptions.isSetAppendId() && oldGmlId != null) {
			gmlId = gmlId + "-" + oldGmlId;
		}

		return gmlId;
	}

	public void propagateXlink(DBXlink xlink) {
		xlinkPool.addWork(xlink);
	}

	@Override
	public boolean lookupAndPutObjectId(String gmlId, long id, int objectClassId) {
		IdCache cache = idCacheManager.getCache(IdCacheType.OBJECT);
		return cache != null && cache.lookupAndPut(gmlId, id, objectClassId);
	}

	@Override
	public boolean lookupObjectId(String gmlId) {
		IdCache cache = idCacheManager.getCache(IdCacheType.OBJECT);
		return cache != null && cache.get(gmlId) != null;
	}

	public void putObjectId(String gmlId, long id, int objectClassId) {
		IdCache cache = idCacheManager.getCache(IdCacheType.OBJECT);
		if (cache != null)
			cache.put(gmlId, id, -1, false, null, objectClassId);
	}	

	public boolean lookupAndPutGeometryId(String gmlId, long id, boolean useLocalScope) {
		boolean isCached = !localGeometryCache.add(gmlId);

		if (!useLocalScope) {
			IdCache cache = idCacheManager.getCache(IdCacheType.GEOMETRY);
			if (cache != null) {
				if (isCached) {
					cache.put(gmlId, id, 0, false, null, MappingConstants.SURFACE_GEOMETRY_OBJECTCLASS_ID);
				} else {
					isCached = cache.lookupAndPut(gmlId, id, MappingConstants.SURFACE_GEOMETRY_OBJECTCLASS_ID);
				}
			}
		}

		return isCached;
	}

	public boolean lookupGeometryId(String gmlId) {
		boolean isCached = localGeometryCache.contains(gmlId);
		if (!isCached) {
			IdCache cache = idCacheManager.getCache(IdCacheType.GEOMETRY);
			isCached = cache != null && cache.get(gmlId) != null;
		}

		return isCached;
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

	protected boolean hasADESupport() {
		return hasADESupport;
	}

	protected FeatureType getFeatureType(Class<? extends AbstractFeature> featureClass) {
		return schemaMapping.getFeatureType(Util.getObjectClassId(featureClass));
	}

	protected AbstractObjectType<?> getAbstractObjectType(Class<? extends AbstractGML> objectClass) {
		return schemaMapping.getAbstractObjectType(Util.getObjectClassId(objectClass));
	}

	public boolean isLazyTextureExport() {
		return !query.getLodFilter().preservesGeometry();
	}

	public void updateExportCounter(AbstractFeature feature) {
		exportCounter.updateExportCounter(feature);
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
		Set<String> adeHookTables = new HashSet<>();

		for (FeatureType featureType : schemaMapping.listFeatureTypesByTable(table.getName(), true)) {
			// skip ADE features - we do not support ADEs of ADEs
			if (adeManager.getExtensionByObjectClassId(featureType.getObjectClassId()) != null) {
				continue;
			}

			adeHookTables.addAll(getADEHookTables(featureType));
		}

		return adeHookTables;
	}

	protected Set<String> getADEHookTables(FeatureType featureType) {
		Set<String> adeHookTables = new HashSet<>();
		for (AbstractProperty property : featureType.listProperties(false, true)) {
			if (property instanceof InjectedProperty) {
				String adeHookTable = ((InjectedProperty) property).getBaseJoin().getTable();

				ADEExtension extension = adeManager.getExtensionByTableName(adeHookTable);
				if (extension == null || !extension.isEnabled()) {
					continue;
				}

				adeHookTables.add(adeHookTable);
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
			if (type == DBSurfaceGeometry.class)
				exporter = new DBSurfaceGeometry(connection, this);
			else if (type == DBCityObject.class)
				exporter = new DBCityObject(connection, query, this);
			else if (type == DBGeneralization.class)
				exporter = new DBGeneralization(connection, this);
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
				if (internalConfig.isExportGlobalAppearances()) {
					cacheTable = cacheTableManager.getCacheTable(CacheTableModel.GLOBAL_APPEARANCE);
					if (cacheTable == null)
						logOrThrowErrorMessage("Failed to access temporary table for global appearances.");
				}

				exporter = new DBGlobalAppearance(cacheTable, this, config);
			} else if (type == DBLocalAppearance.class)
				exporter = new DBLocalAppearance(connection, query, this, config);

			if (exporter == null)
				throw new CityGMLExportException("Failed to build database exporter of type " + type.getName() + ".");

			exporters.put(type, exporter);
		}

		return type.cast(exporter);
	}

}
