/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

package org.citydb.gui.util;

import com.formdev.flatlaf.FlatLaf;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class RSyntaxTextAreaHelper {

    public static void installDefaultTheme(RSyntaxTextArea textArea) {
        Consumer<RSyntaxTextArea> applyTheme = input -> {
            String theme = FlatLaf.isLafDark() ?
                    "/org/fife/ui/rsyntaxtextarea/themes/dark.xml" :
                    "/org/fife/ui/rsyntaxtextarea/themes/idea.xml";

            try (InputStream in = input.getClass().getResourceAsStream(theme)) {
                Theme.load(in).apply(input);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to initialize syntax text editor.", e);
            }
        };

        applyTheme.accept(textArea);
        UIManager.addPropertyChangeListener(e -> {
            if ("lookAndFeel".equals(e.getPropertyName())) {
                applyTheme.accept(textArea);
            }
        });
    }
}
