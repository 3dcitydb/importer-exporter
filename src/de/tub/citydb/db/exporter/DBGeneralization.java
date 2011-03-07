package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.geometry.Point;
import org.citygml4j.impl.jaxb.gml._3_1_1.EnvelopeImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.CityObject;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.core.GeneralizationRelation;
import org.citygml4j.model.gml.Envelope;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.BoundingBoxFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.filter.feature.GmlIdFilter;
import de.tub.citydb.filter.feature.GmlNameFilter;
import de.tub.citydb.util.Util;

public class DBGeneralization implements DBExporter {
	private final CityGMLFactory cityGMLFactory;
	private final ExportFilter exportFilter;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psGeneralization;

	// filter
	private FeatureClassFilter featureClassFilter;
	private GmlIdFilter featureGmlIdFilter;
	private GmlNameFilter featureGmlNameFilter;
	private BoundingBoxFilter boundingBoxFilter;

	private boolean transformCoords;

	public DBGeneralization(Connection connection, CityGMLFactory cityGMLFactory, ExportFilter exportFilter, Config config) throws SQLException {
		this.cityGMLFactory = cityGMLFactory;
		this.exportFilter = exportFilter;
		this.config = config;
		this.connection = connection;

		init();
	}

	private void init() throws SQLException {
		featureClassFilter = exportFilter.getFeatureClassFilter();
		featureGmlIdFilter = exportFilter.getGmlIdFilter();
		featureGmlNameFilter = exportFilter.getGmlNameFilter();
		boundingBoxFilter = exportFilter.getBoundingBoxFilter();

		transformCoords = config.getInternal().isTransformCoordinates();
		if (!transformCoords) {	
			psGeneralization = connection.prepareStatement("select GMLID, CLASS_ID, ENVELOPE from CITYOBJECT where ID=?");
		} else {
			ReferenceSystem targetSRS = config.getInternal().getExportTargetSRS();
			
			psGeneralization = connection.prepareStatement("select GMLID, CLASS_ID, " +
					"geodb_util.transform_or_null(co.ENVELOPE, " + targetSRS.getSrid() + ") AS ENVELOPE " +
			"from CITYOBJECT where ID=?");
		}
	}

	public void read(CityObject cityObject, long cityObjectId, CoreModule core, HashSet<Long> generalizesToSet) throws SQLException {		
		for (Long generalizationId : generalizesToSet) {
			ResultSet rs = null;

			try {
				psGeneralization.setLong(1, generalizationId);
				rs = psGeneralization.executeQuery();

				if (rs.next()) {
					String gmlId = rs.getString("GMLID");			
					if (rs.wasNull() || gmlId == null)
						continue;

					int classId = rs.getInt("CLASS_ID");			
					CityGMLClass type = Util.classId2cityObject(classId);			
					STRUCT struct = (STRUCT)rs.getObject("ENVELOPE");

					if (!rs.wasNull() && struct != null && boundingBoxFilter.isActive()) {
						JGeometry jGeom = JGeometry.load(struct);
						Envelope env = new EnvelopeImpl();

						double[] points = jGeom.getOrdinatesArray();
						Point lower = new Point(points[0], points[1], points[2]);
						Point upper = new Point(points[3], points[4], points[5]);

						env.setLowerCorner(lower);
						env.setUpperCorner(upper);

						if (boundingBoxFilter.filter(env))
							continue;
					}	

					if (featureGmlIdFilter.isActive() && featureGmlIdFilter.filter(gmlId))
						continue;

					if (featureClassFilter.isActive() && featureClassFilter.filter(type))
						continue;

					if (featureGmlNameFilter.isActive()) {
						// we need to get the gml:name of the feature 
						// we only check top-level features
						DBTableEnum table = null;

						switch (type) {
						case BUILDING:
							table = DBTableEnum.BUILDING;
							break;
						case CITYFURNITURE:
							table = DBTableEnum.CITY_FURNITURE;
							break;
						case LANDUSE:
							table = DBTableEnum.LAND_USE;
							break;
						case WATERBODY:
							table = DBTableEnum.WATERBODY;
							break;
						case PLANTCOVER:
							table = DBTableEnum.SOLITARY_VEGETAT_OBJECT;
							break;
						case SOLITARYVEGETATIONOBJECT:
							table = DBTableEnum.PLANT_COVER;
							break;
						case TRANSPORTATIONCOMPLEX:
						case ROAD:
						case RAILWAY:
						case TRACK:
						case SQUARE:
							table = DBTableEnum.TRANSPORTATION_COMPLEX;
							break;
						case RELIEFFEATURE:
							table = DBTableEnum.RELIEF_FEATURE;
							break;
						case GENERICCITYOBJECT:
							table = DBTableEnum.GENERIC_CITYOBJECT;
							break;
						case CITYOBJECTGROUP:
							table = DBTableEnum.CITYOBJECTGROUP;
							break;
						}

						if (table != null) {
							Statement stmt = null;
							ResultSet nameRs = null;

							try {
								String query = "select NAME from " + table.toString() + " where ID=" + generalizationId;
								stmt = connection.createStatement();

								nameRs = stmt.executeQuery(query);
								if (nameRs.next()) {
									String gmlName = nameRs.getString("NAME");
									if (gmlName != null && featureGmlNameFilter.filter(gmlName))
										continue;
								}

							} catch (SQLException sqlEx) {
								continue;
							} finally {
								if (nameRs != null) {
									try {
										nameRs.close();
									} catch (SQLException sqlEx) {
										//
									}

									nameRs = null;
								}

								if (stmt != null) {
									try {
										stmt.close();
									} catch (SQLException sqlEx) {
										//
									}

									stmt = null;
								}
							}
						}
					}

					GeneralizationRelation generalizesTo = cityGMLFactory.createGeneralizationRelation(core);
					generalizesTo.setHref("#" + gmlId);
					cityObject.addGeneralizesTo(generalizesTo);
				}
			} finally {
				if (rs != null)
					rs.close();
			}
		}
	}

	@Override
	public void close() throws SQLException {
		psGeneralization.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.GENERALIZATION;
	}

}
