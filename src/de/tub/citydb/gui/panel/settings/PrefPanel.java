package de.tub.citydb.gui.panel.settings;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.log.Logger;

public class PrefPanel extends JPanel implements TreeSelectionListener {
	private final Logger LOG = Logger.getInstance();

	private JTree menuTree;
	private JPanel col1;
	private JPanel col2;
	private JPanel col2panel;
	private JScrollPane scrollPane;
	private ImpExpGui parentFrame;

	private DefaultMutableTreeNode pref;
	private DefaultMutableTreeNode imp;
	private DefaultMutableTreeNode exp;
	private DefaultMutableTreeNode match;
	private DefaultMutableTreeNode set;
	private DefaultMutableTreeNode impContinuation;
	private DefaultMutableTreeNode impIdHandling;
	private DefaultMutableTreeNode impSpatialIndex;
	private DefaultMutableTreeNode impAppearance;
	private DefaultMutableTreeNode impBoundingBox;
	private DefaultMutableTreeNode impXMLValidation;
	private DefaultMutableTreeNode impResources;
	private DefaultMutableTreeNode expModule;
	private DefaultMutableTreeNode expAppearance;
	private DefaultMutableTreeNode expBoundingBox;
	private DefaultMutableTreeNode expXLink;
	private DefaultMutableTreeNode expResources;
	private DefaultMutableTreeNode setLogging;
	private DefaultMutableTreeNode matchResult;
	private DefaultMutableTreeNode matchGmlName;
	private DefaultMutableTreeNode matchDelete;
	
	private DefaultMutableTreeNode setPath;
	private DefaultMutableTreeNode setLanguage;

	private ImpContinuationPanel impContinuationPanel;
	private ImpIdHandlingPanel impIdHandlingPanel;
	private ImpIndexPanel impIndexPanel;
	private ImpAppearancePanel impAppearancePanel;
	private ImpBoundingBoxPanel impBoundingBoxPanel;
	private ImpXMLValidationPanel impXMLValidationPanel;
	private ImpResourcesPanel impResourcesPanel;
	private ExpModulePanel expModulePanel;
	private ExpAppearancePanel expAppearancePanel;
	private ExpBoundingBoxPanel expBoundingBoxPanel;
	private ExpXLinkPanel expXLinkPanel;
	private ExpResourcesPanel expResourcesPanel;
	private SetLoggingPanel setLoggingPanel;
	private MatchResultPanel matchResultPanel;
	private MatchGmlNamePanel matchGmlNamePanel;
	private MatchDeletePanel matchDeletePanel;
	
	private SetPathPanel setPathPanel;
	private SetLanguagePanel setLanguagePanel;

