package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "injectedFeatureProperty")
public class InjectedFeatureProperty extends FeatureProperty implements InjectedProperty {
	@XmlAttribute
	@XmlJavaTypeAdapter(FeatureTypeAdapter.class)
    protected FeatureType base;
	@XmlAttribute
    protected CityGMLContext context;
	
	@XmlTransient
	protected Join baseJoin;
	
	protected InjectedFeatureProperty() {
	}
    
    public InjectedFeatureProperty(String path, FeatureType type, AppSchema schema) {
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
	public void setBaseJoin(Join baseJoin) {
		this.baseJoin = baseJoin;
	}

}
