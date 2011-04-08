package de.tub.citydb.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceConfigurationError;

public class PluginServiceFactory {
	private static ClassLoader loader;
	
	public static void addPluginDirectory(File directory) throws IOException {
		if (loader == null)
			loader = new ClassLoader();
		
		if (directory.exists())
			for (File file : directory.listFiles())
				if (file.getName().toLowerCase().endsWith(".jar"))
					loader.addURL(file.toURI().toURL());
	}

	public static PluginService createPluginService() throws ServiceConfigurationError {
		return DefaultPluginService.getInstance(loader);
	}

	private static class ClassLoader extends URLClassLoader {
		protected ClassLoader() {
			super(new URL[]{});
		}

		@Override
		protected void addURL(URL url) {
			super.addURL(url);
		}

	}
}
