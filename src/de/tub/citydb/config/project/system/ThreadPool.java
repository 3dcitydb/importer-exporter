package de.tub.citydb.config.project.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ThreadPoolType", propOrder={
		"defaultPool"		
})
public class ThreadPool {
	@XmlElement(name="default", required=true)
	private ThreadPoolConfig defaultPool;

	public ThreadPool() {
		defaultPool = new ThreadPoolConfig();
	}

	public ThreadPoolConfig getDefaultPool() {
		return defaultPool;
	}

	public void setDefaultPool(ThreadPoolConfig defaultPool) {
		if (defaultPool != null)
			this.defaultPool = defaultPool;
	}
	
	
}
