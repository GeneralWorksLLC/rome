/*
 * Copyright 2004 Sun Microsystems, Inc.
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
 *
 */
package com.sun.syndication.io.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.syndication.io.DelegatingModuleGenerator;
import com.sun.syndication.io.DelegatingModuleParser;
import com.sun.syndication.io.WireFeedGenerator;
import com.sun.syndication.io.WireFeedParser;

/**
 * <p>
 * 
 * @author Alejandro Abdelnur
 * 
 */
public abstract class PluginManager {
    private final String[] _propertyValues;
    private Map _pluginsMap;
    private List _pluginsList;
    private final List _keys;
    private final WireFeedParser _parentParser;
    private final WireFeedGenerator _parentGenerator;

    /**
     * Creates a PluginManager
     * <p>
     * 
     * @param propertyKey property key defining the plugins classes
     * 
     */
    protected PluginManager(final String propertyKey) {
        this(propertyKey, null, null);
    }

    protected PluginManager(final String propertyKey, final WireFeedParser parentParser, final WireFeedGenerator parentGenerator) {
        this._parentParser = parentParser;
        this._parentGenerator = parentGenerator;
        this._propertyValues = PropertiesLoader.getPropertiesLoader().getTokenizedProperty(propertyKey, ", ");
        loadPlugins();
        this._pluginsMap = Collections.unmodifiableMap(this._pluginsMap);
        this._pluginsList = Collections.unmodifiableList(this._pluginsList);
        this._keys = Collections.unmodifiableList(new ArrayList(this._pluginsMap.keySet()));
    }

    protected abstract String getKey(Object obj);

    protected List getKeys() {
        return this._keys;
    }

    protected List getPlugins() {
        return this._pluginsList;
    }

    protected Map getPluginMap() {
        return this._pluginsMap;
    }

    protected Object getPlugin(final String key) {
        return this._pluginsMap.get(key);
    }

    // PRIVATE - LOADER PART

    private void loadPlugins() {
        final List finalPluginsList = new ArrayList();
        this._pluginsList = new ArrayList();
        this._pluginsMap = new HashMap();
        String className = null;
        try {
            final Class[] classes = getClasses();
            for (final Class classe : classes) {
                className = classe.getName();
                final Object plugin = classe.newInstance();
                if (plugin instanceof DelegatingModuleParser) {
                    ((DelegatingModuleParser) plugin).setFeedParser(this._parentParser);
                }
                if (plugin instanceof DelegatingModuleGenerator) {
                    ((DelegatingModuleGenerator) plugin).setFeedGenerator(this._parentGenerator);
                }

                this._pluginsMap.put(getKey(plugin), plugin);
                this._pluginsList.add(plugin); // to preserve the order of
                                               // definition
                // in the rome.properties files
            }
            Iterator i = this._pluginsMap.values().iterator();
            while (i.hasNext()) {
                finalPluginsList.add(i.next()); // to remove overridden plugin
                                                // impls
            }

            i = this._pluginsList.iterator();
            while (i.hasNext()) {
                final Object plugin = i.next();
                if (!finalPluginsList.contains(plugin)) {
                    i.remove();
                }
            }
        } catch (final Exception ex) {
            throw new RuntimeException("could not instantiate plugin " + className, ex);
        } catch (final ExceptionInInitializerError er) {
            throw new RuntimeException("could not instantiate plugin " + className, er);
        }
    }

    /**
     * Loads and returns the classes defined in the properties files. If the
     * system property "rome.pluginmanager.useloadclass" is set to true then
     * classLoader.loadClass will be used to load classes (instead of
     * Class.forName). This is designed to improve OSGi compatibility. Further
     * information can be found in
     * https://rome.dev.java.net/issues/show_bug.cgi?id=118
     * <p>
     * 
     * @return array containing the classes defined in the properties files.
     * @throws java.lang.ClassNotFoundException thrown if one of the classes
     *             defined in the properties file cannot be loaded and hard
     *             failure is ON.
     * 
     */
    private Class[] getClasses() throws ClassNotFoundException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final List classes = new ArrayList();
        final boolean useLoadClass = Boolean.valueOf(System.getProperty("rome.pluginmanager.useloadclass", "false")).booleanValue();
        for (final String _propertyValue : this._propertyValues) {
            final Class mClass = useLoadClass ? classLoader.loadClass(_propertyValue) : Class.forName(_propertyValue, true, classLoader);
            classes.add(mClass);
        }
        final Class[] array = new Class[classes.size()];
        classes.toArray(array);
        return array;
    }

}