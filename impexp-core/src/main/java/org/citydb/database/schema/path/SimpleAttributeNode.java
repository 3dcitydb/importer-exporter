/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.database.schema.path.predicate.comparison.ComparisonPredicateName;
import org.citydb.database.schema.path.predicate.comparison.EqualToPredicate;
import org.citydb.database.schema.path.predicate.logical.BinaryLogicalPredicate;

public final class SimpleAttributeNode extends AbstractNode<SimpleAttribute> {

	protected SimpleAttributeNode(SimpleAttribute attribute) {
		super(attribute);
	}
	
	protected SimpleAttributeNode(SimpleAttributeNode other) {
		super(other);
	}

	@Override
	protected boolean isValidChild(AbstractPathElement candidate) {
		return false;
	}

	@Override
	protected boolean isValidPredicate(AbstractNodePredicate candidate) {
		if (candidate.getPredicateName() == ComparisonPredicateName.EQUAL_TO)
			return ((EqualToPredicate)candidate).getLeftOperand() == this.pathElement;

		else {
			BinaryLogicalPredicate predicate = (BinaryLogicalPredicate)candidate;
			if (isValidPredicate(predicate.getLeftOperand()))
				return isValidPredicate(predicate.getRightOperand());			
		}

		return false;
	}

	@Override
	protected SimpleAttributeNode copy() {
		return new SimpleAttributeNode(this);
	}

	public String toString(boolean removeAttributePrefixes) {
		StringBuilder builder = new StringBuilder();

		String name = pathElement.getName();
		boolean usePrefix = true;

		if (pathElement.getPath().startsWith("@")) {
			builder.append("@");

			if (removeAttributePrefixes 
					&& parent != null 
					&& parent.getPathElement().getSchema() == pathElement.getSchema()
					&& !name.equals("id"))
				usePrefix = false;
		}

		if (usePrefix)
			builder.append(pathElement.getSchema().isSetXMLPrefix() ? pathElement.getSchema().getXMLPrefix() : pathElement.getSchema().getId()).append(":");

		return builder.append(name).toString();
	}

	@Override
	public String toString() {
		return toString(false);
	}
}
