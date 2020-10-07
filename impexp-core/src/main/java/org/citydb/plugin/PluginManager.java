/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.plugin;

import org.citydb.plugin.extension.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class PluginManager {
    private static PluginManager instance;
    private final List<InternalPlugin> internalPlugins;
    private final List<Plugin> externalPlugins;
    private final List<CLICommand> commands;

    private PluginManager() {
        internalPlugins = new ArrayList<>();
        externalPlugins = new ArrayList<>();
        commands = new ArrayList<>();
    }

    public static synchronized PluginManager getInstance() {
        if (instance == null)
            instance = new PluginManager();

        return instance;
    }

    public void loadPlugins(ClassLoader loader) {
        ServiceLoader<Plugin> pluginLoader = ServiceLoader.load(Plugin.class, loader);
        for (Plugin plugin : pluginLoader)
            registerExternalPlugin(plugin);
    }

    public void registerInternalPlugin(InternalPlugin plugin) {
        for (Plugin candidate : internalPlugins) {
            if (candidate.getClass() == plugin.getClass())
                return;
        }

        internalPlugins.add(plugin);
    }

    public void registerExternalPlugin(Plugin plugin) {
        for (Plugin candidate : externalPlugins) {
            if (candidate.getClass() == plugin.getClass())
                return;
        }

        externalPlugins.add(plugin);
    }

    public List<InternalPlugin> getInternalPlugins() {
        return internalPlugins;
    }

    public List<Plugin> getExternalPlugins() {
        return externalPlugins;
    }

    public <T extends InternalPlugin> T getInternalPlugin(Class<T> pluginClass) {
        for (InternalPlugin plugin : internalPlugins)
            if (pluginClass.isInstance(plugin))
                return pluginClass.cast(plugin);

        return null;
    }

    public <T extends Extension> List<T> getExternalPlugins(Class<T> extensionClass) {
        List<T> plugins = new ArrayList<>();
        for (Plugin plugin : externalPlugins) {
            if (extensionClass.isAssignableFrom(plugin.getClass()))
                plugins.add(extensionClass.cast(plugin));
        }

        return plugins;
    }

    public List<Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<>(externalPlugins);
        plugins.addAll(internalPlugins);

        return plugins;
    }

    public void loadCLICommands(ClassLoader loader) {
        ServiceLoader<CLICommand> commandLoader = ServiceLoader.load(CLICommand.class, loader);
        for (CLICommand command : commandLoader)
            registerCLICommand(command);
    }

    public void registerCLICommand(CLICommand command) {
        for (CLICommand candidate : commands) {
            if (candidate.getClass() == command.getClass())
                return;
        }

        commands.add(command);
    }

    public List<CLICommand> getCLICommands() {
        return commands;
    }
}
