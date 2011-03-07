package de.tub.citydb.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.tub.citydb.config.Config;

public class PrefPanel extends JPanel implements TreeSelectionListener {

	private JTree menuTree;
	private JPanel col1;
	private JPanel col2;
	private JPanel col2panel;
	private ImpExpGui parentFrame;

	private DefaultMutableTreeNode pref;
	private DefaultMutableTreeNode imp;
	private DefaultMutableTreeNode exp;
	private DefaultMutableTreeNode set;
	private DefaultMutableTreeNode impContinuation;
	private DefaultMutableTreeNode impIdHandling;
	private DefaultMutableTreeNode impSpatialIndex;
	private DefaultMutableTreeNode impAppearance;
	private DefaultMutableTreeNode impBoundingBox;
	private DefaultMutableTreeNode impResources;
	private DefaultMutableTreeNode expModule;
	private DefaultMutableTreeNode expAppearance;
	private DefaultMutableTreeNode expBoundingBox;
	private DefaultMutableTreeNode expResources;
	private DefaultMutableTreeNode setPath;
	private DefaultMutableTreeNode setLanguage;

	private ImpContinuationPanel impContinuationPanel;
	private ImpIdHandlingPanel impIdHandlingPanel;
	private ImpIndexPanel impIndexPanel;
	private ImpAppearancePanel impAppearancePanel;
	private ImpBoundingBoxPanel impBoundingBoxPanel;
	private ImpResourcesPanel impResourcesPanel;
	private ExpModulePanel expModulePanel;
	private ExpAppearancePanel expAppearancePanel;
	private ExpBoundingBoxPanel expBoundingBoxPanel;
	private ExpResourcesPanel expResourcesPanel;
	private SetPathPanel setPathPanel;
	private SetLanguagePanel setLanguagePanel;

	private JLabel prefLabel;
	private JButton restoreButton;
	private JButton standardButton;
	private JButton saveButton;
	private Config config;
	private PrefPanelBase activePanel;
	private TreePath activePanelPath;

	public PrefPanel(Config config, ImpExpGui pFrame) {
		this.config = config;
		activePanel = null;
		activePanelPath = null;
		parentFrame = pFrame;
		initGui();
	}

