package de.tub.citydb.config.project.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ThreadPoolConfigType", propOrder={
		"minThreads",
		"maxThreads"		
})
public class ThreadPoolConfig {
	@XmlElement(required=true)
	@XmlSchemaType(name="positiveInteger")
	private Integer minThreads;
	@XmlElement(required=true)
	@XmlSchemaType(name="positiveInteger")
	private Integer maxThreads;
	
	public ThreadPoolConfig() {
		minThreads = maxThreads = Runtime.getRuntime().availableProcessors() * 2;
	}

	public Integer getMinThreads() {
		return minThreads;
	}

	public void setMinThreads(Integer minThreads) {
		if (minThreads != null && minThreads > 0)
			this.minThreads = minThreads;
	}

	public Integer getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(Integer maxThreads) {
		if (maxThreads != null && maxThreads > 0)
			this.maxThreads = maxThreads;
	}
	
}
