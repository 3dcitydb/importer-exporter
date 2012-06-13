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
package de.tub.citydb.modules.preferences.gui.view;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.tub.citydb.api.plugin.extension.preferences.PreferencesEntry;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesEvent;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesExtension;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.preferences.NullComponent;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.exporter.CityGMLExportPlugin;
import de.tub.citydb.modules.citygml.importer.CityGMLImportPlugin;
import de.tub.citydb.modules.database.DatabasePlugin;
import de.tub.citydb.modules.kml.KMLExportPlugin;
import de.tub.citydb.modules.preferences.gui.preferences.GeneralPreferences;
import de.tub.citydb.modules.preferences.gui.preferences.RootPreferencesEntry;
import de.tub.citydb.plugin.PluginService;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class PreferencesPanel extends JPanel implements TreeSelectionListener {
	private final Logger LOG = Logger.getInstance();
	private final ImpExpGui mainView;
	private final PluginService pluginService;
	private final Config config;

	private JTree menuTree;
	private PreferencesEntry activeEntry;
	private TreePath activePanelPath;

	private JPanel col1;
	private JPanel col2;
	private JPanel col2panel;
	private JScrollPane scrollPane;
	private JLabel prefLabel;
	private JPanel noticePanel;
	private JLabel noticeLabel;
	private JPanel confirmPanel;
	private JCheckBox confirmDialogNoShow;
	private JButton restoreButton;
	private JButton standardButton;
	private JButton applyButton;

	private PreferencesTreeNode rootNode;
	private GeneralPreferences generalPreferences;

	public PreferencesPanel(PluginService pluginService, Config config, ImpExpGui mainView) {
		this.mainView = mainView;
		this.pluginService = pluginService;
		this.config = config;

		initGui();
	}

	private void initGui() {
		restoreButton = new JButton();
		standardButton = new JButton();
		applyButton = new JButton();

		restoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activeEntry != null) 
					activeEntry.handleEvent(PreferencesEvent.RESTORE_SETTINGS);
			}
		});

		standardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activeEntry != null)
					activeEntry.handleEvent(PreferencesEvent.SET_DEFAULT_SETTINGS);
			}
		});

		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activeEntry != null) {
					boolean success = activeEntry.handleEvent(PreferencesEvent.APPLY_SETTINGS);
					if (success)
						LOG.info("Settings successfully applied.");
				}
			}
		});

		rootNode = new PreferencesTreeNode(new RootPreferencesEntry());
		generalPreferences = new GeneralPreferences(mainView, config);

		PreferencesTreeNode initialNode = rootNode.add(pluginService.getInternalPlugin(CityGMLImportPlugin.class).getPreferences().getPreferencesEntry());
		rootNode.add(pluginService.getInternalPlugin(CityGMLExportPlugin.class).getPreferences().getPreferencesEntry());
		rootNode.add(pluginService.getInternalPlugin(KMLExportPlugin.class).getPreferences().getPreferencesEntry());

		for (PreferencesExtension extension : pluginService.getExternalPreferencesExtensions())
			rootNode.add(extension.getPreferences().getPreferencesEntry());	

		rootNode.add(pluginService.getInternalPlugin(DatabasePlugin.class).getPreferences().getPreferencesEntry());
		rootNode.add(generalPreferences.getPreferencesEntry());

		menuTree = new JTree(rootNode);
		menuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		menuTree.addTreeSelectionListener(this);

		// get rid of icons
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		menuTree.setCellRenderer(renderer);

		//layout
		setLayout(new GridBagLayout());
		{			
			col1 = new JPanel();
			col1.setBackground(menuTree.getBackground());
			col1.setLayout(new GridBagLayout());
			{
				JScrollPane scroll = new JScrollPane(menuTree);
				scroll.setBorder(BorderFactory.createEmptyBorder());
				scroll.setViewportBorder(BorderFactory.createEmptyBorder());
				col1.add(scroll, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,2,2,2,2));
			}
		}
		{
			col2 = new JPanel();
			col2.setLayout(new GridBagLayout());
			{
				prefLabel = new JLabel();
				Font font = prefLabel.getFont();
				prefLabel.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
				col2.add(prefLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,10,10,10,10));

				JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
				sep.setMinimumSize(sep.getPreferredSize());
				col2.add(sep, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,0,0,0));

				noticePanel = new JPanel();
				noticePanel.setBorder(BorderFactory.createEmptyBorder());
				noticePanel.setLayout(new GridBagLayout());
				{
					noticeLabel = new JLabel("");			
					noticePanel.add(noticeLabel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,0));
				}

				col2panel = new JPanel();
				col2panel.setBorder(BorderFactory.createEmptyBorder());
				col2panel.setLayout(new GridBagLayout());

				scrollPane = new JScrollPane(col2panel);
				scrollPane.setBorder(BorderFactory.createEmptyBorder());
				scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
				
				col2.add(scrollPane, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));

				JPanel col2buttons = new JPanel();
				col2.add(col2buttons, GuiUtil.setConstraints(0,4,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
				col2buttons.setLayout(new GridBagLayout());
				{
					col2buttons.add(restoreButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
					col2buttons.add(standardButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
					col2buttons.add(applyButton, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
				}
			}
		}

		resetPreferencesMenu();
		for (int i = 0; i < menuTree.getRowCount(); i++)
			menuTree.expandRow(i);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setBorder(BorderFactory.createEtchedBorder());

		splitPane.setLeftComponent(col1);
		splitPane.setRightComponent(col2);
		splitPane.setDividerLocation(menuTree.getPreferredSize().width + 6);

		add(splitPane, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,2,2,2,2));
		setBorder(BorderFactory.createEmptyBorder());

		menuTree.setSelectionPath(new TreePath(new Object[]{rootNode, initialNode}));
		for (int i = 1; i < menuTree.getRowCount(); i++)
			menuTree.collapseRow(i);
	}

	public void doTranslation() {
		restoreButton.setText(Internal.I18N.getString("pref.button.restore"));
		standardButton.setText(Internal.I18N.getString("pref.button.standard"));
		applyButton.setText(Internal.I18N.getString("common.button.apply"));
		noticeLabel.setText(Internal.I18N.getString("common.pref.menu.expand.label"));

		resetPreferencesMenu();
		menuTree.repaint();
		prefLabel.setText(((PreferencesTreeNode)menuTree.getLastSelectedPathComponent()).toString());		
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		PreferencesTreeNode node = (PreferencesTreeNode)menuTree.getLastSelectedPathComponent();
		if (node == null || activePanelPath == menuTree.getSelectionPath())
			return;

		if (!requestChange()) {
			menuTree.setSelectionPath(activePanelPath);
			return;
		}

		col2panel.removeAll();
		if (node.entry.getViewComponent() == null || node.entry.getViewComponent() == NullComponent.getInstance()) {
			col2panel.add(noticePanel, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,5,5,5,5));
			activeEntry = null;
		} else {
			col2panel.add(node.entry.getViewComponent(), GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,5,5,5,5));
			activeEntry = node.entry;
		}

		revalidate();

		prefLabel.setText(node.toString());
		activePanelPath = menuTree.getSelectionPath();
		setEnabledButtons();
		repaint();
	}

	public boolean requestChange() {
		if (activeEntry == null) 
			return true;

		if (activeEntry.isModified()) {			
			int res = -1;

			if (config.getGui().isShowPreferencesConfirmDialog()) {
				confirmPanel = new JPanel(new GridBagLayout());
				confirmDialogNoShow = new JCheckBox(Internal.I18N.getString("common.dialog.msg.noShow"));
				confirmPanel.add(new JLabel(Internal.I18N.getString("pref.dialog.apply.msg")), GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
				confirmPanel.add(confirmDialogNoShow, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,10,0,0,0));

				res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), 
						confirmPanel, 
						Internal.I18N.getString("pref.dialog.apply.title"), 
						JOptionPane.YES_NO_CANCEL_OPTION);

				config.getGui().setShowPreferencesConfirmDialog(!confirmDialogNoShow.isSelected());
			} else
				res = JOptionPane.YES_OPTION;

			if (res == JOptionPane.CANCEL_OPTION) 
				return false;
			else if (res == JOptionPane.YES_OPTION) {
				activeEntry.handleEvent(PreferencesEvent.APPLY_SETTINGS);
			} else
				activeEntry.handleEvent(PreferencesEvent.RESTORE_SETTINGS);
		}

		return true;
	}

	public GeneralPreferences getGeneralPreferences() {
		return generalPreferences;
	}

	private void setEnabledButtons() {
		restoreButton.setEnabled(activeEntry != null);
		standardButton.setEnabled(activeEntry != null);
		applyButton.setEnabled(activeEntry != null);
	}

	private void resetPreferencesMenu() {
		nodesChanged((DefaultTreeModel)menuTree.getModel(), rootNode);
	}

	private void nodesChanged(DefaultTreeModel model, TreeNode node) {
		model.nodeChanged(node);
		for (int i = 0; i < node.getChildCount(); i++)
			nodesChanged(model, node.getChildAt(i));
	}

	private final class PreferencesTreeNode extends DefaultMutableTreeNode {
		private final PreferencesEntry entry;

		public PreferencesTreeNode(PreferencesEntry entry) {
			this.entry = entry;
			setUserObject(entry.getViewComponent());
		}

		public PreferencesTreeNode add(PreferencesEntry entry) {
			PreferencesTreeNode treeNode = new PreferencesTreeNode(entry);
			add(treeNode);

			for (PreferencesEntry childEntry : entry.getChildEntries())
				add(childEntry, treeNode);

			return treeNode;
		}

		private void add(PreferencesEntry entry, PreferencesTreeNode parent) {
			PreferencesTreeNode treeNode = new PreferencesTreeNode(entry);
			parent.add(treeNode);

			for (PreferencesEntry childEntry : entry.getChildEntries())
				add(childEntry, treeNode);
		}

		public String toString() {
			return entry.getLocalizedTitle();
		}
	}

}
