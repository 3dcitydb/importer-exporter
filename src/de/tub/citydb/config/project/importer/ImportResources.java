package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.resources.Resources;
import de.tub.citydb.config.project.resources.UIDCacheConfig;

@XmlType(name="ImportResourcesType", propOrder={
		"texImageCache"
})
public class ImportResources extends Resources {
	@XmlElement(required=true)
	private UIDCacheConfig texImageCache;
	
	public ImportResources() {
		texImageCache = new UIDCacheConfig();
	}

	public UIDCacheConfig getTexImageCache() {
		return texImageCache;
	}

	public void setTexImageCache(UIDCacheConfig texImageCache) {
		this.texImageCache = texImageCache;
	}
}
