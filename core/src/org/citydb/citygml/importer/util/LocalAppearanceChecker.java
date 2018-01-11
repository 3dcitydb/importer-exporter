package org.citydb.citygml.importer.util;

import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.util.walker.FeatureFunctionWalker;

public class LocalAppearanceChecker {
	private final FeatureFunctionWalker<Boolean> walker;
	
	public LocalAppearanceChecker() {
		walker = new FeatureFunctionWalker<Boolean>() {
			public Boolean apply(ParameterizedTexture parameterizedTexture) {
				return true;
			}
		};
	}
	
	public boolean hasLocalAppearance(AbstractCityObject cityObject) {
		return cityObject.accept(walker) != null;
	}
	
}
