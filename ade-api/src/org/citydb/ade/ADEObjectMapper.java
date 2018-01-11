package org.citydb.ade;

import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.module.citygml.CityGMLVersion;

public interface ADEObjectMapper {
	public AbstractGML createObject(int objectClassId, CityGMLVersion version);
	public int getObjectClassId(Class<? extends AbstractGML> adeObjectClass);
}
