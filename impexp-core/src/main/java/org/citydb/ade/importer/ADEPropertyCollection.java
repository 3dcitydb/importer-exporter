package org.citydb.ade.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.citygml4j.model.citygml.ade.binding.ADEModelObject;

public class ADEPropertyCollection {
	private HashMap<Class<? extends ADEModelObject>, List<ADEModelObject>> properties = new HashMap<>();
	
	public void register(ADEModelObject property) {
		List<ADEModelObject> properties = this.properties.get(property.getClass());
		if (properties == null) {
			properties = new ArrayList<>();
			this.properties.put(property.getClass(), properties);
		}
		
		properties.add(property);
	}
	
	public boolean containsOneOf(Class<?>... propertyTypes) {
		for (Class<?> propertyType : propertyTypes) {
			if (properties.containsKey(propertyType))
				return true;
		}
		
		return false;
	}
	
	public boolean contains(Class<? extends ADEModelObject> propertyType) {
		return properties.containsKey(propertyType);
	}
	
	public boolean containsOne(Class<? extends ADEModelObject> propertyType) {
		List<ADEModelObject> properties = this.properties.get(propertyType);
		return properties != null && properties.size() == 1;
	}
	
	public boolean containsMultiple(Class<? extends ADEModelObject> propertyType) {
		List<ADEModelObject> properties = this.properties.get(propertyType);
		return properties != null && properties.size() > 1;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ADEModelObject> List<T> getAll(Class<T> propertyType) {
		List<ADEModelObject> properties = this.properties.get(propertyType);
		return properties != null ? (List<T>)properties : Collections.emptyList();
	}
	
	public <T extends ADEModelObject> T getFirst(Class<T> propertyType) {
		List<ADEModelObject> properties = this.properties.get(propertyType);
		if (properties != null && !properties.isEmpty())
			return propertyType.cast(properties.get(0));
		
		return null;
	}

}
