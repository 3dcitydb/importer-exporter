package de.tub.citydb.gui.panel.settings;

import java.awt.BorderLayout;
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

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.log.Logger;

@SuppressWarnings("serial")
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
	private DefaultMutableTreeNode kmlExp;
	private DefaultMutableTreeNode match;
	private DefaultMutableTreeNode db;
	private DefaultMutableTreeNode set;
	private DefaultMutableTreeNode impContinuation;
	private DefaultMutableTreeNode impIdHandling;
	private DefaultMutableTreeNode impAppearance;
	private DefaultMutableTreeNode impBoundingBox;
	private DefaultMutableTreeNode impXMLValidation;
	private DefaultMutableTreeNode impResources;
	private DefaultMutableTreeNode expModule;
	private DefaultMutableTreeNode expAppearance;
	private DefaultMutableTreeNode expBoundingBox;
	private DefaultMutableTreeNode expXLink;
	private DefaultMutableTreeNode expResources;
	private DefaultMutableTreeNode kmlExpGeneral;
	private DefaultMutableTreeNode kmlExpAltitude;
	private DefaultMutableTreeNode kmlExpBalloon;
	private DefaultMutableTreeNode setLogging;
	private DefaultMutableTreeNode matchMaster;
	private DefaultMutableTreeNode matchCandidate;
	private DefaultMutableTreeNode dbIndex;
	private DefaultMutableTreeNode dbSrs;

	private DefaultMutableTreeNode setPath;
	private DefaultMutableTreeNode setLanguage;

	private ImpContinuationPanel impContinuationPanel;
	private ImpIdHandlingPanel impIdHandlingPanel;
	private ImpAppearancePanel impAppearancePanel;
	private ImpBoundingBoxPanel impBoundingBoxPanel;
	private ImpXMLValidationPanel impXMLValidationPanel;
	private ImpResourcesPanel impResourcesPanel;
	private ExpModulePanel expModulePanel;
	private ExpAppearancePanel expAppearancePanel;
	private ExpBoundingBoxPanel expBoundingBoxPanel;
	private ExpXLinkPanel expXLinkPanel;
	private ExpResourcesPanel expResourcesPanel;
	private KmlExpGeneralPanel kmlExpGeneralPanel;
	private KmlExpAltitudePanel kmlExpAltitudePanel;
	private KmlExpBalloonPanel kmlExpBalloonPanel;
	private SetLoggingPanel setLoggingPanel;
	private DbIndexPanel dbIndexPanel;
	private DbSrsPanel dbSrsPanel;
	private MatchMasterPanel matchMasterPanel;
	private MatchCandidatePanel matchCandidatePanel;

	private SetPathPanel setPathPanel;
	private SetLanguagePanel setLanguagePanel;

	private JLabel prefLabel;
	private JPanel noticePanel;
	private JLabel noticeLabel;
	private JButton restoreButton;
	private JButton standardButton;
	private JButton applyButton;
	private Config config;
	private PrefPanelBase activePanel;
	private TreePath activePanelPath;
	private JPanel confirmPanel;
	private JCheckBox confirmDialogNoShow;
	
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
		applyButton = new JButton("");

		restoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activePanel != null) 
					activePanel.loadSettings();
			}
		});

		standardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activePanel != null)
					activePanel.resetSettings();
			}
		});

		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activePanel != null) {
					activePanel.setSettings();
					LOG.info("Settings successfully applied.");
				}
			}
		});

		pref = new DefaultMutableTreeNode();
		imp = new DefaultMutableTreeNode();
		exp = new DefaultMutableTreeNode();
		kmlExp = new DefaultMutableTreeNode();
		match = new DefaultMutableTreeNode();
		db = new DefaultMutableTreeNode();
		set = new DefaultMutableTreeNode();
		impContinuation = new DefaultMutableTreeNode();
		impIdHandling = new DefaultMutableTreeNode();
		impAppearance = new DefaultMutableTreeNode();
		impBoundingBox = new DefaultMutableTreeNode();
		impXMLValidation = new DefaultMutableTreeNode();
		impResources = new DefaultMutableTreeNode();
		expModule = new DefaultMutableTreeNode();
		expAppearance = new DefaultMutableTreeNode();
		expBoundingBox = new DefaultMutableTreeNode();
		expXLink = new DefaultMutableTreeNode();
		expResources = new DefaultMutableTreeNode();
		kmlExpGeneral = new DefaultMutableTreeNode();
		kmlExpAltitude = new DefaultMutableTreeNode();
		kmlExpBalloon = new DefaultMutableTreeNode();
		setLogging = new DefaultMutableTreeNode();
		matchMaster = new DefaultMutableTreeNode();
		matchCandidate = new DefaultMutableTreeNode();
		dbIndex = new DefaultMutableTreeNode();
		dbSrs = new DefaultMutableTreeNode();
		setPath = new DefaultMutableTreeNode();
		setLanguage = new DefaultMutableTreeNode();

		pref.add(imp);
		pref.add(exp);
		pref.add(kmlExp);
		pref.add(match);
		pref.add(db);
		pref.add(set);
		imp.add(impContinuation);
		imp.add(impIdHandling);
		imp.add(impAppearance);
		imp.add(impBoundingBox);
		imp.add(impXMLValidation);
		imp.add(impResources);
		exp.add(expModule);
		exp.add(expAppearance);
		exp.add(expBoundingBox);
		exp.add(expXLink);
		exp.add(expResources);
		kmlExp.add(kmlExpGeneral);
		kmlExp.add(kmlExpBalloon);
		kmlExp.add(kmlExpAltitude);
		match.add(matchMaster);
		match.add(matchCandidate);
		db.add(dbIndex);
		db.add(dbSrs);
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
		impAppearancePanel = new ImpAppearancePanel(config);
		impBoundingBoxPanel = new ImpBoundingBoxPanel(config);
		impXMLValidationPanel = new ImpXMLValidationPanel(config);
		impResourcesPanel = new ImpResourcesPanel(config);
		expModulePanel = new ExpModulePanel(config);
		expAppearancePanel = new ExpAppearancePanel(config);
		expBoundingBoxPanel = new ExpBoundingBoxPanel(config);
		expXLinkPanel = new ExpXLinkPanel(config);
		expResourcesPanel = new ExpResourcesPanel(config);
		kmlExpGeneralPanel = new KmlExpGeneralPanel(config, parentFrame);
		kmlExpAltitudePanel = new KmlExpAltitudePanel(config);
		kmlExpBalloonPanel = new KmlExpBalloonPanel(config);
		matchMasterPanel = new MatchMasterPanel(config);
		matchCandidatePanel = new MatchCandidatePanel(config);
		dbIndexPanel = new DbIndexPanel(config, parentFrame);
		dbSrsPanel = new DbSrsPanel(config, parentFrame);
		setLoggingPanel = new SetLoggingPanel(config, parentFrame);
		setPathPanel = new SetPathPanel(config);
		setLanguagePanel = new SetLanguagePanel(config, parentFrame);

		// confirm dialog contents
		
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

		menuTree.setSelectionPath(new TreePath(new Object[]{pref, imp}));
		for (int i = 1; i < menuTree.getRowCount(); i++)
			menuTree.collapseRow(i);
	}

	public void doTranslation() {
		restoreButton.setText(Internal.I18N.getString("pref.button.restore"));
		standardButton.setText(Internal.I18N.getString("pref.button.standard"));
		applyButton.setText(Internal.I18N.getString("pref.button.apply"));
		noticeLabel.setText(Internal.I18N.getString("common.pref.menu.expand.label"));

		resetPreferencesMenu();
		menuTree.repaint();

		impContinuationPanel.doTranslation();
		impIdHandlingPanel.doTranslation();
		impAppearancePanel.doTranslation();
		impBoundingBoxPanel.doTranslation();
		impXMLValidationPanel.doTranslation();
		impResourcesPanel.doTranslation();
		expModulePanel.doTranslation();
		expAppearancePanel.doTranslation();
		expBoundingBoxPanel.doTranslation();
		expXLinkPanel.doTranslation();
		expResourcesPanel.doTranslation();
		kmlExpGeneralPanel.doTranslation();
		kmlExpAltitudePanel.doTranslation();
		kmlExpBalloonPanel.doTranslation();
		setLoggingPanel.doTranslation();
		matchMasterPanel.doTranslation();
		matchCandidatePanel.doTranslation();
		dbIndexPanel.doTranslation();
		dbSrsPanel.doTranslation();
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
		else if (node == kmlExpGeneral)
			activePanel = kmlExpGeneralPanel;
		else if (node == kmlExpAltitude)
			activePanel = kmlExpAltitudePanel;
		else if (node == kmlExpBalloon)
			activePanel = kmlExpBalloonPanel;
		else if (node == matchMaster)
			activePanel = matchMasterPanel;
		else if (node == matchCandidate)
			activePanel = matchCandidatePanel;
		else if (node == dbIndex)
			activePanel = dbIndexPanel;
		else if (node == dbSrs)
			activePanel = dbSrsPanel;
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

		revalidate();
		prefLabel.setText(node.getUserObject().toString());
		activePanelPath = menuTree.getSelectionPath();
		setEnabledButtons();
		repaint();
	}

	public boolean requestChange() {
		if (activePanel == null) 
			return true;

		if (activePanel.isModified()) {			
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
				activePanel.setSettings();
			} else
				activePanel.loadSettings();
		}

		return true;
	}

	public void loadSettings() {
		impContinuationPanel.loadSettings();
		impIdHandlingPanel.loadSettings();
		impAppearancePanel.loadSettings();
		impBoundingBoxPanel.loadSettings();
		impXMLValidationPanel.loadSettings();
		impResourcesPanel.loadSettings();
		expModulePanel.loadSettings();
		expAppearancePanel.loadSettings();
		expBoundingBoxPanel.loadSettings();
		expXLinkPanel.loadSettings();
		expResourcesPanel.loadSettings();
		kmlExpGeneralPanel.loadSettings();
		kmlExpAltitudePanel.loadSettings();
		kmlExpBalloonPanel.loadSettings();
		setLoggingPanel.loadSettings();
		matchMasterPanel.loadSettings();
		matchCandidatePanel.loadSettings();
		dbIndexPanel.loadSettings();
		dbSrsPanel.loadSettings();
		setPathPanel.loadSettings();
		setLanguagePanel.loadSettings();
	}

	public void setSettings() {
		impContinuationPanel.setSettings();
		impIdHandlingPanel.setSettings();
		impAppearancePanel.setSettings();
		impBoundingBoxPanel.setSettings();
		impXMLValidationPanel.setSettings();
		impResourcesPanel.setSettings();
		expModulePanel.setSettings();
		expAppearancePanel.setSettings();
		expBoundingBoxPanel.setSettings();
		expXLinkPanel.setSettings();
		expResourcesPanel.setSettings();
		kmlExpGeneralPanel.setSettings();
		kmlExpAltitudePanel.setSettings();
		kmlExpBalloonPanel.setSettings();
		setLoggingPanel.setSettings();
		matchMasterPanel.setSettings();
		matchCandidatePanel.setSettings();
		dbIndexPanel.setSettings();
		dbSrsPanel.setSettings();
		setPathPanel.setSettings();
		setLanguagePanel.setSettings();
	}

	public void setLoggingSettings() {
		setLoggingPanel.setSettings();
	}

	private void setEnabledButtons() {
		restoreButton.setEnabled(activePanel != null);
		standardButton.setEnabled(activePanel != null);
		applyButton.setEnabled(activePanel != null);
	}

	private void resetPreferencesMenu() {
		pref.setUserObject(Internal.I18N.getString("pref.tree.root"));
		imp.setUserObject(Internal.I18N.getString("pref.tree.import"));
		exp.setUserObject(Internal.I18N.getString("pref.tree.export"));
		kmlExp.setUserObject(Internal.I18N.getString("pref.tree.kmlExport"));
		match.setUserObject(Internal.I18N.getString("pref.tree.matching"));
		db.setUserObject(Internal.I18N.getString("pref.tree.database"));
		set.setUserObject(Internal.I18N.getString("pref.tree.general"));
		impContinuation.setUserObject(Internal.I18N.getString("pref.tree.import.continuation"));
		impIdHandling.setUserObject(Internal.I18N.getString("pref.tree.import.idHandling"));
		impAppearance.setUserObject(Internal.I18N.getString("pref.tree.import.appearance"));
		impBoundingBox.setUserObject(Internal.I18N.getString("pref.tree.import.boundingBox"));
		impXMLValidation.setUserObject(Internal.I18N.getString("pref.tree.import.xmlValidation"));		
		impResources.setUserObject(Internal.I18N.getString("pref.tree.import.resources"));
		expModule.setUserObject(Internal.I18N.getString("pref.tree.export.module"));
		expAppearance.setUserObject(Internal.I18N.getString("pref.tree.export.appearance"));
		expBoundingBox.setUserObject(Internal.I18N.getString("pref.tree.export.boundingBox"));
		expXLink.setUserObject(Internal.I18N.getString("pref.tree.export.xlink"));
		expResources.setUserObject(Internal.I18N.getString("pref.tree.export.resources"));
		kmlExpGeneral.setUserObject(Internal.I18N.getString("pref.tree.kmlExport.general"));
		kmlExpAltitude.setUserObject(Internal.I18N.getString("pref.tree.kmlExport.altitude"));
		kmlExpBalloon.setUserObject(Internal.I18N.getString("pref.tree.kmlExport.balloon"));
		setLogging.setUserObject(Internal.I18N.getString("pref.tree.general.logging"));
		matchMaster.setUserObject(Internal.I18N.getString("pref.tree.matching.master"));
		matchCandidate.setUserObject(Internal.I18N.getString("pref.tree.matching.candidate"));
		dbIndex.setUserObject(Internal.I18N.getString("pref.tree.import.index"));
		dbSrs.setUserObject(Internal.I18N.getString("pref.tree.db.srs"));
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