	private void initGui() {
		restoreButton = new JButton("");
		standardButton = new JButton("");
		saveButton = new JButton("");

		restoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activePanel!=null) activePanel.noSaveChanges();
			}
		});

		standardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activePanel!=null) {
					activePanel.resetSettings();
				}
			}
		});

		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activePanel!=null) {
					activePanel.saveChanges();
					parentFrame.saveIniFile();
				}
			}
		});

		// Menüauswahl
		pref = new DefaultMutableTreeNode("seehr langer Platzhalter");
		imp = new DefaultMutableTreeNode("seehr langer Platzhalter");
		exp = new DefaultMutableTreeNode("seehr langer Platzhalter");
		set = new DefaultMutableTreeNode("seehr langer Platzhalter");
		impContinuation = new DefaultMutableTreeNode("seehr langer Platzhalter");
		impIdHandling = new DefaultMutableTreeNode("seehr langer Platzhalter");
		impSpatialIndex = new DefaultMutableTreeNode("seehr langer Platzhalter");
		impAppearance = new DefaultMutableTreeNode("seehr langer Platzhalter");
		impBoundingBox = new DefaultMutableTreeNode("seehr langer Platzhalter");
		impResources = new DefaultMutableTreeNode("seehr langer Platzhalter");
		expModule = new DefaultMutableTreeNode("seehr langer Platzhalter");
		expAppearance = new DefaultMutableTreeNode("seehr langer Platzhalter");
		expBoundingBox = new DefaultMutableTreeNode("seehr langer Platzhalter");
		expResources = new DefaultMutableTreeNode("seehr langer Platzhalter");
		setPath = new DefaultMutableTreeNode("seehr langer Platzhalter");
		setLanguage = new DefaultMutableTreeNode("seehr langer Platzhalter");

		pref.add(imp);
		pref.add(exp);
		pref.add(set);
		imp.add(impContinuation);
		imp.add(impIdHandling);
		imp.add(impSpatialIndex);
		imp.add(impAppearance);
		imp.add(impBoundingBox);
		imp.add(impResources);
		exp.add(expModule);
		exp.add(expAppearance);
		exp.add(expBoundingBox);
		exp.add(expResources);
		set.add(setPath);
		set.add(setLanguage);
		Object[] impContinuationPath = {pref, imp, impContinuation};
		menuTree = new JTree(pref);
		menuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		menuTree.addTreeSelectionListener(this);

		// get rid of icons
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);

		menuTree.setCellRenderer(renderer);
		expandAll();

		//fehlende Panels befüllen
		impContinuationPanel = new ImpContinuationPanel(config);
		impIdHandlingPanel = new ImpIdHandlingPanel(config);
		impIndexPanel = new ImpIndexPanel(config, parentFrame);
		impAppearancePanel = new ImpAppearancePanel(config);
		impBoundingBoxPanel = new ImpBoundingBoxPanel(config);
		impResourcesPanel = new ImpResourcesPanel(config);
		expModulePanel = new ExpModulePanel(config);
		expAppearancePanel = new ExpAppearancePanel(config);
		expBoundingBoxPanel = new ExpBoundingBoxPanel(config);
		expResourcesPanel = new ExpResourcesPanel(config);
		setPathPanel = new SetPathPanel(config);
		setLanguagePanel = new SetLanguagePanel(config, parentFrame);

		//layout
		setLayout(new GridBagLayout());
		{			
			col1 = new JPanel();
			col1.setBackground(menuTree.getBackground());
			col1.setLayout(new GridBagLayout());
			{
				JScrollPane scroll = new JScrollPane(menuTree);
				scroll.setBorder(BorderFactory.createEmptyBorder());
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

				col2panel = new JPanel();

				JScrollPane scroll = new JScrollPane(col2panel);
				scroll.setBorder(BorderFactory.createEmptyBorder());

				col2.add(scroll, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
				col2panel.setBorder(BorderFactory.createEmptyBorder());
				col2panel.setLayout(new BorderLayout());

				JPanel col2buttons = new JPanel();
				col2.add(col2buttons, GuiUtil.setConstraints(0,4,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
				col2buttons.setLayout(new GridBagLayout());
				{
					col2buttons.add(restoreButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
					col2buttons.add(standardButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
					col2buttons.add(saveButton, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
				}
			}
		}

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setBorder(BorderFactory.createEtchedBorder());

		splitPane.setLeftComponent(col1);
		splitPane.setRightComponent(col2);

		this.add(splitPane, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,2,2,2,2));
		menuTree.setSelectionPath(new TreePath(impContinuationPath));
		setBorder(BorderFactory.createEmptyBorder());
	}

	public void doTranslation() {
		restoreButton.setText(ImpExpGui.labels.getString("pref.button.restore"));
		standardButton.setText(ImpExpGui.labels.getString("pref.button.standard"));
		saveButton.setText(ImpExpGui.labels.getString("pref.button.save"));

		//Baumknoten umbenennen
		pref.setUserObject(ImpExpGui.labels.getString("pref.tree.root"));
		imp.setUserObject(ImpExpGui.labels.getString("pref.tree.import"));
		exp.setUserObject(ImpExpGui.labels.getString("pref.tree.export"));
		set.setUserObject(ImpExpGui.labels.getString("pref.tree.general"));
		impContinuation.setUserObject(ImpExpGui.labels.getString("pref.tree.import.continuation"));
		impIdHandling.setUserObject(ImpExpGui.labels.getString("pref.tree.import.idHandling"));
		impSpatialIndex.setUserObject(ImpExpGui.labels.getString("pref.tree.import.index"));
		impAppearance.setUserObject(ImpExpGui.labels.getString("pref.tree.import.appearance"));
		impBoundingBox.setUserObject(ImpExpGui.labels.getString("pref.tree.import.boundingBox"));
		impResources.setUserObject(ImpExpGui.labels.getString("pref.tree.import.resources"));
		expModule.setUserObject(ImpExpGui.labels.getString("pref.tree.export.module"));
		expAppearance.setUserObject(ImpExpGui.labels.getString("pref.tree.export.appearance"));
		expBoundingBox.setUserObject(ImpExpGui.labels.getString("pref.tree.export.boundingBox"));
		expResources.setUserObject(ImpExpGui.labels.getString("pref.tree.export.resources"));
		setPath.setUserObject(ImpExpGui.labels.getString("pref.tree.general.path"));
		setLanguage.setUserObject(ImpExpGui.labels.getString("pref.tree.general.language"));
		menuTree.repaint();

		//do Translation-Methoden der Subklassen aufrufen
		impContinuationPanel.doTranslation();
		impIdHandlingPanel.doTranslation();
		impIndexPanel.doTranslation();
		impAppearancePanel.doTranslation();
		impBoundingBoxPanel.doTranslation();
		impResourcesPanel.doTranslation();
		expModulePanel.doTranslation();
		expAppearancePanel.doTranslation();
		expBoundingBoxPanel.doTranslation();
		expResourcesPanel.doTranslation();
		setPathPanel.doTranslation();
		setLanguagePanel.doTranslation();

		prefLabel.setText(((DefaultMutableTreeNode)menuTree.getLastSelectedPathComponent()).getUserObject().toString());
	}

	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)menuTree.getLastSelectedPathComponent();
		//nichts wurde selektiert
		if (node == null) return;
		if (activePanelPath==menuTree.getSelectionPath()) return;

		menuTree.repaint();

		if (!requestChange()) {
			menuTree.setSelectionPath(activePanelPath);
			return;
		}

		//aktives Panel durch gewähltes Panel ersetzen
		col2panel.removeAll();
		//activePanelName = node.toString();
		if (node == impContinuation){
			col2panel.add(impContinuationPanel, BorderLayout.NORTH);
			activePanel = impContinuationPanel;
		}
		if (node == impIdHandling){
			col2panel.add(impIdHandlingPanel, BorderLayout.NORTH);
			activePanel = impIdHandlingPanel;
		}
		if (node == impSpatialIndex){
			col2panel.add(impIndexPanel, BorderLayout.NORTH);
			activePanel = impIndexPanel;
		}
		if (node == impAppearance){
			col2panel.add(impAppearancePanel, BorderLayout.NORTH);
			activePanel = impAppearancePanel;
		}
		if (node == impBoundingBox){
			col2panel.add(impBoundingBoxPanel, BorderLayout.NORTH);
			activePanel = impBoundingBoxPanel;
		}
		if (node == impResources){
			col2panel.add(impResourcesPanel, BorderLayout.NORTH);
			activePanel = impResourcesPanel;
		}
		if (node == expModule) {
			col2panel.add(expModulePanel, BorderLayout.NORTH);
			activePanel = expModulePanel;
		}
		if (node == expAppearance){
			col2panel.add(expAppearancePanel, BorderLayout.NORTH);
			activePanel = expAppearancePanel;
		}
		if (node == expBoundingBox){
			col2panel.add(expBoundingBoxPanel, BorderLayout.NORTH);
			activePanel = expBoundingBoxPanel;
		}
		if (node == expResources){
			col2panel.add(expResourcesPanel, BorderLayout.NORTH);
			activePanel = expResourcesPanel;
		}
		if (node == setPath){
			col2panel.add(setPathPanel, BorderLayout.NORTH);
			activePanel = setPathPanel;
		}
		if (node == setLanguage){
			col2panel.add(setLanguagePanel, BorderLayout.NORTH);
			activePanel = setLanguagePanel;
		}
		revalidate();
		prefLabel.setText(node.getUserObject().toString());
		activePanelPath = menuTree.getSelectionPath();
		repaint();
	}

	public boolean requestChange() {
		if (activePanel==null) return true;
		if (activePanel.isModified()) {
			int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), ImpExpGui.labels.getString("pref.dialog.save.msg"), ImpExpGui.labels.getString("pref.dialog.save.title"), JOptionPane.YES_NO_CANCEL_OPTION);
			if (res==JOptionPane.CANCEL_OPTION) return false;
			if (res==JOptionPane.YES_OPTION) {
				activePanel.saveChanges();
				parentFrame.saveIniFile();
			} else {
				activePanel.noSaveChanges();
			}
		}
		return true;
	}


	public void loadSettings() {
		impContinuationPanel.loadSettings();
		impIdHandlingPanel.loadSettings();
		impIndexPanel.loadSettings();
		impAppearancePanel.loadSettings();
		impBoundingBoxPanel.loadSettings();
		impResourcesPanel.loadSettings();
		expModulePanel.loadSettings();
		expAppearancePanel.loadSettings();
		expBoundingBoxPanel.loadSettings();
		expResourcesPanel.loadSettings();
		setPathPanel.loadSettings();
		setLanguagePanel.loadSettings();
	}

	public void setSettings() {
		impContinuationPanel.setSettings();
		impIdHandlingPanel.setSettings();
		impIndexPanel.setSettings();
		impAppearancePanel.setSettings();
		impBoundingBoxPanel.setSettings();
		impResourcesPanel.setSettings();
		expModulePanel.setSettings();
		expAppearancePanel.setSettings();
		expBoundingBoxPanel.setSettings();
		expResourcesPanel.setSettings();
		setPathPanel.setSettings();
		setLanguagePanel.setSettings();
	}

	/**
	 * Expand completely a tree
	 */
	private void expandAll() {
		expandSubTree(menuTree.getPathForRow(0));
	}

	private void expandSubTree(TreePath path) {
		menuTree.expandPath(path);
		Object node = path.getLastPathComponent();
		int childrenNumber = menuTree.getModel().getChildCount(node);
		TreePath[] childrenPath = new TreePath[childrenNumber];
		for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
			childrenPath[childIndex] = path.pathByAddingChild(menuTree.getModel().getChild(node, childIndex));
			expandSubTree(childrenPath[childIndex]);
		}
	}

}
