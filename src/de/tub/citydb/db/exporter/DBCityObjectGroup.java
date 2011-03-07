package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import de.tub.citydb.config.Config;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.implementation.gml._3_1_1.GeometryPropertyImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.StringOrRefImpl;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import de.tub.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupMember;
import de.tub.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupModule;
import de.tub.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupParent;
import de.tub.citygml4j.model.gml.GeometryProperty;
import de.tub.citygml4j.model.gml.StringOrRef;

public class DBCityObjectGroup implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psCityObjectGroup;
	private PreparedStatement psParentGmlId;
	private ResultSet rs;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;

	private String gmlNameDelimiter;
	private CityObjectGroupModule grpFactory;

	public DBCityObjectGroup(Connection connection, CityGMLFactory cityGMLFactory, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		grpFactory = config.getProject().getExporter().getModuleVersion().getCityObjectGroup().getModule();

		psCityObjectGroup = connection.prepareStatement("select grp.ID, grp.NAME, grp.NAME_CODESPACE, grp.DESCRIPTION, grp.CLASS, grp.FUNCTION, grp.USAGE, grp.GEOMETRY, grp.SURFACE_GEOMETRY_ID, grp.PARENT_CITYOBJECT_ID, " +
				"gtc.CITYOBJECT_ID, gtc.ROLE from CITYOBJECTGROUP grp " +
				"inner join GROUP_TO_CITYOBJECT gtc on gtc.CITYOBJECTGROUP_ID=grp.ID where grp.ID=?");

		psParentGmlId = connection.prepareStatement("select GMLID from CITYOBJECT where CLASS_ID=23 AND ID=?");
		
		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, JAXBException {
		CityObjectGroup cityObjectGroup = cityGMLFactory.createCityObjectGroup(grpFactory);
		long cityObjectGroupId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(cityObjectGroup, cityObjectGroupId, true);
		if (!success)
			return false;

		psCityObjectGroup.setLong(1, cityObjectGroupId);
		rs = psCityObjectGroup.executeQuery();
		boolean isInited = false;

		while (rs.next()) {
			if (!isInited) {
				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(cityObjectGroup, gmlName, gmlNameCodespace, gmlNameDelimiter);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					cityObjectGroup.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					cityObjectGroup.setClazz(clazz);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) 
					cityObjectGroup.addFunction(function);

				String usage = rs.getString("USAGE");
				if (usage != null) 
					cityObjectGroup.addUsage(usage);

				long lodGeometryId = rs.getLong("SURFACE_GEOMETRY_ID");
				if (!rs.wasNull() && lodGeometryId != 0) {
					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodGeometryId);

					if (geometry != null) {
						GeometryProperty geometryProperty = new GeometryPropertyImpl();

						if (geometry.getAbstractGeometry() != null)
							geometryProperty.setGeometry(geometry.getAbstractGeometry());
						else
							geometryProperty.setHref(geometry.getTarget());

						cityObjectGroup.setGeometry(geometryProperty);
					}
				}

				long parentId = rs.getLong("PARENT_CITYOBJECT_ID");
				if (!rs.wasNull() && parentId != 0) {
					String gmlId = dbExporterManager.getGmlId(parentId, CityGMLClass.CITYOBJECT);

					// retrieve gml:id of parent cityobjectgroups directly from database and assign
					// them. we traverse the group graph by groupMembers not by parents and thus
					// it might be that a parent has not yet been written and thus cannot be retrieved
					// by the lookupServer. however, this also means, that we could set a link to 
					// a parent that will not be written at all...
					if (gmlId == null)
						gmlId = getParentGmlId(parentId);
					
					if (gmlId != null) {
						CityObjectGroupParent parent = cityGMLFactory.createCityObjectGroupParent(grpFactory);
						parent.setHref("#" + gmlId);
						cityObjectGroup.setParent(parent);
					}
				}

				isInited = true;
			}

			long groupMemberId = rs.getLong("CITYOBJECT_ID");
			if (!rs.wasNull() && groupMemberId != 0) {
				String gmlId = dbExporterManager.getGmlId(groupMemberId, CityGMLClass.CITYOBJECT);

				if (gmlId != null) {
					CityObjectGroupMember groupMember = cityGMLFactory.createCityObjectGroupMember(grpFactory);
					groupMember.setHref("#" + gmlId);

					String role = rs.getString("ROLE");
					if (role != null)
						groupMember.setRoleType(role);

					cityObjectGroup.addGroupMember(groupMember);
				} 
			}
		}

		if (cityObjectGroup.getGroupMember() != null && !cityObjectGroup.getGroupMember().isEmpty()) {
			if (cityObjectGroup.getId() != null)
				dbExporterManager.putGmlId(cityObjectGroup.getId(), cityObjectGroupId, cityObjectGroup.getCityGMLClass());
			dbExporterManager.updateFeatureCounter(cityObjectGroup.getCityGMLClass());
			dbExporterManager.print(cityObjectGroup);
			return true;
		} else
			return false;
	}
	
	private String getParentGmlId(long id) {
		String gmlId = null;
		ResultSet rs = null;

		try {
			psParentGmlId.setLong(1, id);
			rs = psParentGmlId.executeQuery();

			if (rs.next()) {
				gmlId = rs.getString(1);
				return gmlId;
			}

		} catch (SQLException sqlEx) {
			System.out.println(sqlEx);
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {
					//
				}

				rs = null;
			}
		}

		return null;
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.CITYOBJECTGROUP;
	}

}
