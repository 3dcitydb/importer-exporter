/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.gui.factory.popup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.internal.Internal;

@SuppressWarnings("serial")
public class StandardTreePopupMenu extends AbstractStandardPopupMenu implements EventHandler {
	private JMenuItem expand;
	private JMenuItem expandAll;
	private JMenuItem collapse;
	private JMenuItem collapseAll;
	private Separator separator;

	private JTree tree;
	private TreePath path;

	public StandardTreePopupMenu() {
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.SWITCH_LOCALE, this);
	}

	public void init() {
		expand = new JMenuItem();
		expandAll = new JMenuItem();		
		collapse = new JMenuItem();
		collapseAll = new JMenuItem();
		separator = new Separator();

		expand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (path != null)
					performActionOnNodes(path, true, false);
			}
		});

		expandAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (path != null)
					performActionOnNodes(path, true, true);
			}
		});

		collapse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (path != null)
					performActionOnNodes(path, false, false);
			}
		});

		collapseAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (path != null)
					performActionOnNodes(path, false, true);
			}
		});

		add(expand);
		add(expandAll);
		add(separator);
		add(collapse);
		add(collapseAll);
	}

	public void prepare(JTree tree, TreePath path) {
		this.tree = tree;
		this.path = path;

		TreeNode node = (TreeNode)path.getLastPathComponent();
		boolean hasNestedChildren = hasNestedChildren(path);		
		boolean isCollapsed = tree.isCollapsed(path);
		boolean isLeaf = node.isLeaf();

		expand.setEnabled(!isLeaf && isCollapsed);
		expandAll.setEnabled(!isLeaf && hasNestedChildren);
		collapse.setEnabled(!isLeaf && !isCollapsed);
		collapseAll.setEnabled(!isLeaf && !isCollapsed && hasNestedChildren);

		expand.setVisible(isLeaf || isCollapsed);
		expandAll.setVisible(!isLeaf && hasNestedChildren && (isCollapsed || showAll(path, path, true)));
		collapse.setVisible(!isLeaf && !isCollapsed);
		collapseAll.setVisible(!isLeaf && !isCollapsed && hasNestedChildren && showAll(path, path, false));
		separator.setVisible((expand.isVisible() || expandAll.isVisible()) && (collapse.isVisible() || collapseAll.isVisible()));
	}

	public void doTranslation() {
		expand.setText(Internal.I18N.getString("pref.popup.expand"));		
		expandAll.setText(Internal.I18N.getString("pref.popup.expandAll"));		
		collapse.setText(Internal.I18N.getString("pref.popup.collapse"));		
		collapseAll.setText(Internal.I18N.getString("pref.popup.collapseAll"));		
	}

	private void performActionOnNodes(TreePath parent, boolean expand, boolean recursive) {
		TreeNode node = (TreeNode)parent.getLastPathComponent();

		if (recursive)
			for (int i = 0; i < node.getChildCount(); ++i)
				performActionOnNodes(parent.pathByAddingChild(node.getChildAt(i)), expand, recursive);

		if (expand)
			tree.expandPath(parent);
		else
			tree.collapsePath(parent);
	}

	private boolean showAll(TreePath root, TreePath sub, boolean expand) {
		TreeNode node = (TreeNode)sub.getLastPathComponent();
		
		for (int i = 0; i < node.getChildCount(); ++i) {
			TreeNode child = node.getChildAt(i);
			if (child.isLeaf())
				continue;
			
			if (showAll(root, sub.pathByAddingChild(child), expand))
				return true;
		}
		
		if (root == sub)
			return false;
		
		return expand ? tree.isCollapsed(sub) : tree.isExpanded(sub);
	}

	private boolean hasNestedChildren(TreePath parent) {
		TreeNode node = (TreeNode)parent.getLastPathComponent();
		boolean hasNestedChildren = false;

		for (int i = 0; i < node.getChildCount(); ++i) {
			TreeNode child = (TreeNode)parent.pathByAddingChild(node.getChildAt(i)).getLastPathComponent();
			if (!child.isLeaf()) {
				hasNestedChildren = true;
				break;
			}
		}

		return hasNestedChildren;
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		doTranslation();
	}
}
