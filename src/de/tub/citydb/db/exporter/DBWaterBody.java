package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.impl.jaxb.gml._3_1_1.MultiSurfacePropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.SolidPropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.StringOrRefImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.SurfacePropertyImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.waterbody.BoundedByWaterSurfaceProperty;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.citygml.waterbody.WaterBodyModule;
import org.citygml4j.model.citygml.waterbody.WaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.gml.AbstractSolid;
import org.citygml4j.model.gml.AbstractSurface;
import org.citygml4j.model.gml.MultiCurveProperty;
import org.citygml4j.model.gml.MultiSurface;
import org.citygml4j.model.gml.MultiSurfaceProperty;
import org.citygml4j.model.gml.SolidProperty;
import org.citygml4j.model.gml.StringOrRef;
import org.citygml4j.model.gml.SurfaceProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.UUIDManager;
import de.tub.citydb.util.Util;

public class DBWaterBody implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psWaterBody;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBSdoGeometry sdoGeometry;
	private FeatureClassFilter featureClassFilter;

	private String gmlNameDelimiter;
	private WaterBodyModule wtr;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean keepOldGmlId;
	private boolean transformCoords;
	private String gmlIdPrefix;
	private String infoSys;

	public DBWaterBody(Connection connection, CityGMLFactory cityGMLFactory, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		wtr = config.getProject().getExporter().getModuleVersion().getWaterBody().getModule();

		useXLink = config.getProject().getExporter().getXlink().getFeature().isModeXLink();
		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetAppendId();
			keepOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetKeepGmlIdAsExternalReference();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getFeature().getIdPrefix();
			infoSys = config.getInternal().getExportFileName();
		}	

		transformCoords = config.getInternal().isTransformCoordinates();
		if (!transformCoords) {		
			psWaterBody = connection.prepareStatement("select wb.NAME as WB_NAME, wb.NAME_CODESPACE as WB_NAME_CODESPACE, wb.DESCRIPTION as WB_DESCRIPTION, wb.CLASS, wb.FUNCTION, wb.USAGE, " +
					"wb.LOD1_SOLID_ID, wb.LOD2_SOLID_ID, wb.LOD3_SOLID_ID, wb.LOD4_SOLID_ID, wb.LOD0_MULTI_SURFACE_ID, wb.LOD1_MULTI_SURFACE_ID, " +
					"wb.LOD0_MULTI_CURVE, wb.LOD1_MULTI_CURVE, " +
					"ws.ID as WS_ID, ws.NAME as WS_NAME, ws.NAME_CODESPACE as WS_NAME_CODESPACE, ws.DESCRIPTION as WS_DESCRIPTION, upper(ws.TYPE) as TYPE, ws.WATER_LEVEL, " +
					"ws.LOD2_SURFACE_ID, ws.LOD3_SURFACE_ID, ws.LOD4_SURFACE_ID " +
			"from WATERBODY wb left join WATERBOD_TO_WATERBND_SRF w2s on wb.ID=w2s.WATERBODY_ID left join WATERBOUNDARY_SURFACE ws on ws.ID=w2s.WATERBOUNDARY_SURFACE_ID where wb.ID=?");
		} else {
			ReferenceSystem targetSRS = config.getInternal().getExportTargetSRS();
			int srid = targetSRS.getSrid();
			
			psWaterBody = connection.prepareStatement("select wb.NAME as WB_NAME, wb.NAME_CODESPACE as WB_NAME_CODESPACE, wb.DESCRIPTION as WB_DESCRIPTION, wb.CLASS, wb.FUNCTION, wb.USAGE, " +
					"wb.LOD1_SOLID_ID, wb.LOD2_SOLID_ID, wb.LOD3_SOLID_ID, wb.LOD4_SOLID_ID, wb.LOD0_MULTI_SURFACE_ID, wb.LOD1_MULTI_SURFACE_ID, " +
					"geodb_util.transform_or_null(wb.LOD0_MULTI_CURVE, " + srid + ") AS LOD0_MULTI_CURVE, " +
					"geodb_util.transform_or_null(wb.LOD1_MULTI_CURVE, " + srid + ") AS LOD1_MULTI_CURVE, " +
					"ws.ID as WS_ID, ws.NAME as WS_NAME, ws.NAME_CODESPACE as WS_NAME_CODESPACE, ws.DESCRIPTION as WS_DESCRIPTION, upper(ws.TYPE) as TYPE, ws.WATER_LEVEL, " +
					"ws.LOD2_SURFACE_ID, ws.LOD3_SURFACE_ID, ws.LOD4_SURFACE_ID " +
			"from WATERBODY wb left join WATERBOD_TO_WATERBND_SRF w2s on wb.ID=w2s.WATERBODY_ID left join WATERBOUNDARY_SURFACE ws on ws.ID=w2s.WATERBOUNDARY_SURFACE_ID where wb.ID=?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, JAXBException {
		WaterBody waterBody = cityGMLFactory.createWaterBody(wtr);
		long waterBodyId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(waterBody, waterBodyId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psWaterBody.setLong(1, waterBodyId);
			rs = psWaterBody.executeQuery();
			boolean waterBodyRead = false;

			while (rs.next()) {

				if (!waterBodyRead) {
					// name and name_codespace
					String gmlName = rs.getString("WB_NAME");
					String gmlNameCodespace = rs.getString("WB_NAME_CODESPACE");

					Util.dbGmlName2featureName(waterBody, gmlName, gmlNameCodespace, gmlNameDelimiter);

					String description = rs.getString("WB_DESCRIPTION");
					if (description != null) {
						StringOrRef stringOrRef = new StringOrRefImpl();
						stringOrRef.setValue(description);
						waterBody.setDescription(stringOrRef);
					}

					String clazz = rs.getString("CLASS");
					if (clazz != null) {
						waterBody.setClazz(clazz);
					}

					String function = rs.getString("FUNCTION");
					if (function != null) {
						Pattern p = Pattern.compile("\\s+");
						String[] functionList = p.split(function.trim());
						waterBody.setFunction(Arrays.asList(functionList));
					}

					String usage = rs.getString("USAGE");
					if (usage != null) {
						Pattern p = Pattern.compile("\\s+");
						String[] usageList = p.split(usage.trim());
						waterBody.setUsage(Arrays.asList(usageList));
					}

					for (int lod = 1; lod < 5 ; lod++) {
						long geometryId = rs.getLong("LOD" + lod + "_SOLID_ID");

						if (!rs.wasNull() && geometryId != 0) {
							DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

							if (geometry != null) {
								SolidProperty solidProperty = new SolidPropertyImpl();

								if (geometry.getAbstractGeometry() != null)
									solidProperty.setSolid((AbstractSolid)geometry.getAbstractGeometry());
								else
									solidProperty.setHref(geometry.getTarget());

								switch (lod) {
								case 1:
									waterBody.setLod1Solid(solidProperty);
									break;
								case 2:
									waterBody.setLod2Solid(solidProperty);
									break;
								case 3:
									waterBody.setLod3Solid(solidProperty);
									break;
								case 4:
									waterBody.setLod4Solid(solidProperty);
									break;
								}
							}
						}
					}

					for (int lod = 0; lod < 2 ; lod++) {
						long geometryId = rs.getLong("LOD" + lod + "_MULTI_SURFACE_ID");

						if (!rs.wasNull() && geometryId != 0) {
							DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

							if (geometry != null) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfacePropertyImpl();

								if (geometry.getAbstractGeometry() != null)
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getTarget());

								switch (lod) {
								case 0:
									waterBody.setLod0MultiSurface(multiSurfaceProperty);
									break;
								case 1:
									waterBody.setLod1MultiSurface(multiSurfaceProperty);
									break;
								}
							}
						}
					}

					// lodXMultiCurve
					for (int lod = 0; lod < 2; lod++) {
						JGeometry multiCurve = null;
						STRUCT multiCurveObj = (STRUCT)rs.getObject("LOD" + lod + "_MULTI_CURVE");

						if (!rs.wasNull() && multiCurveObj != null) {
							multiCurve = JGeometry.load(multiCurveObj);

							if (multiCurve != null) {
								MultiCurveProperty multiCurveProperty = sdoGeometry.getMultiCurveProperty(multiCurve, false);
								if (multiCurveProperty != null) {
									switch (lod) {
									case 0:
										waterBody.setLod0MultiCurve(multiCurveProperty);
										break;
									case 1:
										waterBody.setLod1MultiCurve(multiCurveProperty);
										break;
									}
								}
							}
						}
					}

					waterBodyRead = true;
				}

				// water boundary surfaces
				long waterBoundarySurfaceId = rs.getLong("WS_ID");
				if (rs.wasNull())
					continue;

				// create new water boundary object
				WaterBoundarySurface waterBoundarySurface = null;
				String type = rs.getString("TYPE");
				if (rs.wasNull() || type == null || type.length() == 0)
					continue;

				if (type.equals(CityGMLClass.WATERSURFACE.toString().toUpperCase()))
					waterBoundarySurface = cityGMLFactory.createWaterSurface(wtr);
				else if (type.equals(CityGMLClass.WATERGROUNDSURFACE.toString().toUpperCase()))
					waterBoundarySurface = cityGMLFactory.createWaterGroundSurface(wtr);
				else if (type.equals(CityGMLClass.WATERCLOSURESURFACE.toString().toUpperCase()))
					waterBoundarySurface = cityGMLFactory.createWaterClosureSurface(wtr);

				if (waterBoundarySurface == null)
					continue;

				// cityobject stuff
				cityObjectExporter.read(waterBoundarySurface, waterBoundarySurfaceId);

				if (waterBoundarySurface.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(waterBoundarySurface.getId(), waterBoundarySurfaceId, CityGMLClass.WATERBOUNDARYSURFACE)) {
						if (useXLink) {
							BoundedByWaterSurfaceProperty boundedByProperty = cityGMLFactory.createBoundedByWaterSurfaceProperty(wtr);
							boundedByProperty.setHref("#" + waterBoundarySurface.getId());

							waterBody.addBoundedBySurface(boundedByProperty);
							continue;
						} else {
							String newGmlId = UUIDManager.randomUUID(gmlIdPrefix);
							if (appendOldGmlId)
								newGmlId += '-' + waterBoundarySurface.getId();

							if (keepOldGmlId) {
								CoreModule core = (CoreModule)waterBoundarySurface.getCityGMLModule().getModuleDependencies().getModule(CityGMLModuleType.CORE);

								ExternalReference externalReference = cityGMLFactory.createExternalReference(core);
								externalReference.setInformationSystem(infoSys);

								ExternalObject externalObject = cityGMLFactory.createExternalObject(core);
								externalObject.setName(waterBoundarySurface.getId());

								externalReference.setExternalObject(externalObject);
								waterBoundarySurface.addExternalReference(externalReference);
							}

							waterBoundarySurface.setId(newGmlId);
						}
					}
				}

				String gmlName = rs.getString("WS_NAME");
				String gmlNameCodespace = rs.getString("WS_NAME_CODESPACE");

				Util.dbGmlName2featureName(waterBoundarySurface, gmlName, gmlNameCodespace, gmlNameDelimiter);

				String description = rs.getString("WS_DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);

					waterBoundarySurface.setDescription(stringOrRef);
				}

				if (waterBoundarySurface.getCityGMLClass() == CityGMLClass.WATERSURFACE) {
					String waterLevel = rs.getString("WATER_LEVEL");
					if (waterLevel != null)
						((WaterSurface)waterBoundarySurface).setWaterLevel(waterLevel);
				}

				for (int lod = 2; lod < 5 ; lod++) {
					long geometryId = rs.getLong("LOD" + lod + "_SURFACE_ID");

					if (!rs.wasNull() && geometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

						if (geometry != null) {
							SurfaceProperty surfaceProperty = new SurfacePropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								surfaceProperty.setSurface((AbstractSurface)geometry.getAbstractGeometry());
							else
								surfaceProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 2:
								waterBoundarySurface.setLod2Surface(surfaceProperty);
								break;
							case 3:
								waterBoundarySurface.setLod3Surface(surfaceProperty);
								break;
							case 4:
								waterBoundarySurface.setLod4Surface(surfaceProperty);
								break;
							}
						}
					}
				}

				BoundedByWaterSurfaceProperty boundedByProperty = cityGMLFactory.createBoundedByWaterSurfaceProperty(wtr);
				boundedByProperty.setObject(waterBoundarySurface);
				waterBody.addBoundedBySurface(boundedByProperty);
			}

			if (waterBody.isSetId() && !featureClassFilter.filter(CityGMLClass.CITYOBJECTGROUP))
				dbExporterManager.putGmlId(waterBody.getId(), waterBodyId, waterBody.getCityGMLClass());
			dbExporterManager.print(waterBody);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psWaterBody.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.WATERBODY;
	}

}
