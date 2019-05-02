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
package org.citydb.config.project.query.filter.projection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import java.util.LinkedHashSet;
import java.util.Objects;

@XmlType(name = "ProjectionContextType")
public class ProjectionContext {
	@XmlAttribute(required = true)
	private QName typeName;
	@XmlAttribute(name="mode")
	private ProjectionMode mode;
	@XmlElements({
        @XmlElement(name = "propertyName", type = PropertyName.class),
        @XmlElement(name = "genericAttributeName", type = GenericAttributeName.class)
    })
	private LinkedHashSet<AbstractPropertyName> propertyNames;

	public QName getTypeName() {
		return typeName;
	}

	public void setTypeName(QName typeName) {
		this.typeName = typeName;
	}

	public ProjectionMode getMode() {
		return mode != null ? mode : ProjectionMode.KEEP;
	}

	public void setMode(ProjectionMode mode) {
		this.mode = mode;
	}

	public LinkedHashSet<AbstractPropertyName> getPropertyNames() {
		return propertyNames;
	}

	public void setPropertyNames(LinkedHashSet<AbstractPropertyName> propertyNames) {
		this.propertyNames = propertyNames;
	}
	
	public boolean isSetPropertyNames() {
		return propertyNames != null && !propertyNames.isEmpty();
	}

	@Override
	public final boolean equals(Object obj) {
		if (!(obj instanceof ProjectionContext))
			return false;
		
		return typeName != null && typeName.equals(((ProjectionContext)obj).typeName);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(typeName);
	}
	
}
