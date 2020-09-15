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
package org.citydb.citygml.exporter.database.content;

import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class SurfaceGeometry {
	private final AbstractGeometry geometry;
	private final String reference;
	private final GMLClass type;

	public SurfaceGeometry(AbstractGeometry geometry) {
		this.geometry = geometry;
		type = geometry.getGMLClass();
		reference = null;
	}

	public SurfaceGeometry(String reference, GMLClass type) {
		this.reference = reference;
		this.type = type;
		geometry = null;
	}
	
	public boolean isSetGeometry() {
		return geometry != null;
	}

	public AbstractGeometry getGeometry() {
		return geometry;
	}

	public boolean isSetReference() {
		return reference != null;
	}
	
	public String getReference() {
		return reference;
	}

	public GMLClass getType() {
		return type;
	}

	public <S extends AbstractGeometry, T extends GeometryProperty<S>> T fill(T property) {
		if (property.getAssociableClass().isInstance(geometry))
			property.setGeometry(property.getAssociableClass().cast(geometry));
		else if (reference != null)
			property.setHref(reference);

		return property;
	}
}
