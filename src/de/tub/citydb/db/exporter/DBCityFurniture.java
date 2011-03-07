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
import org.citygml4j.impl.jaxb.gml._3_1_1.GeometryPropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.StringOrRefImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.cityfurniture.CityFurnitureModule;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.GeometryProperty;
import org.citygml4j.model.gml.MultiCurveProperty;
import org.citygml4j.model.gml.StringOrRef;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBCityFurniture implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psCityFurniture;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private DBSdoGeometry sdoGeometry;
	private FeatureClassFilter featureClassFilter;

	private String gmlNameDelimiter;
	private CityFurnitureModule frn;
	private CoreModule core;
	private boolean transformCoords;

	public DBCityFurniture(Connection connection, CityGMLFactory cityGMLFactory, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		frn = config.getProject().getExporter().getModuleVersion().getCityFurniture().getModule();
		core = (CoreModule)frn.getModuleDependencies().getModule(CityGMLModuleType.CORE);
		transformCoords = config.getInternal().isTransformCoordinates();
		
		if (!transformCoords) {
			psCityFurniture = connection.prepareStatement("select * from CITY_FURNITURE where ID = ?");		
		} else {
			ReferenceSystem targetSRS = config.getInternal().getExportTargetSRS();
			int srid = targetSRS.getSrid();
			
			psCityFurniture = connection.prepareStatement("select NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, " +
					"geodb_util.transform_or_null(LOD1_TERRAIN_INTERSECTION, " + srid + ") AS LOD1_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(LOD2_TERRAIN_INTERSECTION, " + srid + ") AS LOD2_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(LOD3_TERRAIN_INTERSECTION, " + srid + ") AS LOD3_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(LOD4_TERRAIN_INTERSECTION, " + srid + ") AS LOD4_TERRAIN_INTERSECTION, " +
					"LOD1_GEOMETRY_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, LOD4_GEOMETRY_ID, " +
					"LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID," +
					"geodb_util.transform_or_null(LOD1_IMPLICIT_REF_POINT, " + srid + ") AS LOD1_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD2_IMPLICIT_REF_POINT, " + srid + ") AS LOD2_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD3_IMPLICIT_REF_POINT, " + srid + ") AS LOD3_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD4_IMPLICIT_REF_POINT, " + srid + ") AS LOD4_IMPLICIT_REF_POINT, " +
			"LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION from CITY_FURNITURE where ID = ?");					
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, JAXBException {
		CityFurniture cityFurniture = cityGMLFactory.createCityFurniture(frn);
		long cityFurnitureId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(cityFurniture, cityFurnitureId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psCityFurniture.setLong(1, cityFurnitureId);
			rs = psCityFurniture.executeQuery();

			if (rs.next()) {
				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(cityFurniture, gmlName, gmlNameCodespace, gmlNameDelimiter);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					cityFurniture.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					cityFurniture.setClazz(clazz);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());
					cityFurniture.setFunction(Arrays.asList(functionList));
				}

				for (int lod = 1; lod < 5 ; lod++) {
					long geometryId = rs.getLong("LOD" + lod + "_GEOMETRY_ID");

					if (!rs.wasNull() && geometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

						if (geometry != null) {
							GeometryProperty geometryProperty = new GeometryPropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								geometryProperty.setGeometry(geometry.getAbstractGeometry());
							else
								geometryProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 1:
								cityFurniture.setLod1Geometry(geometryProperty);
								break;
							case 2:
								cityFurniture.setLod2Geometry(geometryProperty);
								break;
							case 3:
								cityFurniture.setLod3Geometry(geometryProperty);
								break;
							case 4:
								cityFurniture.setLod4Geometry(geometryProperty);
								break;
							}
						}
					}
				}

				for (int lod = 1; lod < 5 ; lod++) {
					// get implicit geometry details
					long implicitGeometryId = rs.getLong("LOD" + lod + "_IMPLICIT_REP_ID");
					if (rs.wasNull())
						continue;

					JGeometry referencePoint = null;
					STRUCT struct = (STRUCT)rs.getObject("LOD" + lod +"_IMPLICIT_REF_POINT");
					if (!rs.wasNull() && struct != null)
						referencePoint = JGeometry.load(struct);

					String transformationMatrix = rs.getString("LOD" + lod + "_IMPLICIT_TRANSFORMATION");

					ImplicitGeometry implicit = implicitGeometryExporter.read(implicitGeometryId, referencePoint, transformationMatrix, core);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = cityGMLFactory.createImplicitRepresentationProperty(core);
						implicitProperty.setObject(implicit);

						switch (lod) {
						case 1:
							cityFurniture.setLod1ImplicitRepresentation(implicitProperty);
							break;
						case 2:
							cityFurniture.setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							cityFurniture.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 4:
							cityFurniture.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}

				// lodXTerrainIntersection
				for (int lod = 1; lod < 5; lod++) {
					JGeometry terrainIntersection = null;
					STRUCT terrainIntersectionObj = (STRUCT)rs.getObject("LOD" + lod + "_TERRAIN_INTERSECTION");

					if (!rs.wasNull() && terrainIntersectionObj != null) {
						terrainIntersection = JGeometry.load(terrainIntersectionObj);

						if (terrainIntersection != null) {
							MultiCurveProperty multiCurveProperty = sdoGeometry.getMultiCurveProperty(terrainIntersection, false);
							if (multiCurveProperty != null) {
								switch (lod) {
								case 1:
									cityFurniture.setLod1TerrainIntersection(multiCurveProperty);
									break;
								case 2:
									cityFurniture.setLod2TerrainIntersection(multiCurveProperty);
									break;
								case 3:
									cityFurniture.setLod3TerrainIntersection(multiCurveProperty);
									break;
								case 4:
									cityFurniture.setLod4TerrainIntersection(multiCurveProperty);
									break;
								}
							}
						}
					}			
				}
			}

			if (cityFurniture.isSetId() && !featureClassFilter.filter(CityGMLClass.CITYOBJECTGROUP))
				dbExporterManager.putGmlId(cityFurniture.getId(), cityFurnitureId, cityFurniture.getCityGMLClass());
			dbExporterManager.print(cityFurniture);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psCityFurniture.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.CITY_FURNITURE;
	}

}
