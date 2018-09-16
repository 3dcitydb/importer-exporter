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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "injectedGeometryProperty")
public class InjectedGeometryProperty extends GeometryProperty implements InjectedProperty {
	@XmlAttribute
	@XmlJavaTypeAdapter(FeatureTypeAdapter.class)
    protected FeatureType base;
	@XmlAttribute
    protected CityGMLContext context;
	
	@XmlTransient
	protected Join baseJoin;
	
	protected InjectedGeometryProperty() {
	}
    
    public InjectedGeometryProperty(String path, GeometryType type, AppSchema schema) {
    	super(path, type, schema);
    }
	
	@Override
	public FeatureType getBase() {
		return base;
	}

	@Override
	public boolean isSetBase() {
		return base != null;
	}

	@Override
	public void setBase(FeatureType value) {
		this.base = value;
	}

	@Override
    public CityGMLContext getContext() {
		return context;
	}
	
	@Override
	public boolean isSetContext() {
		return context != null;
	}
	
	@Override
	public void setContext(CityGMLContext context) {
		this.context = context;
	}

	@Override
	public Join getBaseJoin() {
		return baseJoin;
	}

	@Override
	public boolean isSetBaseJoin() {
		return baseJoin != null;
	}

	@Override
	public void setBaseJoin(Join basejoin) {
		this.baseJoin = basejoin;
	}

}
