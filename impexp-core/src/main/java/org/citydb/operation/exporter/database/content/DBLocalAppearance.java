/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.operation.exporter.database.content;

import org.citydb.config.Config;
import org.citydb.operation.exporter.CityGMLExportException;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.sql.AppearanceFilterBuilder;
import org.citydb.sqlbuilder.expression.LiteralSelectExpression;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.core.AbstractCityObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DBLocalAppearance extends AbstractAppearanceExporter {
	private final PreparedStatement psBulk;
	private final PreparedStatement psSelect;
	private final Map<Long, AbstractCityObject> batches;
	private final int batchSize;

	private List<PlaceHolder<?>> themes;

	public DBLocalAppearance(Connection connection, Query query, CityGMLExportManager exporter, Config config) throws CityGMLExportException, SQLException {
		super(false, null, exporter, config);
		batches = new LinkedHashMap<>();
		batchSize = exporter.getFeatureBatchSize();

		if (query.isSetAppearanceFilter()) {
			try {
				PredicateToken predicate = new AppearanceFilterBuilder(exporter.getDatabaseAdapter()).buildAppearanceFilter(query.getAppearanceFilter(), table.getColumn("theme"));
				select.addSelection(predicate);
				themes = new ArrayList<>();
				predicate.getInvolvedPlaceHolders(themes);
			} catch (QueryBuildException e) {
				throw new CityGMLExportException("Failed to build appearance filter.", e);
			}
		}

		String placeHolders = String.join(",", Collections.nCopies(batchSize, "?"));
		psBulk = connection.prepareStatement(new Select(select)
				.addSelection(ComparisonFactory.in(table.getColumn("id"), new LiteralSelectExpression(placeHolders))).toString());

		psSelect = connection.prepareStatement(new Select(select)
				.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>())).toString());
	}

	protected void addBatch(long appearanceId, AbstractCityObject cityObject) throws CityGMLExportException, SQLException {
		batches.put(appearanceId, cityObject);
		if (batches.size() == batchSize)
			executeBatch();
	}

	protected void executeBatch() throws CityGMLExportException, SQLException {
		if (batches.isEmpty())
			return;

		try {
			if (batches.size() == 1) {
				Map.Entry<Long, AbstractCityObject> entry = batches.entrySet().iterator().next();
				Appearance appearance = doExport(entry.getKey());
				if (appearance != null)
					entry.getValue().addAppearance(new AppearanceProperty(appearance));
			} else {
				int i = 1;
				if (themes != null) {
					for (PlaceHolder<?> theme : themes)
						psBulk.setString(i++, (String) theme.getValue());
				}

				Long[] ids = batches.keySet().toArray(new Long[0]);
				for (int j = 0; j < batchSize; j++)
					psBulk.setLong(i + j, j < ids.length ? ids[j] : 0);

				try (ResultSet rs = psBulk.executeQuery()) {
					Map<Long, Appearance> appearances = doExport(rs);
					for (Map.Entry<Long, Appearance> entry : appearances.entrySet()) {
						AbstractCityObject cityObject = batches.get(entry.getKey());
						if (cityObject == null) {
							exporter.logOrThrowErrorMessage("Failed to assign appearance with id " + entry.getKey() + " to a city object.");
							continue;
						}

						cityObject.addAppearance(new AppearanceProperty(entry.getValue()));
					}
				}
			}
		} finally {
			batches.clear();
		}
	}

	protected Appearance doExport(long appearanceId) throws CityGMLExportException, SQLException {
		int i = 1;
		if (themes != null) {
			for (PlaceHolder<?> theme : themes)
				psSelect.setString(i++, (String) theme.getValue());
		}

		psSelect.setLong(i, appearanceId);

		try (ResultSet rs = psSelect.executeQuery()) {
			Map<Long, Appearance> appearances = doExport(rs);
			return !appearances.isEmpty() ? appearances.values().iterator().next() : null;
		}
	}

	@Override
	public void close() throws SQLException {
		psBulk.close();
		psSelect.close();
	}
}
