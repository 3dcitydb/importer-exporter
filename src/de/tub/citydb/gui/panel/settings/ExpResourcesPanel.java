package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.system.SysGmlIdLookupServerConfig;
import de.tub.citydb.config.project.system.SysThreadPoolConfig;
import de.tub.citydb.gui.components.DigitsOnlyDocument;
import de.tub.citydb.gui.util.GuiUtil;

public class ExpResourcesPanel extends PrefPanelBase{
	private JPanel block1;
	private JPanel block2;
	private JLabel expResMinThreadsLabel;
	private JTextField expResMinThreadsText;
	private JLabel expResMaxThreadsLabel;
	private JTextField expResMaxThreadsText;	
	private JLabel expResGeomLabel;
	private JTextField expResGeomCacheText;
	private JLabel expResGeomCacheLabel;	
	private JTextField expResGeomDrainText;
	private JLabel expResGeomDrainLabel;
	private JLabel expResGeomPartLabel;
	private JTextField expResGeomPartText;
	private JLabel expResFeatLabel;
	private JTextField expResFeatCacheText;
	private JLabel expResFeatCacheLabel;
	private JTextField expResFeatDrainText;
	private JLabel expResFeatDrainLabel;
	private JLabel expResFeatPartLabel;
	private JTextField expResFeatPartText;

	public ExpResourcesPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		SysThreadPoolConfig threadPool = config.getProject().getExporter().getSystem().getThreadPool().getDefaultPool();
		SysGmlIdLookupServerConfig geometry = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getGeometry();
		SysGmlIdLookupServerConfig feature = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getFeature();

		try {
			if (!Integer.valueOf(expResMinThreadsText.getText()).equals(threadPool.getMinThreads())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(expResMaxThreadsText.getText()).equals(threadPool.getMaxThreads())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(expResGeomCacheText.getText()).equals(geometry.getCacheSize())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(expResGeomDrainText.getText()).equals((int)(geometry.getPageFactor() * 100))) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}
		
