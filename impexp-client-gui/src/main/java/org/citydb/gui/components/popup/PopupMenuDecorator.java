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
package org.citydb.gui.components.popup;

import org.citydb.gui.components.TitledPanel;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class PopupMenuDecorator {
	private static PopupMenuDecorator instance;
	private final Map<String, AbstractPopupMenu> standardPopupMenus = new HashMap<>();
	private final Set<AbstractPopupMenu> popupMenus = Collections.newSetFromMap(new WeakHashMap<>());

	private PopupMenuDecorator() {
		// just to thwart instantiation
	}

	public static synchronized PopupMenuDecorator getInstance() {
		if (instance == null) {
			instance = new PopupMenuDecorator();
		}

		return instance;
	}

	private void decorate(final JComponent component, final AbstractPopupMenu popupMenu) {
		component.addMouseListener(new MouseAdapter() {
			private void processMouseEvent(MouseEvent e) {
				if (e.isPopupTrigger()) {
					if (!e.getComponent().isEnabled()) {
						return;
					}

					if (popupMenu instanceof EditPopupMenu && e.getComponent() instanceof JTextComponent) {
						((EditPopupMenu) popupMenu).prepare(((JTextComponent) e.getComponent()).isEditable());
					} else if (popupMenu instanceof TitledPanelGroupPopupMenu) {
						((TitledPanelGroupPopupMenu) popupMenu).prepare();
					} else if (popupMenu instanceof CheckBoxGroupPopupMenu) {
						((CheckBoxGroupPopupMenu) popupMenu).prepare();
					} else if (popupMenu instanceof TreePopupMenu && e.getComponent() instanceof JTree) {
						Point point = e.getPoint();
						TreePath path = ((JTree) e.getComponent()).getPathForLocation(point.x, point.y);
						if (path == null
								|| (path.getLastPathComponent() instanceof TreeNode
								&& ((TreeNode) path.getLastPathComponent()).isLeaf())) {
							return;
						}

						((TreePopupMenu) popupMenu).prepare((JTree) e.getComponent(), path);
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

		popupMenus.add(popupMenu);
	}

	public void decorate(final JComponent... components) {
		for (final JComponent component : components) {
			decorate(component, getStandardPopupMenu(component));
		}
	}

	public void decorate(JTextComponent... components) {
		decorate((JComponent[]) components);
	}

	public JPopupMenu decorateAndGet(JTextComponent component) {
		EditPopupMenu popupMenu = new EditPopupMenu();
		popupMenu.init(component);
		decorate(component, popupMenu);

		return popupMenu;
	}

	public void decorate(JTree... trees) {
		decorate((JComponent[]) trees);
	}

	public JPopupMenu decorateAndGet(JTree tree) {
		TreePopupMenu popupMenu = new TreePopupMenu();
		popupMenu.init();
		decorate(tree, popupMenu);

		return popupMenu;
	}

	public void decorateCheckBoxGroup(JCheckBox... group) {
		decorateAndGetCheckBoxGroup(group);
	}

	public JPopupMenu[] decorateAndGetCheckBoxGroup(JCheckBox... group) {
		if (group == null || group.length <= 1) {
			throw new IllegalArgumentException("The check box group may not be null and must contain more than two members.");
		}

		JPopupMenu[] popupMenus = new JPopupMenu[group.length];
		for (int i = 0; i < group.length; i++) {
			CheckBoxGroupPopupMenu popupMenu = new CheckBoxGroupPopupMenu();
			popupMenu.init(group[i], group);
			decorate(group[i], popupMenu);

			popupMenus[i] = popupMenu;
		}

		return popupMenus;
	}

	public void decorateTitledPanelGroup(TitledPanel... group) {
		decorateAndGetTitledPanelGroup(group);
	}

	public JPopupMenu[] decorateAndGetTitledPanelGroup(TitledPanel... group) {
		if (group == null || group.length <= 1) {
			throw new IllegalArgumentException("The titled panel group may not be null and must contain more than two members.");
		}

		JPopupMenu[] popupMenus = new JPopupMenu[group.length];
		for (int i = 0; i < group.length; i++) {
			TitledPanelGroupPopupMenu popupMenu = new TitledPanelGroupPopupMenu();
			popupMenu.init(group[i], group);
			decorate(group[i].getTitleLabel(), popupMenu);
			if (group[i].hasToggleButton()) {
				decorate(group[i].getToggleButton(), popupMenu);
			}

			popupMenus[i] = popupMenu;
		}

		return popupMenus;
	}

	public void updateUI() {
		for (AbstractPopupMenu popupMenu : popupMenus) {
			try {
				SwingUtilities.updateComponentTreeUI(popupMenu);
			} catch (Exception e) {
				Logger.getInstance().error("Failed to update UI for component '" + popupMenu + "'.", e);
			}
		}
	}

	private AbstractPopupMenu getStandardPopupMenu(JComponent component) {
		AbstractPopupMenu popupMenu = standardPopupMenus.get(component.getClass().getName());

		if (popupMenu == null) {
			if (component instanceof JTree) {
				popupMenu = new TreePopupMenu();
				((TreePopupMenu) popupMenu).init();
			} else {
				popupMenu = new EditPopupMenu();
				((EditPopupMenu) popupMenu).init(component);
			}

			standardPopupMenus.put(component.getClass().getName(), popupMenu);
		}

		return popupMenu;
	}
}
