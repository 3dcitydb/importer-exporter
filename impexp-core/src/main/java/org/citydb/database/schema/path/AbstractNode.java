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
package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.path.predicate.comparison.EqualToPredicate;
import org.citydb.database.schema.path.predicate.logical.BinaryLogicalPredicate;

public abstract class AbstractNode<T extends AbstractPathElement> {
	protected final T pathElement;
	protected AbstractNodePredicate predicate;

	protected AbstractNode<? extends AbstractPathElement> child;
	protected AbstractNode<? extends AbstractPathElement> parent;

	protected AbstractNode(T pathElement) {
		this.pathElement = pathElement;
	}
	
	protected AbstractNode(AbstractNode<T> other) {
		this.pathElement = other.pathElement;
		this.predicate = other.predicate;
	}

	protected abstract boolean isValidChild(AbstractPathElement candidate);
	protected abstract boolean isValidPredicate(AbstractNodePredicate candidate);
	protected abstract AbstractNode<T> copy();
	
	public final AbstractNode<? extends AbstractPathElement> child() {
		return child;
	}

	public final AbstractNode<? extends AbstractPathElement> parent() {
		return parent;
	}

	public final T getPathElement() {
		return pathElement;
	}

	protected final void setChild(AbstractNode<? extends AbstractPathElement> node) throws InvalidSchemaPathException {
		if (!isValidChild(node.pathElement))
			throw new InvalidSchemaPathException(node + " is not a valid child of " + this + ".");

		node.parent = this;
		this.child = node;
	}
	
	public final void setPredicate(AbstractNodePredicate predicate) throws InvalidSchemaPathException {
		if (!isValidPredicate(predicate))
			throw new InvalidSchemaPathException(predicate + " is not a valid predicate for " + this + ".");
		
		setPredicateContextNode(predicate);
		this.predicate = predicate;
	}
	
	public boolean isSetPredicate() {
		return predicate != null;
	}
	
	public AbstractNodePredicate getPredicate() {
		return predicate;
	}
	
	public void unsetPredicate() {
		predicate = null;
	}
	
	public boolean isEqualTo(AbstractNode<? extends AbstractPathElement> other, boolean includePredicates) {
		if (other == null)
			return false;
		
		if (other == this)
			return true;
		
		if (includePredicates) {
			if ((predicate == null) != (other.predicate == null))
				return false;
			
			if (predicate != null && !predicate.isEqualTo(other.predicate))
				return false;
		}
		
		return pathElement == other.pathElement;
	}
	
	private void setPredicateContextNode(AbstractNodePredicate predicate) {
		if (predicate instanceof EqualToPredicate)
			predicate.contextNode = this;
		else {
			BinaryLogicalPredicate logicalPredicate = (BinaryLogicalPredicate)predicate;
			logicalPredicate.getLeftOperand().contextNode = this;
			setPredicateContextNode(logicalPredicate.getRightOperand());
		}
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append(pathElement.getSchema().isSetXMLPrefix() ? pathElement.getSchema().getXMLPrefix() : pathElement.getSchema().getId())
				.append(":").append(pathElement.getPath()).toString();
	}

}
