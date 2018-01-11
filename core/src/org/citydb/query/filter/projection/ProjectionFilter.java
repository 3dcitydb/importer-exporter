package org.citydb.query.filter.projection;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.AppSchema;
import org.citydb.query.filter.FilterException;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.GenericsModule;

public class ProjectionFilter {
	private final AbstractObjectType<?> objectType;
	private final ProjectionMode mode;
	private Set<AbstractProperty> properties;
	private Set<GenericAttribute> genericAttributes;

	private boolean TRUE;
	private boolean FALSE;

	public ProjectionFilter(AbstractObjectType<?> objectType) {
		this(objectType, ProjectionMode.KEEP);
	}

	public ProjectionFilter(AbstractObjectType<?> objectType, ProjectionMode mode) {
		this.objectType = objectType;
		this.mode = mode;

		TRUE = mode == ProjectionMode.KEEP;
		FALSE = !TRUE;
	}

	public ProjectionFilter(ProjectionFilter other) {
		objectType = other.objectType;
		mode = other.mode;
		properties = new HashSet<>(other.properties);
		genericAttributes = new HashSet<>(other.genericAttributes);

		TRUE = other.TRUE;
		FALSE = other.FALSE;
	}

	public void addProperty(AbstractProperty property) {
		if (properties == null)
			properties = new HashSet<>();

		properties.add(property);
	}

	public void addProperty(QName propertyName) {
		AbstractProperty property = objectType.getProperty(propertyName.getLocalPart(), propertyName.getNamespaceURI(), true);
		if (property != null)
			addProperty(property);
	}

	public boolean addGenericAttribute(GenericAttribute genericAttribute) {
		if (genericAttributes == null)
			genericAttributes = new HashSet<>();

		return genericAttributes.add(genericAttribute);
	}

	public boolean addGenericAttribute(String name, CityGMLClass type) throws FilterException {
		return addGenericAttribute(new GenericAttribute(name, type));
	}

	public AbstractObjectType<?> getObjectType() {
		return objectType;
	}

	public ProjectionMode getMode() {
		return mode;
	}

	public boolean containsProperty(AbstractProperty property) {
		return (properties == null || properties.contains(property)) ? TRUE : FALSE;
	}

	public boolean containsProperty(String name, String namespaceURI) {
		if (properties == null && genericAttributes == null)
			return TRUE;

		if (properties != null) {
			for (AbstractProperty property : properties) {
				if (property.getPath().equals(name) 
						&& property.isSetSchema()
						&& property.getSchema().matchesNamespaceURI(namespaceURI))
					return TRUE;
			}
		}

		if (genericAttributes != null) {
			boolean isGenericAttribute = false;
			for (CityGMLModule module : Modules.getCityGMLModules(CityGMLModuleType.GENERICS)) {
				if (module.getNamespaceURI().equals(namespaceURI)) {
					isGenericAttribute = true;
					break;
				}
			}

			if (isGenericAttribute) {
				for (GenericAttribute genericAttribute : genericAttributes) {					
					if (genericAttribute.getType() == CityGMLClass.UNDEFINED)
						return TRUE;

					String attrName = getGenericAttributeName(genericAttribute.getType());
					if (name.equals(attrName))
						return TRUE;
				}
			}
		}

		return FALSE;
	}

	public boolean containsProperty(QName name) {
		return containsProperty(name.getLocalPart(), name.getNamespaceURI());
	}

	public boolean containsProperty(String name, AppSchema schema) {
		if (properties == null && genericAttributes == null)
			return TRUE;

		if (properties != null) {
			for (AbstractProperty property : properties) {
				if (property.getSchema() == schema && property.getPath().equals(name))
					return TRUE;
			}
		}

		if (genericAttributes != null) {
			if (schema.matchesNamespaceURI(GenericsModule.v2_0_0.getNamespaceURI())) {
				for (GenericAttribute genericAttribute : genericAttributes) {					
					if (genericAttribute.getType() == CityGMLClass.UNDEFINED)
						return TRUE;

					String attrName = getGenericAttributeName(genericAttribute.getType());
					if (name.equals(attrName))
						return TRUE;
				}
			}
		}

		return FALSE;
	}

	public boolean containsProperty(String name) {
		return containsProperty(name, objectType.getSchema());
	}

	public boolean containsGenericAttribute(GenericAttribute genericAttribute) {
		return (genericAttributes == null || genericAttributes.contains(genericAttribute)) ? TRUE : FALSE;
	}

	public boolean containsGenericAttribute(String name, CityGMLClass type) {
		if (genericAttributes == null && properties == null)
			return TRUE;			

		if (genericAttributes != null) {
			for (GenericAttribute genericAttribute : genericAttributes) {
				if (genericAttribute.getName().equals(name) 
						&& (genericAttribute.getType() == CityGMLClass.UNDEFINED 
						|| genericAttribute.getType() == type)) 
					return TRUE;
			}
		}

		if (properties != null) {
			String attrName = getGenericAttributeName(type);
			for (AbstractProperty property : properties) {
				if (property.getPath().equals(attrName) 
						&& property.getSchema().matchesNamespaceURI(GenericsModule.v2_0_0.getNamespaceURI()))
					return TRUE;
			}
		}

		return FALSE;
	}

	private String getGenericAttributeName(CityGMLClass type) {
		switch (type) {
		case STRING_ATTRIBUTE: return "stringAttribute";
		case INT_ATTRIBUTE: return "intAttribute";
		case DOUBLE_ATTRIBUTE: return "doubleAttribute";
		case URI_ATTRIBUTE: return "uriAttribute";
		case DATE_ATTRIBUTE: return "dateAttribute";
		case MEASURE_ATTRIBUTE: return "measureAttribute";
		case GENERIC_ATTRIBUTE_SET: return "genericAttributeSet";
		default: return "";
		}
	}

}
