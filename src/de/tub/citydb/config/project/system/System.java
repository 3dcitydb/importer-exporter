package de.tub.citydb.config.project.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SystemType", propOrder={
		"gmlIdLookupServer",
		"threadPool"
})
public class System {
	@XmlElement(required=true)
	private GmlIdLookupServer gmlIdLookupServer;
	@XmlElement(required=true)
	private ThreadPool threadPool;

	public System() {
		gmlIdLookupServer = new GmlIdLookupServer();
		threadPool = new ThreadPool();
	}

	public GmlIdLookupServer getGmlIdLookupServer() {
		return gmlIdLookupServer;
	}

	public void setGmlIdLookupServer(GmlIdLookupServer gmlIdLookupServer) {
		if (gmlIdLookupServer != null)
			this.gmlIdLookupServer = gmlIdLookupServer;
	}

	public ThreadPool getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(ThreadPool threadPool) {
		if (threadPool != null)
			this.threadPool = threadPool;
	}


}
