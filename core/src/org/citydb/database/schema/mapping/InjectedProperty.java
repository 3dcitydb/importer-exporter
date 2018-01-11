package org.citydb.database.schema.mapping;

public interface InjectedProperty {
	public FeatureType getBase();
	public boolean isSetBase();
	public void setBase(FeatureType value);
	public CityGMLContext getContext();
	public boolean isSetContext();
	public void setContext(CityGMLContext context);
	public Join getBaseJoin();
	public boolean isSetBaseJoin();
	public void setBaseJoin(Join join);
}
