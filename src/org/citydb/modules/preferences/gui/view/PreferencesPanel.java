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
package org.citydb.modules.preferences.gui.view;

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
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.citydb.api.plugin.extension.preferences.PreferencesEntry;
import org.citydb.api.plugin.extension.preferences.PreferencesEvent;
import org.citydb.api.plugin.extension.preferences.PreferencesExtension;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.NullComponent;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.exporter.CityGMLExportPlugin;
import org.citydb.modules.citygml.importer.CityGMLImportPlugin;
import org.citydb.modules.database.DatabasePlugin;
import org.citydb.modules.kml.KMLExportPlugin;
import org.citydb.modules.preferences.gui.preferences.GeneralPreferences;
import org.citydb.modules.preferences.gui.preferences.RootPreferencesEntry;
import org.citydb.plugin.PluginService;
import org.citydb.util.gui.GuiUtil;

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

		rootNode.add(pluginService.getInternalPlugin(CityGMLImportPlugin.class).getPreferences().getPreferencesEntry());
		rootNode.add(pluginService.getInternalPlugin(CityGMLExportPlugin.class).getPreferences().getPreferencesEntry());
		rootNode.add(pluginService.getInternalPlugin(KMLExportPlugin.class).getPreferences().getPreferencesEntry());

		for (PreferencesExtension extension : pluginService.getExternalPreferencesExtensions())
			rootNode.add(extension.getPreferences().getPreferencesEntry());	

		rootNode.add(pluginService.getInternalPlugin(DatabasePlugin.class).getPreferences().getPreferencesEntry());
		rootNode.add(generalPreferences.getPreferencesEntry());

		menuTree = new JTree(rootNode);
		menuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		menuTree.addTreeSelectionListener(this);
		menuTree.setRootVisible(false);
		menuTree.setShowsRootHandles(true);
		
		// get rid of icons
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		menuTree.setCellRenderer(renderer);
		
		// set row height
		int padding = menuTree.getRowHeight() - UIManager.getFont("Tree.font").getSize();
		menuTree.setRowHeight(UIManager.getFont("Tree.font").getSize() + Math.max(padding, 5));

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
				col1.add(scroll, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,9,4,4,4));
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
			performActionOnNodes(menuTree.getPathForRow(i), true, true);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setBorder(BorderFactory.createEmptyBorder());

		splitPane.setLeftComponent(col1);
		splitPane.setRightComponent(col2);
		splitPane.setDividerLocation(menuTree.getPreferredSize().width + 10);
		splitPane.setDividerSize(1);
		
		add(splitPane, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));
		setBorder(BorderFactory.createEmptyBorder());

		menuTree.setSelectionPath(new TreePath(new Object[]{rootNode}));
		for (int i = 0; i < menuTree.getRowCount(); i++)
			performActionOnNodes(menuTree.getPathForRow(i), false, true);

		PopupMenuDecorator.getInstance().decorate(menuTree);
	}

	public void doTranslation() {
		restoreButton.setText(Language.I18N.getString("pref.button.restore"));
		standardButton.setText(Language.I18N.getString("pref.button.standard"));
		applyButton.setText(Language.I18N.getString("common.button.apply"));
		noticeLabel.setText(Language.I18N.getString("common.pref.menu.expand.label"));

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
				confirmDialogNoShow = new JCheckBox(Language.I18N.getString("common.dialog.msg.noShow"));
				confirmDialogNoShow.setIconTextGap(10);
				confirmPanel.add(new JLabel(Language.I18N.getString("pref.dialog.apply.msg")), GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
				confirmPanel.add(confirmDialogNoShow, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,10,0,0,0));

				res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), 
						confirmPanel, 
						Language.I18N.getString("pref.dialog.apply.title"), 
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

	private void performActionOnNodes(TreePath parent, boolean expand, boolean recursive) {
		TreeNode node = (TreeNode)parent.getLastPathComponent();

		if (recursive)
			for (int i = 0; i < node.getChildCount(); ++i)
				performActionOnNodes(parent.pathByAddingChild(node.getChildAt(i)), expand, recursive);

		if (expand)
			menuTree.expandPath(parent);
		else
			menuTree.collapsePath(parent);
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
