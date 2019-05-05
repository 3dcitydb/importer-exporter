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
package org.citydb.citygml.importer.database.content;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.ade.importer.ADEImportManager;
import org.citydb.ade.importer.ADEPropertyCollection;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.common.database.uid.UIDCache;
import org.citydb.citygml.common.database.uid.UIDCacheEntry;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.uid.UIDCacheType;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.database.SequenceHelper;
import org.citydb.citygml.importer.database.TableHelper;
import org.citydb.citygml.importer.util.ADEPropertyCollector;
import org.citydb.citygml.importer.util.AffineTransformer;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.citygml.importer.util.ExternalFileChecker;
import org.citydb.citygml.importer.util.ImportLogger.ImportLogEntry;
import org.citydb.citygml.importer.util.LocalAppearanceHandler;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.importer.Importer;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.ObjectType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.log.Logger;
import org.citydb.util.CoreConstants;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.marshal.JAXBMarshaller;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLModuleComponent;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
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
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.TransformationMatrix4x4;
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficArea;
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
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.module.ModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.util.xml.SAXWriter;
import org.citygml4j.xml.CityGMLNamespaceContext;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CityGMLImportManager implements CityGMLImportHelper {
	private final Logger log = Logger.getInstance();
	private final IdentityHashMap<Class<? extends DBImporter>, DBImporter> importers = new IdentityHashMap<>();
	private final IdentityHashMap<ADEExtension, ADEImportManager> adeImporters = new IdentityHashMap<>();

	private final Connection connection;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SchemaMapping schemaMapping;
	private final ADEExtensionManager adeManager;
	private final CityGMLBuilder cityGMLBuilder;
	private final WorkerPool<DBXlink> xlinkPool;
	private final UIDCacheManager uidCacheManager;
	private final Config config;

	private final TableHelper tableHelper;
	private final SequenceHelper sequenceHelper;
	private final GeometryConverter geometryConverter;
	private final Map<Integer, Long> objectCounter;
	private final Map<GMLClass, Long> geometryCounter;
	private final AttributeValueJoiner attributeValueJoiner;

	private ADEPropertyCollector propertyCollector;
	private LocalAppearanceHandler localAppearanceHandler;
	private List<ImportLogEntry> importLogEntries;
	private AffineTransformer affineTransformer;
	private ExternalFileChecker externalFileChecker;
	private CityGMLVersion cityGMLVersion;
	private JAXBMarshaller jaxbMarshaller;
	private SAXWriter saxWriter;

	private boolean failOnError = false;
	private boolean hasADESupport = false;

	public CityGMLImportManager(Connection connection, 
			AbstractDatabaseAdapter databaseAdapter, 
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkPool,
			UIDCacheManager uidCacheManager,
			AffineTransformer affineTransformer,
			Config config) throws SQLException {
		this.connection = connection;
		this.databaseAdapter = databaseAdapter;
		this.schemaMapping = schemaMapping;
		this.cityGMLBuilder = cityGMLBuilder;
		this.xlinkPool = xlinkPool;
		this.uidCacheManager = uidCacheManager;
		this.config = config;

		adeManager = ADEExtensionManager.getInstance();		
		hasADESupport = !adeManager.getEnabledExtensions().isEmpty();

		tableHelper = new TableHelper(schemaMapping);
		sequenceHelper = new SequenceHelper(connection, databaseAdapter, config);
		geometryConverter = new GeometryConverter(databaseAdapter, affineTransformer, config);
		objectCounter = new HashMap<>();
		geometryCounter = new HashMap<>();
		attributeValueJoiner = new AttributeValueJoiner();
		externalFileChecker = new ExternalFileChecker(config.getInternal().getCurrentImportFile());

		if (config.getProject().getImporter().getAppearances().isSetImportAppearance())
			localAppearanceHandler = new LocalAppearanceHandler(this);

		if (config.getProject().getImporter().getImportLog().isSetLogImportedFeatures())
			importLogEntries = new ArrayList<>();

		if (config.getProject().getImporter().getAffineTransformation().isEnabled())
			this.affineTransformer = affineTransformer;

		if (config.getProject().getImporter().getAddress().isSetImportXAL()) {
			cityGMLVersion = CityGMLVersion.DEFAULT;
			jaxbMarshaller = cityGMLBuilder.createJAXBMarshaller(cityGMLVersion);
			saxWriter = new SAXWriter();
		}

		if (hasADESupport)
			propertyCollector = new ADEPropertyCollector();
	}

	@Override
	public long importObject(AbstractGML object) throws CityGMLImportException, SQLException {
		if (object instanceof ADEModelObject) {
			ADEExtension extension = adeManager.getExtensionByObject((ADEModelObject)object);
			if (!extension.isEnabled())
				throw new CityGMLImportException("ADE extension for object " + getObjectSignature(object) + " is disabled. Skipping import.");
		}

		try {
			long id = 0;

			// top-level feature types
			if (object instanceof AbstractBuilding)
				id = getImporter(DBBuilding.class).doImport((AbstractBuilding)object);
			else if (object instanceof AbstractBridge)
				id = getImporter(DBBridge.class).doImport((AbstractBridge)object);
			else if (object instanceof AbstractTunnel)
				id = getImporter(DBTunnel.class).doImport((AbstractTunnel)object);
			else if (object instanceof CityFurniture)
				id = getImporter(DBCityFurniture.class).doImport((CityFurniture)object);
			else if (object instanceof CityObjectGroup)
				id = getImporter(DBCityObjectGroup.class).doImport((CityObjectGroup)object);
			else if (object instanceof GenericCityObject)
				id = getImporter(DBGenericCityObject.class).doImport((GenericCityObject)object);
			else if (object instanceof LandUse)
				id = getImporter(DBLandUse.class).doImport((LandUse)object);
			else if (object instanceof PlantCover)
				id = getImporter(DBPlantCover.class).doImport((PlantCover)object);
			else if (object instanceof SolitaryVegetationObject)
				id = getImporter(DBSolitaryVegetatObject.class).doImport((SolitaryVegetationObject)object);
			else if (object instanceof ReliefFeature)
				id = getImporter(DBReliefFeature.class).doImport((ReliefFeature)object);
			else if (object instanceof TransportationComplex)
				id = getImporter(DBTransportationComplex.class).doImport((TransportationComplex)object);
			else if (object instanceof WaterBody)
				id = getImporter(DBWaterBody.class).doImport((WaterBody)object);

			// nested feature types
			else if (object instanceof AbstractBoundarySurface)
				id = getImporter(DBThematicSurface.class).doImport((AbstractBoundarySurface)object);
			else if (object instanceof AbstractOpening)
				id = getImporter(DBOpening.class).doImport((AbstractOpening)object);
			else if (object instanceof BuildingInstallation)
				id = getImporter(DBBuildingInstallation.class).doImport((BuildingInstallation)object);
			else if (object instanceof IntBuildingInstallation)
				id = getImporter(DBBuildingInstallation.class).doImport((IntBuildingInstallation)object);
			else if (object instanceof Room)
				id = getImporter(DBRoom.class).doImport((Room)object);
			else if (object instanceof BuildingFurniture)
				id = getImporter(DBBuildingFurniture.class).doImport((BuildingFurniture)object);
			else if (object instanceof BridgeConstructionElement)
				id = getImporter(DBBridgeConstrElement.class).doImport((BridgeConstructionElement)object);
			else if (object instanceof BridgeFurniture)
				id = getImporter(DBBridgeFurniture.class).doImport((BridgeFurniture)object);
			else if (object instanceof BridgeInstallation)
				id = getImporter(DBBridgeInstallation.class).doImport((BridgeInstallation)object);
			else if (object instanceof IntBridgeInstallation)
				id = getImporter(DBBridgeInstallation.class).doImport((IntBridgeInstallation)object);
			else if (object instanceof org.citygml4j.model.citygml.bridge.AbstractOpening)
				id = getImporter(DBBridgeOpening.class).doImport((org.citygml4j.model.citygml.bridge.AbstractOpening)object);
			else if (object instanceof BridgeRoom)
				id = getImporter(DBBridgeRoom.class).doImport((BridgeRoom)object);
			else if (object instanceof org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)
				id = getImporter(DBBridgeThematicSurface.class).doImport((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)object);
			else if (object instanceof AbstractReliefComponent)
				id = getImporter(DBReliefComponent.class).doImport((AbstractReliefComponent)object);
			else if (object instanceof TrafficArea)
				id = getImporter(DBTrafficArea.class).doImport((TrafficArea)object);
			else if (object instanceof AuxiliaryTrafficArea)
				id = getImporter(DBTrafficArea.class).doImport((AuxiliaryTrafficArea)object);
			else if (object instanceof TunnelFurniture)
				id = getImporter(DBTunnelFurniture.class).doImport((TunnelFurniture)object);
			else if (object instanceof HollowSpace)
				id = getImporter(DBTunnelHollowSpace.class).doImport((HollowSpace)object);
			else if (object instanceof TunnelInstallation)
				id = getImporter(DBTunnelInstallation.class).doImport((TunnelInstallation)object);
			else if (object instanceof IntTunnelInstallation)
				id = getImporter(DBTunnelInstallation.class).doImport((IntTunnelInstallation)object);
			else if (object instanceof org.citygml4j.model.citygml.tunnel.AbstractOpening)
				id = getImporter(DBTunnelOpening.class).doImport((org.citygml4j.model.citygml.tunnel.AbstractOpening)object);
			else if (object instanceof org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)
				id = getImporter(DBTunnelThematicSurface.class).doImport((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)object);
			else if (object instanceof AbstractWaterBoundarySurface)
				id = getImporter(DBWaterBoundarySurface.class).doImport((AbstractWaterBoundarySurface)object);		
			else if (object instanceof Address)
				id = getImporter(DBAddress.class).doImport((Address)object);

			// generic fallback for any ADE object
			else if (object != null)
				id = getImporter(DBCityObject.class).doImport(object);

			// import local appearances
			if (id != 0 && !object.isSetParent())
				getImporter(DBAppearance.class).importLocalAppearance();

			return id;
		} catch (CityGMLImportException e) {
			throw new CityGMLImportException("Failed to import " + getObjectSignature(object) + ".", e);
		}
	}

	@Override
	public long importObject(AbstractGML object, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
		if (foreignKeys != null)
			object.setLocalProperty(CoreConstants.FOREIGN_KEYS_SET, foreignKeys);

		return importObject(object);
	}

	protected void delegateToADEImporter(AbstractGML object, long objectId, AbstractObjectType<?> objectType) throws CityGMLImportException, SQLException {
		// delegate import of ADE object to an ADE importer
		if (object instanceof ADEModelObject) {
			ADEModelObject adeObject = (ADEModelObject)object;

			ForeignKeys foreignKeys = (ForeignKeys)object.getLocalProperty(CoreConstants.FOREIGN_KEYS_SET);
			if (foreignKeys == null)
				foreignKeys = ForeignKeys.EMPTY_SET;

			getADEImportManager(adeObject).importObject(adeObject, objectId, objectType, foreignKeys);
		}

		// if the object is a CityGML feature or an ADE feature derived from a CityGML feature
		// then check for generic ADE properties and delegate their import to an ADE importer
		if (object instanceof AbstractFeature && object instanceof CityGMLModuleComponent) {
			AbstractFeature feature = (AbstractFeature)object;

			List<ADEModelObject> properties = propertyCollector.getADEProperties(feature);
			if (properties != null && !properties.isEmpty()) {
				IdentityHashMap<ADEImportManager, ADEPropertyCollection> groupedBy = new IdentityHashMap<>();
				for (ADEModelObject property : properties) {
					ADEImportManager adeImporter = getADEImportManager(property);
					ADEPropertyCollection collection = groupedBy.get(adeImporter);
					if (collection == null) {
						collection = new ADEPropertyCollection();
						groupedBy.put(adeImporter, collection);
					}

					collection.register(property);
				}

				for (Entry<ADEImportManager, ADEPropertyCollection> entry : groupedBy.entrySet())
					entry.getKey().importGenericApplicationProperties(entry.getValue(), feature, objectId, (FeatureType)objectType);
			}
		}
	}

	@Override
	public long importGlobalAppearance(Appearance appearance) throws CityGMLImportException, SQLException {
		return getImporter(DBAppearance.class).doImport(appearance, 0, false);
	}

	@Override
	public long importSurfaceGeometry(AbstractGeometry surfaceGeometry, long cityObjectId) throws CityGMLImportException, SQLException {
		return getImporter(DBSurfaceGeometry.class).doImport(surfaceGeometry, cityObjectId);
	}	

	@Override
	public long importImplicitGeometry(ImplicitGeometry implicitGeometry) throws CityGMLImportException, SQLException {
		return getImporter(DBImplicitGeometry.class).doImport(implicitGeometry);
	}

	public void importExternalReference(ExternalReference externalReference, long cityObjectId) throws CityGMLImportException, SQLException {
		getImporter(DBExternalReference.class).doImport(externalReference, cityObjectId);
	}

	public void importGenericAttribute(AbstractGenericAttribute genericAttribute, long cityObjectId) throws CityGMLImportException, SQLException {
		getImporter(DBCityObjectGenericAttrib.class).doImport(genericAttribute, cityObjectId);
	}

	public void importGenericAttribute(AbstractGenericAttribute genericAttribute, long parentId, long rootId, long cityObjectId) throws CityGMLImportException, SQLException {
		getImporter(DBCityObjectGenericAttrib.class).doImport(genericAttribute, parentId, rootId, cityObjectId);
	}

	@Override
	public boolean isSurfaceGeometry(AbstractGeometry abstractGeometry) {
		return geometryConverter.isSurfaceGeometry(abstractGeometry);
	}

	@Override
	public boolean isPointOrLineGeometry(AbstractGeometry abstractGeometry) {
		return geometryConverter.isPointOrLineGeometry(abstractGeometry);
	}

	@Override
	public GeometryConverter getGeometryConverter() {
		return geometryConverter;
	}

	@Override
	public String convertImplicitGeometryTransformationMatrix(TransformationMatrix4x4 transformationMatrix) {
		if (transformationMatrix != null && transformationMatrix.isSetMatrix()) {
			Matrix matrix = transformationMatrix.getMatrix();
			if (affineTransformer != null)
				matrix = affineTransformer.transformImplicitGeometryTransformationMatrix(matrix);
			
			return attributeValueJoiner.join(" ", matrix.toRowPackedList());
		} else
			return null;
	}

	@Override
	public AbstractDatabaseAdapter getDatabaseAdapter() {
		return databaseAdapter;
	}

	@Override
	public void propagateObjectXlink(AbstractObjectType<?> objectType, long objectId, String xlink, String propertyColumn) {
		xlinkPool.addWork(new DBXlinkBasic(objectType.getTable(), objectId, xlink, propertyColumn));
	}

	@Override
	public void propagateObjectXlink(String intermediateTable, long objectId, String fromColumn, String xlink, String toColumn) {
		xlinkPool.addWork(new DBXlinkBasic(intermediateTable, objectId, fromColumn, xlink, toColumn));
	}

	@Override
	public void propagateReverseObjectXlink(String toTable, String gmlId, long objectId, String propertyColumn) {
		xlinkPool.addWork(new DBXlinkBasic(toTable, gmlId, objectId, propertyColumn));
	}

	@Override
	public void propagateSurfaceGeometryXlink(String xlink, AbstractObjectType<?> objectType, long objectId, String propertyColumn) {
		xlinkPool.addWork(new DBXlinkSurfaceGeometry(objectType.getObjectClassId(), objectId, xlink, propertyColumn));
	}

	@Override
	public int getObjectClassId(AbstractGML object) {
		return Util.getObjectClassId(object.getClass());
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
		return object instanceof AbstractFeature ? getFeatureType((AbstractFeature)object) : getObjectType(object);
	}

	@Override
	public void executeBatch(String tableName) throws CityGMLImportException, SQLException {
		for (String dependency : tableHelper.getCommitOrder(tableName))
			doExecuteBatch(dependency);
	}

	@Override
	public void executeBatch(AbstractObjectType<?> type) throws CityGMLImportException, SQLException {
		for (String dependency : tableHelper.getCommitOrder(type.getTable()))
			doExecuteBatch(dependency);
	}

	@Override
	public String getTableNameWithSchema(String tableName) {
		return databaseAdapter.getConnectionDetails().getSchema() + '.' + tableName;
	}

	@Override
	public long getNextSequenceValue(String sequence) throws SQLException {
		return sequenceHelper.getNextSequenceValue(sequence);
	}

	@Override
	public AttributeValueJoiner getAttributeValueJoiner() {
		return attributeValueJoiner;
	}

	@Override
	public void logOrThrowUnsupportedXLinkMessage(AbstractGML from, Class<? extends AbstractGML> to, String xlink) throws CityGMLImportException {
		StringBuilder msg = new StringBuilder(getObjectSignature(from)).append(": Unsupported XLink reference '").append(xlink).append("'");
		AbstractObjectType<?> target = schemaMapping.getAbstractObjectType(Util.getObjectClassId(to));				
		if (target != null)
			msg.append(" to ").append(target.getPath()).append(" object.");
		else
			msg.append(".");

		logOrThrowErrorMessage(msg.toString());
	}

	@Override
	public void logOrThrowUnsupportedGeometryMessage(AbstractGML from, AbstractGeometry geometry) throws CityGMLImportException {
		logOrThrowErrorMessage(getObjectSignature(from) +
				": Unsupported geometry type " +
				"gml:" + geometry.getGMLClass() + ".");
	}

	@Override
	public void logOrThrowErrorMessage(String message) throws CityGMLImportException {
		if (!failOnError)
			log.error(message);
		else
			throw new CityGMLImportException(message);
	}

	@Override
	public String getObjectSignature(AbstractGML object) {
		return getObjectSignature(object, object.hasLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID) ?
				(String)object.getLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID) : object.getId());
	}

	@Override
	public boolean isFailOnError() {
		return failOnError;
	}

	@Override
	public Importer getImportConfig() {
		return config.getProject().getImporter();
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	protected boolean hasADESupport() {
		return hasADESupport;
	}

	public String generateNewGmlId() {
		return DefaultGMLIdManager.getInstance().generateUUID(config.getProject().getImporter().getGmlId().getIdPrefix());
	}

	public LocalAppearanceHandler getLocalAppearanceHandler() {
		return localAppearanceHandler;
	}

	public AffineTransformer getAffineTransformer() {
		return affineTransformer;
	}

	public ExternalFileChecker getExternalFileChecker() {
		return externalFileChecker;
	}

	public void propagateXlink(DBXlink xlink) {
		xlinkPool.addWork(xlink);
	}

	public CityGMLBuilder getCityGMLBuilder() {
		return cityGMLBuilder;
	}

	protected void updateObjectCounter(AbstractGML object, int objectClassId, long id) {
		Long counter = objectCounter.get(objectClassId);
		if (counter == null)
			objectCounter.put(objectClassId, 1l);
		else
			objectCounter.put(objectClassId, counter + 1);		

	}

	protected void updateObjectCounter(AbstractGML object, AbstractObjectType<?> type, long id) {
		updateObjectCounter(object, type.getObjectClassId(), id);

		// create import log entry for top-level features
		if (!object.isSetParent() && importLogEntries != null)
			importLogEntries.add(new ImportLogEntry(type.getPath(), id, (String)object.getLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID)));
	}

	protected void updateGeometryCounter(GMLClass type) {
		Long counter = geometryCounter.get(type);
		if (counter == null)
			geometryCounter.put(type, 1l);
		else
			geometryCounter.put(type, counter + 1);
	}

	public Map<Integer, Long> getAndResetObjectCounter() {
		Map<Integer, Long> tmp = new HashMap<>(objectCounter);
		objectCounter.clear();
		return tmp;
	}

	public Map<GMLClass, Long> getAndResetGeometryCounter() {
		Map<GMLClass, Long> tmp = new HashMap<>(geometryCounter);
		geometryCounter.clear();
		return tmp;
	}

	public List<ImportLogEntry> getAndResetImportLogEntries() {
		List<ImportLogEntry> tmp = new ArrayList<>(importLogEntries);
		importLogEntries.clear();
		return tmp;
	}

	public void putObjectUID(String gmlId, long id, String mapping, int objectClassId) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.OBJECT);
		if (cache != null)
			cache.put(gmlId, id, -1, false, mapping, objectClassId);
	}

	public void putObjectUID(String gmlId, long id, int objectClassId) {
		putObjectUID(gmlId, id, null, objectClassId);
	}

	protected boolean lookupAndPutObjectUID(String gmlId, long id, int objectClassId) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.OBJECT);
		if (cache != null)
			return cache.lookupAndPut(gmlId, id, objectClassId);
		else
			return false;
	}

	public long getObjectId(String gmlId) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.OBJECT);
		if (cache != null) {
			UIDCacheEntry entry = cache.get(gmlId);
			if (entry != null)
				return entry.getId();
		}

		return -1;
	}

	public void putTextureImageUID(String gmlId, long id) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.TEXTURE_IMAGE);
		if (cache != null)
			cache.put(gmlId, id, -1, false, null, 0);
	}

	public long getTextureImageId(String gmlId) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.TEXTURE_IMAGE);

		if (cache != null) {
			UIDCacheEntry entry = cache.get(gmlId);
			if (entry != null)
				return entry.getId();
		}

		return -1;
	}

	public void putGeometryUID(String gmlId, long id, long rootId, boolean reverse, String mapping) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.GEOMETRY);
		if (cache != null)
			cache.put(gmlId, id, rootId, reverse, mapping, 0);
	}

	public long getGeometryIdFromMemory(String gmlId) {
		UIDCache cache = uidCacheManager.getCache(UIDCacheType.GEOMETRY);

		if (cache != null) {
			UIDCacheEntry entry = cache.getFromMemory(gmlId);
			if (entry != null)
				return entry.getId();
		}

		return -1;
	}

	protected String getObjectSignature(AbstractGML object, String gmlId) {
		StringBuilder signature = new StringBuilder();

		if (object instanceof AbstractGeometry)		
			signature.append("gml:").append(((AbstractGeometry)object).getGMLClass());
		else {		
			AbstractObjectType<?> type = getAbstractObjectType(object);			
			if (type != null)
				signature.append(type.getSchema().getXMLPrefix()).append(":").append(type.getPath());
			else
				signature.append((object instanceof CityGML) ? ((CityGML)object).getCityGMLClass().toString() : object.getGMLClass().toString());
		}

		if (gmlId != null)
			signature.append(" '").append(gmlId).append("'");
		else
			signature.append(" (unknown gml:id)");

		return signature.toString();	
	}

	public String marshalObject(ModelObject object, ModuleType... moduleTypes) {
		String result = null;

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
			CityGMLNamespaceContext ctx = new CityGMLNamespaceContext();
			for (ModuleType moduleType : moduleTypes)
				ctx.setPrefix(cityGMLVersion.getModule(moduleType));

			saxWriter.setOutput(out);
			saxWriter.setNamespaceContext(ctx);

			Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
			JAXBElement<?> jaxbElement = jaxbMarshaller.marshalJAXBElement(object);
			if (jaxbElement != null)
				marshaller.marshal(jaxbElement, saxWriter);

			saxWriter.flush();
			result = out.toString();
			out.reset();
		} catch (JAXBException | IOException | SAXException e) {
			//
		} finally {
			saxWriter.reset();
		}

		return result;
	}

	public void executeBatch() throws CityGMLImportException, SQLException {
		for (String dependency : tableHelper.getCommitOrder())
			doExecuteBatch(dependency);
	}

	public void executeBatch(TableEnum table) throws CityGMLImportException, SQLException {
		for (String dependency : tableHelper.getCommitOrder(table.getName()))
			doExecuteBatch(dependency);
	}

	private void doExecuteBatch(String tableName) throws CityGMLImportException, SQLException {
		// check whether whether we deal with a predefined 3dcitydb table
		// in which case we pick a predefined importer to execute the batch
		TableEnum table = TableEnum.fromTableName(tableName);
		if (table != TableEnum.UNDEFINED) {
			DBImporter importer = importers.get(tableHelper.getImporterClass(table));
			if (importer != null)
				importer.executeBatch();
		}

		else {
			// otherwise, let an ADE extension deal with the ADE table
			ADEExtension extension = adeManager.getExtensionByTableName(tableName);
			if (extension == null)
				throw new CityGMLImportException("Failed to find an ADE extension for the ADE table '" + tableName + "'.");

			ADEImportManager adeImporter = adeImporters.get(extension);
			if (adeImporter != null)
				adeImporter.executeBatch(tableName);
		}
	}

	public void close() throws CityGMLImportException, SQLException {
		sequenceHelper.close();
		for (DBImporter importer : importers.values())
			importer.close();

		for (ADEImportManager adeImporter : adeImporters.values())			
			adeImporter.close();

		importers.clear();
	}

	private ADEImportManager getADEImportManager(ADEModelObject object) throws CityGMLImportException, SQLException {		
		ADEExtension extension = adeManager.getExtensionByObject(object);
		if (!extension.isEnabled()) {
			throw new CityGMLImportException("ADE extension for object " +
					(object instanceof AbstractGML ? getObjectSignature((AbstractGML) object) : object.getClass().getName()) +
					" is disabled. Skipping import.");
		}

		ADEImportManager adeImporter = adeImporters.get(extension);
		if (adeImporter == null) {
			adeImporter = extension.createADEImportManager();
			if (adeImporter == null)
				throw new CityGMLImportException("Failed to create ADE importer for '" +
						extension.getMetadata().getIdentifier() + "'");

			adeImporter.init(connection, this);
			adeImporters.put(extension, adeImporter);
		}

		return adeImporter;
	}

	public <T extends DBImporter> T getImporter(Class<T> type) throws CityGMLImportException, SQLException {
		DBImporter importer = importers.get(type);

		if (importer == null) {			
			// core module
			if (type == DBSurfaceGeometry.class)
				importer = new DBSurfaceGeometry(connection, config, this);
			else if (type == DBCityObject.class)
				importer = new DBCityObject(connection, config, this);
			else if (type == DBExternalReference.class)
				importer = new DBExternalReference(connection, config, this);
			else if (type == DBCityObjectGenericAttrib.class)
				importer = new DBCityObjectGenericAttrib(connection, config, this);
			else if (type == DBAddress.class)
				importer = new DBAddress(connection, config, this);
			else if (type == DBImplicitGeometry.class)
				importer = new DBImplicitGeometry(connection, config, this);

			// building module
			else if (type == DBBuilding.class)
				importer = new DBBuilding(connection, config, this);
			else if (type == DBThematicSurface.class)
				importer = new DBThematicSurface(connection, config, this);
			else if (type == DBOpening.class)
				importer = new DBOpening(connection, config, this);
			else if (type == DBBuildingInstallation.class)
				importer = new DBBuildingInstallation(connection, config, this);
			else if (type == DBRoom.class)
				importer = new DBRoom(connection, config, this);
			else if (type == DBBuildingFurniture.class)
				importer = new DBBuildingFurniture(connection, config, this);
			else if (type == DBOpeningToThemSurface.class)
				importer = new DBOpeningToThemSurface(connection, config, this);
			else if (type == DBAddressToBuilding.class)
				importer = new DBAddressToBuilding(connection, config, this);

			// bridge module
			else if (type == DBBridge.class)
				importer = new DBBridge(connection, config, this);
			else if (type == DBBridgeConstrElement.class)
				importer = new DBBridgeConstrElement(connection, config, this);
			else if (type == DBBridgeFurniture.class)
				importer = new DBBridgeFurniture(connection, config, this);
			else if (type == DBBridgeInstallation.class)
				importer = new DBBridgeInstallation(connection, config, this);
			else if (type == DBBridgeOpening.class)
				importer = new DBBridgeOpening(connection, config, this);
			else if (type == DBBridgeOpenToThemSrf.class)
				importer = new DBBridgeOpenToThemSrf(connection, config, this);
			else if (type == DBBridgeRoom.class)
				importer = new DBBridgeRoom(connection, config, this);
			else if (type == DBBridgeThematicSurface.class)
				importer = new DBBridgeThematicSurface(connection, config, this);
			else if (type == DBAddressToBridge.class)
				importer = new DBAddressToBridge(connection, config, this);

			// city furniture module
			else if (type == DBCityFurniture.class)
				importer = new DBCityFurniture(connection, config, this);

			// city object group module
			else if (type == DBCityObjectGroup.class)
				importer = new DBCityObjectGroup(connection, config, this);

			// generics module
			else if (type == DBGenericCityObject.class)
				importer = new DBGenericCityObject(connection, config, this);

			// land use module
			else if (type == DBLandUse.class)
				importer = new DBLandUse(connection, config, this);

			// vegetation module
			else if (type == DBPlantCover.class)
				importer = new DBPlantCover(connection, config, this);
			else if (type == DBSolitaryVegetatObject.class)
				importer = new DBSolitaryVegetatObject(connection, config, this);

			// relief module
			else if (type == DBReliefComponent.class)
				importer = new DBReliefComponent(connection, config, this);
			else if (type == DBReliefFeatToRelComp.class)
				importer = new DBReliefFeatToRelComp(connection, config, this);
			else if (type == DBReliefFeature.class)
				importer = new DBReliefFeature(connection, config, this);

			// transportation module
			else if (type == DBTrafficArea.class)
				importer = new DBTrafficArea(connection, config, this);
			else if (type == DBTransportationComplex.class)
				importer = new DBTransportationComplex(connection, config, this);

			// tunnel module
			else if (type == DBTunnel.class)
				importer = new DBTunnel(connection, config, this);
			else if (type == DBTunnelFurniture.class)
				importer = new DBTunnelFurniture(connection, config, this);
			else if (type == DBTunnelHollowSpace.class)
				importer = new DBTunnelHollowSpace(connection, config, this);
			else if (type == DBTunnelInstallation.class)
				importer = new DBTunnelInstallation(connection, config, this);
			else if (type == DBTunnelOpening.class)
				importer = new DBTunnelOpening(connection, config, this);
			else if (type == DBTunnelOpenToThemSrf.class)
				importer = new DBTunnelOpenToThemSrf(connection, config, this);
			else if (type == DBTunnelThematicSurface.class)
				importer = new DBTunnelThematicSurface(connection, config, this);

			// waterbody module
			else if (type == DBWaterBodToWaterBndSrf.class)
				importer = new DBWaterBodToWaterBndSrf(connection, config, this);
			else if (type == DBWaterBody.class)
				importer = new DBWaterBody(connection, config, this);
			else if (type == DBWaterBoundarySurface.class)
				importer = new DBWaterBoundarySurface(connection, config, this);

			// appearance module
			else if (type == DBAppearance.class)
				importer = new DBAppearance(connection, config, this);
			else if (type == DBSurfaceData.class)
				importer = new DBSurfaceData(connection, config, this);
			else if (type == DBTextureParam.class)
				importer = new DBTextureParam(connection, config, this);
			else if (type == DBAppearToSurfaceData.class)
				importer = new DBAppearToSurfaceData(connection, config, this);
			else if (type == DBTexImage.class)
				importer = new DBTexImage(connection, config, this);

			if (importer == null)
				throw new CityGMLImportException("Failed to build database importer of type " + type.getName() + ".");

			importers.put(type, importer);
		}

		return type.cast(importer);
	}

}
