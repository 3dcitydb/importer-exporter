package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashSet;

import javax.xml.datatype.XMLGregorianCalendar;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import de.tub.citydb.config.Config;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.BoundingBoxFilter;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.geometry.Point;
import de.tub.citygml4j.implementation.gml._3_1_1.BoundingShapeImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.EnvelopeImpl;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.core.CityObject;
import de.tub.citygml4j.model.citygml.core.CoreModule;
import de.tub.citygml4j.model.citygml.core.ExternalObject;
import de.tub.citygml4j.model.citygml.core.ExternalReference;
import de.tub.citygml4j.model.citygml.generics.GenericAttribute;
import de.tub.citygml4j.model.citygml.generics.GenericDateAttribute;
import de.tub.citygml4j.model.citygml.generics.GenericDoubleAttribute;
import de.tub.citygml4j.model.citygml.generics.GenericIntAttribute;
import de.tub.citygml4j.model.citygml.generics.GenericStringAttribute;
import de.tub.citygml4j.model.citygml.generics.GenericUriAttribute;
import de.tub.citygml4j.model.citygml.generics.GenericsModule;
import de.tub.citygml4j.model.gml.BoundingShape;
import de.tub.citygml4j.model.gml.Envelope;

public class DBCityObject implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psCityObject;
	private ResultSet rs;

	private DBAppearance appearanceExporter;
	private DBGeneralization generalizesToExporter;
	private String gmlSrsName;
	private boolean exportAppearance;
	private BoundingBoxFilter boundingBoxFilter;

	HashSet<Long> generalizesToSet;
	HashSet<Long> externalReferenceSet;
	HashSet<Long> genericAttributeSet;
	
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
		gmlSrsName = config.getInternal().getDbSrsName();

		generalizesToSet = new HashSet<Long>();
		externalReferenceSet = new HashSet<Long>();
		genericAttributeSet = new HashSet<Long>();
		
		psCityObject = connection.prepareStatement("select co.GMLID, co.ENVELOPE, co.CREATION_DATE, co.TERMINATION_DATE, ex.ID as EXID, ex.INFOSYS, ex.NAME, ex.URI, " +
				"ga.ID as GAID, ga.ATTRNAME, ga.DATATYPE, ga.STRVAL, ga.INTVAL, ga.REALVAL, ga.URIVAL, ga.DATEVAL, ge.GENERALIZES_TO_ID " +
				"from CITYOBJECT co left join EXTERNAL_REFERENCE ex on co.ID = ex.CITYOBJECT_ID " +
				"left join CITYOBJECT_GENERICATTRIB ga on co.ID = ga.CITYOBJECT_ID " +
				"left join GENERALIZATION ge on ge.CITYOBJECT_ID=co.ID where co.ID = ?");

		appearanceExporter = (DBAppearance)dbExporterManager.getDBExporter(DBExporterEnum.APPEARANCE);
		generalizesToExporter = (DBGeneralization)dbExporterManager.getDBExporter(DBExporterEnum.GENERALIZATION);
	}


	public boolean read(CityObject cityObject, long parentId) throws SQLException {
		return read(cityObject, parentId, false);
	}

	public boolean read(CityObject cityObject, long parentId, boolean checkBoundingBox) throws SQLException {
		psCityObject.setLong(1, parentId);
		rs = psCityObject.executeQuery();

		CoreModule coreFactory = cityObject.getCityGMLModule().getCoreDependency();
		GenericsModule genericsFactory = cityObject.getCityGMLModule().getGenericsDependency();

		if (rs.next()) {
			generalizesToSet.clear();
			externalReferenceSet.clear();
			genericAttributeSet.clear();

			// boundedBy
			STRUCT struct = (STRUCT)rs.getObject("ENVELOPE");
			if (!rs.wasNull() && struct != null) {
				JGeometry jGeom = JGeometry.load(struct);
				Envelope env = new EnvelopeImpl();

				double[] points = jGeom.getOrdinatesArray();
				Point lower = new Point(points[0], points[1], points[2]);
				Point upper = new Point(points[3], points[4], points[5]);

				env.setLowerCorner(lower);
				env.setUpperCorner(upper);
				env.setSrsDimension(3);
				env.setSrsName(gmlSrsName);

				BoundingShape boundedBy = new BoundingShapeImpl();
				boundedBy.setEnvelope(env);
				cityObject.setBoundedBy(boundedBy);
			}

			// check bounding volume filter
			if (checkBoundingBox && cityObject.getBoundedBy() != null &&
					cityObject.getBoundedBy().getEnvelope() != null &&
					boundingBoxFilter.filter(cityObject.getBoundedBy().getEnvelope()))
				return false;

			String gmlId = rs.getString("GMLID");
			if (gmlId != null)
				cityObject.setId(gmlId);

			// creationDate
			Date creationDate = rs.getDate("CREATION_DATE");
			if (creationDate != null) {
				GregorianCalendar gregDate = new GregorianCalendar();
				gregDate.setTime(creationDate);
				XMLGregorianCalendar xmlDate = new XMLGregorianCalendarImpl(gregDate);
				cityObject.setCreationDate(xmlDate);
			}

			// terminationDate
			Date terminationDate = rs.getDate("TERMINATION_DATE");
			if (terminationDate != null) {
				GregorianCalendar gregDate = new GregorianCalendar();
				gregDate.setTime(terminationDate);
				XMLGregorianCalendar xmlDate = new XMLGregorianCalendarImpl(gregDate);
				cityObject.setTerminationDate(xmlDate);
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

					ExternalReference externalReference = cityGMLFactory.createExternalReference(coreFactory);
					ExternalObject externalObject = cityGMLFactory.createExternalObject(coreFactory);

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
							genericAttrib = cityGMLFactory.createGenericStringAttribute(genericsFactory);
							((GenericStringAttribute)genericAttrib).setValue(strVal);
						}
						break;
					case 2:
						Integer intVal = rs.getInt("INTVAL");
						if (!rs.wasNull() && intVal != null) {
							genericAttrib = cityGMLFactory.createGenericIntAttribute(genericsFactory);
							((GenericIntAttribute)genericAttrib).setValue(intVal);
						}
						break;
					case 3:
						Double realVal = rs.getDouble("REALVAL");
						if (!rs.wasNull() && realVal != null) {
							genericAttrib = cityGMLFactory.createGenericDoubleAttribute(genericsFactory);
							((GenericDoubleAttribute)genericAttrib).setValue(realVal);
						}
						break;
					case 4:
						String uriVal = rs.getString("URIVAL");
						if (uriVal != null) {
							genericAttrib = cityGMLFactory.createGenericUriAttribute(genericsFactory);
							((GenericUriAttribute)genericAttrib).setValue(uriVal);
						}
						break;
					case 5:
						Date dateVal = rs.getDate("DATEVAL");
						if (dateVal != null) {
							genericAttrib = cityGMLFactory.createGenericDateAttribute(genericsFactory);
							GregorianCalendar gregDate = new GregorianCalendar();
							gregDate.setTime(dateVal);
							XMLGregorianCalendar xmlDate = new XMLGregorianCalendarImpl(gregDate);
							((GenericDateAttribute)genericAttrib).setValue(xmlDate);
						}
						break;
					}

					if (genericAttrib != null) {
						genericAttrib.setName(attrName);
						cityObject.addGenericAttribute(genericAttrib);
					}
				}

			} while (rs.next());

			// generalizesTo relation
			if (!generalizesToSet.isEmpty())
				generalizesToExporter.read(cityObject, parentId, coreFactory, generalizesToSet);

			// get appearance information associated with the cityobject
			if (exportAppearance)
				appearanceExporter.read(cityObject, parentId);

			if (cityObject.getCityGMLClass() != CityGMLClass.CITYOBJECTGROUP)
				dbExporterManager.updateFeatureCounter(cityObject.getCityGMLClass());
		}

		return true;
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.CITYOBJECT;
	}
}
