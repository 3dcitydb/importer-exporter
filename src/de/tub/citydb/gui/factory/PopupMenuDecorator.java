/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.gui.factory;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;

import de.tub.citydb.api.gui.StandardPopupMenuDecorator;
import de.tub.citydb.gui.factory.popup.AbstractStandardPopupMenu;
import de.tub.citydb.gui.factory.popup.StandardEditingPopupMenu;
import de.tub.citydb.gui.factory.popup.StandardTreePopupMenu;

public class PopupMenuDecorator implements StandardPopupMenuDecorator {
	private static PopupMenuDecorator instance;
	private static HashMap<Class<? extends Component>, AbstractStandardPopupMenu> popupMenus = new HashMap<Class<? extends Component>, AbstractStandardPopupMenu>();

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
			decorate(component, getStandardEditingPopupMenu(component));
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

	private AbstractStandardPopupMenu getStandardEditingPopupMenu(JComponent component) {
		AbstractStandardPopupMenu popupMenu = popupMenus.get(component.getClass());

		if (popupMenu == null) {
			if (component instanceof JTree) {
				popupMenu = new StandardTreePopupMenu();
				((StandardTreePopupMenu)popupMenu).init();				
			} else {
				popupMenu = new StandardEditingPopupMenu();
				((StandardEditingPopupMenu)popupMenu).init(component);
			}

			popupMenu.doTranslation();
			popupMenus.put(component.getClass(), popupMenu);
		}

		return popupMenu;
	}

}
