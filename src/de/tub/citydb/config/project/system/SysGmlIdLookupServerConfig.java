package de.tub.citydb.config.project.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="GmlIdLookupServerConfigType", propOrder={
		"cacheSize",
		"pageFactor",
		"partitions"
})
public class SysGmlIdLookupServerConfig {
	@XmlSchemaType(name="positiveInteger")
	@XmlElement(required=true, defaultValue="200000")
	private Integer cacheSize = 200000;
	@XmlElement(required=true, defaultValue="0.85")
	private Float pageFactor = 0.85f;
	@XmlElement(required=true, defaultValue="50")
	private Integer partitions = 10;
	
	public SysGmlIdLookupServerConfig() {
	}

	public Integer getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(Integer cacheSize) {
		this.cacheSize = cacheSize;
	}

	public Float getPageFactor() {
		return pageFactor;
	}

	public void setPageFactor(Float pageFactor) {
		this.pageFactor = pageFactor;
	}

	public Integer getPartitions() {
		return partitions;
	}

	public void setPartitions(Integer concurrentTempTables) {
		this.partitions = concurrentTempTables;
	}
	
}
