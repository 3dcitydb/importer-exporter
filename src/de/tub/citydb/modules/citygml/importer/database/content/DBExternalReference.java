/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;

import de.tub.citydb.config.internal.Internal;

public class DBExternalReference implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psExternalReference;
	private int batchCounter;

	public DBExternalReference(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psExternalReference = batchConn.prepareStatement("insert into EXTERNAL_REFERENCE (ID, INFOSYS, NAME, URI, CITYOBJECT_ID) values " +
				"(EXTERNAL_REF_SEQ.nextval, ?, ?, ?, ?)");
	}

	public void insert(ExternalReference externalReference, long cityObjectId) throws SQLException {
		// informationSystem
		if (externalReference.isSetInformationSystem()) {
			psExternalReference.setString(1, externalReference.getInformationSystem());
		} else {
			psExternalReference.setNull(1, Types.VARCHAR);
		}

		// ExternalObject
		if (externalReference.isSetExternalObject()) {
			ExternalObject externalObject = externalReference.getExternalObject();

			// name
			if (externalObject.isSetName()) {
				psExternalReference.setString(2, externalObject.getName());
			} else {
				psExternalReference.setNull(2, Types.VARCHAR);
			}

			// uri
			if (externalObject.isSetUri()) {
				psExternalReference.setString(3, externalObject.getUri());
			} else {
				psExternalReference.setNull(3, Types.VARCHAR);
			}
		} else {
			psExternalReference.setNull(2, Types.VARCHAR);
			psExternalReference.setNull(3, Types.VARCHAR);
		}

		// cityObjectId
		psExternalReference.setLong(4, cityObjectId);

		psExternalReference.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.EXTERNAL_REFERENCE);
	}

	@Override
	public void executeBatch() throws SQLException {
		psExternalReference.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psExternalReference.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.EXTERNAL_REFERENCE;
	}

}
