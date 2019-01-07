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
package org.citydb.database.schema.mapping;

import org.citygml4j.model.module.citygml.CityGMLVersion;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@XmlRootElement
@XmlType(name = "schemaMapping", propOrder = {
		"metadata",
		"schemas",
		"attributeTypes",
		"complexTypes",
		"objectTypes",
		"featureTypes",
		"propertyInjections"
})
public class SchemaMapping {
	protected Metadata metadata;
	@XmlElementWrapper(name="applicationSchemas", required = true)
	@XmlElement(name="schema", required = true)
	protected List<AppSchema> schemas;
	@XmlElementWrapper(name="attributeTypes")
	@XmlElement(name="attributeType")
	protected List<ComplexAttributeType> attributeTypes;
	@XmlElementWrapper(name="complexTypes")
	@XmlElement(name="complexType")
	protected List<ComplexType> complexTypes;
	@XmlElementWrapper(name="objectTypes")
	@XmlElement(name="objectType")
	protected List<ObjectType> objectTypes;
	@XmlElementWrapper(name="featureTypes")
	@XmlElement(name="featureType")
	protected List<FeatureType> featureTypes;
	@XmlElementWrapper(name="propertyInjections")
	@XmlElement(name="propertyInjection")
	protected List<PropertyInjection> propertyInjections;

	@XmlTransient
	protected HashMap<String, AppSchema> uriToSchemaMap;
	@XmlTransient
	protected boolean isMerged;

	public SchemaMapping() {
		uriToSchemaMap = new HashMap<>();
		schemas = new ArrayList<>();
		attributeTypes = new ArrayList<>();
		complexTypes = new ArrayList<>();
		objectTypes = new ArrayList<>();
		featureTypes = new ArrayList<>();
		propertyInjections = new ArrayList<>();
	}
	
	public boolean isMerged() {
		return isMerged;
	}

	public Metadata getMetadata() {
		return metadata;
	}
	
