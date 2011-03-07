package de.tub.citydb.gui.panel.settings;

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
import de.tub.citydb.gui.util.GuiUtil;

public class ImpResourcesPanel extends PrefPanelBase{
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
	
	public ImpResourcesPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
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
			if (!Integer.valueOf(impResGeomDrainText.getText()).equals((int)(geometry.getPageFactor() * 100))) return true;
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
			if (!Integer.valueOf(impResFeatDrainText.getText()).equals((int)(feature.getPageFactor() * 100))) return true;
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

	private void initGui(){
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
		impResGeomCacheText = new JTextField(new DigitsOnlyDocument(8), "" , 0);
		impResGeomCacheLabel = new JLabel("");	
		impResGeomDrainText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		impResGeomDrainLabel = new JLabel("");	
		impResGeomPartText = new JTextField(new DigitsOnlyDocument(3), "", 0);
		impResGeomPartLabel = new JLabel("");
		impResFeatLabel = new JLabel("");		
		impResFeatCacheText = new JTextField(new DigitsOnlyDocument(8), "" , 0);
		impResFeatCacheLabel = new JLabel("");
		impResFeatDrainText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		impResFeatDrainLabel = new JLabel("");
		impResFeatPartText = new JTextField(new DigitsOnlyDocument(3), "", 0);
		impResFeatPartLabel = new JLabel("");

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

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("common.pref.resources.border.multiCPU")));
		impResMinThreadsLabel.setText(Internal.I18N.getString("common.pref.resources.label.minThreads"));
		impResMaxThreadsLabel.setText(Internal.I18N.getString("common.pref.resources.label.maxThreads"));
		
		block2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.import.resources.border.commit")));
		impResTransaktLabel.setText(Internal.I18N.getString("pref.import.resources.label.commit"));
		impResTransaktFeatureLabel.setText(Internal.I18N.getString("pref.import.resources.label.commit.feature"));
		impResTransaktCacheLabel.setText(Internal.I18N.getString("pref.import.resources.label.commit.cache"));
		impResTransaktTempLabel.setText(Internal.I18N.getString("pref.import.resources.label.commit.temp"));

		block3.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("common.pref.resources.border.idCache")));
		impResGeomLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry"));
		impResGeomCacheLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.entry"));
		impResGeomDrainLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.drain"));
		impResGeomPartLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.partition"));
		impResFeatLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature"));
		impResFeatCacheLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.entry"));
		impResFeatDrainLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.drain"));
		impResFeatPartLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.partition"));
	}

	@Override
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

	@Override
	public void setSettings() {
		SysThreadPoolConfig threadPool = config.getProject().getImporter().getSystem().getThreadPool().getDefaultPool();
		DBUpdateBatching commit = config.getProject().getDatabase().getUpdateBatching();
		SysGmlIdLookupServerConfig geometry = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getGeometry();
		SysGmlIdLookupServerConfig feature = config.getProject().getImporter().getSystem().getGmlIdLookupServer().getFeature();

		int minThreads , maxThreads;
		boolean isValid;
		
		try {
			maxThreads = Integer.valueOf(impResMaxThreadsText.getText());
			if (maxThreads <= 0) {
				maxThreads = 1;
				impResMaxThreadsText.setText("1");
			}
			
			threadPool.setMaxThreads(maxThreads);
		} catch (NumberFormatException nfe) {
			maxThreads = threadPool.getMaxThreads();
			impResMaxThreadsText.setText(String.valueOf(maxThreads));
		}
		try {
			minThreads = Integer.valueOf(impResMinThreadsText.getText());
			isValid = true;
			
			if (minThreads <= 0) {
				minThreads = 1;
				isValid = false;
			}
			
			if (minThreads > maxThreads) {
				minThreads = maxThreads;
				isValid = false;
			}
			
			if (!isValid)
				impResMinThreadsText.setText(String.valueOf(minThreads));
			
			threadPool.setMinThreads(minThreads);
		} catch (NumberFormatException nfe) {
			impResMinThreadsText.setText(String.valueOf(threadPool.getMinThreads()));
		}
		try {
			int featBatch = Integer.valueOf(impResTransaktFeatureText.getText());
			isValid = true;
			
			if (featBatch <= 0) {
				featBatch = 20;
				isValid = false;
			}
			
			if (featBatch > Internal.ORACLE_MAX_BATCH_SIZE) {
				featBatch = Internal.ORACLE_MAX_BATCH_SIZE;
				isValid = false;
			}
			
			if (!isValid)
				impResTransaktFeatureText.setText(String.valueOf(featBatch));
			
			commit.setFeatureBatchValue(featBatch);
		} catch (NumberFormatException nfe) {
			impResTransaktFeatureText.setText(String.valueOf(commit.getFeatureBatchValue()));
		}
		try {
			int lookupBatch = Integer.valueOf(impResTransaktCacheText.getText());
			isValid = true;
			
			if (lookupBatch <= 0) {
				lookupBatch = 10000;
				isValid = false;
			}
			
			if (lookupBatch > Internal.ORACLE_MAX_BATCH_SIZE) {
				lookupBatch = Internal.ORACLE_MAX_BATCH_SIZE;
				isValid = false;
			}
			
			if (!isValid)
				impResTransaktCacheText.setText(String.valueOf(lookupBatch));
			
			commit.setGmlIdLookupServerBatchValue(lookupBatch);
		} catch (NumberFormatException nfe) {
			impResTransaktCacheText.setText(String.valueOf(commit.getGmlIdLookupServerBatchValue()));
		}
		try {
			int tempBatch = Integer.valueOf(impResTransaktTempText.getText());
			isValid = true;
			
			if (tempBatch <= 0) {
				tempBatch = 10000;
				isValid = false;
			}
			
			if (tempBatch > Internal.ORACLE_MAX_BATCH_SIZE) {
				tempBatch = Internal.ORACLE_MAX_BATCH_SIZE;
				isValid = false;
			}
			
			if (!isValid)
				impResTransaktTempText.setText(String.valueOf(tempBatch));
			
			commit.setTempBatchValue(tempBatch);
		} catch (NumberFormatException nfe) {
			impResTransaktTempText.setText(String.valueOf(commit.getTempBatchValue()));
		}
		try {
			int geomCache = Integer.valueOf(impResGeomCacheText.getText());			
			if (geomCache <= 0) {
				geomCache = 200000;
				impResGeomCacheText.setText("200000");
			}
			
			geometry.setCacheSize(geomCache);
		} catch (NumberFormatException nfe) {
			impResGeomCacheText.setText(String.valueOf(geometry.getCacheSize()));
		}
		try {
			float pageFactor = Float.valueOf(impResGeomDrainText.getText()) / 100f;
			isValid = true;
			
			if (pageFactor > 1) {
				pageFactor = 1.0f;
				isValid = false;
			}
			
			if (pageFactor <= 0) {
				pageFactor = 0.85f;
				isValid = false;
			}
			
			if (!isValid)
				impResGeomDrainText.setText(String.valueOf((int)(pageFactor * 100)));

			geometry.setPageFactor(pageFactor);
		} catch (NumberFormatException nfe) {
			impResGeomDrainText.setText(String.valueOf((int)(geometry.getPageFactor() * 100)));
		}
		try {
			int geomPart = Integer.valueOf(impResGeomPartText.getText());
			isValid = true;
			
			if (geomPart <= 0) {
				geomPart = 10;
				isValid = false;
			}
			
			if (geomPart > 100) {
				geomPart = 100;
				isValid = false;
			}
			
			if (!isValid)
				impResGeomPartText.setText(String.valueOf(geomPart));
			
			geometry.setPartitions(geomPart);
		} catch (NumberFormatException nfe) {
			impResGeomPartText.setText(String.valueOf(geometry.getPartitions()));
		}
		try {
			int featCache = Integer.valueOf(impResFeatCacheText.getText());
			if (featCache <= 0) {
				featCache = 200000;
				impResFeatCacheText.setText("200000");
			}
			
			feature.setCacheSize(featCache);
		} catch (NumberFormatException nfe) {
			impResFeatCacheText.setText(String.valueOf(feature.getCacheSize()));
		}
		try {
			float pageFactor = Float.valueOf(impResFeatDrainText.getText()) / 100f;
			isValid = true;
			
			if (pageFactor > 1) {
				pageFactor = 1.0f;
				isValid = false;
			}
			
			if (pageFactor <= 0) {
				pageFactor = 0.85f;
				isValid = false;
			}
			
			if (!isValid)
				impResFeatDrainText.setText(String.valueOf((int)(pageFactor * 100)));

			feature.setPageFactor(pageFactor);
		} catch (NumberFormatException nfe) {
			impResFeatDrainText.setText(String.valueOf((int)(feature.getPageFactor() * 100)));
		}
		try {
			int featPart = Integer.valueOf(impResFeatPartText.getText());
			isValid = true;
			
			if (featPart <= 0) {
				featPart = 10;
				isValid = false;
			}
			
			if (featPart > 100) {
				featPart = 100;
				isValid = false;
			}
			
			if (!isValid)
				impResFeatPartText.setText(String.valueOf(featPart));
			
			feature.setPartitions(featPart);
		} catch (NumberFormatException nfe) {
			impResFeatPartText.setText(String.valueOf(feature.getPartitions()));
		}
	}
}
