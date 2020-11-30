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
package org.citydb.config.gui.window;

import org.citydb.config.gui.style.ConsoleStyle;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ConsoleWindowType", propOrder = {
        "size",
        "style"
})
public class ConsoleWindow {
    @XmlAttribute(required = true)
    private boolean isDetached = false;
    private WindowSize size;
    private ConsoleStyle style;

    public ConsoleWindow() {
        size = new WindowSize();
        style = new ConsoleStyle();
    }

    public boolean isDetached() {
        return isDetached;
    }

    public void setDetached(boolean isDetached) {
        this.isDetached = isDetached;
    }

    public WindowSize getSize() {
        return size;
    }

    public void setSize(WindowSize size) {
        if (size != null)
            this.size = size;
    }

    public ConsoleStyle getStyle() {
        return style;
    }

    public void setStyle(ConsoleStyle style) {
        if (style != null)
            this.style = style;
    }
}
