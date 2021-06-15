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
package org.citydb.core.database.schema.mapping;

import org.citygml4j.model.module.citygml.CityGMLVersion;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@XmlType(name = "schema", propOrder = {
		"namespaces"
})
public class AppSchema {
	@XmlElement(name="namespace", required = true)
	protected List<Namespace> namespaces;
	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String id;
	@XmlAttribute
	protected String xmlPrefix;
	@XmlAttribute
	protected Boolean isADERoot;
	
	@XmlTransient
	protected final List<ComplexType> complexTypes;
	@XmlTransient
	protected final List<ObjectType> objectTypes;
	@XmlTransient
	protected final List<FeatureType> featureTypes;
	@XmlTransient
    private HashMap<String, Object> localProperties;
	@XmlTransient
	protected SchemaMapping schemaMapping;
	
	@XmlTransient
	private static volatile int adeNamespaceCounter = 1;
	@XmlTransient
	private boolean isGeneratedXMLPrefix = false;

	protected AppSchema() {
		namespaces = new ArrayList<>();
		complexTypes = new ArrayList<>();
		objectTypes = new ArrayList<>();
		featureTypes = new ArrayList<>();
	}
	
	public AppSchema(String id, SchemaMapping schemaMapping) {
		this();
		this.id = id;
		this.schemaMapping = schemaMapping;
	}

	public List<Namespace> getNamespaces() {
		return new ArrayList<>(namespaces);
	}
	
	public boolean isSetNamespace() {
		return namespaces != null && !namespaces.isEmpty();
	}
	
	public void addNamespace(Namespace namespace) {
		if (namespace != null && !namespaces.contains(namespace))
			namespaces.add(namespace);
	}

	public String getId() {
		return id;
	}

	public boolean isSetId() {
		return id != null && !id.isEmpty();
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getXMLPrefix() {
		return xmlPrefix;
	}
	
	public boolean isSetXMLPrefix() {
		return xmlPrefix != null;
	}

	public void setXMLPrefix(String xmlPrefix) {
		this.xmlPrefix = xmlPrefix;
		isGeneratedXMLPrefix = false;
	}
	
	public void generateXMLPrefix() {
		xmlPrefix = MappingConstants.ADE_DEFAULT_XML_PREFIX + (adeNamespaceCounter++);
		isGeneratedXMLPrefix = true;
	}
	
	public boolean isGeneratedXMLPrefix() {
		return isGeneratedXMLPrefix;
	}

	public Boolean isADERoot() {
		return isADERoot != null ? isADERoot.booleanValue() : false;
	}
	
	public void setIsADERoot(Boolean isADERoot) {
		this.isADERoot = isADERoot;
	}

	public boolean matchesNamespaceURI(String namespaceURI) {
		for (Namespace namespace : namespaces) {
			if (namespace.getURI().equals(namespaceURI))
				return true;
		}
		
		return false;
	}
	
	public Namespace getNamespace(CityGMLVersion version) {
		for (Namespace namespace : namespaces) {
			if (namespace.getContext() != null && namespace.getContext().getCityGMLVersion() == version)
				return namespace;
		}
		
		return null;
	}
	
	public CityGMLVersion getCityGMLVersion(String namespaceURI) {
		for (Namespace namespace : namespaces) {
			if (namespace.getURI().equals(namespaceURI) && namespace.getContext() != null)
				return namespace.getContext().getCityGMLVersion();
		}
		
		return null;
	}
	
	public boolean isAvailableForCityGML(CityGMLVersion version) {
		return getNamespace(version) != null;
	}
	
	public List<ComplexType> getComplexTypes() {
		return new ArrayList<>(complexTypes);
	}
	
	protected void addComplexType(ComplexType complexType) {
		if (complexType != null && !complexTypes.contains(complexType))
			complexTypes.add(complexType);
	}

	public List<ObjectType> getObjectTypes() {
		return new ArrayList<>(objectTypes);
	}
	
	protected void addObjectType(ObjectType objectType) {
		if (objectType != null && !objectTypes.contains(objectType))
			objectTypes.add(objectType);
	}

	public List<FeatureType> getFeatureTypes() {
		return new ArrayList<>(featureTypes);
	}
	
	protected void addFeatureType(FeatureType featureType) {
		if (featureType != null && !featureTypes.contains(featureType))
			featureTypes.add(featureType);
	}
	
	public Object getLocalProperty(String name) {
		if (localProperties != null)
			return localProperties.get(name);
			
		return null;
	}

	public void setLocalProperty(String name, Object value) {
		if (localProperties == null)
			localProperties = new HashMap<String, Object>();
		
		localProperties.put(name, value);
	}

	public boolean hasLocalProperty(String name) {
		return localProperties != null && localProperties.containsKey(name);
	}

	public Object unsetLocalProperty(String name) {
		if (localProperties != null)
			return localProperties.remove(name);
		
		return null;
	}
	
	public List<FeatureType> listTopLevelFeatureTypes(boolean onlyQueryable) {
		List<FeatureType> result = new ArrayList<FeatureType>();
		for (FeatureType featureType : featureTypes) {
			if (!featureType.isTopLevel())
				continue;

			if (!onlyQueryable || featureType.isQueryable())
				result.add(featureType);
		}

		return result;
	}
	
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		this.schemaMapping = schemaMapping;
		
		if (!isSetId())
			throw new SchemaMappingException("An application schema must have an id.");
		else if (!isSetNamespace())
			throw new SchemaMappingException("The application schema '" + id + "' lacks a namespace declaration.");
		
		if (xmlPrefix == null)
			generateXMLPrefix();
		
		for (Namespace namespace : namespaces)
			namespace.validate(schemaMapping, this);
	}

}
