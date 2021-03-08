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
package org.citydb.database.schema.util;

import org.citydb.database.schema.mapping.AbstractAttribute;
import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.AbstractType;
import org.citydb.database.schema.mapping.AbstractTypeProperty;
import org.citydb.database.schema.mapping.ComplexAttribute;
import org.citydb.database.schema.mapping.ComplexAttributeType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.database.schema.mapping.SimpleType;
import org.citydb.database.schema.path.AbstractNodePredicate;
import org.citydb.database.schema.path.FeatureTypeNode;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.database.schema.path.predicate.comparison.EqualToPredicate;
import org.citydb.database.schema.path.predicate.logical.BinaryLogicalPredicate;
import org.citydb.database.schema.path.predicate.logical.LogicalPredicateName;
import org.citydb.query.filter.selection.expression.AbstractLiteral;
import org.citydb.query.filter.selection.expression.BooleanLiteral;
import org.citydb.query.filter.selection.expression.DateLiteral;
import org.citydb.query.filter.selection.expression.DoubleLiteral;
import org.citydb.query.filter.selection.expression.LiteralType;
import org.citydb.query.filter.selection.expression.LongLiteral;
import org.citydb.query.filter.selection.expression.StringLiteral;
import org.citydb.query.filter.selection.expression.TimestampLiteral;
import org.citydb.registry.ObjectRegistry;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleXPathParser {
	// this parser only supports XPath subset as defined in OGC 09-026r2, Annex D
	private final String simplifiedStepExpr_ = "([^\\[]+)(?:\\[(.+)\\]$)?";
	private final String nameChar_ = "[_a-zA-Z][\\w\\-.]*";
	private final String stringLiteral_ = "\"(?:(?:\"\")|[^\"])*?\"|'(?:(?:'')|[^\"])*?'";
	private final String qName_ = "(?:(" + nameChar_ + "):)??(" + nameChar_ + ")";
	private final String nameTest_ = "@?" + qName_;
	private final String kindTest_ = "schema-element\\(" + qName_ + "\\)";
	private final String functionCall_ = qName_ + "\\((.*?)\\)";

	private final SchemaMapping schemaMapping;
	private final DatatypeFactory datatypeFactory;
	private final Matcher matcher;
	private final Pattern stepExprTest;
	private final Pattern nameTest;
	private final Pattern kindTest;
	private final Pattern functionCallTest;
	private final Pattern stringLiteralTest;

	private AbstractType<?> currentType;
	private SchemaPath schemaPath;

	public SimpleXPathParser(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;

		matcher = Pattern.compile("").matcher("");
		stepExprTest = Pattern.compile(simplifiedStepExpr_, Pattern.UNICODE_CHARACTER_CLASS);
		nameTest = Pattern.compile(nameTest_, Pattern.UNICODE_CHARACTER_CLASS);
		kindTest = Pattern.compile(kindTest_, Pattern.UNICODE_CHARACTER_CLASS);
		functionCallTest = Pattern.compile(functionCall_, Pattern.UNICODE_CHARACTER_CLASS);
		stringLiteralTest = Pattern.compile(stringLiteral_, Pattern.UNICODE_CHARACTER_CLASS);
		datatypeFactory = ObjectRegistry.getInstance().getDatatypeFactory();
	}

	public SchemaPath parse(String xpath, FeatureType rootNode, NamespaceContext namespaceContext) throws XPathException, InvalidSchemaPathException {
		// initialize context
		currentType = rootNode;
		schemaPath = new SchemaPath();
		schemaPath.appendChild(rootNode);

		xpath = xpath.trim().replaceAll("^/", "");		
		String[] stepExprs = xpath.split("/", -1);

		for (String stepExpr : stepExprs) {
			if (stepExpr.isEmpty())
				throw new XPathException("Empty child step expressions are not supported.");

			matcher.reset(stepExpr).usePattern(stepExprTest);

			if (matcher.matches()) {
				String stepToken = matcher.group(1);
				String predicateToken = matcher.group(2);

				// abbreviated child step using a name test
				matcher.reset(stepToken).usePattern(nameTest);
				if (matcher.matches()) {
					handleAbbrevChildStep(matcher.group(1), matcher.group(2), stepToken.startsWith("@"), namespaceContext);
					handlePredicate(predicateToken, namespaceContext);
					continue;
				}

				// abbreviated child step using a kind test
				matcher.reset().usePattern(kindTest);
				if (matcher.matches()) {
					handleAbbrevChildStep(matcher.group(1), matcher.group(2), stepToken.startsWith("@"), namespaceContext);
					handlePredicate(predicateToken, namespaceContext);

					if (schemaPath.getLastNode() instanceof FeatureTypeNode)
						((FeatureTypeNode)schemaPath.getLastNode()).setUseSchemaElement(true);

					continue;
				}

				// function call
				matcher.reset().usePattern(functionCallTest);
				if (matcher.matches()) {
					// currently we do not handle function calls
				}
			}

			// failed to interpret step token
			throw new XPathException("The XPath expression does not confirm to the XPath subset specification of OGC FES 2.0.");
		}

		// complete the schema path in case of it ends with a complex attribute
		if (schemaPath.getLastNode().getPathElement().getElementType() == PathElementType.COMPLEX_ATTRIBUTE) {
			SimpleAttribute attribute = getSimpleAttribute((ComplexAttribute)schemaPath.getLastNode().getPathElement());
			if (attribute != null)
				schemaPath.appendChild(attribute);
		}

		matcher.reset();
		return schemaPath;
	}

	private void handleAbbrevChildStep(String prefix, String localPart, boolean isAttribute, NamespaceContext namespaceContext) throws XPathException, InvalidSchemaPathException {
		QName name = getQName(prefix, localPart, namespaceContext);

		// first, try and map the name onto an object type
		if (!isAttribute) {
			AbstractType<?> type = findType(name);

			if (type != null) {
				if (schemaPath.size() > 1)
					schemaPath.appendChild(type);

				else if (schemaPath.size() == 1 && 
						(type != currentType && !currentType.isSubTypeOf(type))) {
					// if the root node of the schema path does not match the requested feature type,
					// we can only accept this if the requested feature type is a subtype of the root node.
					// this may be the case if the schema path uses a schema-element() function
					throw new InvalidSchemaPathException("The context node '" + name + "' does not match the requested feature type.");
				}

				currentType = type;
				return;
			}
		}

		// second, try and map the name onto a property
		AbstractProperty property = findProperty(name, isAttribute);
		if (!isAttribute && property.getPath().startsWith("@"))
			throw new XPathException("Use @ to select the XML attribute '" + name + "'.");
		else if (isAttribute && !property.getPath().startsWith("@"))
			throw new XPathException("The node '" + name + "' is not an XML attribute. Do not use @ to select it.");

		schemaPath.appendChild(property);
	}

	private void handlePredicate(String token, NamespaceContext namespaceContext) throws XPathException, InvalidSchemaPathException {
		if (token == null)
			return;

		Object predicate = evaluatePredicateToken(token, namespaceContext);
		if (predicate instanceof AbstractNodePredicate) {
			schemaPath.setPredicate((AbstractNodePredicate)predicate);
			return;
		}

		else if (predicate instanceof LongLiteral) {
			throw new InvalidSchemaPathException("Selecting childs of the context node by an index is not supported.");
		}

		throw new InvalidSchemaPathException("Failed to interpret predicate expression '" + token + "'.");
	}

	private Object evaluatePredicateToken(String token, NamespaceContext namespaceContext) throws XPathException, InvalidSchemaPathException {
		token = token.trim();
		int index;

		if ((index = token.indexOf(" and ")) != -1)
			return evaluateLogicalExpr(token.substring(0, index), token.substring(index + 5), LogicalPredicateName.AND, namespaceContext);

		else if ((index = token.indexOf(" or ")) != -1)
			return evaluateLogicalExpr(token.substring(0, index), token.substring(index + 4), LogicalPredicateName.OR, namespaceContext);

		else if ((index = token.indexOf("=")) != -1)
			return evaluateComparisonExpr(token.substring(0, index), token.substring(index + 1), "=", namespaceContext);

		else {
			// test for context item expression
			if (token.equals("."))
				return evaluateContextItemExpression();

			// test for element element name
			matcher.reset(token).usePattern(nameTest);
			if (matcher.matches())
				return evaluateElementName(matcher.group(1), matcher.group(2), token.startsWith("@"), namespaceContext);

			// test for string literal
			matcher.reset().usePattern(stringLiteralTest);
			if (matcher.matches()) {
				String quotOrApos = token.substring(0, 1);
				token = token.substring(1, token.length()-1).replaceAll(quotOrApos + quotOrApos, quotOrApos);
				return new StringLiteral(token);
			}

			// test for integer literal
			try {
				long value = Long.parseLong(token);
				return new LongLiteral(value);
			} catch (NumberFormatException e) {
				//
			}

			// test for double literal
			try {
				double value = Double.parseDouble(token);
				return new DoubleLiteral(value);
			} catch (NumberFormatException e) {
				//
			}

			// test for function call
			matcher.reset().usePattern(functionCallTest);
			if (matcher.matches()) {
				String name = matcher.group(2);
				String parameters = matcher.group(3);

				if ("true".equals(name) && parameters.isEmpty())
					return new BooleanLiteral(true);
				else if ("false".equals(name) && parameters.isEmpty())
					return new BooleanLiteral(false);

				// currently we do not handle further function calls
			}
		}

		// failed to interpret step token
		throw new XPathException("The XPath expression does not confirm to the XPath subset specification of OGC FES 2.0.");
	}

	private SimpleAttribute evaluateElementName(String prefix, String localPart, boolean isAttribute, NamespaceContext namespaceContext) throws XPathException, InvalidSchemaPathException {
		QName name = getQName(prefix, localPart, namespaceContext);
		AbstractProperty property = findProperty(name, isAttribute);

		if (property.getElementType() == PathElementType.COMPLEX_ATTRIBUTE) {
			property = getSimpleAttribute((ComplexAttribute) property);
			if (property == null)
				throw new XPathException("Failed to map node '" + name + "' to a simple attribute.");
		}

		if (property.getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
			throw new InvalidSchemaPathException("'" + name + "' is not a valid property element of " + schemaPath.getLastNode());

		return (SimpleAttribute)property;
	}

	private SimpleAttribute evaluateContextItemExpression() throws InvalidSchemaPathException {
		AbstractPathElement pathElement = schemaPath.getLastNode().getPathElement();

		switch (pathElement.getElementType()) {
		case SIMPLE_ATTRIBUTE:
			return (SimpleAttribute)pathElement;
		case COMPLEX_ATTRIBUTE:
			SimpleAttribute attribute = getSimpleAttribute((ComplexAttribute)pathElement);
			if (attribute != null)
				return attribute;
		case FEATURE_TYPE:
		case OBJECT_TYPE:
		case COMPLEX_TYPE:
		case FEATURE_PROPERTY:
		case OBJECT_PROPERTY:
		case COMPLEX_PROPERTY:
		case IMPLICIT_GEOMETRY_PROPERTY:
		case GEOMETRY_PROPERTY:
			break;
		}

		throw new InvalidSchemaPathException("'.' evaluates to '" + schemaPath.getLastNode() + "' which is not supported as child context in a predicate expression.");
	}

	private EqualToPredicate evaluateComparisonExpr(String leftToken, String rightToken, String operator, NamespaceContext namespaceContext) throws XPathException, InvalidSchemaPathException {
		Object[] operands = new Object[2];
		operands[0] = evaluatePredicateToken(leftToken, namespaceContext);
		operands[1] = evaluatePredicateToken(rightToken, namespaceContext);

		SimpleAttribute attribute = null;
		AbstractLiteral<?> literal = null;

		for (Object operand : operands) {
			if (operand instanceof SimpleAttribute)
				attribute = (SimpleAttribute)operand;
			else if (operand instanceof AbstractLiteral<?>)
				literal = (AbstractLiteral<?>)operand;
		}

		if (attribute == null || literal == null)
			throw new XPathException("Only equality tests of the form 'child=value' may be used.");

		// try and apply an implicit type conversion if required
		if (!literal.evaluatesToSchemaType(attribute.getType()))
			literal = implicitTypeConversion(literal, attribute.getType());

		return new EqualToPredicate(attribute, literal);
	}

	private BinaryLogicalPredicate evaluateLogicalExpr(String leftToken, String rightToken, LogicalPredicateName name, NamespaceContext namespaceContext) throws XPathException, InvalidSchemaPathException {
		Object leftOperand = evaluatePredicateToken(leftToken, namespaceContext);
		Object rightOperand = evaluatePredicateToken(rightToken, namespaceContext);

		if (!(leftOperand instanceof EqualToPredicate) || !(rightOperand instanceof AbstractNodePredicate))
			throw new XPathException("Only equality tests of the form child=value can be logically combined using the 'and' or 'or' operators.");

		return new BinaryLogicalPredicate((EqualToPredicate)leftOperand, name, (AbstractNodePredicate)rightOperand);
	}

	private QName getQName(String prefix, String name, NamespaceContext namespaceContext) throws XPathException {
		if (prefix == null)
			prefix = XMLConstants.DEFAULT_NS_PREFIX;

		String namespaceURI = namespaceContext.getNamespaceURI(prefix);
		if (namespaceURI == null || namespaceURI.isEmpty())
			throw new XPathException("Failed to find namespace URI for prefix '" + prefix + "' used with '" + name + "'.");

		return new QName(namespaceURI, name);
	}

	private AbstractType<?> findType(QName name) throws InvalidSchemaPathException {
		// check whether the name matches a feature or object type
		AbstractType<?> type = schemaMapping.getAbstractObjectType(name);

		// check for complex types which can have more than one definition per name
		if (type == null && PathElementType.TYPE_PROPERTIES.contains(schemaPath.getLastNode().getPathElement().getElementType())) {
			AbstractTypeProperty<?> property = (AbstractTypeProperty<?>)schemaPath.getLastNode().getPathElement();
			AbstractType<?> candidate = property.getType();
			if (candidate.matchesName(name))
				type = candidate;
		}

		if (type != null && !type.isQueryable())
			throw new InvalidSchemaPathException("'" + name + "' cannot be queried.");

		return type;
	}

	private AbstractProperty findProperty(QName name, boolean isAttribute) throws InvalidSchemaPathException {
		AbstractPathElement pathElement = schemaPath.getLastNode().getPathElement();
		AbstractProperty property = null;

		switch (pathElement.getElementType()) {
		case FEATURE_TYPE:
		case OBJECT_TYPE:
		case COMPLEX_TYPE:
			property = currentType.getProperty(name.getLocalPart(), name.getNamespaceURI(), true);
			break;
		case COMPLEX_ATTRIBUTE:
			ComplexAttributeType type = ((ComplexAttribute)pathElement).getType();
			property = type.getAttribute(name.getLocalPart(), name.getNamespaceURI());
			break;
		case SIMPLE_ATTRIBUTE:
		case FEATURE_PROPERTY:
		case OBJECT_PROPERTY:
		case COMPLEX_PROPERTY:
		case IMPLICIT_GEOMETRY_PROPERTY:
		case GEOMETRY_PROPERTY:
			break;
		}

		if (property == null)
			throw new InvalidSchemaPathException("'" + name + "' is not a valid " + (isAttribute ? "attribute" : "child element") + " of " + schemaPath.getLastNode());

		if (!property.isQueryable())
			throw new InvalidSchemaPathException("'" + name + "' cannot be queried.");

		return property;			
	}

	private SimpleAttribute getSimpleAttribute(ComplexAttribute complexAttribute) {
		for (AbstractAttribute attribute : complexAttribute.getType().getAttributes()) {
			if (attribute.getPath().equals(".") && attribute.getElementType() == PathElementType.SIMPLE_ATTRIBUTE)
				return (SimpleAttribute)attribute;
		}

		return null;
	}

	private AbstractLiteral<?> implicitTypeConversion(AbstractLiteral<?> literal, SimpleType type) throws XPathException {
		switch (type) {
			case DOUBLE:
				if (literal.getLiteralType() == LiteralType.LONG)
					literal = new DoubleLiteral(((LongLiteral)literal).getValue().doubleValue());
				else if (literal.getLiteralType() == LiteralType.STRING) {
					try {
						literal = new DoubleLiteral(Double.parseDouble(((StringLiteral)literal).getValue()));
					} catch (NumberFormatException e) {
						//
					}
				}
				break;
			case INTEGER:
				if (literal.getLiteralType() == LiteralType.DOUBLE) {
					double value = ((DoubleLiteral)literal).getValue();
					if (value == (long)value)
						literal = new LongLiteral((long)value);
				} else if (literal.getLiteralType() == LiteralType.STRING) {
					try {
						literal = new LongLiteral(Long.parseLong(((StringLiteral)literal).getValue()));
					} catch (NumberFormatException e) {
						//
					}
				}
				break;
			case STRING:
				if (literal.getLiteralType() == LiteralType.LONG)
					literal = new StringLiteral(((LongLiteral)literal).getValue().toString());
				else if (literal.getLiteralType() == LiteralType.DOUBLE)
					literal = new StringLiteral(String.valueOf(((DoubleLiteral)literal).getValue()));
				break;
			case BOOLEAN:
				if (literal.getLiteralType() == LiteralType.STRING) {
					String value = ((StringLiteral)literal).getValue();
					if ("true".equals(value))
						literal = new BooleanLiteral(true);
					else if ("false".equals(value))
						literal = new BooleanLiteral(false);
				}
				break;
			case DATE:
				if (literal.getLiteralType() == LiteralType.STRING) {
					try {
						String value = ((StringLiteral)literal).getValue();
						XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar(value);
						literal = new DateLiteral(cal.toGregorianCalendar());
						((DateLiteral)literal).setXMLLiteral(value);
					} catch (IllegalArgumentException e) {
						//
					}
				}
				break;
			case TIMESTAMP:
				if (literal.getLiteralType() == LiteralType.STRING) {
					try {
						String value = ((StringLiteral)literal).getValue();
						XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar(value);
						literal = new TimestampLiteral(cal.toGregorianCalendar());
						((TimestampLiteral)literal).setXMLLiteral(value);
						((TimestampLiteral)literal).setDate(cal.getXMLSchemaType() == DatatypeConstants.DATE);
					} catch (IllegalArgumentException e) {
						//
					}
				}
				break;
			case CLOB:
				throw new XPathException("CLOB columns are not supported in XPath expressions.");
		}

		return literal;
	}

}
