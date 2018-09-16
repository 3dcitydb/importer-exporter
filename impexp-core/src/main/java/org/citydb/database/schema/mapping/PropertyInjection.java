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
package org.citydb.database.schema.mapping;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "propertyInjection", propOrder = {
		"join",
		"properties"
})
public class PropertyInjection implements Joinable {
	@XmlAttribute(required = true)
	protected String table;
	@XmlAttribute
	@XmlJavaTypeAdapter(FeatureTypeAdapter.class)
	protected FeatureType defaultBase;
	protected Join join;
	@XmlElements({
		@XmlElement(name = "attribute", type = InjectedSimpleAttribute.class),
		@XmlElement(name = "complexAttribute", type = InjectedComplexAttribute.class),
		@XmlElement(name = "complexProperty", type = InjectedComplexProperty.class),
		@XmlElement(name = "objectProperty", type = InjectedObjectProperty.class),
		@XmlElement(name = "featureProperty", type = InjectedFeatureProperty.class),
		@XmlElement(name = "geometryProperty", type = InjectedGeometryProperty.class),
		@XmlElement(name = "implicitGeometryProperty", type = InjectedImplicitGeometryProperty.class)
	})
	protected List<InjectedProperty> properties;

	protected PropertyInjection() {
		properties = new ArrayList<>();
	}

	public PropertyInjection(String table, Join join) {
		this();
		this.table = table;
		this.join = join;
	}

	public String getTable() {
		return table;
	}

	public boolean isSetTable() {
		return table != null;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public FeatureType getDefaultBase() {
		return defaultBase;
	}

	public boolean isSetDefaultBase() {
		return defaultBase != null;
	}

	public void setDefaultBase(FeatureType defaultBase) {
		this.defaultBase = defaultBase;
	}

	@Override
	public Join getJoin() {
		return join;
	}

	@Override
	public boolean isSetJoin() {
		return join != null;
	}

	public void setJoin(Join join) {
		this.join = join;
	}

	public List<InjectedProperty> getProperties() {
		return new ArrayList<>(properties);
	}

	public boolean isSetProperties() {
		return properties != null && !this.properties.isEmpty();
	}

	public void addProperty(InjectedProperty property) {
		if (property != null)
			properties.add(property);
	}

	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		if (table == null || table.isEmpty())
			throw new SchemaMappingException("A property injection requires a table.");

		if (defaultBase != null && defaultBase.hasLocalProperty(MappingConstants.IS_XLINK)) {
			FeatureType ref = schemaMapping.getFeatureTypeById(defaultBase.getId());
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve feature type reference '" + defaultBase.getId() + "'.");

			defaultBase = ref;
		}

		if (join != null) {
			Join join = (Join)this.join;
			if (!join.table.equals(table))
				throw new SchemaMappingException("The join table '" + join.table + "' does not match the table '" + table + "' of the property injection.");
		} else
			throw new SchemaMappingException("A property injection must define a join relation.");

		for (InjectedProperty injectedProperty : properties) {
			AbstractProperty property = (AbstractProperty)injectedProperty;

			CityGMLContext context = injectedProperty.getContext();
			if (context != null && !property.getSchema().isAvailableForCityGML(context.getCityGMLVersion()))
				throw new SchemaMappingException("The context '" + context + "' of the injected property '" + property.path + "' is not offered by the schema '" + property.getSchema().id + "'.");

			FeatureType base = injectedProperty.getBase();
			if (base != null && base.hasLocalProperty(MappingConstants.IS_XLINK)) {
				FeatureType ref = schemaMapping.getFeatureTypeById(base.getId());
				if (ref == null)
					throw new SchemaMappingException("Failed to resolve feature type reference '" + base.getId() + "'.");
				if (defaultBase != null && ref != defaultBase && !ref.isSubTypeOf(defaultBase))
					throw new SchemaMappingException("The base type '" + ref.id + "' is not a subtype of the default base type '" + defaultBase.id + "'.");	

				base = ref;
			} else if (base == null) {
				if (defaultBase == null)
					throw new SchemaMappingException("No base type provided for the injected property '" + property.path + "'.");

				base = defaultBase;
			}

			injectedProperty.setBase(base);
			injectedProperty.setBaseJoin(join);
			property.validate(schemaMapping, this);

			if (!injectedProperty.getBase().getProperties().contains(property))
				injectedProperty.getBase().addProperty(property);
		}
	}
}
