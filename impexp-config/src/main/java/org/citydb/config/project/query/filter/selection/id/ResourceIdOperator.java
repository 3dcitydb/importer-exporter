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
package org.citydb.config.project.query.filter.selection.id;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.PredicateName;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="resourceIds")
@XmlType(name="ResourceIdType", propOrder={
		"ids"
})
public class ResourceIdOperator extends AbstractPredicate {
	@XmlElement(name="id")
	private List<String> ids;
	
	public ResourceIdOperator() {
		ids = new ArrayList<>();
	}
	
	public boolean isSetResourceIds() {
		return !ids.isEmpty();
	}

	public List<String> getResourceIds() {
		return ids;
	}

	public void setResourceIds(List<String> ids) {
		if (ids != null && !ids.isEmpty())
			this.ids = ids;
	}
	
	@Override
	public void reset() {
		ids.clear();
	}
	
	@Override
	public PredicateName getPredicateName() {
		return PredicateName.ID_OPERATOR;
	}
}
