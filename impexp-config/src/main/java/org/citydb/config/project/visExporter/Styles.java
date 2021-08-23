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

package org.citydb.config.project.visExporter;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class Styles {
    private final Map<DisplayFormType, Style> styles = new EnumMap<>(DisplayFormType.class);

    public Styles() {
    }

    public boolean isEmpty() {
        return styles.isEmpty();
    }

    public boolean contains(DisplayFormType type) {
        return styles.containsKey(type);
    }

    public Style get(DisplayFormType type) {
        return styles.get(type);
    }

    public Style getOrDefault(DisplayFormType type) {
        Style style = styles.get(type);
        return style != null ? style : Style.of(type);
    }

    public Style getOrSet(DisplayFormType type) {
        return styles.computeIfAbsent(type, Style::of);
    }

    public void add(Style style) {
        styles.put(style.getType(), style);
    }

    Collection<Style> values() {
        return styles.values();
    }
}
