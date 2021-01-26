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
package org.citydb.gui.modules.preferences.view;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.dialog.ConfirmationCheckDialog;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.NullComponent;
import org.citydb.gui.modules.database.DatabasePlugin;
import org.citydb.gui.modules.exporter.CityGMLExportPlugin;
import org.citydb.gui.modules.importer.CityGMLImportPlugin;
import org.citydb.gui.modules.kml.KMLExportPlugin;
import org.citydb.gui.modules.preferences.preferences.GeneralPreferences;
import org.citydb.gui.modules.preferences.preferences.RootPreferencesEntry;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.PluginManager;
import org.citydb.plugin.extension.preferences.Preferences;
import org.citydb.plugin.extension.preferences.PreferencesEntry;
import org.citydb.plugin.extension.preferences.PreferencesEvent;
import org.citydb.plugin.extension.preferences.PreferencesExtension;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class PreferencesPanel extends JPanel implements TreeSelectionListener {
	private final Logger log = Logger.getInstance();
	private final ImpExpGui mainView;
	private final PluginManager pluginManager;
	private final Config config;

	private JSplitPane splitPane;
	private JPanel treePanel;
	private JTree menuTree;
	private PreferencesEntry activeEntry;
	private TreePath activePanelPath;

	private JPanel settingsContentPanel;
	private JLabel settingsNameLabel;
	private JPanel hintPanel;
	private JLabel hintLabel;
	private JButton restoreButton;
	private JButton standardButton;
	private JButton applyButton;

	private PreferencesTreeNode rootNode;
	private GeneralPreferences generalPreferences;

	public PreferencesPanel(ImpExpGui mainView, Config config) {
		this.mainView = mainView;
		this.config = config;

		pluginManager = PluginManager.getInstance();
		initGui();
	}

	private void initGui() {
		restoreButton = new JButton();
		standardButton = new JButton();
		applyButton = new JButton();

		restoreButton.addActionListener(e -> {
			if (activeEntry != null)
				activeEntry.handleEvent(PreferencesEvent.RESTORE_SETTINGS);
		});

		standardButton.addActionListener(e -> {
			if (activeEntry != null)
				activeEntry.handleEvent(PreferencesEvent.SET_DEFAULT_SETTINGS);
		});

		applyButton.addActionListener(e -> {
			if (activeEntry != null) {
				boolean success = activeEntry.handleEvent(PreferencesEvent.APPLY_SETTINGS);
				if (success)
					log.info("Settings successfully applied.");
			}
		});

		generalPreferences = new GeneralPreferences(mainView, config);

		rootNode = new PreferencesTreeNode(new RootPreferencesEntry());
		rootNode.add(pluginManager.getInternalPlugin(CityGMLImportPlugin.class).getPreferences().getPreferencesEntry());
		rootNode.add(pluginManager.getInternalPlugin(CityGMLExportPlugin.class).getPreferences().getPreferencesEntry());
		rootNode.add(pluginManager.getInternalPlugin(KMLExportPlugin.class).getPreferences().getPreferencesEntry());

		for (PreferencesExtension extension : pluginManager.getExternalPlugins(PreferencesExtension.class)) {
			Preferences preferences = extension.getPreferences();
			if (preferences == null || preferences.getPreferencesEntry() == null) {
				log.error("Failed to get preference entry from plugin " + extension.getClass().getName() + ".");
				continue;
			}

			rootNode.add(preferences.getPreferencesEntry());
		}

		rootNode.add(pluginManager.getInternalPlugin(DatabasePlugin.class).getPreferences().getPreferencesEntry());
		rootNode.add(generalPreferences.getPreferencesEntry());

		menuTree = new JTree(rootNode);
		menuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		menuTree.addTreeSelectionListener(this);
		menuTree.setRootVisible(false);
		menuTree.setShowsRootHandles(true);
		menuTree.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		// render top-level entries in bold
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

				if (((DefaultMutableTreeNode) value).getLevel() == 1) {
					setFont(getFont().deriveFont(Font.BOLD));
				} else {
					setFont(getFont().deriveFont(Font.PLAIN));
				}

				return this;
			}
		};

		// get rid of icons
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		menuTree.setCellRenderer(renderer);

		// layout
		setLayout(new GridBagLayout());
		treePanel = new JPanel();
		treePanel.setLayout(new GridBagLayout());
		{
			JScrollPane scroll = new JScrollPane(menuTree);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			scroll.setViewportBorder(BorderFactory.createEmptyBorder());
			treePanel.add(scroll, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridBagLayout());
		{
			settingsNameLabel = new JLabel();
			settingsNameLabel.setFont(settingsNameLabel.getFont().deriveFont(Font.BOLD));
			contentPanel.add(settingsNameLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 15, 10, 15, 10));

			hintPanel = new JPanel();
			hintPanel.setLayout(new GridBagLayout());
			{
				hintLabel = new JLabel();
				hintPanel.add(hintLabel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
			}

			settingsContentPanel = new JPanel();
			settingsContentPanel.setLayout(new GridBagLayout());

			JScrollPane scrollPane = new JScrollPane(settingsContentPanel);
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

			JPanel buttons = new JPanel();
			buttons.setLayout(new GridBagLayout());
			{
				buttons.add(restoreButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 5, 0, 5, 5));
				buttons.add(standardButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
				buttons.add(applyButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.BOTH, 5, 5, 5, 0));
			}

			contentPanel.add(scrollPane, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
			contentPanel.add(buttons, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 5, 10, 5, 10));
		}

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		splitPane.setLeftComponent(treePanel);
		splitPane.setRightComponent(contentPanel);

		add(splitPane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
		setBorder(BorderFactory.createEmptyBorder());

		menuTree.setSelectionPath(new TreePath(new Object[]{rootNode}));
		menuTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				splitPane.setDividerLocation(treePanel.getPreferredSize().width);
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				splitPane.setDividerLocation(treePanel.getPreferredSize().width);
			}
		});

		PopupMenuDecorator.getInstance().decorate(menuTree);
	}

	public void doTranslation() {
		restoreButton.setText(Language.I18N.getString("common.button.restore"));
		standardButton.setText(Language.I18N.getString("common.button.standard"));
		applyButton.setText(Language.I18N.getString("common.button.apply"));
		hintLabel.setText(Language.I18N.getString("common.pref.menu.expand.label"));

		resetPreferencesMenu();
		settingsNameLabel.setText(menuTree.getLastSelectedPathComponent().toString());
		splitPane.setDividerLocation(treePanel.getPreferredSize().width);
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

		settingsContentPanel.removeAll();
		if (node.entry.getViewComponent() == null || node.entry.getViewComponent() == NullComponent.getInstance()) {
			settingsContentPanel.add(hintPanel, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 10, 0, 10));
			activeEntry = null;
		} else {
			settingsContentPanel.add(node.entry.getViewComponent(), GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 10, 0, 10));
			activeEntry = node.entry;
		}

		activePanelPath = menuTree.getSelectionPath();
		settingsNameLabel.setText(node.toString());
		setEnabledButtons();
		splitPane.setDividerLocation(treePanel.getPreferredSize().width);
	}

	public boolean requestChange() {
		if (activeEntry == null) 
			return true;

		if (activeEntry.isModified()) {			
			int res;
			if (config.getGuiConfig().isShowPreferencesConfirmDialog()) {
				ConfirmationCheckDialog dialog = new ConfirmationCheckDialog(getTopLevelAncestor(),
						Language.I18N.getString("pref.dialog.apply.title"),
						Language.I18N.getString("pref.dialog.apply.msg"));

				res = dialog.show();
				config.getGuiConfig().setShowPreferencesConfirmDialog(dialog.keepShowingDialog());
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

	@Override
	public void updateUI() {
		super.updateUI();

		if (rootNode != null) {
			updateUI(rootNode);
		}

		if (hintPanel != null) {
			SwingUtilities.updateComponentTreeUI(hintPanel);
		}
	}

	private void updateUI(PreferencesTreeNode node) {
		if (menuTree.getSelectionPath() == null
				|| menuTree.getSelectionPath().getLastPathComponent() != node) {
			try {
				SwingUtilities.updateComponentTreeUI(node.entry.getViewComponent());
			} catch (Exception e) {
				log.error("Failed to update UI for component '" + node.entry.getViewComponent() + "'.", e);
			}
		}

		for (int i = 0; i < node.getChildCount(); i++) {
			updateUI((PreferencesTreeNode) node.getChildAt(i));
		}
	}

	private static final class PreferencesTreeNode extends DefaultMutableTreeNode {
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
