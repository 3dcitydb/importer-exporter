/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
package org.citydb.gui.factory;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.citydb.gui.components.popup.AbstractStandardPopupMenu;
import org.citydb.gui.components.popup.CheckBoxGroupPopupMenu;
import org.citydb.gui.components.popup.StandardEditingPopupMenu;
import org.citydb.gui.components.popup.StandardTreePopupMenu;
import org.citydb.plugin.extension.view.components.StandardEditingPopupMenuDecorator;

public class PopupMenuDecorator implements StandardEditingPopupMenuDecorator {
	private static PopupMenuDecorator instance;
	private static HashMap<String, AbstractStandardPopupMenu> popupMenus = new HashMap<String, AbstractStandardPopupMenu>();

	private PopupMenuDecorator() {
		// just to thwart instantiation
	}

	public static synchronized PopupMenuDecorator getInstance() {
		if (instance == null)
			instance = new PopupMenuDecorator();

		return instance;
	}

	private void decorate(final JComponent component, final AbstractStandardPopupMenu popupMenu) {
		component.addMouseListener(new MouseAdapter() {
			private void processMouseEvent(MouseEvent e) {
				if (e.isPopupTrigger()) {
					if (!e.getComponent().isEnabled())
						return;

					if (e.getComponent() instanceof JTextComponent) {
						boolean isEditable = ((JTextComponent)e.getComponent()).isEditable();
						((StandardEditingPopupMenu)popupMenu).prepare(isEditable);
					}

					else if (e.getComponent() instanceof JTree) {
						Point point = e.getPoint();
						TreePath path = ((JTree)e.getComponent()).getPathForLocation(point.x, point.y);
						if (path == null)
							return;

						if (path.getLastPathComponent() instanceof TreeNode
								&& ((TreeNode)path.getLastPathComponent()).isLeaf())
							return;
						
						((StandardTreePopupMenu)popupMenu).prepare((JTree)e.getComponent(), path);
					}

					popupMenu.show(e.getComponent(), e.getX(), e.getY());
					popupMenu.setInvoker(e.getComponent());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				processMouseEvent(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				processMouseEvent(e);
			}

		});
	}

	public void decorate(final JComponent... components) {
		for (final JComponent component : components)
			decorate(component, getStandardPopupMenu(component));
	}

	@Override
	public void decorate(JTextComponent... components) {
		decorate((JComponent[])components);
	}

	@Override
	public JPopupMenu decorateAndGet(JTextComponent component) {
		StandardEditingPopupMenu popupMenu = new StandardEditingPopupMenu();
		popupMenu.init(component);
		popupMenu.doTranslation();
		decorate(component, popupMenu);

		return popupMenu;
	}

	@Override
	public void decorate(JTree... trees) {
		decorate((JComponent[])trees);
	}

	@Override
	public JPopupMenu decorateAndGet(JTree tree) {
		StandardTreePopupMenu popupMenu = new StandardTreePopupMenu();
		popupMenu.init();
		popupMenu.doTranslation();
		decorate(tree, popupMenu);

		return popupMenu;
	}

	@Override
	public void decorateCheckBoxGroup(JCheckBox... group) {
		decorateAndGetCheckBoxGroup(group);
	}

	@Override
	public JPopupMenu[] decorateAndGetCheckBoxGroup(JCheckBox... group) {
		if (group == null || group.length <= 1)
			throw new IllegalArgumentException("The check box group may not be null and must contain more than two members.");

		JPopupMenu[] popupMenus = new JPopupMenu[group.length];
		for (int i = 0; i < group.length; i++) {
			CheckBoxGroupPopupMenu popupMenu = new CheckBoxGroupPopupMenu();
			popupMenu.init(i, group);
			popupMenu.doTranslation();
			decorate(group[i], popupMenu);
			
			popupMenus[i] = popupMenu;
		}
		
		return popupMenus;
	}

	private AbstractStandardPopupMenu getStandardPopupMenu(JComponent component) {
		AbstractStandardPopupMenu popupMenu = popupMenus.get(component.getClass().getName());

		if (popupMenu == null) {
			if (component instanceof JTree) {
				popupMenu = new StandardTreePopupMenu();
				((StandardTreePopupMenu)popupMenu).init();				
			} else {
				popupMenu = new StandardEditingPopupMenu();
				((StandardEditingPopupMenu)popupMenu).init(component);
			}

			popupMenu.doTranslation();
			popupMenus.put(component.getClass().getName(), popupMenu);
		}

		return popupMenu;
	}

}