	private JLabel prefLabel;
	private JPanel noticePanel;
	private JLabel noticeLabel;
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
				if (activePanel != null) 
					activePanel.noSaveChanges();
			}
		});

		standardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activePanel != null)
					activePanel.resetSettings();
			}
		});

		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activePanel != null) {
					activePanel.saveChanges();
					if (parentFrame.saveIniFile())
						LOG.info("Settings successfully saved to config file '" + 
								new File(config.getInternal().getConfigPath()).getAbsolutePath() + File.separator + config.getInternal().getConfigProject() + "'.");
				}
			}
		});

		pref = new DefaultMutableTreeNode();
		imp = new DefaultMutableTreeNode();
		exp = new DefaultMutableTreeNode();
		match = new DefaultMutableTreeNode();
		set = new DefaultMutableTreeNode();
		impContinuation = new DefaultMutableTreeNode();
		impIdHandling = new DefaultMutableTreeNode();
		impSpatialIndex = new DefaultMutableTreeNode();
		impAppearance = new DefaultMutableTreeNode();
		impBoundingBox = new DefaultMutableTreeNode();
		impXMLValidation = new DefaultMutableTreeNode();
		impResources = new DefaultMutableTreeNode();
		expModule = new DefaultMutableTreeNode();
		expAppearance = new DefaultMutableTreeNode();
		expBoundingBox = new DefaultMutableTreeNode();
		expXLink = new DefaultMutableTreeNode();
		expResources = new DefaultMutableTreeNode();
		setLogging = new DefaultMutableTreeNode();
		matchGmlName = new DefaultMutableTreeNode();
		matchResult = new DefaultMutableTreeNode();
		matchDelete = new DefaultMutableTreeNode();
		setPath = new DefaultMutableTreeNode();
		setLanguage = new DefaultMutableTreeNode();
	
		pref.add(imp);
		pref.add(exp);
		pref.add(match);
		pref.add(set);
		imp.add(impContinuation);
		imp.add(impIdHandling);
		imp.add(impSpatialIndex);
		imp.add(impAppearance);
		imp.add(impBoundingBox);
		imp.add(impXMLValidation);
		imp.add(impResources);
		exp.add(expModule);
		exp.add(expAppearance);
		exp.add(expBoundingBox);
		exp.add(expXLink);
		exp.add(expResources);
		match.add(matchResult);
		match.add(matchGmlName);
		match.add(matchDelete);
		set.add(setLogging);
		set.add(setPath);
		set.add(setLanguage);

		menuTree = new JTree(pref);
		menuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		menuTree.addTreeSelectionListener(this);
		
		// get rid of icons
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		menuTree.setCellRenderer(renderer);
		
		impContinuationPanel = new ImpContinuationPanel(config);
		impIdHandlingPanel = new ImpIdHandlingPanel(config);
		impIndexPanel = new ImpIndexPanel(config, parentFrame);
		impAppearancePanel = new ImpAppearancePanel(config);
		impBoundingBoxPanel = new ImpBoundingBoxPanel(config);
		impXMLValidationPanel = new ImpXMLValidationPanel(config);
		impResourcesPanel = new ImpResourcesPanel(config);
		expModulePanel = new ExpModulePanel(config);
		expAppearancePanel = new ExpAppearancePanel(config);
		expBoundingBoxPanel = new ExpBoundingBoxPanel(config);
		expXLinkPanel = new ExpXLinkPanel(config);
		expResourcesPanel = new ExpResourcesPanel(config);
		matchResultPanel = new MatchResultPanel(config);
		matchGmlNamePanel = new MatchGmlNamePanel(config, parentFrame);
		matchDeletePanel = new MatchDeletePanel(config, parentFrame);		
		setLoggingPanel = new SetLoggingPanel(config);
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
				
				noticePanel = new JPanel();
				noticePanel.setBorder(BorderFactory.createEmptyBorder());
				noticePanel.setLayout(new GridBagLayout());
				{
					noticeLabel = new JLabel("");			
					noticePanel.add(noticeLabel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,0,0));
				}
				
				col2panel = new JPanel();
				col2panel.setBorder(BorderFactory.createEmptyBorder());
				col2panel.setLayout(new BorderLayout());

				scrollPane = new JScrollPane(col2panel);
				scrollPane.setBorder(BorderFactory.createEmptyBorder());

				col2.add(scrollPane, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
				
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
		
		menuTree.setSelectionPath(new TreePath(new Object[]{pref, imp}));
		for (int i = 1; i < menuTree.getRowCount(); i++)
			menuTree.collapseRow(i);
	}
	
	public void doTranslation() {
		restoreButton.setText(Internal.I18N.getString("pref.button.restore"));
		standardButton.setText(Internal.I18N.getString("pref.button.standard"));
		saveButton.setText(Internal.I18N.getString("pref.button.save"));
		noticeLabel.setText(Internal.I18N.getString("common.pref.menu.expand.label"));

		resetPreferencesMenu();
		menuTree.repaint();

		impContinuationPanel.doTranslation();
		impIdHandlingPanel.doTranslation();
		impIndexPanel.doTranslation();
		impAppearancePanel.doTranslation();
		impBoundingBoxPanel.doTranslation();
		impXMLValidationPanel.doTranslation();
		impResourcesPanel.doTranslation();
		expModulePanel.doTranslation();
		expAppearancePanel.doTranslation();
		expBoundingBoxPanel.doTranslation();
		expXLinkPanel.doTranslation();
		expResourcesPanel.doTranslation();
		setLoggingPanel.doTranslation();
		matchResultPanel.doTranslation();
		matchGmlNamePanel.doTranslation();
		matchDeletePanel.doTranslation();
		setPathPanel.doTranslation();
		setLanguagePanel.doTranslation();

		prefLabel.setText(((DefaultMutableTreeNode)menuTree.getLastSelectedPathComponent()).getUserObject().toString());
	}

	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)menuTree.getLastSelectedPathComponent();
		if (node == null || activePanelPath == menuTree.getSelectionPath())
			return;

		if (!requestChange()) {
			menuTree.setSelectionPath(activePanelPath);
			return;
		}

		col2panel.removeAll();
		
		if (node == impContinuation)
			activePanel = impContinuationPanel;
		else if (node == impIdHandling)
			activePanel = impIdHandlingPanel;
		else if (node == impSpatialIndex)
			activePanel = impIndexPanel;
		else if (node == impAppearance)
			activePanel = impAppearancePanel;
		else if (node == impBoundingBox)
			activePanel = impBoundingBoxPanel;
		else if (node == impXMLValidation)
			activePanel = impXMLValidationPanel;
		else if (node == impResources)
			activePanel = impResourcesPanel;
		else if (node == expModule)
			activePanel = expModulePanel;
		else if (node == expAppearance)
			activePanel = expAppearancePanel;
		else if (node == expBoundingBox)
			activePanel = expBoundingBoxPanel;
		else if (node == expXLink)
			activePanel = expXLinkPanel;
		else if (node == expResources)
			activePanel = expResourcesPanel;
		else if (node == matchResult)
			activePanel = matchResultPanel;
		else if (node == matchGmlName)
			activePanel = matchGmlNamePanel;
		else if (node == matchDelete)
			activePanel = matchDeletePanel;
		else if (node == setLogging)
			activePanel = setLoggingPanel;
		else if (node == setPath)
			activePanel = setPathPanel;
		else if (node == setLanguage)
			activePanel = setLanguagePanel;
		else {
			col2panel.add(noticePanel, BorderLayout.NORTH);
			activePanel = null;
		}

		if (activePanel != null)
			col2panel.add(activePanel, BorderLayout.NORTH);
		
		prefLabel.setText(node.getUserObject().toString());
		activePanelPath = menuTree.getSelectionPath();
		repaint();
	}

	public boolean requestChange() {
		if (activePanel == null) 
			return true;

		if (activePanel.isModified()) {
			int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), Internal.I18N.getString("pref.dialog.save.msg"), Internal.I18N.getString("pref.dialog.save.title"), JOptionPane.YES_NO_CANCEL_OPTION);
			if (res == JOptionPane.CANCEL_OPTION) 
				return false;
			else if (res == JOptionPane.YES_OPTION) {
				activePanel.saveChanges();
				parentFrame.saveIniFile();
			} else
				activePanel.noSaveChanges();
		}

		return true;
	}

	public void loadSettings() {
		impContinuationPanel.loadSettings();
		impIdHandlingPanel.loadSettings();
		impIndexPanel.loadSettings();
		impAppearancePanel.loadSettings();
		impBoundingBoxPanel.loadSettings();
		impXMLValidationPanel.loadSettings();
		impResourcesPanel.loadSettings();
		expModulePanel.loadSettings();
		expAppearancePanel.loadSettings();
		expBoundingBoxPanel.loadSettings();
		expXLinkPanel.loadSettings();
		expResourcesPanel.loadSettings();
		setLoggingPanel.loadSettings();
		matchResultPanel.loadSettings();
		matchGmlNamePanel.loadSettings();
		matchDeletePanel.loadSettings();
		setPathPanel.loadSettings();
		setLanguagePanel.loadSettings();
	}

	public void setSettings() {
		impContinuationPanel.setSettings();
		impIdHandlingPanel.setSettings();
		impIndexPanel.setSettings();
		impAppearancePanel.setSettings();
		impBoundingBoxPanel.setSettings();
		impXMLValidationPanel.setSettings();
		impResourcesPanel.setSettings();
		expModulePanel.setSettings();
		expAppearancePanel.setSettings();
		expBoundingBoxPanel.setSettings();
		expXLinkPanel.setSettings();
		expResourcesPanel.setSettings();
		setLoggingPanel.setSettings();
		matchResultPanel.setSettings();
		matchGmlNamePanel.setSettings();
		matchDeletePanel.setSettings();
		setPathPanel.setSettings();
		setLanguagePanel.setSettings();
	}

	public void setLoggingSettings() {
		setLoggingPanel.setSettings();
	}
	
	private void resetPreferencesMenu() {
		pref.setUserObject(Internal.I18N.getString("pref.tree.root"));
		imp.setUserObject(Internal.I18N.getString("pref.tree.import"));
		exp.setUserObject(Internal.I18N.getString("pref.tree.export"));
		match.setUserObject(Internal.I18N.getString("pref.tree.matching"));
		set.setUserObject(Internal.I18N.getString("pref.tree.general"));
		impContinuation.setUserObject(Internal.I18N.getString("pref.tree.import.continuation"));
		impIdHandling.setUserObject(Internal.I18N.getString("pref.tree.import.idHandling"));
		impSpatialIndex.setUserObject(Internal.I18N.getString("pref.tree.import.index"));
		impAppearance.setUserObject(Internal.I18N.getString("pref.tree.import.appearance"));
		impBoundingBox.setUserObject(Internal.I18N.getString("pref.tree.import.boundingBox"));
		impXMLValidation.setUserObject(Internal.I18N.getString("pref.tree.import.xmlValidation"));		
		impResources.setUserObject(Internal.I18N.getString("pref.tree.import.resources"));
		expModule.setUserObject(Internal.I18N.getString("pref.tree.export.module"));
		expAppearance.setUserObject(Internal.I18N.getString("pref.tree.export.appearance"));
		expBoundingBox.setUserObject(Internal.I18N.getString("pref.tree.export.boundingBox"));
		expXLink.setUserObject(Internal.I18N.getString("pref.tree.export.xlink"));
		expResources.setUserObject(Internal.I18N.getString("pref.tree.export.resources"));
		setLogging.setUserObject(Internal.I18N.getString("pref.tree.general.logging"));
		matchResult.setUserObject(Internal.I18N.getString("pref.tree.matching.result"));
		matchGmlName.setUserObject(Internal.I18N.getString("pref.tree.matching.gmlName"));
		matchDelete.setUserObject(Internal.I18N.getString("pref.tree.matching.delete"));
		setPath.setUserObject(Internal.I18N.getString("pref.tree.general.path"));
		setLanguage.setUserObject(Internal.I18N.getString("pref.tree.general.language"));

		nodesChanged((DefaultTreeModel)menuTree.getModel(), pref);
	}
	
	private void nodesChanged(DefaultTreeModel model, TreeNode node) {
		model.nodeChanged(node);
		for (int i = 0; i < node.getChildCount(); i++)
			nodesChanged(model, node.getChildAt(i));
	}
	
}
