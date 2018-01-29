package org.citydb.query.filter.projection;

import java.util.EnumSet;
import java.util.Objects;

import org.citydb.query.filter.FilterException;
import org.citygml4j.model.citygml.CityGMLClass;

public class GenericAttribute {
	private final String name;
	private final CityGMLClass type;
	
	private final EnumSet<CityGMLClass> types = EnumSet.of(
			CityGMLClass.STRING_ATTRIBUTE,
			CityGMLClass.DOUBLE_ATTRIBUTE,
			CityGMLClass.INT_ATTRIBUTE,
			CityGMLClass.DATE_ATTRIBUTE,
			CityGMLClass.URI_ATTRIBUTE,
			CityGMLClass.MEASURE_ATTRIBUTE,
			CityGMLClass.GENERIC_ATTRIBUTE_SET,
			CityGMLClass.UNDEFINED);
	
	public GenericAttribute(String name, CityGMLClass type) throws FilterException {
		if (type == null)
			type = CityGMLClass.UNDEFINED;
		
		if (!types.contains(type))
			throw new FilterException(type + " is not a valid generic attribute type.");
		
		this.name = name;
		this.type = type;
	}
	
	public GenericAttribute(String name) throws FilterException {
		this(name, CityGMLClass.UNDEFINED);
	}

	public String getName() {
		return name;
	}

	public CityGMLClass getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof GenericAttribute))
			return false;

		GenericAttribute other = (GenericAttribute)obj;
		return name.equals(other.name) && type == other.type;
	}
	
}
