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
package org.citydb.gui.operation.preferences.view;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.core.plugin.Plugin;
import org.citydb.core.plugin.PluginManager;
import org.citydb.core.plugin.PluginStateEvent;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.ScrollablePanel;
import org.citydb.gui.components.dialog.ConfirmationCheckDialog;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.operation.database.DatabasePlugin;
import org.citydb.gui.operation.exporter.CityGMLExportPlugin;
import org.citydb.gui.operation.importer.CityGMLImportPlugin;
import org.citydb.gui.operation.preferences.plugin.PluginsOverviewPlugin;
import org.citydb.gui.operation.preferences.preferences.GeneralPreferences;
import org.citydb.gui.operation.preferences.preferences.RootPreferencesEntry;
import org.citydb.gui.operation.visExporter.VisExportPlugin;
import org.citydb.gui.plugin.preferences.Preferences;
import org.citydb.gui.plugin.preferences.PreferencesEntry;
import org.citydb.gui.plugin.preferences.PreferencesEvent;
import org.citydb.gui.plugin.preferences.PreferencesExtension;
import org.citydb.gui.plugin.util.EmptyPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class PreferencesPanel extends JPanel implements TreeSelectionListener, EventHandler {
	private final Logger log = Logger.getInstance();
	private final ImpExpGui mainView;
	private final PluginManager pluginManager;
	private final List<PreferencesExtension> preferencesExtensions;
	private final Config config;

	private JSplitPane splitPane;
	private JScrollPane treeScrollPane;
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
	private volatile boolean isRebuildingPreferencesTree;

	public PreferencesPanel(ImpExpGui mainView, Config config) {
		this.mainView = mainView;
		this.config = config;

		pluginManager = PluginManager.getInstance();
		preferencesExtensions = new ArrayList<>();
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.PLUGIN_STATE, this);

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

		preferencesExtensions.add(pluginManager.getInternalPlugin(CityGMLImportPlugin.class));
		preferencesExtensions.add(pluginManager.getInternalPlugin(CityGMLExportPlugin.class));
		preferencesExtensions.add(pluginManager.getInternalPlugin(VisExportPlugin.class));

		for (PreferencesExtension extension : pluginManager.getExternalPlugins(PreferencesExtension.class)) {
			Preferences preferences = extension.getPreferences();
			if (preferences == null || preferences.getPreferencesEntry() == null) {
				log.error("Failed to get preference entry from plugin " + extension.getClass().getName() + ".");
				continue;
			}

			preferencesExtensions.add(extension);
		}

		preferencesExtensions.add(pluginManager.getInternalPlugin(DatabasePlugin.class));
		if (!pluginManager.getExternalPlugins().isEmpty()) {
			preferencesExtensions.add(pluginManager.getInternalPlugin(PluginsOverviewPlugin.class));
		}

		menuTree = new JTree();
		menuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		menuTree.addTreeSelectionListener(this);
		menuTree.setRootVisible(false);
		menuTree.setShowsRootHandles(true);
		menuTree.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		rootNode = new PreferencesTreeNode(new RootPreferencesEntry());
		buildPreferencesTree();

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

		treeScrollPane = new JScrollPane(menuTree) {
			@Override
			public Dimension getMinimumSize() {
				return menuTree.getPreferredSize();
			}
		};
		treeScrollPane.setBorder(BorderFactory.createEmptyBorder());
		treeScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

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

			JPanel buttons = new JPanel();
			buttons.setLayout(new GridBagLayout());
			{
				buttons.add(restoreButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 5, 0, 5, 5));
				buttons.add(standardButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
				buttons.add(applyButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.BOTH, 5, 5, 5, 0));
			}

			contentPanel.add(settingsContentPanel, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
			contentPanel.add(buttons, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 5, 10, 5, 10));
		}

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		splitPane.setLeftComponent(treeScrollPane);
		splitPane.setRightComponent(contentPanel);

		add(splitPane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
		setBorder(BorderFactory.createEmptyBorder());

		menuTree.setSelectionPath(new TreePath(rootNode));
		menuTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				splitPane.setDividerLocation(treeScrollPane.getPreferredSize().width);
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				splitPane.setDividerLocation(treeScrollPane.getPreferredSize().width);
			}
		});

		PopupMenuDecorator.getInstance().decorate(menuTree);
	}

	private void buildPreferencesTree() {
		Deque<PreferencesTreeNode> children = new ArrayDeque<>();
		Enumeration<TreePath> expanded = null;

		// store current tree state
		if (rootNode.getChildCount() > 0) {
			for (int i = 0; i < rootNode.getChildCount(); i++) {
				children.add((PreferencesTreeNode) rootNode.getChildAt(i));
			}

			expanded = menuTree.getExpandedDescendants(new TreePath(rootNode));
			isRebuildingPreferencesTree = true;
			rootNode.removeAllChildren();
		}

		// add preferences extensions
		for (PreferencesExtension preferencesExtension : preferencesExtensions) {
			if (((Plugin) preferencesExtension).isEnabled()) {
				PreferencesTreeNode child = children.stream()
						.filter(v -> v.entry == preferencesExtension.getPreferences().getPreferencesEntry())
						.findFirst()
						.orElse(null);

				if (child != null) {
					rootNode.add(child);
				} else {
					rootNode.add(preferencesExtension.getPreferences().getPreferencesEntry());
				}
			}
		}

		// add general preferences
		if (!children.isEmpty() && children.getLast().entry == generalPreferences.getPreferencesEntry()) {
			rootNode.add(children.getLast());
		} else {
			rootNode.add(generalPreferences.getPreferencesEntry());
		}

		menuTree.setModel(new DefaultTreeModel(rootNode));

		// restore tree state
		if (isRebuildingPreferencesTree) {
			if (expanded != null) {
				while (expanded.hasMoreElements()) {
					TreePath path = expanded.nextElement();
					if (!path.isDescendant(activePanelPath)) {
						Object[] pathElements = path.getPath();
						if (replaceFirstChild(pathElements, rootNode)) {
							menuTree.expandPath(new TreePath(pathElements));
						}
					}
				}
			}

			if (activePanelPath != null) {
				Object[] pathElements = activePanelPath.getPath();
				menuTree.setSelectionPath(replaceFirstChild(pathElements, rootNode) ?
						new TreePath(pathElements) :
						new TreePath(rootNode));
			}

			isRebuildingPreferencesTree = false;
		}
	}

	private boolean replaceFirstChild(Object[] pathElements, PreferencesTreeNode parent) {
		if (pathElements.length > 1) {
			for (int i = 0; i < parent.getChildCount(); i++) {
				PreferencesTreeNode child = (PreferencesTreeNode) parent.getChildAt(i);
				if (child.entry == ((PreferencesTreeNode) pathElements[1]).entry) {
					pathElements[1] = child;
					return true;
				}
			}
		}

		return false;
	}

	public void switchLocale() {
		restoreButton.setText(Language.I18N.getString("common.button.restore"));
		standardButton.setText(Language.I18N.getString("common.button.standard"));
		applyButton.setText(Language.I18N.getString("common.button.apply"));
		hintLabel.setText(Language.I18N.getString("common.pref.menu.expand.label"));

		resetPreferencesMenu();
		settingsNameLabel.setText(menuTree.getLastSelectedPathComponent().toString());
		splitPane.setDividerLocation(treeScrollPane.getPreferredSize().width);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		PreferencesTreeNode node = (PreferencesTreeNode)menuTree.getLastSelectedPathComponent();
		if (node == null || activePanelPath == menuTree.getSelectionPath())
			return;

		if (!isRebuildingPreferencesTree && !requestChange()) {
			menuTree.setSelectionPath(activePanelPath);
			return;
		}

		settingsContentPanel.removeAll();
		if (node.entry.getViewComponent() == null || node.entry.getViewComponent() == EmptyPreferencesComponent.getInstance()) {
			settingsContentPanel.add(hintPanel, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 10, 0, 10));
			activeEntry = null;
		} else {
			Component component;
			if (node.entry.getViewComponent().isScrollable()) {
				ScrollablePanel scrollablePanel = new ScrollablePanel(true, false);
				scrollablePanel.setLayout(new BorderLayout());
				scrollablePanel.add(node.entry.getViewComponent());

				component = new JScrollPane(scrollablePanel);
				((JScrollPane) component).setBorder(BorderFactory.createEmptyBorder());
				((JScrollPane) component).setViewportBorder(BorderFactory.createEmptyBorder());
			} else {
				component = node.entry.getViewComponent();
			}

			settingsContentPanel.add(component, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 0, 10, 0, 10));
			activeEntry = node.entry;
		}

		activePanelPath = menuTree.getSelectionPath();
		settingsNameLabel.setText(node.toString());
		setEnabledButtons();
		splitPane.setDividerLocation(treeScrollPane.getPreferredSize().width);
	}

	public boolean requestChange() {
		if (activeEntry == null) 
			return true;

		if (activeEntry.isModified()) {			
			int res;
			if (config.getGuiConfig().getPreferencesGuiConfig().isShowPreferencesConfirmDialog()) {
				ConfirmationCheckDialog dialog = ConfirmationCheckDialog.defaults()
						.withParentComponent(getTopLevelAncestor())
						.withTitle(Language.I18N.getString("pref.dialog.apply.title"))
						.addMessage(Language.I18N.getString("pref.dialog.apply.msg"));

				res = dialog.show();
				config.getGuiConfig().getPreferencesGuiConfig().setShowPreferencesConfirmDialog(dialog.keepShowingDialog());
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

	@Override
	public void handleEvent(Event event) throws Exception {
		if (((PluginStateEvent) event).getPlugins().stream().anyMatch(p -> p instanceof PreferencesExtension)) {
			SwingUtilities.invokeLater(this::buildPreferencesTree);
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
