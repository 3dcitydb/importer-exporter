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
package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.geometry.BoundingBox;

@XmlRootElement(name="bbox")
@XmlType(name="BBOXOperatorType", propOrder={
		"operand"
})
public class BBOXOperator extends AbstractSpatialOperator {	
	@XmlElementRefs({
		@XmlElementRef(type=FileReference.class),
		@XmlElementRef(type=BoundingBox.class)
	})
	private Object operand;
	
	public boolean isSetFileReference() {
		return operand instanceof FileReference;
	}

	public String getFileReference() {
		return isSetFileReference() ? ((FileReference)operand).getValue() : null;
	}

	public void setFileReference(String file) {
		this.operand = file;
	}

	public boolean isSetEnvelope() {
		return operand instanceof BoundingBox;
	}

	public BoundingBox getEnvelope() {
		return isSetEnvelope() ? (BoundingBox)operand : null;
	}

	public void setEnvelope(BoundingBox operand) {
		this.operand = operand;
	}
	
	@Override
	public void reset() {
		operand = null;
		super.reset();
	}
	
	@Override
	public SpatialOperatorName getOperatorName() {
		return SpatialOperatorName.BBOX;
	}
	
}
