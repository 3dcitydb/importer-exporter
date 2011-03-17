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
import org.citygml4j.impl.jaxb.gml._3_1_1.StringOrRefImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.transportation.TransportationModule;
import org.citygml4j.model.citygml.transportation.TransportationObject;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.GeometricComplexProperty;
import org.citygml4j.model.gml.MultiSurface;
import org.citygml4j.model.gml.MultiSurfaceProperty;
import org.citygml4j.model.gml.StringOrRef;

import de.tub.citydb.config.Config;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBTransportationComplex implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psTranComplex;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBSdoGeometry sdoGeometry;
	private FeatureClassFilter featureClassFilter;

	private TransportationModule tran;
	private boolean transformCoords;

	public DBTransportationComplex(Connection connection, CityGMLFactory cityGMLFactory, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		tran = config.getProject().getExporter().getModuleVersion().getTransportation().getModule();
		transformCoords = config.getInternal().isTransformCoordinates();

		if (!transformCoords) {		
			psTranComplex = connection.prepareStatement("select tc.ID as TC_ID, tc.NAME as TC_NAME, tc.NAME_CODESPACE as TC_NAME_CODESPACE, tc.DESCRIPTION as TC_DESCRIPTION, tc.FUNCTION as TC_FUNCTION, tc.USAGE as TC_USAGE, " +
					"upper(tc.TYPE) as TC_TYPE, tc.LOD1_MULTI_SURFACE_ID as TC_LOD1_MULTI_SURFACE_ID, tc.LOD2_MULTI_SURFACE_ID as TC_LOD2_MULTI_SURFACE_ID, tc.LOD3_MULTI_SURFACE_ID as TC_LOD3_MULTI_SURFACE_ID, " +
					"tc.LOD4_MULTI_SURFACE_ID as TC_LOD4_MULTI_SURFACE_ID, tc.LOD0_NETWORK as TC_LOD0_NETWORK, " +
					"ta.ID as TA_ID, ta.IS_AUXILIARY, ta.NAME as TA_NAME, ta.NAME_CODESPACE as TA_NAME_CODESPACE, ta.DESCRIPTION as TA_DESCRIPTION, ta.FUNCTION as TA_FUNCTION, ta.USAGE as TA_USAGE, " +
					"ta.SURFACE_MATERIAL, ta.LOD2_MULTI_SURFACE_ID as TA_LOD2_MULTI_SURFACE_ID, ta.LOD3_MULTI_SURFACE_ID as TA_LOD3_MULTI_SURFACE_ID, " +
			"ta.LOD4_MULTI_SURFACE_ID as TA_LOD4_MULTI_SURFACE_ID from TRANSPORTATION_COMPLEX tc left join TRAFFIC_AREA ta on tc.ID=ta.TRANSPORTATION_COMPLEX_ID where tc.ID=?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			
			psTranComplex = connection.prepareStatement("select tc.ID as TC_ID, tc.NAME as TC_NAME, tc.NAME_CODESPACE as TC_NAME_CODESPACE, tc.DESCRIPTION as TC_DESCRIPTION, tc.FUNCTION as TC_FUNCTION, tc.USAGE as TC_USAGE, " +
					"upper(tc.TYPE) as TC_TYPE, tc.LOD1_MULTI_SURFACE_ID as TC_LOD1_MULTI_SURFACE_ID, tc.LOD2_MULTI_SURFACE_ID as TC_LOD2_MULTI_SURFACE_ID, tc.LOD3_MULTI_SURFACE_ID as TC_LOD3_MULTI_SURFACE_ID, " +
					"tc.LOD4_MULTI_SURFACE_ID as TC_LOD4_MULTI_SURFACE_ID, " +
					"geodb_util.transform_or_null(tc.LOD0_NETWORK, " + srid + ") as TC_LOD0_NETWORK, " +
					"ta.ID as TA_ID, ta.IS_AUXILIARY, ta.NAME as TA_NAME, ta.NAME_CODESPACE as TA_NAME_CODESPACE, ta.DESCRIPTION as TA_DESCRIPTION, ta.FUNCTION as TA_FUNCTION, ta.USAGE as TA_USAGE, " +
					"ta.SURFACE_MATERIAL, ta.LOD2_MULTI_SURFACE_ID as TA_LOD2_MULTI_SURFACE_ID, ta.LOD3_MULTI_SURFACE_ID as TA_LOD3_MULTI_SURFACE_ID, " +
			"ta.LOD4_MULTI_SURFACE_ID as TA_LOD4_MULTI_SURFACE_ID from TRANSPORTATION_COMPLEX tc left join TRAFFIC_AREA ta on tc.ID=ta.TRANSPORTATION_COMPLEX_ID where tc.ID=?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, JAXBException {
		TransportationComplex transComplex = null;
		long transComplexId = splitter.getPrimaryKey();

		switch (splitter.getCityObjectType()) {
		case ROAD:
			transComplex = cityGMLFactory.createRoad(tran);
			break;
		case RAILWAY:
			transComplex = cityGMLFactory.createRailway(tran);
			break;
		case SQUARE:
			transComplex = cityGMLFactory.createSquare(tran);
			break;
		case TRACK:
			transComplex = cityGMLFactory.createTrack(tran);
			break;
		default:
			transComplex = cityGMLFactory.createTransportationComplex(tran);
		}

		// cityObject stuff
		boolean success = cityObjectExporter.read(transComplex, transComplexId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psTranComplex.setLong(1, transComplexId);
			rs = psTranComplex.executeQuery();

			boolean isInited = false;

			while (rs.next()) {
				if (!isInited) {
					String gmlName = rs.getString("TC_NAME");
					String gmlNameCodespace = rs.getString("TC_NAME_CODESPACE");

					Util.dbGmlName2featureName(transComplex, gmlName, gmlNameCodespace);

					String description = rs.getString("TC_DESCRIPTION");
					if (description != null) {
						StringOrRef stringOrRef = new StringOrRefImpl();
						stringOrRef.setValue(description);
						transComplex.setDescription(stringOrRef);
					}

					String function = rs.getString("TC_FUNCTION");
					if (function != null) {
						Pattern p = Pattern.compile("\\s+");
						String[] functionList = p.split(function.trim());
						transComplex.setFunction(Arrays.asList(functionList));
					}

					String usage = rs.getString("TC_USAGE");
					if (usage != null) {
						Pattern p = Pattern.compile("\\s+");
						String[] usageList = p.split(usage.trim());
						transComplex.setUsage(Arrays.asList(usageList));
					}

					for (int lod = 1; lod < 5 ; lod++) {
						long multiSurfaceId = rs.getLong("TC_LOD" + lod + "_MULTI_SURFACE_ID");

						if (!rs.wasNull() && multiSurfaceId != 0) {
							DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(multiSurfaceId);

							if (geometry != null && geometry.getType() == GMLClass.MULTISURFACE) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfacePropertyImpl();

								if (geometry.getAbstractGeometry() != null)
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getTarget());

								switch (lod) {
								case 1:
									transComplex.setLod1MultiSurface(multiSurfaceProperty);
									break;
								case 2:
									transComplex.setLod2MultiSurface(multiSurfaceProperty);
									break;
								case 3:
									transComplex.setLod3MultiSurface(multiSurfaceProperty);
									break;
								case 4:
									transComplex.setLod4MultiSurface(multiSurfaceProperty);
									break;
								}
							}
						}
					}

					// lod0Network
					STRUCT struct = (STRUCT)rs.getObject("TC_LOD0_NETWORK");
					if (!rs.wasNull() && struct != null) {
						JGeometry lod0Network = JGeometry.load(struct);

						GeometricComplexProperty complexProperty = sdoGeometry.getGeometricComplexPropertyOfCurves(lod0Network, false);
						transComplex.addLod0Network(complexProperty);
					}

					isInited = true;
				}

				long trafficAreaId = rs.getLong("TA_ID");
				if (rs.wasNull())
					continue;

				TransportationObject transObject = null;
				boolean isAuxiliary = rs.getBoolean("IS_AUXILIARY");

				if (isAuxiliary)
					transObject = cityGMLFactory.createAuxiliaryTrafficArea(tran);
				else
					transObject = cityGMLFactory.createTrafficArea(tran);

				// cityobject stuff
				cityObjectExporter.read(transObject, trafficAreaId);

				String gmlName = rs.getString("TA_NAME");
				String gmlNameCodespace = rs.getString("TA_NAME_CODESPACE");

				Util.dbGmlName2featureName(transObject, gmlName, gmlNameCodespace);

				String description = rs.getString("TA_DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					transObject.setDescription(stringOrRef);
				}

				String function = rs.getString("TA_FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());

					if (isAuxiliary)
						((AuxiliaryTrafficArea)transObject).setFunction(Arrays.asList(functionList));
					else
						((TrafficArea)transObject).setFunction(Arrays.asList(functionList));
				}

				String usage = rs.getString("TA_USAGE");
				if (usage != null && !isAuxiliary) {
					Pattern p = Pattern.compile("\\s+");
					String[] usageList = p.split(usage.trim());
					((TrafficArea)transObject).setUsage(Arrays.asList(usageList));
				}

				String surfaceMaterial = rs.getString("SURFACE_MATERIAL");
				if (surfaceMaterial != null) {
					if (isAuxiliary)
						((AuxiliaryTrafficArea)transObject).setSurfaceMaterial(surfaceMaterial);
					else
						((TrafficArea)transObject).setSurfaceMaterial(surfaceMaterial);
				}

				for (int lod = 2; lod < 5 ; lod++) {
					long multiSurfaceId = rs.getLong("TA_LOD" + lod + "_MULTI_SURFACE_ID");

					if (!rs.wasNull() && multiSurfaceId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(multiSurfaceId);

						if (geometry != null && geometry.getType() == GMLClass.MULTISURFACE) {
							MultiSurfaceProperty multiSurfaceProperty = new MultiSurfacePropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
							else
								multiSurfaceProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 2:
								if (isAuxiliary)
									((AuxiliaryTrafficArea)transObject).setLod2MultiSurface(multiSurfaceProperty);
								else
									((TrafficArea)transObject).setLod2MultiSurface(multiSurfaceProperty);
								break;
							case 3:
								if (isAuxiliary)
									((AuxiliaryTrafficArea)transObject).setLod3MultiSurface(multiSurfaceProperty);
								else
									((TrafficArea)transObject).setLod3MultiSurface(multiSurfaceProperty);
								break;
							case 4:
								if (isAuxiliary)
									((AuxiliaryTrafficArea)transObject).setLod4MultiSurface(multiSurfaceProperty);
								else
									((TrafficArea)transObject).setLod4MultiSurface(multiSurfaceProperty);
								break;
							}
						}
					}
				}

				if (isAuxiliary) {
					AuxiliaryTrafficAreaProperty auxProperty  = cityGMLFactory.createAuxiliaryTrafficAreaProperty(tran);
					auxProperty.setObject((AuxiliaryTrafficArea)transObject);
					transComplex.addAuxiliaryTrafficArea(auxProperty);
				} else {
					TrafficAreaProperty trafficProperty = cityGMLFactory.createTrafficAreaProperty(tran);
					trafficProperty.setObject((TrafficArea)transObject);
					transComplex.addTrafficArea(trafficProperty);
				}
			}

			if (transComplex.isSetId() && !featureClassFilter.filter(CityGMLClass.CITYOBJECTGROUP))
				dbExporterManager.putGmlId(transComplex.getId(), transComplexId, transComplex.getCityGMLClass());
			dbExporterManager.print(transComplex);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psTranComplex.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.TRANSPORTATION_COMPLEX;
	}

}
