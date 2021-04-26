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
package org.citydb.query.builder.config;

import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.operator.id.AbstractIdOperator;
import org.citydb.query.filter.selection.operator.id.IdOperationFactory;

public class IdOperatorBuilder {
	
	protected IdOperatorBuilder() {
		
	}

	protected Predicate buildIdOperator(org.citydb.config.project.query.filter.selection.id.AbstractIdOperator operatorConfig) throws QueryBuildException {
		AbstractIdOperator operator = null;

		try {
			switch (operatorConfig.getOperatorName()) {
				case RESOURCE_ID:
					operator = buildResourceIdOperator((org.citydb.config.project.query.filter.selection.id.ResourceIdOperator) operatorConfig);
					break;
				case DATABASE_ID:
					operator = buildDatabaseIdOperator((org.citydb.config.project.query.filter.selection.id.DatabaseIdOperator) operatorConfig);
					break;
			}
		} catch (FilterException e) {
			throw new QueryBuildException("Failed to build the " + operatorConfig.getOperatorName() + " operator.", e);
		}

		return operator;
	}

	private AbstractIdOperator buildResourceIdOperator(org.citydb.config.project.query.filter.selection.id.ResourceIdOperator operatorConfig) throws FilterException, QueryBuildException {
		if (operatorConfig.getResourceIds().isEmpty())  {
			throw new QueryBuildException("No valid gml:ids provided for resource id filter.");
		}

		return IdOperationFactory.resourceIds(operatorConfig.getResourceIds());
	}

	private AbstractIdOperator buildDatabaseIdOperator(org.citydb.config.project.query.filter.selection.id.DatabaseIdOperator operatorConfig) throws FilterException, QueryBuildException {
		if (operatorConfig.getDatabaseIds().isEmpty())  {
			throw new QueryBuildException("No valid ids provided for database id filter.");
		}

		return IdOperationFactory.databaseIds(operatorConfig.getDatabaseIds());
	}
}
