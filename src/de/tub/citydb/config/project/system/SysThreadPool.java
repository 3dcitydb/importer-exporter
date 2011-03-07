package de.tub.citydb.config.project.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ThreadPoolType", propOrder={
		"defaultPool"		
})
public class SysThreadPool {
	@XmlElement(name="default", required=true)
	private SysThreadPoolConfig defaultPool;

	public SysThreadPool() {
		defaultPool = new SysThreadPoolConfig();
	}

	public SysThreadPoolConfig getDefaultPool() {
		return defaultPool;
	}

	public void setDefaultPool(SysThreadPoolConfig defaultPool) {
		if (defaultPool != null)
			this.defaultPool = defaultPool;
	}
	
	
}
