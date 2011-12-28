package de.tub.citydb.api.registry;

import java.util.concurrent.ConcurrentHashMap;

import de.tub.citydb.api.controller.DatabaseController;
import de.tub.citydb.api.controller.IOController;
import de.tub.citydb.api.controller.LogController;
import de.tub.citydb.api.controller.PluginConfigController;
import de.tub.citydb.api.controller.ViewController;
import de.tub.citydb.api.event.EventDispatcher;

public class ObjectRegistry {
	private static final ObjectRegistry instance = new ObjectRegistry();

	private ConcurrentHashMap<String, Object> registry;
	private EventDispatcher eventDispatcher;
	private ViewController viewController;
	private DatabaseController databaseController;
	private LogController logController;
	private PluginConfigController pluginConfigController;
	private IOController ioController;

	private ObjectRegistry() {
		// just to thwart instantiation
	}

	public static ObjectRegistry getInstance() {
		return instance;
	}

	public void register(String name, Object object) {
		if (registry == null)
			registry = new ConcurrentHashMap<String, Object>();

		registry.put(name, object);
	}

	public void unregister(String name) {
		registry.remove(name);
	}

	public Object lookup(String name) {
		return registry.get(name);
	}

	public EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	public void setEventDispatcher(EventDispatcher eventDispatcher) {
		if (this.eventDispatcher != null)
			throw new IllegalArgumentException("Event dispatcher is already registered with the object registry.");

		this.eventDispatcher = eventDispatcher;
	}

	public ViewController getViewController() {
		return viewController;
	}

	public void setViewController(ViewController viewController) {
		if (this.viewController != null)
			throw new IllegalArgumentException("View controller is already registered with the object registry.");

		this.viewController = viewController;
	}

	public DatabaseController getDatabaseController() {
		return databaseController;
	}

	public void setDatabaseController(DatabaseController databaseController) {
		if (this.databaseController != null)
			throw new IllegalArgumentException("Database controller is already registered with the object registry.");

		this.databaseController = databaseController;
	}

	public LogController getLogController() {
		return logController;
	}

	public void setLogController(LogController logController) {
		if (this.logController != null)
			throw new IllegalArgumentException("Log controller is already registered with the object registry.");

		this.logController = logController;
	}

	public PluginConfigController getPluginConfigController() {
		return pluginConfigController;
	}

	public void setPluginConfigController(PluginConfigController pluginConfigController) {
		if (this.pluginConfigController != null)
			throw new IllegalArgumentException("Plugin config controller is already registered with the object registry.");

		this.pluginConfigController = pluginConfigController;
	}

	public IOController getIOController() {
		return ioController;
	}

	public void setIOController(IOController ioController) {
		if (this.ioController != null)
			throw new IllegalArgumentException("I/O controller is already registered with the object registry.");

		this.ioController = ioController;
	}

}
