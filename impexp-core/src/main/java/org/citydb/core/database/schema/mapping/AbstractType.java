/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlTransient
public abstract class AbstractType<T extends AbstractType<T>> extends AbstractPathElement {
    @XmlAttribute(name = "abstract")
    protected Boolean _abstract;
    @XmlElements({
            @XmlElement(name = "attribute", type = SimpleAttribute.class),
            @XmlElement(name = "complexAttribute", type = ComplexAttribute.class),
            @XmlElement(name = "complexProperty", type = ComplexProperty.class),
            @XmlElement(name = "objectProperty", type = ObjectProperty.class),
            @XmlElement(name = "featureProperty", type = FeatureProperty.class),
            @XmlElement(name = "geometryProperty", type = GeometryProperty.class),
            @XmlElement(name = "implicitGeometryProperty", type = ImplicitGeometryProperty.class)
    })
    protected List<AbstractProperty> properties;

    @XmlTransient
    protected SchemaMapping schemaMapping;

    protected AbstractType() {
        properties = new ArrayList<>();
    }

    public AbstractType(String path, AppSchema schema, SchemaMapping schemaMapping) {
        super(path, schema);
        this.schemaMapping = schemaMapping;
        properties = new ArrayList<>();
    }

    public boolean isAbstract() {
        return _abstract == null ? false : _abstract.booleanValue();
    }

    public boolean isSetAbstract() {
        return _abstract != null;
    }

    public void setAbstract(boolean _abstract) {
        this._abstract = _abstract ? true : null;
    }

    public abstract String getId();

    public abstract boolean isSetId();

    public abstract void setId(String id);

    public abstract String getTable();

    public abstract boolean isSetTable();

    public abstract void setTable(String table);

    public abstract int getObjectClassId();

    public abstract boolean isSetObjectClass();

    public abstract void setObjectClassId(int objectClassId);

    public abstract AbstractExtension<T> getExtension();

    public abstract boolean isSetExtension();

    public abstract void setExtension(AbstractExtension<T> extension);

    public abstract List<T> listSubTypes(boolean skipAbstractTypes);

    @SuppressWarnings("unchecked")
    public List<T> listSuperTypes(boolean includeType) {
        List<T> superTypes = new ArrayList<T>();
        T type = (T) this;
        if (includeType)
            superTypes.add(type);

        while (type.isSetExtension()) {
            type = type.getExtension().getBase();
            superTypes.add(type);
        }

        return superTypes;
    }

    public boolean isSubTypeOf(AbstractType<?> superType) {
        if (!isSetExtension())
            return false;

        T parent = getExtension().getBase();
        while (parent != null) {
            if (parent == superType)
                return true;

            parent = parent.isSetExtension() ? parent.getExtension().getBase() : null;
        }

        return false;
    }

    public boolean isEqualToOrSubTypeOf(AbstractType<?> superType) {
        return this == superType || isSubTypeOf(superType);
    }

    protected List<T> listSubTypes(List<T> candidates, boolean skipAbstractTypes) {
        List<T> result = new ArrayList<T>();
        for (T candidate : candidates) {
            if (skipAbstractTypes && candidate.isAbstract())
                continue;

            if (candidate.isSubTypeOf(this))
                result.add(candidate);
        }

        return result;
    }

    public List<AbstractProperty> getProperties() {
        return new ArrayList<>(properties);
    }

    public List<AbstractProperty> listProperties(boolean onlyQueryable, boolean includeInherited) {
        List<AbstractProperty> result = new ArrayList<AbstractProperty>();
        AbstractType<?> type = this;

        while (type != null) {
            for (AbstractProperty property : type.properties) {
                if (!onlyQueryable || property.isQueryable())
                    result.add(property);
            }

            type = includeInherited && type.isSetExtension() ? type.getExtension().getBase() : null;
        }

        return result;
    }

    public AbstractProperty getProperty(String name, String namespaceURI, boolean includeInherited) {
        for (AbstractProperty property : listProperties(false, includeInherited)) {
            if (property.getSchema().matchesNamespaceURI(namespaceURI)) {
                String path = property.getPath();
                if (path.startsWith("@"))
                    path = path.substring(1, path.length());

                if (path.equals(name))
                    return property;
            }
        }

        return null;
    }

    public FeatureProperty getFeatureProperty(String name, String namespaceURI, boolean includeInherited) {
        AbstractProperty property = getProperty(name, namespaceURI, includeInherited);
        return property != null && property.getElementType() == PathElementType.FEATURE_PROPERTY ? (FeatureProperty) property : null;
    }

    public boolean isSetProperties() {
        return properties != null && !properties.isEmpty();
    }

    public void addProperty(AbstractProperty property) {
        if (property == null)
            return;

        if (property instanceof SimpleAttribute)
            ((SimpleAttribute) property).setParentType(this);

        properties.add(property);
    }

    @Override
    protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
        super.validate(schemaMapping, parent);
        this.schemaMapping = schemaMapping;

        for (AbstractProperty property : properties)
            property.validate(schemaMapping, this);
    }

}
