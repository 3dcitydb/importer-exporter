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

package org.citydb.gui.components.dialog;

import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.core.database.DatabaseController;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.plugin.view.ViewController;

import javax.swing.*;
import java.text.MessageFormat;

public class DatabaseConnectionDialog {

    public static boolean show(String message, ViewController viewController) {
        DatabaseController controller = ObjectRegistry.getInstance().getDatabaseController();
        if (!controller.isConnected()) {
            DatabaseConnection connection = ObjectRegistry.getInstance().getConfig().getDatabaseConfig().getActiveConnection();

            int option = viewController.showOptionDialog(
                    Language.I18N.getString("common.dialog.dbConnect.title"),
                    MessageFormat.format(Language.I18N.getString("common.dialog.dbConnect.message.generic"),
                            message,
                            connection.getDescription(),
                            connection.toConnectString()),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            return option == JOptionPane.YES_OPTION && controller.connect();
        } else {
            return true;
        }
    }
}
