/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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
package org.citydb.gui.components.checkboxtree;

import org.citydb.gui.components.checkboxtree.QuadristateButtonModel.State;

import javax.swing.*;
import javax.swing.plaf.ActionMapUIResource;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Checkbox with four states. Available states are UNCHECKED, CHECKED,
 * GREY_CHECKED, GREY_UNCHECKED. The rendering is obtained via a visualization
 * hack. The checkbox exploits the different rendering (greyed) of checkbox
 * pressed, thus the press, arm, rollover events are not available. Maintenance
 * tip - Some tricks are needed to get this code working: 1. You have to
 * overwite addMouseListener() to do nothing 2. You have to add a mouse event on
 * mousePressed by calling super.addMouseListener() 3. You have to replace the
 * UIActionMap for the keyboard event "pressed" with your own one. 4. You have
 * to remove the UIActionMap for the keyboard event "released". 5. You have to
 * grab focus when the next state is entered, otherwise clicking on the
 * component will not get the focus.
 *
 * @author boldrini
 * @author bigagli
 */

public class QuadristateCheckbox extends JCheckBox {

    public QuadristateCheckbox() {
        this(null);
    }

    public QuadristateCheckbox(String text) {
        this(text, State.UNCHECKED);
    }

    public QuadristateCheckbox(String text, Icon icon, State state) {
        super(text, icon);
        // Add a listener for when the mouse is pressed
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                grabFocus();
                getModel().nextState();
            }
        });
        // Reset the keyboard action map
        ActionMap map = new ActionMapUIResource();
        map.put("pressed", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                grabFocus();
                getModel().nextState();
            }
        });
        map.put("released", null);
        SwingUtilities.replaceUIActionMap(this, map);
        setState(state);
    }

    public QuadristateCheckbox(String text, State initial) {
        this(text, null, initial);
    }

    /**
     * No one may add mouse listeners, not even Swing!
     */
    @Override
    public void addMouseListener(MouseListener l) {
    }

    @Override
    public QuadristateButtonModel getModel() {
        return (QuadristateButtonModel) super.getModel();
    }

    /**
     * Return the current state, which is determined by the selection status of
     * the model.
     */
    public State getState() {
        return getModel().getState();
    }

    @Override
    protected void init(String text, Icon icon) {
        // substitutes the underlying checkbox model:
        // if we had call setModel an exception would be raised
        // because setModel calls a getModel that return a
        // QuadristateButtonModel, but at this point we
        // have a JToggleButtonModel
        this.model = new QuadristateButtonModel();
        super.setModel(this.model);// side effect: set listeners
        super.init(text, icon);
    }

    public void setModel(QuadristateButtonModel model) {
        super.setModel(model);
    }

    /**
     * Set the new state to either CHECKED, UNCHECKED or GREY_CHECKED. If state
     * == null, it is treated as GREY_CHECKED.
     */
    public void setState(State state) {
        getModel().setState(state);
    }

}
