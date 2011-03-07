package de.tub.citydb.config.project.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SystemType", propOrder={
		"gmlIdLookupServer",
		"threadPool"
})
public class System {
	@XmlElement(required=true)
	private SysGmlIdLookupServer gmlIdLookupServer;
	@XmlElement(required=true)
	private SysThreadPool threadPool;

	public System() {
		gmlIdLookupServer = new SysGmlIdLookupServer();
		threadPool = new SysThreadPool();
	}

	public SysGmlIdLookupServer getGmlIdLookupServer() {
		return gmlIdLookupServer;
	}

	public void setGmlIdLookupServer(SysGmlIdLookupServer gmlIdLookupServer) {
		if (gmlIdLookupServer != null)
			this.gmlIdLookupServer = gmlIdLookupServer;
	}

	public SysThreadPool getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(SysThreadPool threadPool) {
		if (threadPool != null)
			this.threadPool = threadPool;
	}


}
