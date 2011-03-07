package de.tub.citydb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBUpdateBatching;
import de.tub.citydb.config.project.system.SysGmlIdLookupServerConfig;
import de.tub.citydb.config.project.system.SysThreadPoolConfig;
import de.tub.citydb.gui.components.DigitsOnlyDocument;

public class ImpResourcesPanel extends PrefPanelBase{

	//Variablendefinition
	private JPanel block1;
	private JPanel block2;
	private JPanel block3;
	private JLabel impResMinThreadsLabel;
	private JTextField impResMinThreadsText;
	private JLabel impResMaxThreadsLabel;
	private JTextField impResMaxThreadsText;
	private JLabel impResTransaktLabel;
	private JTextField impResTransaktFeatureText;
	private JLabel impResTransaktFeatureLabel;	
	private JTextField impResTransaktCacheText;
	private JLabel impResTransaktCacheLabel;
	private JTextField impResTransaktTempText;
	private JLabel impResTransaktTempLabel;
	private JLabel impResGeomLabel;
	private JTextField impResGeomCacheText;
	private JLabel impResGeomCacheLabel;	
	private JTextField impResGeomDrainText;
	private JLabel impResGeomDrainLabel;
	private JLabel impResGeomPartLabel;
	private JTextField impResGeomPartText;
	private JLabel impResFeatLabel;
	private JTextField impResFeatCacheText;
	private JLabel impResFeatCacheLabel;
	private JTextField impResFeatDrainText;
	private JLabel impResFeatDrainLabel;
	private JLabel impResFeatPartLabel;
	private JTextField impResFeatPartText;
	
	//Konstruktor
	public ImpResourcesPanel(Config inpConfig) {
		super(inpConfig);
		initGui();
	}

