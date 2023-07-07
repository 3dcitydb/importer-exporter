/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.core.plugin;

import org.citydb.config.Config;
import org.citydb.config.project.plugin.PluginConfig;
import org.citydb.core.plugin.extension.Extension;
import org.citydb.core.plugin.extension.config.ConfigExtension;
import org.citydb.core.plugin.internal.InternalPlugin;
import org.citydb.core.plugin.metadata.PluginMetadata;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.log.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.*;

public class PluginManager {
    private static PluginManager instance;
    private final Logger log = Logger.getInstance();
    private final List<InternalPlugin> internalPlugins = new ArrayList<>();
    private final List<Plugin> externalPlugins = new ArrayList<>();
    private final List<CliCommand> commands = new ArrayList<>();
    private Map<String, List<PluginException>> exceptions;

    public static synchronized PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }

        return instance;
    }

    public void loadPlugins(ClassLoader loader) {
        JAXBContext context = createJAXBContext();
        ServiceLoader<Plugin> pluginLoader = ServiceLoader.load(Plugin.class, loader);

        List<Plugin> plugins = new ArrayList<>();
        try {
            for (Plugin plugin : pluginLoader) {
                plugins.add(plugin);
            }
        } catch (NoClassDefFoundError e) {
            log.error("Failed to load plugins.", e);
            return;
        }

        for (Plugin plugin : plugins) {
            registerExternalPlugin(plugin, context);
        }
    }

    public void registerInternalPlugin(InternalPlugin plugin) {
        for (Plugin internalPlugin : internalPlugins) {
            if (internalPlugin.getClass() == plugin.getClass()) {
                return;
            }
        }

        internalPlugins.add(plugin);
    }

    public void registerExternalPlugin(Plugin plugin) {
        registerExternalPlugin(plugin, createJAXBContext());
    }

    private void registerExternalPlugin(Plugin plugin, JAXBContext context) {
        try {
            // skip plugin if it already failed to load
            if (exceptions != null && exceptions.containsKey(plugin.getClass().getName())) {
                return;
            }

            for (Plugin externalPlugin : externalPlugins) {
                if (externalPlugin.getClass() == plugin.getClass()) {
                    return;
                }
            }

            if (context != null) {
                try {
                    Unmarshaller unmarshaller = context.createUnmarshaller();
                    Object object = unmarshaller.unmarshal(plugin.getClass().getResource("plugin.xml"));
                    if (object instanceof PluginMetadata) {
                        plugin.setMetadata((PluginMetadata) object);
                    }
                } catch (Exception e) {
                    throw new PluginException("Failed to load plugin metadata from plugin.xml.", e);
                }
            }

            plugin.validate();

            externalPlugins.add(plugin);
        } catch (PluginException e) {
            addException(plugin, e);
        } catch (Throwable e) {
            addException(plugin, new PluginException("Unexpected error while loading the plugin.", e));
        }
    }

    public void setPluginsEnabled(Map<Plugin, Boolean> pluginStates) {
        List<Plugin> plugins = new ArrayList<>();
        for (Map.Entry<Plugin, Boolean> entry : pluginStates.entrySet()) {
            Plugin plugin = entry.getKey();
            Boolean enabled = entry.getValue();
            if (enabled != plugin.isEnabled() && !(plugin instanceof InternalPlugin)) {
                plugin.setEnabled(enabled);
                plugins.add(plugin);
            }
        }

        if (!plugins.isEmpty()) {
            ObjectRegistry.getInstance().getEventDispatcher().triggerEvent(new PluginStateEvent(plugins));
        }
    }

    public void setPluginEnabled(Plugin plugin, boolean enabled) {
        setPluginsEnabled(Collections.singletonMap(plugin, enabled));
    }

    public List<InternalPlugin> getInternalPlugins() {
        return internalPlugins;
    }

    public List<Plugin> getExternalPlugins() {
        return externalPlugins;
    }

    public <T extends InternalPlugin> T getInternalPlugin(Class<T> type) {
        for (InternalPlugin plugin : internalPlugins) {
            if (type.isInstance(plugin)) {
                return type.cast(plugin);
            }
        }

        return null;
    }

    public <T extends Extension> List<T> getExternalPlugins(Class<T> type) {
        List<T> plugins = new ArrayList<>();
        for (Plugin plugin : externalPlugins) {
            if (type.isAssignableFrom(plugin.getClass())) {
                plugins.add(type.cast(plugin));
            }
        }

        return plugins;
    }

    public <T extends Extension> List<T> getEnabledExternalPlugins(Class<T> type) {
        List<T> plugins = getExternalPlugins(type);
        plugins.removeIf(p -> !((Plugin) p).isEnabled());
        return plugins;
    }

    public List<Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<>(externalPlugins);
        plugins.addAll(internalPlugins);
        return plugins;
    }

    public void loadCliCommands(ClassLoader loader) {
        ServiceLoader<CliCommand> commandLoader = ServiceLoader.load(CliCommand.class, loader);
        for (CliCommand command : commandLoader) {
            registerCliCommand(command);
        }
    }

    public void registerCliCommand(CliCommand command) {
        for (CliCommand candidate : commands) {
            if (candidate.getClass() == command.getClass()) {
                return;
            }
        }

        commands.add(command);
    }

    public List<CliCommand> getCliCommands() {
        return commands;
    }

    @SuppressWarnings("unchecked")
    public <T extends PluginConfig> Class<T> getConfigClass(ConfigExtension<T> plugin) throws PluginException {
        try {
            return (Class<T>) plugin.getClass().getMethod("getConfig").getReturnType();
        } catch (Exception e) {
            throw new PluginException("Failed to determine config type of plugin " + plugin.getClass().getName() + ".", e);
        }
    }

    public <T extends PluginConfig> void propagatePluginConfig(ConfigExtension<T> plugin, Config config) throws PluginException {
        Class<T> type = getConfigClass(plugin);
        T pluginConfig = config.getPluginConfig(type);

        if (pluginConfig == null) {
            try {
                pluginConfig = type.getDeclaredConstructor().newInstance();
                config.registerPluginConfig(pluginConfig);
            } catch (Exception e) {
                throw new PluginException("Failed to invoke default constructor of " + type.getName() + ".", e);
            }
        }

        plugin.configLoaded(pluginConfig);
    }

    public boolean hasExceptions() {
        return exceptions != null;
    }

    public void logExceptions() {
        if (exceptions != null) {
            for (Map.Entry<String, List<PluginException>> entry : exceptions.entrySet()) {
                log.error("Failed to initialize the plugin " + entry.getKey());
                for (PluginException e : entry.getValue()) {
                    log.error("Caused by: " + e.getMessage(), e.getCause());
                }
            }
        }
    }

    public Map<String, List<PluginException>> getExceptions() {
        return exceptions;
    }

    private void addException(Plugin plugin, PluginException exception) {
        if (exceptions == null) {
            exceptions = new HashMap<>();
        }

        exceptions.computeIfAbsent(plugin.getClass().getName(), k -> new ArrayList<>())
                .add(exception);
    }

    private JAXBContext createJAXBContext() {
        try {
            return JAXBContext.newInstance(PluginMetadata.class);
        } catch (Exception e) {
            return null;
        }
    }
}