		try {
			if (!Integer.valueOf(expResGeomPartText.getText()).equals(geometry.getPartitions())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(expResFeatCacheText.getText()).equals(feature.getCacheSize())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		try {
			if (!Integer.valueOf(expResFeatDrainText.getText()).equals((int)(feature.getPageFactor() * 100))) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}
		
		try {
			if (!Integer.valueOf(expResFeatPartText.getText()).equals(feature.getPartitions())) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}

		return false;

	}


	private void initGui(){
		block1 = new JPanel();
		block2 = new JPanel();
		expResMinThreadsLabel = new JLabel("");
		expResMinThreadsText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		expResMaxThreadsLabel = new JLabel("");
		expResMaxThreadsText = new JTextField(new DigitsOnlyDocument(3), "" , 0);	
		expResGeomLabel = new JLabel("");
		expResGeomCacheText = new JTextField(new DigitsOnlyDocument(8), "" , 0);
		expResGeomCacheLabel = new JLabel("");	
		expResGeomDrainText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		expResGeomDrainLabel = new JLabel("");
		expResGeomPartText = new JTextField(new DigitsOnlyDocument(3), "", 0);
		expResGeomPartLabel = new JLabel("");
		expResFeatLabel = new JLabel("");		
		expResFeatCacheText = new JTextField(new DigitsOnlyDocument(8), "" , 0);
		expResFeatCacheLabel = new JLabel("");
		expResFeatDrainText = new JTextField(new DigitsOnlyDocument(3), "" , 0);
		expResFeatDrainLabel = new JLabel("");
		expResFeatPartText = new JTextField(new DigitsOnlyDocument(3), "", 0);
		expResFeatPartLabel = new JLabel("");
		
		setLayout(new GridBagLayout());
		add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block1.setBorder(BorderFactory.createTitledBorder(""));
		block1.setLayout(new GridBagLayout());
		{
			block1.add(expResMinThreadsLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(expResMinThreadsText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(expResMaxThreadsLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(expResMaxThreadsText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}

		add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block2.setBorder(BorderFactory.createTitledBorder(""));
		block2.setLayout(new GridBagLayout());
		{
			block2.add(expResGeomLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomCacheText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomCacheLabel, GuiUtil.setConstraints(2,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomDrainText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomDrainLabel, GuiUtil.setConstraints(2,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomPartText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResGeomPartLabel, GuiUtil.setConstraints(2,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatCacheText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatCacheLabel, GuiUtil.setConstraints(2,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatDrainText, GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatDrainLabel, GuiUtil.setConstraints(2,4,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatPartText, GuiUtil.setConstraints(1,5,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block2.add(expResFeatPartLabel, GuiUtil.setConstraints(2,5,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}
	}

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("common.pref.resources.border.multiCPU")));
		expResMinThreadsLabel.setText(Internal.I18N.getString("common.pref.resources.label.minThreads"));
		expResMaxThreadsLabel.setText(Internal.I18N.getString("common.pref.resources.label.maxThreads"));

		block2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("common.pref.resources.border.idCache")));
		expResGeomLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry"));
		expResGeomCacheLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.entry"));
		expResGeomDrainLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.drain"));
		expResGeomPartLabel.setText(Internal.I18N.getString("common.pref.resources.label.geometry.partition"));
		expResFeatLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature"));
		expResFeatCacheLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.entry"));
		expResFeatDrainLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.drain"));
		expResFeatPartLabel.setText(Internal.I18N.getString("common.pref.resources.label.feature.partition"));
	}

	@Override
	public void loadSettings() {
		SysThreadPoolConfig threadPool = config.getProject().getExporter().getSystem().getThreadPool().getDefaultPool();
		SysGmlIdLookupServerConfig geometry = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getGeometry();
		SysGmlIdLookupServerConfig feature = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getFeature();

		expResMinThreadsText.setText(String.valueOf(threadPool.getMinThreads()));
		expResMaxThreadsText.setText(String.valueOf(threadPool.getMaxThreads()));
		expResGeomCacheText.setText(String.valueOf(geometry.getCacheSize()));
		expResGeomDrainText.setText(String.valueOf((int)(geometry.getPageFactor() * 100)));
		expResGeomPartText.setText(String.valueOf((int)geometry.getPartitions()));
		expResFeatCacheText.setText(String.valueOf(feature.getCacheSize()));
		expResFeatDrainText.setText(String.valueOf((int)(feature.getPageFactor() * 100)));
		expResFeatPartText.setText(String.valueOf((int)feature.getPartitions()));
	}

	@Override
	public void setSettings() {
		SysThreadPoolConfig threadPool = config.getProject().getExporter().getSystem().getThreadPool().getDefaultPool();
		SysGmlIdLookupServerConfig geometry = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getGeometry();
		SysGmlIdLookupServerConfig feature = config.getProject().getExporter().getSystem().getGmlIdLookupServer().getFeature();

		int minThreads, maxThreads;
		boolean isValid;
		
		try {
			maxThreads = Integer.valueOf(expResMaxThreadsText.getText());
			if (maxThreads <= 0) {
				maxThreads = 1;
				expResMaxThreadsText.setText("1");
			}
			
			threadPool.setMaxThreads(maxThreads);
		} catch (NumberFormatException nfe) {
			maxThreads = threadPool.getMaxThreads();
			expResMaxThreadsText.setText(String.valueOf(maxThreads));
		}
		try {
			minThreads = Integer.valueOf(expResMinThreadsText.getText());
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
				expResMinThreadsText.setText(String.valueOf(minThreads));
			
			threadPool.setMinThreads(minThreads);
		} catch (NumberFormatException nfe) {
			expResMinThreadsText.setText(String.valueOf(threadPool.getMinThreads()));
		}
		try {
			int geomCache = Integer.valueOf(expResGeomCacheText.getText());			
			if (geomCache <= 0) {
				geomCache = 200000;
				expResGeomCacheText.setText("200000");
			}
						
			geometry.setCacheSize(geomCache);
		} catch (NumberFormatException nfe) {
			expResGeomCacheText.setText(String.valueOf(geometry.getCacheSize()));
		}
		try {
			float pageFactor = Float.valueOf(expResGeomDrainText.getText()) / 100f;
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
				expResGeomDrainText.setText(String.valueOf((int)(pageFactor * 100)));				

			geometry.setPageFactor(pageFactor);
		} catch (NumberFormatException nfe) {
			expResGeomDrainText.setText(String.valueOf((int)(geometry.getPageFactor() * 100)));
		}
		try {
			int geomPart = Integer.valueOf(expResGeomPartText.getText());
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
				expResGeomPartText.setText(String.valueOf(geomPart));
			
			geometry.setPartitions(geomPart);
		} catch (NumberFormatException nfe) {
			expResGeomPartText.setText(String.valueOf(geometry.getPartitions()));
		}
		try {
			int featCache = Integer.valueOf(expResFeatCacheText.getText());
			if (featCache <= 0) {
				featCache = 200000;
				expResFeatCacheText.setText("200000");
			}
			
			feature.setCacheSize(featCache);
		} catch (NumberFormatException nfe) {
			expResFeatCacheText.setText(String.valueOf(feature.getCacheSize()));
		}
		try {
			float pageFactor = Float.valueOf(expResFeatDrainText.getText()) / 100f;
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
				expResFeatDrainText.setText(String.valueOf((int)(pageFactor * 100)));				

			feature.setPageFactor(pageFactor);
		} catch (NumberFormatException nfe) {
			expResFeatDrainText.setText(String.valueOf((int)(feature.getPageFactor() * 100)));
		}
		try {
			int featPart = Integer.valueOf(expResFeatPartText.getText());
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
				expResFeatPartText.setText(String.valueOf(featPart));
			
			feature.setPartitions(featPart);
		} catch (NumberFormatException nfe) {
			expResFeatPartText.setText(String.valueOf(feature.getPartitions()));
		}
	}
}