	public boolean isModified() {
		if (super.isModified()) return true;

		SysThreadPoolConfig threadPool = config.getProject().getImporter().getSystem().getThreadPool().getDefaultPool();
		DBUpdateBatching commit = config.getProject().getDatabase().getUpdateBatching();
		SysGmlIdLookupServerConfig geometry = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getGeometry();
		SysGmlIdLookupServerConfig feature = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getFeature();

		try {
			if (!Integer.valueOf(impResMinThreadsText.getText()).equals(threadPool.getMinThreads())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(impResMaxThreadsText.getText()).equals(threadPool.getMaxThreads())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(impResTransaktFeatureText.getText()).equals(commit.getFeatureBatchValue())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(impResTransaktCacheText.getText()).equals(commit.getGmlIdLookupServerBatchValue())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}
		
		try {
			if (!Integer.valueOf(impResTransaktTempText.getText()).equals(commit.getTempBatchValue())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}
		
		try {
			if (!Integer.valueOf(impResGeomCacheText.getText()).equals(geometry.getCacheSize())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Float.valueOf(impResGeomDrainText.getText()).equals(geometry.getPageFactor() * 100)) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(impResGeomPartText.getText()).equals(geometry.getPartitions())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}
		
		try {
			if (!Integer.valueOf(impResFeatCacheText.getText()).equals(feature.getCacheSize())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Float.valueOf(impResFeatDrainText.getText()).equals(feature.getPageFactor() * 100)) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}
		
		try {
			if (!Integer.valueOf(impResFeatPartText.getText()).equals(feature.getPartitions())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		return false;

	}


	//InitGui-Methode
	public void initGui(){
		//Variablendeklaration
		block1 = new JPanel();
		block2 = new JPanel();
		block3 = new JPanel();
		impResMinThreadsLabel = new JLabel("");
		impResMinThreadsText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		impResMaxThreadsLabel = new JLabel("");
		impResMaxThreadsText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		impResTransaktLabel = new JLabel("");
		impResTransaktFeatureText = new JTextField(new DigitsOnlyDocument(5), "" , 0);
		impResTransaktFeatureLabel = new JLabel("");	
		impResTransaktCacheText = new JTextField(new DigitsOnlyDocument(5), "" , 0);
		impResTransaktCacheLabel = new JLabel("");
		impResTransaktTempText = new JTextField(new DigitsOnlyDocument(5), "" , 0);
		impResTransaktTempLabel = new JLabel("");
		impResGeomLabel = new JLabel("");
		impResGeomCacheText = new JTextField(new DigitsOnlyDocument(), "" , 0);
		impResGeomCacheLabel = new JLabel("");	
		impResGeomDrainText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		impResGeomDrainLabel = new JLabel("");	
		impResGeomPartText = new JTextField(new DigitsOnlyDocument(3), "", 0);
		impResGeomPartLabel = new JLabel("");
		impResFeatLabel = new JLabel("");		
		impResFeatCacheText = new JTextField(new DigitsOnlyDocument(), "" , 0);
		impResFeatCacheLabel = new JLabel("");
		impResFeatDrainText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		impResFeatDrainLabel = new JLabel("");
		impResFeatPartText = new JTextField(new DigitsOnlyDocument(3), "", 0);
		impResFeatPartLabel = new JLabel("");

		//Layout
		setLayout(new GridBagLayout());
		add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block1.setBorder(BorderFactory.createTitledBorder(""));
		block1.setLayout(new GridBagLayout());
		{
			block1.add(impResMinThreadsLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(impResMinThreadsText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(impResMaxThreadsLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(impResMaxThreadsText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
		add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block2.setBorder(BorderFactory.createTitledBorder(""));
		block2.setLayout(new GridBagLayout());
		{
			block2.add(impResTransaktLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktFeatureText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktFeatureLabel, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktCacheText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktCacheLabel, GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktTempText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(impResTransaktTempLabel, GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
		add(block3, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block3.setBorder(BorderFactory.createTitledBorder(""));
		block3.setLayout(new GridBagLayout());
		{
			block3.add(impResGeomLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomCacheText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomCacheLabel, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomDrainText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomDrainLabel, GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomPartText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResGeomPartLabel, GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatCacheText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatCacheLabel, GuiUtil.setConstraints(2,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatDrainText, GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatDrainLabel, GuiUtil.setConstraints(2,4,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatPartText, GuiUtil.setConstraints(1,5,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block3.add(impResFeatPartLabel, GuiUtil.setConstraints(2,5,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
	}

	//doTranslation-Methode
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("pref.import.resources.border.multiCPU")));
		impResMinThreadsLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.minThreads"));
		impResMaxThreadsLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.maxThreads"));
		block2.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("pref.import.resources.border.commit")));
		impResTransaktLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.commit"));
		impResTransaktFeatureLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.commit.feature"));
		impResTransaktCacheLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.commit.cache"));
		impResTransaktTempLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.commit.temp"));

		block3.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("pref.import.resources.border.idCache")));
		impResGeomLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.geometry"));
		impResGeomCacheLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.geometry.entry"));
		impResGeomDrainLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.geometry.drain"));
		impResGeomPartLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.geometry.partition"));
		impResFeatLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.feature"));
		impResFeatCacheLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.feature.entry"));
		impResFeatDrainLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.feature.drain"));
		impResFeatPartLabel.setText(ImpExpGui.labels.getString("pref.import.resources.label.feature.partition"));
	}

	//Config
	public void loadSettings() {

		SysThreadPoolConfig threadPool = config.getProject().getImporter().getSystem().getThreadPool().getDefaultPool();
		DBUpdateBatching commit = config.getProject().getDatabase().getUpdateBatching();
		SysGmlIdLookupServerConfig geometry = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getGeometry();
		SysGmlIdLookupServerConfig feature = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getFeature();

		int commitFeature = commit.getFeatureBatchValue();
		if (commitFeature > Internal.ORACLE_MAX_BATCH_SIZE)
			commitFeature = Internal.ORACLE_MAX_BATCH_SIZE;
		
		int commitCache = commit.getGmlIdLookupServerBatchValue();
		if (commitCache > Internal.ORACLE_MAX_BATCH_SIZE)
			commitCache = Internal.ORACLE_MAX_BATCH_SIZE;
		
		int commitTemp = commit.getTempBatchValue();
		if (commitTemp > Internal.ORACLE_MAX_BATCH_SIZE)
			commitTemp = Internal.ORACLE_MAX_BATCH_SIZE;
		
		impResMinThreadsText.setText(String.valueOf(threadPool.getMinThreads()));
		impResMaxThreadsText.setText(String.valueOf(threadPool.getMaxThreads()));
		impResTransaktFeatureText.setText(String.valueOf(commitFeature));
		impResTransaktCacheText.setText(String.valueOf(commitCache));
		impResTransaktTempText.setText(String.valueOf(commitTemp));
		impResGeomCacheText.setText(String.valueOf(geometry.getCacheSize()));		
		impResGeomDrainText.setText(String.valueOf((int)(geometry.getPageFactor() * 100)));
		impResGeomPartText.setText(String.valueOf((int)geometry.getPartitions()));
		impResFeatCacheText.setText(String.valueOf(feature.getCacheSize()));
		impResFeatDrainText.setText(String.valueOf((int)(feature.getPageFactor() * 100)));
		impResFeatPartText.setText(String.valueOf((int)feature.getPartitions()));
	}


	public void setSettings() {
		SysThreadPoolConfig threadPool = config.getProject().getImporter().getSystem().getThreadPool().getDefaultPool();
		DBUpdateBatching commit = config.getProject().getDatabase().getUpdateBatching();
		SysGmlIdLookupServerConfig geometry = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getGeometry();
		SysGmlIdLookupServerConfig feature = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getFeature();

		try {
			threadPool.setMinThreads(Integer.valueOf(impResMinThreadsText.getText()));
		} catch (NumberFormatException nfe) {
			impResMinThreadsText.setText(String.valueOf(threadPool.getMinThreads()));
		}
		try {
			threadPool.setMaxThreads(Integer.valueOf(impResMaxThreadsText.getText()));
		} catch (NumberFormatException nfe) {
			impResMaxThreadsText.setText(String.valueOf(threadPool.getMaxThreads()));
		}
		try {
			commit.setFeatureBatchValue(Integer.valueOf(impResTransaktFeatureText.getText()));
		} catch (NumberFormatException nfe) {
			impResTransaktFeatureText.setText(String.valueOf(commit.getFeatureBatchValue()));
		}
		try {
			commit.setGmlIdLookupServerBatchValue(Integer.valueOf(impResTransaktCacheText.getText()));
		} catch (NumberFormatException nfe) {
			impResTransaktCacheText.setText(String.valueOf(commit.getGmlIdLookupServerBatchValue()));
		}
		try {
			commit.setTempBatchValue(Integer.valueOf(impResTransaktTempText.getText()));
		} catch (NumberFormatException nfe) {
			impResTransaktTempText.setText(String.valueOf(commit.getTempBatchValue()));
		}
		try {
			geometry.setCacheSize(Integer.valueOf(impResGeomCacheText.getText()));
		} catch (NumberFormatException nfe) {
			impResGeomCacheText.setText(String.valueOf(geometry.getCacheSize()));
		}
		try {
			float pageFactor = Float.valueOf(impResGeomDrainText.getText()) / 100f;
			if (pageFactor > 1) {
				pageFactor = 1.0f;
				impResGeomDrainText.setText("100");				
			}

			geometry.setPageFactor(pageFactor);
		} catch (NumberFormatException nfe) {
			impResGeomDrainText.setText(String.valueOf((int)(geometry.getPageFactor() * 100)));
		}
		try {
			geometry.setPartitions(Integer.valueOf(impResGeomPartText.getText()));
		} catch (NumberFormatException nfe) {
			impResGeomPartText.setText(String.valueOf(geometry.getPartitions()));
		}
		try {
			feature.setCacheSize(Integer.valueOf(impResFeatCacheText.getText()));
		} catch (NumberFormatException nfe) {
			impResFeatCacheText.setText(String.valueOf(feature.getCacheSize()));
		}
		try {
			float pageFactor = Float.valueOf(impResFeatDrainText.getText()) / 100f;
			if (pageFactor > 1) {
				pageFactor = 1.0f;
				impResFeatDrainText.setText("100");				
			}

			feature.setPageFactor(pageFactor);
		} catch (NumberFormatException nfe) {
			impResFeatDrainText.setText(String.valueOf((int)(feature.getPageFactor() * 100)));
		}
		try {
			feature.setPartitions(Integer.valueOf(impResFeatPartText.getText()));
		} catch (NumberFormatException nfe) {
			impResFeatPartText.setText(String.valueOf(feature.getPartitions()));
		}
	}
}
