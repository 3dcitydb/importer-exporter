package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.geometry.BoundingVolume;
import org.citygml4j.geometry.Point;
import org.citygml4j.impl.jaxb.gml._3_1_1.BoundingShapeImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.EnvelopeImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.core.CityObject;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.generics.GenericAttribute;
import org.citygml4j.model.citygml.generics.GenericDateAttribute;
import org.citygml4j.model.citygml.generics.GenericDoubleAttribute;
import org.citygml4j.model.citygml.generics.GenericIntAttribute;
import org.citygml4j.model.citygml.generics.GenericStringAttribute;
import org.citygml4j.model.citygml.generics.GenericUriAttribute;
import org.citygml4j.model.citygml.generics.GenericsModule;
import org.citygml4j.model.gml.BoundingShape;
import org.citygml4j.model.gml.Envelope;
import org.citygml4j.util.CityGMLModules;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.Tiling;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.BoundingBoxFilter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class DBCityObject implements DBExporter {
	private final Logger LOG = Logger.getInstance();

	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psCityObject;

	private DBAppearance appearanceExporter;
	private DBGeneralization generalizesToExporter;
	private String gmlSrsName;
	private boolean exportAppearance;
	private boolean useInternalBBoxFilter;
	private boolean useTiling;
	private boolean setTileInfoAsGenericAttribute;
	private boolean transformCoords;
	private BoundingBoxFilter boundingBoxFilter;
	private BoundingVolume activeTile;
	private Tiling tiling;
	private DatatypeFactory datatypeFactory;

	private HashSet<Long> generalizesToSet;
	private HashSet<Long> externalReferenceSet;
	private HashSet<Long> genericAttributeSet;

	public DBCityObject(Connection connection, CityGMLFactory cityGMLFactory, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.connection = connection;
		this.boundingBoxFilter = exportFilter.getBoundingBoxFilter();

		init();
	}

	private void init() throws SQLException {
		exportAppearance = config.getProject().getExporter().getAppearances().isSetExportAppearance();
		gmlSrsName = config.getInternal().getOpenConnection().getMetaData().getSrsName();
		useInternalBBoxFilter = config.getInternal().isUseInternalBBoxFilter();

		tiling = config.getProject().getExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling();
		useTiling = boundingBoxFilter.isActive() && tiling.getMode() != TilingMode.NO_TILING;
		setTileInfoAsGenericAttribute = useTiling && tiling.isIncludeTileAsGenericAttribute();
		if (setTileInfoAsGenericAttribute)
			activeTile = boundingBoxFilter.getFilterState();

		generalizesToSet = new HashSet<Long>();
		externalReferenceSet = new HashSet<Long>();
		genericAttributeSet = new HashSet<Long>();

		transformCoords = config.getInternal().isTransformCoordinates();
		if (!transformCoords) {		
			psCityObject = connection.prepareStatement("select co.GMLID, co.ENVELOPE, co.CREATION_DATE, co.TERMINATION_DATE, ex.ID as EXID, ex.INFOSYS, ex.NAME, ex.URI, " +
					"ga.ID as GAID, ga.ATTRNAME, ga.DATATYPE, ga.STRVAL, ga.INTVAL, ga.REALVAL, ga.URIVAL, ga.DATEVAL, ge.GENERALIZES_TO_ID " +
					"from CITYOBJECT co left join EXTERNAL_REFERENCE ex on co.ID = ex.CITYOBJECT_ID " +
					"left join CITYOBJECT_GENERICATTRIB ga on co.ID = ga.CITYOBJECT_ID " +
			"left join GENERALIZATION ge on ge.CITYOBJECT_ID=co.ID where co.ID = ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			
			psCityObject = connection.prepareStatement("select co.GMLID, " +
					"geodb_util.transform_or_null(co.ENVELOPE, " + srid + ") AS ENVELOPE, " +
					"co.CREATION_DATE, co.TERMINATION_DATE, ex.ID as EXID, ex.INFOSYS, ex.NAME, ex.URI, " +
					"ga.ID as GAID, ga.ATTRNAME, ga.DATATYPE, ga.STRVAL, ga.INTVAL, ga.REALVAL, ga.URIVAL, ga.DATEVAL, ge.GENERALIZES_TO_ID " +
					"from CITYOBJECT co left join EXTERNAL_REFERENCE ex on co.ID = ex.CITYOBJECT_ID " +
					"left join CITYOBJECT_GENERICATTRIB ga on co.ID = ga.CITYOBJECT_ID " +
			"left join GENERALIZATION ge on ge.CITYOBJECT_ID=co.ID where co.ID = ?");
		}

		appearanceExporter = (DBAppearance)dbExporterManager.getDBExporter(DBExporterEnum.APPEARANCE);
		generalizesToExporter = (DBGeneralization)dbExporterManager.getDBExporter(DBExporterEnum.GENERALIZATION);

		try {
			datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			//
		}
	}


	public boolean read(CityObject cityObject, long parentId) throws SQLException {
		return read(cityObject, parentId, false);
	}

	public boolean read(CityObject cityObject, long parentId, boolean isTopLevelObject) throws SQLException {
		ResultSet rs = null;

		try {
			psCityObject.setLong(1, parentId);
			rs = psCityObject.executeQuery();

			CoreModule core = (CoreModule)cityObject.getCityGMLModule().getModuleDependencies().getModule(CityGMLModuleType.CORE);
			GenericsModule gen = (GenericsModule)CityGMLModules.getModuleByTypeAndVersion(CityGMLModuleType.GENERICS, core.getModuleVersion());

			if (rs.next()) {
				generalizesToSet.clear();
				externalReferenceSet.clear();
				genericAttributeSet.clear();

				// boundedBy
				STRUCT struct = (STRUCT)rs.getObject("ENVELOPE");
				if (!rs.wasNull() && struct != null) {
					JGeometry jGeom = JGeometry.load(struct);
					int dim = jGeom.getDimensions();
					if (dim == 2 || dim == 3) {
						double[] points = jGeom.getOrdinatesArray();
						Envelope env = new EnvelopeImpl();

						Point lower = null;
						Point upper = null;

						if (dim == 2) {
							lower = new Point(points[0], points[1], 0);
							upper = new Point(points[2], points[3], 0);
						} else {					
							lower = new Point(points[0], points[1], points[2]);
							upper = new Point(points[3], points[4], points[5]);
						}

						env.setLowerCorner(lower);
						env.setUpperCorner(upper);
						env.setSrsDimension(3);
						env.setSrsName(gmlSrsName);

						BoundingShape boundedBy = new BoundingShapeImpl();
						boundedBy.setEnvelope(env);
						cityObject.setBoundedBy(boundedBy);
					}
				}

				// check bounding volume filter
				if (isTopLevelObject && (useInternalBBoxFilter || useTiling)) {
					if (!cityObject.isSetBoundedBy() ||
							!cityObject.getBoundedBy().isSetEnvelope() ||
							boundingBoxFilter.filter(cityObject.getBoundedBy().getEnvelope()))
						return false;
				}

				String gmlId = rs.getString("GMLID");
				if (gmlId != null)
					cityObject.setId(gmlId);

				// creationDate
				Date creationDate = rs.getDate("CREATION_DATE");
				if (creationDate != null) {
					GregorianCalendar gregDate = new GregorianCalendar();
					gregDate.setTime(creationDate);

					if (datatypeFactory != null)
						cityObject.setCreationDate(datatypeFactory.newXMLGregorianCalendarDate(
								gregDate.get(Calendar.YEAR),
								gregDate.get(Calendar.MONTH) + 1,
								gregDate.get(Calendar.DAY_OF_MONTH),
								DatatypeConstants.FIELD_UNDEFINED));
					else
						LOG.error(Util.getFeatureSignature(cityObject.getCityGMLClass(), cityObject.getId()) + 
						": Failed to write attribute 'creationDate' due to an internal error.");
				}

				// terminationDate
				Date terminationDate = rs.getDate("TERMINATION_DATE");
				if (terminationDate != null) {
					GregorianCalendar gregDate = new GregorianCalendar();
					gregDate.setTime(terminationDate);

					if (datatypeFactory != null)
						cityObject.setTerminationDate(datatypeFactory.newXMLGregorianCalendarDate(
								gregDate.get(Calendar.YEAR),
								gregDate.get(Calendar.MONTH) + 1,
								gregDate.get(Calendar.DAY_OF_MONTH),
								DatatypeConstants.FIELD_UNDEFINED));
					else
						LOG.error(Util.getFeatureSignature(cityObject.getCityGMLClass(), cityObject.getId()) + 
						": Failed to write attribute 'terminationDate' due to an internal error.");
				}

				do {
					// generalizesTo
					long generalizesTo = rs.getLong("GENERALIZES_TO_ID");
					if (!rs.wasNull())
						generalizesToSet.add(generalizesTo);

					// externalReference
					long externalReferenceId = rs.getLong("EXID");
					if (!rs.wasNull() && !externalReferenceSet.contains(externalReferenceId)) {
						externalReferenceSet.add(externalReferenceId);

						ExternalReference externalReference = cityGMLFactory.createExternalReference(core);
						ExternalObject externalObject = cityGMLFactory.createExternalObject(core);

						String infoSys = rs.getString("INFOSYS");
						if (infoSys != null)
							externalReference.setInformationSystem(infoSys);

						String name = rs.getString("NAME");
						String uri = rs.getString("URI");

						if (name != null || uri != null) {
							if (name != null)
								externalObject.setName(name);

							if (uri != null)
								externalObject.setUri(uri);
						} else if (name == null && uri == null) {
							externalObject.setUri("");
						}

						externalReference.setExternalObject(externalObject);
						cityObject.addExternalReference(externalReference);
					}

					// generic attributes
					long genericAttribId = rs.getLong("GAID");
					if (!rs.wasNull() && !genericAttributeSet.contains(genericAttribId)) {
						genericAttributeSet.add(genericAttribId);

						GenericAttribute genericAttrib = null;
						String attrName = rs.getString("ATTRNAME");
						int dataType = rs.getInt("DATATYPE");

						switch (dataType) {
						case 1:
							String strVal = rs.getString("STRVAL");
							if (strVal != null) {
								genericAttrib = cityGMLFactory.createGenericStringAttribute(gen);
								((GenericStringAttribute)genericAttrib).setValue(strVal);
							}
							break;
						case 2:
							Integer intVal = rs.getInt("INTVAL");
							if (!rs.wasNull() && intVal != null) {
								genericAttrib = cityGMLFactory.createGenericIntAttribute(gen);
								((GenericIntAttribute)genericAttrib).setValue(intVal);
							}
							break;
						case 3:
							Double realVal = rs.getDouble("REALVAL");
							if (!rs.wasNull() && realVal != null) {
								genericAttrib = cityGMLFactory.createGenericDoubleAttribute(gen);
								((GenericDoubleAttribute)genericAttrib).setValue(realVal);
							}
							break;
						case 4:
							String uriVal = rs.getString("URIVAL");
							if (uriVal != null) {
								genericAttrib = cityGMLFactory.createGenericUriAttribute(gen);
								((GenericUriAttribute)genericAttrib).setValue(uriVal);
							}
							break;
						case 5:
							Date dateVal = rs.getDate("DATEVAL");
							if (dateVal != null) {
								genericAttrib = cityGMLFactory.createGenericDateAttribute(gen);
								GregorianCalendar gregDate = new GregorianCalendar();
								gregDate.setTime(dateVal);

								if (datatypeFactory != null)
									((GenericDateAttribute)genericAttrib).setValue(datatypeFactory.newXMLGregorianCalendarDate(
											gregDate.get(Calendar.YEAR),
											gregDate.get(Calendar.MONTH) + 1,
											gregDate.get(Calendar.DAY_OF_MONTH),
											DatatypeConstants.FIELD_UNDEFINED));
								else
									LOG.error(Util.getFeatureSignature(cityObject.getCityGMLClass(), cityObject.getId()) + 
											": Failed to write generic dateAttribute '" + genericAttrib.getName() + "' due to an internal error.");
							}
							break;
						}

						if (genericAttrib != null) {
							genericAttrib.setName(attrName);
							cityObject.addGenericAttribute(genericAttrib);
						}
					}

				} while (rs.next());

				if (isTopLevelObject && setTileInfoAsGenericAttribute) {
					String value;

					double minX = activeTile.getLowerCorner().getX();
					double minY = activeTile.getLowerCorner().getY();
					double maxX = activeTile.getUpperCorner().getX();
					double maxY = activeTile.getUpperCorner().getY();

					switch (tiling.getGenericAttributeValue()) {
					case XMIN_YMIN:
						value = String.valueOf(minX) + ' ' + String.valueOf(minY);
						break;
					case XMAX_YMIN:
						value = String.valueOf(maxX) + ' ' + String.valueOf(minY);
						break;
					case XMIN_YMAX:
						value = String.valueOf(minX) + ' ' + String.valueOf(maxY);
						break;
					case XMAX_YMAX:
						value = String.valueOf(maxX) + ' ' + String.valueOf(maxY);
						break;
					case XMIN_YMIN_XMAX_YMAX:
						value = String.valueOf(minX) + ' ' + String.valueOf(minY) + ' ' + String.valueOf(maxX) + ' ' + String.valueOf(maxY);
						break;
					default:
						value = String.valueOf(boundingBoxFilter.getTileRow()) + ' ' + String.valueOf(boundingBoxFilter.getTileColumn());
					} 

					GenericStringAttribute genericStringAttrib = cityGMLFactory.createGenericStringAttribute(gen);
					genericStringAttrib.setName("TILE");
					genericStringAttrib.setValue(value);
					cityObject.addGenericAttribute(genericStringAttrib);
				}

				// generalizesTo relation
				if (!generalizesToSet.isEmpty())
					generalizesToExporter.read(cityObject, parentId, core, generalizesToSet);

				// get appearance information associated with the cityobject
				if (exportAppearance)
					appearanceExporter.read(cityObject, parentId);

				if (cityObject.getCityGMLClass() != CityGMLClass.CITYOBJECTGROUP)
					dbExporterManager.updateFeatureCounter(cityObject.getCityGMLClass());
			}

			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psCityObject.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.CITYOBJECT;
	}
}
