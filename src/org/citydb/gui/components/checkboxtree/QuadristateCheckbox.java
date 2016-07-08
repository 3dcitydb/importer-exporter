/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ActionMapUIResource;

import org.citydb.gui.components.checkboxtree.QuadristateButtonModel.State;

/**
 * Checkbox with four states. Available states are UNCHECKED, CHECKED,
 * GREY_CHECKED, GREY_UNCHECKED. The rendering is obtained via a visualization
 * hack. The checkbox exploits the different rendering (greyed) of checkbox
 * pressed, thus the press, arm, rollover events are not available. Maintenance
 * tip - There were some tricks to getting this code working: 1. You have to
 * overwite addMouseListener() to do nothing 2. You have to add a mouse event on
 * mousePressed by calling super.addMouseListener() 3. You have to replace the
 * UIActionMap for the keyboard event "pressed" with your own one. 4. You have
 * to remove the UIActionMap for the keyboard event "released". 5. You have to
 * grab focus when the next state is entered, otherwise clicking on the
 * component won't get the focus.
 * 
 * @author boldrini
 * @author bigagli
 */

@SuppressWarnings("serial")
public class QuadristateCheckbox extends JCheckBox {

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

    public QuadristateCheckbox(String text) {
	this(text, State.UNCHECKED);
    }

    public QuadristateCheckbox() {
	this(null);
    }

    @Override
    protected void init(String text, Icon icon) {
	// substitutes the underlying checkbox model:
	// if we had call setModel an exception would be raised
	// because setModel calls a getModel that return a
	// QuadristateButtonModel, but at this point we
	// have a JToggleButtonModel
	this.model = new QuadristateButtonModel();
	super.setModel(this.model); // side effect: set listeners
	super.init(text, icon);
    }

    @Override
    public QuadristateButtonModel getModel() {
	return (QuadristateButtonModel) super.getModel();
    }

    public void setModel(QuadristateButtonModel model) {
	super.setModel(model);
    }

    @Override
    @Deprecated
    public void setModel(ButtonModel model) {
	// if (!(model instanceof TristateButtonModel))
	// useless: Java always calls the most specific method
	super.setModel(model);
    }

    /** No one may add mouse listeners, not even Swing! */
    @Override
    public void addMouseListener(MouseListener l) {
    }

    /**
         * Set the new state to either CHECKED, UNCHECKED or GREY_CHECKED. If
         * state == null, it is treated as GREY_CHECKED.
         */
    public void setState(State state) {
	getModel().setState(state);
    }

    /**
         * Return the current state, which is determined by the selection status
         * of the model.
         */
    public State getState() {
	return getModel().getState();
    }

}
