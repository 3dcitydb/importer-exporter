/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.citygml.exporter.database.content;

import org.citydb.citygml.common.xal.AddressExportFactory;
import org.citydb.citygml.common.xal.AddressObject;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.exporter.AddressMode;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.xal.AddressDetails;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DBAddress extends AbstractFeatureExporter<Address> {
	private final AddressExportFactory factory;
	private final GMLConverter gmlConverter;
	private Set<String> adeHookTables;

	public DBAddress(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(Address.class, connection, exporter);

		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.ADDRESS.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"), table.getColumn("street"), table.getColumn("house_number"), table.getColumn("po_box"),
				table.getColumn("zip_code"), table.getColumn("city"), table.getColumn("state"), table.getColumn("country"), table.getColumn("xal_source"),
				exporter.getGeometryColumn(table.getColumn("multi_point")));

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			adeHookTables = exporter.getADEHookTables(TableEnum.ADDRESS);			
			if (adeHookTables != null) addJoinsToADEHookTables(adeHookTables, table);
		}
		
		factory = new AddressExportFactory(exporter.getExportConfig());
		gmlConverter = exporter.getGMLConverter();
	}

	@Override
	protected Collection<Address> doExport(long id, Address root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<Address> addresses = new ArrayList<>();

			while (rs.next()) {
				long addressId = rs.getLong("id");

				Address address;
				if (addressId == id && root != null)
					address = root;
				else
					address = new Address();
				
				AddressProperty addressProperty = doExport(address, rs);
				if (addressProperty != null) {					
					// delegate export of generic ADE properties
					if (adeHookTables != null) {
						List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
						if (adeHookTables != null) {
							FeatureType featureType = exporter.getFeatureType(address);
							exporter.delegateToADEExporter(adeHookTables, address, addressId, featureType, exporter.getProjectionFilter(featureType));
						}
					}
					
					addresses.add(address);
				}
			}

			return addresses;
		}
	}

	protected AddressProperty doExport(ResultSet rs) throws CityGMLExportException, SQLException {
		return doExport(null, rs);
	}

	private AddressProperty doExport(Address address, ResultSet rs) throws CityGMLExportException, SQLException {
		AddressObject addressObject = factory.newAddressObject();

		// note: we do not export gml:ids for address objects
		// otherwise we would have to check for xlink references

		fillAddressObject(addressObject, factory.getPrimaryMode(), rs);
		if (!addressObject.canCreate(factory.getPrimaryMode()) && factory.isUseFallback())
			fillAddressObject(addressObject, factory.getFallbackMode(), rs);

		AddressProperty addressProperty = null;
		if (addressObject.canCreate()) {
			// multiPointGeometry
			Object multiPointObj = rs.getObject("multi_point");
			if (!rs.wasNull()) {
				GeometryObject multiPoint = exporter.getDatabaseAdapter().getGeometryConverter().getMultiPoint(multiPointObj);
				MultiPointProperty multiPointProperty = gmlConverter.getMultiPointProperty(multiPoint, false);
				if (multiPointProperty != null)
					addressObject.setMultiPointProperty(multiPointProperty);
			}

			// create xAL address
			if (address == null)
				addressProperty = factory.create(addressObject);
			else
				addressProperty = factory.create(addressObject, address);
		}

		return addressProperty;
	}

	private void fillAddressObject(AddressObject addressObject, AddressMode mode, ResultSet rs) throws SQLException {
		if (mode == AddressMode.DB) {
			addressObject.setStreet(rs.getString("street"));
			addressObject.setHouseNumber(rs.getString("house_number"));
			addressObject.setPOBox(rs.getString("po_box"));
			addressObject.setZipCode(rs.getString("zip_code"));
			addressObject.setCity(rs.getString("city"));
			addressObject.setState(rs.getString("state"));
			addressObject.setCountry(rs.getString("country"));
		} else {
			String xal = rs.getString("xal_source");
			if (!rs.wasNull()) {
				Object object = exporter.unmarshal(new StringReader(xal));
				if (object instanceof AddressDetails)
					addressObject.setAddressDetails((AddressDetails)object);
			}
		}
	}

}
