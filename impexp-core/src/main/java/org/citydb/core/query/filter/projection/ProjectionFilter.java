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
package org.citydb.core.query.filter.projection;

import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.database.schema.mapping.AbstractProperty;
import org.citydb.core.database.schema.mapping.AppSchema;
import org.citydb.core.query.filter.FilterException;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.GenericsModule;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

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

    ProjectionFilter(AbstractObjectType<?> objectType, ProjectionMode mode, Set<AbstractProperty> properties, Set<GenericAttribute> genericAttributes) {
        this(objectType, mode);

        if (properties != null)
            this.properties = new HashSet<>(properties);

        if (genericAttributes != null)
            this.genericAttributes = new HashSet<>(genericAttributes);
    }

    public AbstractObjectType<?> getObjectType() {
        return objectType;
    }

    public ProjectionMode getMode() {
        return mode;
    }

    Set<AbstractProperty> getProperties() {
        return properties;
    }

    Set<GenericAttribute> getGenericAttributes() {
        return genericAttributes;
    }

    public void addProperty(AbstractProperty property) {
        if (properties == null)
            properties = new HashSet<>();

        properties.add(property);
    }

    public void addProperty(QName propertyName) throws FilterException {
        AbstractProperty property = objectType.getProperty(propertyName.getLocalPart(), propertyName.getNamespaceURI(), true);
        if (property == null)
            throw new FilterException("'" + propertyName + "' is not a valid property of " + objectType + ".");

        addProperty(property);
    }

    void addProperties(Set<AbstractProperty> properties) {
        if (properties != null) {
            if (this.properties != null)
                this.properties.addAll(properties);
            else
                this.properties = new HashSet<>(properties);
        }
    }

    public boolean addGenericAttribute(GenericAttribute genericAttribute) {
        if (genericAttributes == null)
            genericAttributes = new HashSet<>();

        return genericAttributes.add(genericAttribute);
    }

    public boolean addGenericAttribute(String name, CityGMLClass type) throws FilterException {
        return addGenericAttribute(new GenericAttribute(name, type));
    }

    void addGenericAttributes(Set<GenericAttribute> genericAttributes) {
        if (genericAttributes != null) {
            if (this.genericAttributes != null)
                this.genericAttributes.addAll(genericAttributes);
            else
                this.genericAttributes = new HashSet<>(genericAttributes);
        }
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
            case STRING_ATTRIBUTE:
                return "stringAttribute";
            case INT_ATTRIBUTE:
                return "intAttribute";
            case DOUBLE_ATTRIBUTE:
                return "doubleAttribute";
            case URI_ATTRIBUTE:
                return "uriAttribute";
            case DATE_ATTRIBUTE:
                return "dateAttribute";
            case MEASURE_ATTRIBUTE:
                return "measureAttribute";
            case GENERIC_ATTRIBUTE_SET:
                return "genericAttributeSet";
            default:
                return "";
        }
    }

}
