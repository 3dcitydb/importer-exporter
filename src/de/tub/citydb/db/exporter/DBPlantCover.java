package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import de.tub.citydb.config.Config;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.implementation.gml._3_1_1.LengthImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.MultiSolidPropertyImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.MultiSurfacePropertyImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.StringOrRefImpl;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.vegetation.PlantCover;
import de.tub.citygml4j.model.citygml.vegetation.VegetationModule;
import de.tub.citygml4j.model.gml.GMLClass;
import de.tub.citygml4j.model.gml.Length;
import de.tub.citygml4j.model.gml.MultiSolid;
import de.tub.citygml4j.model.gml.MultiSolidProperty;
import de.tub.citygml4j.model.gml.MultiSurface;
import de.tub.citygml4j.model.gml.MultiSurfaceProperty;
import de.tub.citygml4j.model.gml.StringOrRef;

public class DBPlantCover implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psPlantCover;
	private ResultSet rs;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private FeatureClassFilter featureClassFilter;

	private String gmlNameDelimiter;
	private VegetationModule vegFactory;

	public DBPlantCover(Connection connection, CityGMLFactory cityGMLFactory, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		vegFactory = config.getProject().getExporter().getModuleVersion().getVegetation().getModule();

		psPlantCover = connection.prepareStatement("select * from PLANT_COVER where ID = ?");

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, JAXBException {
		PlantCover plantCover = cityGMLFactory.createPlantCover(vegFactory);
		long plantCoverId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(plantCover, plantCoverId, true);
		if (!success)
			return false;

		psPlantCover.setLong(1, plantCoverId);
		rs = psPlantCover.executeQuery();

		if (rs.next()) {
			String gmlName = rs.getString("NAME");
			String gmlNameCodespace = rs.getString("NAME_CODESPACE");

			Util.dbGmlName2featureName(plantCover, gmlName, gmlNameCodespace, gmlNameDelimiter);

			String description = rs.getString("DESCRIPTION");
			if (description != null) {
				StringOrRef stringOrRef = new StringOrRefImpl();
				stringOrRef.setValue(description);
				plantCover.setDescription(stringOrRef);
			}

			String clazz = rs.getString("CLASS");
			if (clazz != null) {
				plantCover.setClazz(clazz);
			}

			String function = rs.getString("FUNCTION");
			if (function != null) {
				Pattern p = Pattern.compile("\\s+");
				String[] functionList = p.split(function.trim());
				plantCover.setFunction(Arrays.asList(functionList));
			}

			double averageHeight = rs.getDouble("AVERAGE_HEIGHT");
			if (!rs.wasNull()) {
				Length length = new LengthImpl();
				length.setValue(averageHeight);
				length.setUom("urn:ogc:def:uom:UCUM::m");
				plantCover.setAverageHeight(length);
			}

			for (int lod = 1; lod < 5 ; lod++) {
				long geometryId = rs.getLong("LOD" + lod + "_GEOMETRY_ID");

				if (!rs.wasNull() && geometryId != 0) {
					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

					if (geometry != null) {
						if (geometry.getType() == GMLClass.MULTISURFACE) {
							MultiSurfaceProperty multiSurfaceProperty = new MultiSurfacePropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
							else
								multiSurfaceProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 1:
								plantCover.setLod1MultiSurface(multiSurfaceProperty);
								break;
							case 2:
								plantCover.setLod2MultiSurface(multiSurfaceProperty);
								break;
							case 3:
								plantCover.setLod3MultiSurface(multiSurfaceProperty);
								break;
							case 4:
								plantCover.setLod4MultiSurface(multiSurfaceProperty);
								break;
							}
						}

						else if (geometry.getType() == GMLClass.MULTISOLID) {
							MultiSolidProperty multiSolidProperty = new MultiSolidPropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								multiSolidProperty.setMultiSolid((MultiSolid)geometry.getAbstractGeometry());
							else
								multiSolidProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 1:
								plantCover.setLod1MultiSolid(multiSolidProperty);
								break;
							case 2:
								plantCover.setLod2MultiSolid(multiSolidProperty);
								break;
							case 3:
								plantCover.setLod3MultiSolid(multiSolidProperty);
								break;
							}
						}
					}
				}
			}
		}

		if (plantCover.getId() != null && !featureClassFilter.filter(CityGMLClass.CITYOBJECTGROUP))
			dbExporterManager.putGmlId(plantCover.getId(), plantCoverId, plantCover.getCityGMLClass());
		dbExporterManager.print(plantCover);
		return true;
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.PLANT_COVER;
	}

}
