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
import org.citygml4j.impl.jaxb.gml._3_1_1.LengthImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.StringOrRefImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.vegetation.VegetationModule;
import org.citygml4j.model.gml.GeometryProperty;
import org.citygml4j.model.gml.Length;
import org.citygml4j.model.gml.StringOrRef;

import de.tub.citydb.config.Config;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBSolitaryVegetatObject implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psSolVegObject;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private FeatureClassFilter featureClassFilter;

	private VegetationModule veg;
	private CoreModule core;
	private boolean transformCoords;

	public DBSolitaryVegetatObject(Connection connection, CityGMLFactory cityGMLFactory, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		veg = config.getProject().getExporter().getModuleVersion().getVegetation().getModule();
		core = (CoreModule)veg.getModuleDependencies().getModule(CityGMLModuleType.CORE);
		transformCoords = config.getInternal().isTransformCoordinates();

		if (!transformCoords) {
			psSolVegObject = connection.prepareStatement("select * from SOLITARY_VEGETAT_OBJECT where ID = ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			
			psSolVegObject = connection.prepareStatement("select NAME, NAME_CODESPACE, DESCRIPTION, CLASS, SPECIES, FUNCTION, HEIGHT, TRUNC_DIAMETER, CROWN_DIAMETER," +
					"LOD1_GEOMETRY_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, LOD4_GEOMETRY_ID, " +
					"LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID," +
					"geodb_util.transform_or_null(LOD1_IMPLICIT_REF_POINT, " + srid + ") AS LOD1_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD2_IMPLICIT_REF_POINT, " + srid + ") AS LOD2_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD3_IMPLICIT_REF_POINT, " + srid + ") AS LOD3_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD4_IMPLICIT_REF_POINT, " + srid + ") AS LOD4_IMPLICIT_REF_POINT, " +
			"LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION from SOLITARY_VEGETAT_OBJECT where ID = ?");					
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, JAXBException {
		SolitaryVegetationObject solVegObject = cityGMLFactory.createSolitaryVegetationObject(veg);
		long solVegObjectId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(solVegObject, solVegObjectId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psSolVegObject.setLong(1, solVegObjectId);
			rs = psSolVegObject.executeQuery();

			if (rs.next()) {
				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(solVegObject, gmlName, gmlNameCodespace);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					solVegObject.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					solVegObject.setClazz(clazz);
				}

				String species = rs.getString("SPECIES");
				if (species != null) {
					solVegObject.setSpecies(species);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());
					solVegObject.setFunction(Arrays.asList(functionList));
				}

				double height = rs.getDouble("HEIGHT");
				if (!rs.wasNull()) {
					Length length = new LengthImpl();
					length.setValue(height);
					length.setUom("urn:ogc:def:uom:UCUM::m");
					solVegObject.setHeight(length);
				}

				double truncDiameter = rs.getDouble("TRUNC_DIAMETER");
				if (!rs.wasNull()) {
					Length length = new LengthImpl();
					length.setValue(truncDiameter);
					length.setUom("urn:ogc:def:uom:UCUM::m");
					solVegObject.setTrunkDiameter(length);
				}

				double crownDiameter = rs.getDouble("CROWN_DIAMETER");
				if (!rs.wasNull()) {
					Length length = new LengthImpl();
					length.setValue(crownDiameter);
					length.setUom("urn:ogc:def:uom:UCUM::m");
					solVegObject.setCrownDiameter(length);
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
								solVegObject.setLod1Geometry(geometryProperty);
								break;
							case 2:
								solVegObject.setLod2Geometry(geometryProperty);
								break;
							case 3:
								solVegObject.setLod3Geometry(geometryProperty);
								break;
							case 4:
								solVegObject.setLod4Geometry(geometryProperty);
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
							solVegObject.setLod1ImplicitRepresentation(implicitProperty);
							break;
						case 2:
							solVegObject.setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							solVegObject.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 4:
							solVegObject.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}
			}

			if (solVegObject.isSetId() && !featureClassFilter.filter(CityGMLClass.CITYOBJECTGROUP))
				dbExporterManager.putGmlId(solVegObject.getId(), solVegObjectId, solVegObject.getCityGMLClass());
			dbExporterManager.print(solVegObject);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psSolVegObject.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.SOLITARY_VEGETAT_OBJECT;
	}

}