	public boolean isSetMetadata() {
		return metadata != null;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public List<AppSchema> getSchemas() {
		return new ArrayList<>(schemas);
	}

	public AppSchema getSchema(String namespaceURI) {
		return uriToSchemaMap.get(namespaceURI);
	}

	protected AppSchema getSchemaById(String id) {
		for (AppSchema schema : schemas) {
			if (schema.getId().equals(id))
				return schema;
		}

		return null;
	}

	public boolean isSetSchemas() {
		return schemas != null && !schemas.isEmpty();
	}

	public void addSchema(AppSchema schema) throws SchemaMappingException {
		if (schema != null && !schemas.contains(schema)) {
			schema.validate(this, this);
			schemas.add(schema);
			
			for (Namespace namespace : schema.namespaces)
				uriToSchemaMap.put(namespace.getURI(), schema);	
		}
	}

	public List<ComplexAttributeType> getComplexAttributeTypes() {
		return new ArrayList<>(attributeTypes);
	}

	protected ComplexAttributeType getComplexAttributeTypeById(String id) {
		for (ComplexAttributeType attributeType : attributeTypes) {
			if (attributeType.getId().equals(id))
				return attributeType;
		}

		return null;
	}

	public boolean isSetComplexAttributeTypes() {
		return attributeTypes != null && !attributeTypes.isEmpty();
	}

	public void addComplexAttributeType(ComplexAttributeType attributeType) throws SchemaMappingException {
		if (attributeType != null && !attributeTypes.contains(attributeType)) {
			attributeType.validate(this, this);
			attributeTypes.add(attributeType);
			registerProperties(attributeType.attributes);
		}
	}

	public List<ComplexType> getComplexTypes() {
		return new ArrayList<>(complexTypes);
	}

	public ComplexType getComplexType(String name, String namespaceURI) {
		AppSchema schema = getSchema(namespaceURI);		
		if (schema != null) {
			for (ComplexType complexType : schema.complexTypes) {
				if (complexType.getPath().equals(name))
					return complexType;
			}
		}

		return null;
	}

	public ComplexType getComplexType(QName name) {
		return getComplexType(name.getLocalPart(), name.getNamespaceURI());
	}

	protected ComplexType getComplexTypeById(String id) {
		for (ComplexType complexType : complexTypes) {
			if (complexType.getId().equals(id))
				return complexType;
		}

		return null;
	}

	public boolean isSetComplexTypes() {
		return complexTypes != null && !complexTypes.isEmpty();
	}

	public void addComplexType(ComplexType complexType) throws SchemaMappingException {
		if (complexType != null && !complexTypes.contains(complexType)) {
			complexType.validate(this, this);
			complexTypes.add(complexType);
			registerType(complexType);
		}
	}

	public List<ObjectType> getObjectTypes() {
		return new ArrayList<>(objectTypes);
	}

	public Set<ObjectType> listObjectTypesByTable(String tableName, boolean skipAbstractTypes) {
		Set<ObjectType> result = new HashSet<>();
		for (ObjectType objectType : objectTypes) {
			if (objectType.getTable().equalsIgnoreCase(tableName)) {
				if (!objectType.isAbstract() || !skipAbstractTypes)
					result.add(objectType);

				result.addAll(objectType.listSubTypes(skipAbstractTypes));
			}
		}

		return result;
	}

	public ObjectType getObjectType(String name, String namespaceURI) {
		AppSchema schema = getSchema(namespaceURI);		
		if (schema != null) {
			for (ObjectType objectType : schema.objectTypes) {
				if (objectType.getPath().equals(name))
					return objectType;
			}
		}

		return null;
	}

	public ObjectType getObjectType(QName name) {
		return getObjectType(name.getLocalPart(), name.getNamespaceURI());
	}
	
	public ObjectType getObjectType(int objectClassId) {
		for (ObjectType objectType : objectTypes) {
			if (objectType.getObjectClassId() == objectClassId)
				return objectType;
		}
		
		return null;
	}

	protected ObjectType getObjectTypeById(String id) {
		for (ObjectType objectType : objectTypes) {
			if (objectType.getId().equals(id))
				return objectType;
		}

		return null;
	}

	public boolean isSetObjectTypes() {
		return objectTypes != null && !objectTypes.isEmpty();
	}

	public void addObjectType(ObjectType objectType) throws SchemaMappingException {
		if (objectType != null && !objectTypes.contains(objectType)) {
			objectType.validate(this, this);

			AbstractObjectType<?> other = checkUniqueObjectClassId(objectType);
			if (other != null)
				throw new SchemaMappingException("The value " + objectType.objectClassId + " of the attribute 'objectClassId' is assigned to both '" + objectType.id + "' and '" + other.id + "'.");

			objectTypes.add(objectType);
			registerType(objectType);
		}
	}	

	public List<FeatureType> getFeatureTypes() {
		return new ArrayList<>(featureTypes);
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

	public Set<FeatureType> listFeatureTypesByTable(String tableName, boolean skipAbstractTypes) {
		Set<FeatureType> result = new HashSet<>();
		for (FeatureType featureType : featureTypes) {
			if (featureType.getTable().equalsIgnoreCase(tableName)) {
				if (!featureType.isAbstract() || !skipAbstractTypes)
					result.add(featureType);
				
				result.addAll(featureType.listSubTypes(skipAbstractTypes));
			}
		}

		if (result.isEmpty()) {
			for (PropertyInjection propertyInjection : propertyInjections) {
				if (propertyInjection.getTable().equalsIgnoreCase(tableName)) {
				    Set<FeatureType> baseTypes = new HashSet<>();
				    for (InjectedProperty property : propertyInjection.properties)
				        baseTypes.add(property.getBase());

					for (FeatureType featureType : baseTypes) {
                        if (!featureType.isAbstract() || !skipAbstractTypes)
                            result.add(featureType);

                        result.addAll(featureType.listSubTypes(skipAbstractTypes));
                    }
				}
			}
		}

		return result;
	}

	public FeatureType getFeatureType(String name, String namespaceURI) {
		AppSchema schema = getSchema(namespaceURI);		
		if (schema != null) {
			for (FeatureType featureType : schema.featureTypes) {
				if (featureType.getPath().equals(name))
					return featureType;
			}
		}

		return null;
	}

	public FeatureType getFeatureType(QName name) {
		return name != null ? getFeatureType(name.getLocalPart(), name.getNamespaceURI()) : null;
	}
	
	public FeatureType getFeatureType(int objectClassId) {
		for (FeatureType featureType : featureTypes) {
			if (featureType.getObjectClassId() == objectClassId)
				return featureType;
		}
		
		return null;
	}

	protected FeatureType getFeatureTypeById(String id) {
		for (FeatureType featureType : featureTypes) {
			if (featureType.getId().equals(id))
				return featureType;
		}

		return null;
	}

	public FeatureType getCommonSuperType(Collection<FeatureType> featureTypes) {
		if (featureTypes != null && !featureTypes.isEmpty()) {
			if (featureTypes.size() == 1)
				return featureTypes.iterator().next();

			Iterator<FeatureType> iter = featureTypes.iterator();
			List<FeatureType> candidates = iter.next().listSuperTypes(true);
			while (iter.hasNext())
				candidates.retainAll(iter.next().listSuperTypes(true));

			return !candidates.isEmpty() ? candidates.get(0) : null;
		}

		return null;
	}

	public boolean isSetFeatureTypes() {
		return featureTypes != null && !featureTypes.isEmpty();
	}

	public void addFeatureType(FeatureType featureType) throws SchemaMappingException {
		if (featureType != null && !featureTypes.contains(featureType)) {
			featureType.validate(this, this);

			AbstractObjectType<?> other = checkUniqueObjectClassId(featureType);
			if (other != null)
				throw new SchemaMappingException("The value " + featureType.objectClassId + " of the attribute 'objectClassId' is assigned to both '" + featureType.id + "' and '" + other.id + "'.");

			featureTypes.add(featureType);
			registerType(featureType);
		}
	}	

	public AbstractObjectType<?> getAbstractObjectType(String name, String namespaceURI) {
		AbstractObjectType<?> type = getFeatureType(name, namespaceURI);
		if (type == null)
			type = getObjectType(name, namespaceURI);

		return type;
	}

	public AbstractObjectType<?> getAbstractObjectType(QName name) {
		return getAbstractObjectType(name.getLocalPart(), name.getNamespaceURI());
	}
	
	public AbstractObjectType<?> getAbstractObjectType(int objectClassId) {
		AbstractObjectType<?> type = getFeatureType(objectClassId);
		if (type == null)
			type = getObjectType(objectClassId);

		return type;
	}

	public List<AbstractObjectType<?>> getAbstractObjectTypes() {
		List<AbstractObjectType<?>> types = new ArrayList<>(featureTypes);
		types.addAll(objectTypes);

		return types;
	}

	public Set<? extends AbstractObjectType<?>> listAbstractObjectTypesByTable(String tableName, boolean skipAbstractTypes) {
		Set<? extends AbstractObjectType<?>> types = listFeatureTypesByTable(tableName, skipAbstractTypes);
		if (types.isEmpty())
			types = listObjectTypesByTable(tableName, skipAbstractTypes);

		return types;
	}

	public List<AbstractType<?>> getAbstractTypes() {
		List<AbstractType<?>> types = new ArrayList<>(featureTypes);
		types.addAll(objectTypes);
		types.addAll(complexTypes);
		
		return types;
	}

	public List<PropertyInjection> getPropertyInjections() {
		return new ArrayList<>(propertyInjections);
	}

	public boolean isSetPropertyInjections() {
		return propertyInjections != null && !propertyInjections.isEmpty();
	}
	
	public void addPropertyInjection(PropertyInjection propertyInjection) throws SchemaMappingException {
		if (propertyInjection != null && !propertyInjections.contains(propertyInjection)) {
			propertyInjection.validate(this, this);
			propertyInjections.add(propertyInjection);
			registerPropertyInjection(propertyInjection);
		}
	}

	public Map<String, String> getNamespaceContext(CityGMLVersion version) {
		Map<String, String> context = new HashMap<>();

		// register app schema namespaces
		for (AppSchema schema : schemas) {
			for (Namespace namespace : schema.namespaces) {
				if (namespace.isSetContext() && namespace.getContext().getCityGMLVersion() == version)
					context.put(schema.getXMLPrefix(), namespace.getURI());
			}	
		}

		return context;
	}

	public Map<String, String> getNamespaceContext() {
		return getNamespaceContext(CityGMLVersion.v2_0_0);
	}
	
	private AbstractObjectType<?> checkUniqueObjectClassId(AbstractObjectType<?> type) {
		if (type.objectClassId == null)
			return null;
		
		for (FeatureType featureType : featureTypes) {
			if (featureType != type 
					&& featureType.objectClassId != null
					&& featureType.objectClassId.intValue() == type.objectClassId.intValue())
				return featureType;
		}
		
		for (ObjectType objectType : objectTypes) {
			if (objectType != type 
					&& objectType.objectClassId != null
					&& objectType.objectClassId.intValue() == type.objectClassId.intValue())
				return objectType;
		}
		
		return null;
	}
	
	private void registerType(AbstractType<?> type) throws SchemaMappingException {
		if (type.schema != null && type.schema.schemaMapping == this)
			addSchema(type.schema);
		
		if (type.isSetExtension() 
				&& type.getExtension().isSetBase() 
				&& type.getExtension().getBase().schemaMapping == this) {
			AbstractType<?> superType = type.getExtension().getBase();
			switch (superType.getElementType()) {
			case FEATURE_TYPE:
				addFeatureType((FeatureType)superType);
				break;
			case OBJECT_TYPE:
				addObjectType((ObjectType)superType);
				break;
			default:
				// nothing to do
			}
		}
		
		registerProperties(type.properties);
	}
	
	private void registerPropertyInjection(PropertyInjection propertyInjection) throws SchemaMappingException {
		if (propertyInjection.defaultBase != null && propertyInjection.defaultBase.schemaMapping == this)
			addFeatureType(propertyInjection.defaultBase);
		
		registerInjectedProperties(propertyInjection.properties);
	}
	
	private void registerProperties(List<? extends AbstractProperty> properties) throws SchemaMappingException {
		for (AbstractProperty property : properties)
			registerAbstractProperty(property);
	}
	
	private void registerInjectedProperties(List<InjectedProperty> properties) throws SchemaMappingException {
		for (InjectedProperty property : properties) {
			if (property.isSetBase() && property.getBase().schemaMapping == this)
				addFeatureType(property.getBase());
			
			if (property instanceof AbstractProperty)
				registerAbstractProperty((AbstractProperty)property);
		}
	}
	
	private void registerAbstractProperty(AbstractProperty property) throws SchemaMappingException {
		if (property.schema != null && property.schema.schemaMapping == this)
			addSchema(property.schema);
		
		switch (property.getElementType()) {
		case COMPLEX_ATTRIBUTE:
			ComplexAttribute complexAttribute = (ComplexAttribute)property;
			if (complexAttribute.refType != null && complexAttribute.refType.schemaMapping == this)
				addComplexAttributeType(complexAttribute.refType);
			break;
		case COMPLEX_PROPERTY:
			ComplexProperty complexProperty = (ComplexProperty)property;
			if (complexProperty.refType != null && complexProperty.refType.schemaMapping == this)
				addComplexType(complexProperty.refType);
			break;
		case OBJECT_PROPERTY:
			ObjectType objectType = ((ObjectProperty)property).getType();
			if (objectType.schemaMapping == this)
				addObjectType(objectType);
			break;
		case FEATURE_PROPERTY:
			FeatureType featureType = ((FeatureProperty)property).getType();
			if (featureType.schemaMapping == this)
				addFeatureType(featureType);
			break;
		default:
			// nothing to do
		}
	}
	
	public void merge(SchemaMapping other) throws SchemaMappingException {
		isMerged = true;
		
		// remove metadata
		metadata = null;
		
		for (AppSchema schema : other.schemas)
			addSchema(schema);
		
		for (ComplexAttributeType attributeType : other.attributeTypes)
			addComplexAttributeType(attributeType);
		
		for (ComplexType complexType : other.complexTypes)
			addComplexType(complexType);
		
		for (ObjectType objectType : other.objectTypes)
			addObjectType(objectType);

		for (FeatureType featureType : other.featureTypes)
			addFeatureType(featureType);
		
		for (PropertyInjection propertyInjection : other.propertyInjections)
			addPropertyInjection(propertyInjection);
	}

	public void validate() throws SchemaMappingException {
		if (isSetMetadata())
			metadata.validate(this, this);
		
		for (AppSchema schema : schemas) {
			schema.validate(this, this);
			for (Namespace namespace : schema.namespaces)
				uriToSchemaMap.put(namespace.getURI(), schema);
		}

		for (ComplexAttributeType attributeType : attributeTypes)
			attributeType.validate(this, this);

		for (ComplexType complexType : complexTypes)
			complexType.validate(this, this);
		
		for (AbstractObjectType<?> type : getAbstractObjectTypes()) {
			type.validate(this, this);
			
			AbstractObjectType<?> other = checkUniqueObjectClassId(type);
			if (other != null)
				throw new SchemaMappingException("The value " + type.objectClassId + " of the attribute 'objectClassId' is assigned to both '" + type.id + "' and '" + other.id + "'.");
		}

		for (PropertyInjection propertyInjection : propertyInjections)
			propertyInjection.validate(this, this);
	}

}
