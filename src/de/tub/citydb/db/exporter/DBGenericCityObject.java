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
import de.tub.citydb.config.Config;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.implementation.gml._3_1_1.GeometryPropertyImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.StringOrRefImpl;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.core.CoreModule;
import de.tub.citygml4j.model.citygml.core.ImplicitGeometry;
import de.tub.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import de.tub.citygml4j.model.citygml.generics.GenericCityObject;
import de.tub.citygml4j.model.citygml.generics.GenericsModule;
import de.tub.citygml4j.model.gml.GeometryProperty;
import de.tub.citygml4j.model.gml.MultiCurveProperty;
import de.tub.citygml4j.model.gml.StringOrRef;

public class DBGenericCityObject implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psGenericCityObject;
	private ResultSet rs;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private DBSdoGeometry sdoGeometry;
	private FeatureClassFilter featureClassFilter;

	private String gmlNameDelimiter;
	private GenericsModule genFactory;

	public DBGenericCityObject(Connection connection, CityGMLFactory cityGMLFactory, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		genFactory = config.getProject().getExporter().getModuleVersion().getGenerics().getModule();

		psGenericCityObject = connection.prepareStatement("select * from GENERIC_CITYOBJECT where ID = ?");

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);
	}
	
	public boolean read(DBSplittingResult splitter) throws SQLException, JAXBException {
		GenericCityObject genericCityObject = cityGMLFactory.createGenericCityObject(genFactory);
		long genericCityObjectId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(genericCityObject, genericCityObjectId, true);
		if (!success)
			return false;

		CoreModule factory = genericCityObject.getCityGMLModule().getCoreDependency();
		
		psGenericCityObject.setLong(1, genericCityObjectId);
		rs = psGenericCityObject.executeQuery();

		if (rs.next()) {
			String gmlName = rs.getString("NAME");
			String gmlNameCodespace = rs.getString("NAME_CODESPACE");

			Util.dbGmlName2featureName(genericCityObject, gmlName, gmlNameCodespace, gmlNameDelimiter);

			String description = rs.getString("DESCRIPTION");
			if (description != null) {
				StringOrRef stringOrRef = new StringOrRefImpl();
				stringOrRef.setValue(description);
				genericCityObject.setDescription(stringOrRef);
			}

			String clazz = rs.getString("CLASS");
			if (clazz != null) {
				genericCityObject.setClazz(clazz);
			}

			String function = rs.getString("FUNCTION");
			if (function != null) {
				Pattern p = Pattern.compile("\\s+");
				String[] functionList = p.split(function.trim());
				genericCityObject.setFunction(Arrays.asList(functionList));
			}

			String usage = rs.getString("USAGE");
			if (usage != null) {
				Pattern p = Pattern.compile("\\s+");
				String[] usageList = p.split(usage.trim());
				genericCityObject.setUsage(Arrays.asList(usageList));
			}
			
			for (int lod = 0; lod < 5 ; lod++) {
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
						case 0:
							genericCityObject.setLod0Geometry(geometryProperty);
							break;
						case 1:
							genericCityObject.setLod1Geometry(geometryProperty);
							break;
						case 2:
							genericCityObject.setLod2Geometry(geometryProperty);
							break;
						case 3:
							genericCityObject.setLod3Geometry(geometryProperty);
							break;
						case 4:
							genericCityObject.setLod4Geometry(geometryProperty);
							break;
						}
					}
				}
			}

			for (int lod = 0; lod < 5 ; lod++) {
				// get implicit geometry details
				long implicitGeometryId = rs.getLong("LOD" + lod + "_IMPLICIT_REP_ID");
				if (rs.wasNull())
					continue;

				JGeometry referencePoint = null;
				STRUCT struct = (STRUCT)rs.getObject("LOD" + lod +"_IMPLICIT_REF_POINT");
				if (!rs.wasNull() && struct != null)
					referencePoint = JGeometry.load(struct);

				String transformationMatrix = rs.getString("LOD" + lod + "_IMPLICIT_TRANSFORMATION");

				ImplicitGeometry implicit = implicitGeometryExporter.read(implicitGeometryId, referencePoint, transformationMatrix, factory);
				if (implicit != null) {
					ImplicitRepresentationProperty implicitProperty = cityGMLFactory.createImplicitRepresentationProperty(factory);
					implicitProperty.setObject(implicit);

					switch (lod) {
					case 0:
						genericCityObject.setLod0ImplicitRepresentation(implicitProperty);
						break;
					case 1:
						genericCityObject.setLod1ImplicitRepresentation(implicitProperty);
						break;
					case 2:
						genericCityObject.setLod2ImplicitRepresentation(implicitProperty);
						break;
					case 3:
						genericCityObject.setLod3ImplicitRepresentation(implicitProperty);
						break;
					case 4:
						genericCityObject.setLod4ImplicitRepresentation(implicitProperty);
						break;
					}
				}
			}
			
			// lodXTerrainIntersection
			for (int lod = 0; lod < 5; lod++) {
				JGeometry terrainIntersection = null;
				STRUCT terrainIntersectionObj = (STRUCT)rs.getObject("LOD" + lod + "_TERRAIN_INTERSECTION");
				
				if (!rs.wasNull() && terrainIntersectionObj != null) {
					terrainIntersection = JGeometry.load(terrainIntersectionObj);
					
					if (terrainIntersection != null) {
						MultiCurveProperty multiCurveProperty = sdoGeometry.getMultiCurveProperty(terrainIntersection, false);
						if (multiCurveProperty != null) {
							switch (lod) {
							case 0:
								genericCityObject.setLod0TerainIntersection(multiCurveProperty);
								break;
							case 1:
								genericCityObject.setLod1TerainIntersection(multiCurveProperty);
								break;
							case 2:
								genericCityObject.setLod2TerainIntersection(multiCurveProperty);
								break;
							case 3:
								genericCityObject.setLod3TerainIntersection(multiCurveProperty);
								break;
							case 4:
								genericCityObject.setLod4TerainIntersection(multiCurveProperty);
								break;
							}
						}
					}
				}			
			}
		}

		if (genericCityObject.getId() != null && !featureClassFilter.filter(CityGMLClass.CITYOBJECTGROUP))
			dbExporterManager.putGmlId(genericCityObject.getId(), genericCityObjectId, genericCityObject.getCityGMLClass());
		dbExporterManager.print(genericCityObject);
		return true;
	}
	
	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.GENERIC_CITYOBJECT;
	}

}
