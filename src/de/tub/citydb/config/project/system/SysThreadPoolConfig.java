package de.tub.citydb.config.project.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ThreadPoolConfigType", propOrder={
		"minThreads",
		"maxThreads"		
})
public class SysThreadPoolConfig {
	@XmlElement(required=true, defaultValue="4")
	@XmlSchemaType(name="positiveInteger")
	private Integer minThreads = 4;
	@XmlElement(required=true, defaultValue="4")
	@XmlSchemaType(name="positiveInteger")
	private Integer maxThreads = 4;
	
	public SysThreadPoolConfig() {
	}

	public Integer getMinThreads() {
		return minThreads;
	}

	public void setMinThreads(Integer minThreads) {
		this.minThreads = minThreads;
	}

	public Integer getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(Integer maxThreads) {
		this.maxThreads = maxThreads;
	}
	
}
