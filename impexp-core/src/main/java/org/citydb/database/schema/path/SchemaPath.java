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
import org.citydb.database.schema.mapping.ComplexAttribute;
import org.citydb.database.schema.mapping.ComplexProperty;
import org.citydb.database.schema.mapping.ComplexType;
import org.citydb.database.schema.mapping.FeatureProperty;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.GeometryProperty;
import org.citydb.database.schema.mapping.ImplicitGeometryProperty;
import org.citydb.database.schema.mapping.ObjectProperty;
import org.citydb.database.schema.mapping.ObjectType;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.mapping.SimpleAttribute;

public class SchemaPath {
	private int size;
	private FeatureTypeNode head;
	private AbstractNode<? extends AbstractPathElement> tail;

	public SchemaPath() {

	}

	public SchemaPath(FeatureType featureType) {
		setFirstNode(featureType);		
	}

	public FeatureTypeNode getFirstNode() {
		return head;
	}

	public FeatureTypeNode setFirstNode(FeatureType featureType) {
		tail = head = new FeatureTypeNode(featureType);
		size = 1;

		return head;
	}

	public AbstractNode<? extends AbstractPathElement> getLastNode() {
		return tail;
	}

	public boolean removeLastPathElement() {
		if (head == null)
			return false;

		tail = tail.parent;
		if (tail == null)
			head = null;
		else {
			if (tail.child != null
					&& tail.child.getPathElement().getElementType() == PathElementType.SIMPLE_ATTRIBUTE
					&& tail.child.getPathElement().getPath().equals(".")) {
				tail = tail.parent;
				size--;
			}
			
			tail.child = null;
		}

		size--;
		return true;
	}

	public AbstractNode<? extends AbstractPathElement> appendChild(AbstractPathElement pathElement) throws InvalidSchemaPathException {
		if (head == null) {
			if (pathElement.getElementType() != PathElementType.FEATURE_TYPE)
				throw new InvalidSchemaPathException("Only FeatureType objects are allowed as head of a schema path.");

			return setFirstNode((FeatureType)pathElement);
		} else {
			AbstractNode<? extends AbstractPathElement> node = null;

			switch (pathElement.getElementType()) {
			case FEATURE_TYPE:
				node = new FeatureTypeNode((FeatureType)pathElement);
				break;
			case FEATURE_PROPERTY:
				node = new FeaturePropertyNode((FeatureProperty)pathElement);
				break;
			case GEOMETRY_PROPERTY:
				node = new GeometryPropertyNode((GeometryProperty)pathElement);
				break;
			case SIMPLE_ATTRIBUTE:
				node = new SimpleAttributeNode((SimpleAttribute)pathElement);
				break;
			case COMPLEX_ATTRIBUTE:
				node = new ComplexAttributeNode((ComplexAttribute)pathElement);
				break;
			case OBJECT_TYPE:
				node = new ObjectTypeNode((ObjectType)pathElement);
				break;
			case OBJECT_PROPERTY:
				node = new ObjectPropertyNode((ObjectProperty)pathElement);
				break;
			case COMPLEX_TYPE:
				node = new ComplexTypeNode((ComplexType)pathElement);
				break;
			case COMPLEX_PROPERTY:
				node = new ComplexPropertyNode((ComplexProperty)pathElement);
				break;
			case IMPLICIT_GEOMETRY_PROPERTY:
				node = new ImplicitGeometryPropertyNode((ImplicitGeometryProperty)pathElement);
				break;
			}

			tail.setChild(node);		
			tail = node;
			size++;

			return tail;
		}
	}

	public boolean isValidChild(AbstractPathElement pathElement) {
		return tail != null && tail.isValidChild(pathElement);
	}

	public AbstractNode<? extends AbstractPathElement> setPredicate(AbstractNodePredicate predicate) throws InvalidSchemaPathException {
		if (tail == null)
			throw new InvalidSchemaPathException("The schema path does not contain an element node.");

		tail.setPredicate(predicate);
		return tail;
	}

	public boolean isValidPredicate(AbstractNodePredicate predicate) {
		return tail != null && tail.isValidPredicate(predicate);
	}

	public int size() {
		return size;
	}
	
	public boolean contains(SchemaPath other, boolean includePredicates) {
		if (other.size > size)
			return false;
		
		AbstractNode<?> thisNode = head;
		AbstractNode<?> otherNode = other.head;
		
		while (otherNode != null) {
			if (!thisNode.isEqualTo(otherNode, includePredicates))
				return false;
			
			thisNode = thisNode.child;
			otherNode = otherNode.child;
		}
		
		return true;
	}
	
	public SchemaPath copy() {
		SchemaPath copy = new SchemaPath();
		copy.head = new FeatureTypeNode(head);
		copy.size = size;

		AbstractNode<?> current = copy.head;
		AbstractNode<?> node = head;
		
		while ((node = node.child) != null) {
			AbstractNode<?> tmp = node.copy();
			current.child = tmp;
			tmp.parent = current;
			current = tmp;
		}
		
		copy.tail = current;
		return copy;
	}

	public String toXPath(boolean includeHead, boolean removeAttributePrefixes) {
		StringBuilder builder = new StringBuilder();

		AbstractNode<? extends AbstractPathElement> node = includeHead ? head : head.child;
		if (node == null)
			return "";

		boolean first = true;
		do {
			if (node.getPathElement().getElementType() == PathElementType.SIMPLE_ATTRIBUTE
					&& node.getPathElement().getPath().equals("."))
				continue;

			if (!first)
				builder.append("/");
			else
				first = false;

			switch (node.getPathElement().getElementType()) {
			case SIMPLE_ATTRIBUTE:
				builder.append(((SimpleAttributeNode)node).toString(removeAttributePrefixes));
				break;
			default:
				builder.append(node);
			}

			if (node.isSetPredicate())
				builder.append("[").append(node.predicate.toString(removeAttributePrefixes)).append("]");

		} while ((node = node.child) != null);

		return builder.toString();
	}

	public String toXPath() {
		return toXPath(false, false);
	}

}
