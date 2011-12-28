package de.tub.citydb.api.io;

import java.net.Proxy;

public interface ProxyConfig {
	public boolean isEnabled();
	public boolean requiresAuthentication();
	public ProxyType getType();
	public String getHost();
	public int getPort();
	public String getUsername();
	public String getPassword();
	public Proxy toProxy();
}
