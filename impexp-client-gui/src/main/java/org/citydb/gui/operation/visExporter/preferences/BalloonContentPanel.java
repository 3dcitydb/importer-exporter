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
package org.citydb.gui.operation.visExporter.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.common.PathMode;
import org.citydb.config.project.visExporter.Balloon;
import org.citydb.config.project.visExporter.BalloonContentMode;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.util.DefaultPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Locale;
import java.util.function.Supplier;

public class BalloonContentPanel extends DefaultPreferencesComponent {
	private final Supplier<String> titleSupplier;
	private final boolean showPointBalloon;
	private final boolean showCurveBalloon;

	private final BalloonContext objectBalloon;
	private BalloonContext pointBalloon;
	private BalloonContext curveBalloon;

	public BalloonContentPanel(
			Supplier<String> titleSupplier,
			Supplier<Balloon> objectBalloonSupplier,
			Supplier<Balloon> pointBalloonSupplier,
			Supplier<Balloon> curveBalloonSupplier,
			Config config) {
		super(config);
		this.titleSupplier = titleSupplier;

		objectBalloon = initBalloonContext(objectBalloonSupplier);

		showPointBalloon = pointBalloonSupplier != null;
		if (showPointBalloon) {
			pointBalloon = initBalloonContext(pointBalloonSupplier);
		}

		showCurveBalloon = curveBalloonSupplier != null;
		if (showCurveBalloon) {
			curveBalloon = initBalloonContext(curveBalloonSupplier);
		}

		initGui();
	}

	public BalloonContentPanel(Supplier<String> titleSupplier, Supplier<Balloon> objectBalloonSupplier, Config config) {
		this(titleSupplier, objectBalloonSupplier, null, null, config);
	}

	@Override
	public String getLocalizedTitle() {
		return titleSupplier.get();
	}

	@Override
	public boolean isModified() {
		if (isModified(objectBalloon)) return true;
		if (showPointBalloon && isModified(pointBalloon)) return true;
		if (showCurveBalloon && isModified(curveBalloon)) return true;
		return false;
	}

	private boolean isModified(BalloonContext context) {
		setInternalBalloonValues(context);
		return !context.supplier.get().equals(context.balloon);
	}

	private void initGui() {
		setLayout(new GridBagLayout());
		add(initBalloonPanel(objectBalloon), GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		if (showPointBalloon) {
			add(initBalloonPanel(pointBalloon), GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		if (showCurveBalloon) {
			add(initBalloonPanel(curveBalloon), GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
	}

	private BalloonContext initBalloonContext(Supplier<Balloon> supplier) {
		return new BalloonContext(supplier,
				new TitledPanel(),
				new JCheckBox(),
				new JRadioButton(),
				new JRadioButton(),
				new JCheckBox(),
				new JTextField(),
				new JButton(),
				new JCheckBox()
		);
	}

	private TitledPanel initBalloonPanel(BalloonContext context) {
		ButtonGroup group = new ButtonGroup();
		group.add(context.useAttribute);
		group.add(context.useFile);

		context.browseText.setPreferredSize(new Dimension(0, 0));
		int lmargin = GuiUtil.getTextOffset(context.useFile);

		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());
		content.add(context.useAttribute, GuiUtil.setConstraints(0, 0, 3, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 0));
		content.add(context.useFile, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
		content.add(context.browseText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 5));
		content.add(context.browseButton, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
		content.add(context.useFallback, GuiUtil.setConstraints(0, 2, 3, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, lmargin, 0, 0));
		content.add(context.separateFile, GuiUtil.setConstraints(0, 3, 3, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));

		context.titledPanel.withToggleButton(context.useBalloon).build(content);

		PopupMenuDecorator.getInstance().decorate(context.browseText);

		context.useBalloon.addActionListener(e -> setEnabledComponents(context));
		context.browseButton.addActionListener(e -> loadFile(context.balloon, context.browseText));
		context.useAttribute.addActionListener(e -> setEnabledComponents(context));
		context.useFile.addActionListener(e -> setEnabledComponents(context));
		context.useFallback.addActionListener(e -> setEnabledComponents(context));

		return context.titledPanel;
	}

	@Override
	public void switchLocale(Locale locale) {
		objectBalloon.titledPanel.setTitle(Language.I18N.getString("pref.visExport.balloon.border.createBalloon"));
		doTranslation(objectBalloon);

		if (showPointBalloon) {
			pointBalloon.titledPanel.setTitle(Language.I18N.getString("pref.visExport.balloon.border.createPointBalloon"));
			doTranslation(pointBalloon);
		}

		if (showCurveBalloon) {
			curveBalloon.titledPanel.setTitle(Language.I18N.getString("pref.visExport.balloon.border.createCurveBalloon"));
			doTranslation(curveBalloon);
		}
	}

	private void doTranslation(BalloonContext context) {
		context.useAttribute.setText(Language.I18N.getString("pref.visExport.balloon.label.genAttrib"));
		context.useFile.setText(Language.I18N.getString("pref.visExport.balloon.label.file"));
		context.useFallback.setText(Language.I18N.getString("pref.visExport.balloon.label.genAttribAndFile"));
		context.browseButton.setText(Language.I18N.getString("common.button.browse"));
		context.separateFile.setText(Language.I18N.getString("pref.visExport.balloon.label.contentInSeparateFile"));
	}

	@Override
	public void loadSettings() {
		loadSettings(objectBalloon);

		if (showPointBalloon) {
			loadSettings(pointBalloon);
		}

		if (showCurveBalloon) {
			loadSettings(curveBalloon);
		}

		setEnabledComponents();
	}

	private void loadSettings(BalloonContext context) {
		Balloon balloon = context.supplier.get();
		copyBalloonContents(balloon, context.balloon);

		context.useBalloon.setSelected(context.balloon.isIncludeDescription());
		if (context.balloon.getBalloonContentMode() == BalloonContentMode.GEN_ATTRIB) {
			context.useAttribute.setSelected(true);
		} else {
			context.useFile.setSelected(true);
			if (context.balloon.getBalloonContentMode() == BalloonContentMode.GEN_ATTRIB_AND_FILE) {
				context.useFallback.setSelected(true);
			}
		}

		context.browseText.setText(context.balloon.getBalloonContentTemplateFile());
		context.separateFile.setSelected(context.balloon.isBalloonContentInSeparateFile());
	}

	@Override
	public void setSettings() {
		setInternalBalloonValues(objectBalloon);
		copyBalloonContents(objectBalloon.balloon, objectBalloon.supplier.get());

		if (showPointBalloon) {
			setInternalBalloonValues(pointBalloon);
			copyBalloonContents(pointBalloon.balloon, pointBalloon.supplier.get());
		}

		if (showCurveBalloon) {
			setInternalBalloonValues(curveBalloon);
			copyBalloonContents(curveBalloon.balloon, curveBalloon.supplier.get());
		}
	}

	private void setInternalBalloonValues(BalloonContext context) {
		context.balloon.setIncludeDescription(context.useBalloon.isSelected());

		if (context.useAttribute.isSelected()) {
			context.balloon.setBalloonContentMode(BalloonContentMode.GEN_ATTRIB);
		} else {
			if (context.useFallback.isSelected()) {
				context.balloon.setBalloonContentMode(BalloonContentMode.GEN_ATTRIB_AND_FILE);
			} else {
				context.balloon.setBalloonContentMode(BalloonContentMode.FILE);
			}
		}

		context.balloon.getBalloonContentPath().setLastUsedPath(context.browseText.getText().trim());
		context.balloon.setBalloonContentTemplateFile(context.browseText.getText().trim());
		context.balloon.setBalloonContentInSeparateFile(context.separateFile.isSelected());
	}
	
	private void loadFile(Balloon balloon, JTextField browseText) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(Language.I18N.getString("pref.visExport.balloon.dialog.file"));

		FileNameExtensionFilter filter = new FileNameExtensionFilter("HTML Files (*.htm, *.html)", "htm", "html");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
		fileChooser.setFileFilter(filter);

		if (balloon.getBalloonContentPath().isSetLastUsedMode()) {
			fileChooser.setCurrentDirectory(new File(balloon.getBalloonContentPath().getLastUsedPath()));
		} else {
			fileChooser.setCurrentDirectory(new File(balloon.getBalloonContentPath().getStandardPath()));
		}

		int result = fileChooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;

		try {
			String exportString = fileChooser.getSelectedFile().toString();
			browseText.setText(exportString);
			balloon.getBalloonContentPath().setLastUsedPath(fileChooser.getCurrentDirectory().getAbsolutePath());
			balloon.getBalloonContentPath().setPathMode(PathMode.LASTUSED);
		} catch (Exception e) {
			//
		}
	}

	private void setEnabledComponents() {
		setEnabledComponents(objectBalloon);

		if (showPointBalloon) {
			setEnabledComponents(pointBalloon);
		}

		if (showCurveBalloon) {
			setEnabledComponents(curveBalloon);
		}
	}

	private void setEnabledComponents(BalloonContext context) {
		context.useAttribute.setEnabled(context.useBalloon.isSelected());
		context.useFile.setEnabled(context.useBalloon.isSelected());
		context.useFallback.setEnabled(context.useBalloon.isSelected() && context.useFile.isSelected());
		context.browseText.setEnabled(context.useBalloon.isSelected() && context.useFile.isSelected());
		context.browseButton.setEnabled(context.useBalloon.isSelected() && context.useFile.isSelected());
		context.separateFile.setEnabled(context.useBalloon.isSelected());
	}

	private void copyBalloonContents(Balloon source, Balloon target) {
		target.setBalloonContentInSeparateFile(source.isBalloonContentInSeparateFile());
		target.setBalloonContentMode(source.getBalloonContentMode());
		target.setBalloonContentTemplateFile(source.getBalloonContentTemplateFile());
		target.setIncludeDescription(source.isIncludeDescription());
		target.getBalloonContentPath().setPathMode(source.getBalloonContentPath().getPathMode());
		target.getBalloonContentPath().setLastUsedPath(source.getBalloonContentPath().getLastUsedPath());
		target.getBalloonContentPath().setStandardPath(source.getBalloonContentPath().getStandardPath());
	}

	private static class BalloonContext {
		private final Balloon balloon;
		private final Supplier<Balloon> supplier;
		private final TitledPanel titledPanel;
		private final JCheckBox useBalloon;
		private final JRadioButton useAttribute;
		private final JRadioButton useFile;
		private final JCheckBox useFallback;
		private final JTextField browseText;
		private final JButton browseButton;
		private final JCheckBox separateFile;

		BalloonContext(
				Supplier<Balloon> supplier,
				TitledPanel titledPanel,
				JCheckBox useBalloon,
				JRadioButton useAttribute,
				JRadioButton useFile,
				JCheckBox useFallback,
				JTextField browseText,
				JButton browseButton,
				JCheckBox separateFile) {
			balloon = new Balloon();
			this.supplier = supplier;
			this.titledPanel = titledPanel;
			this.useBalloon = useBalloon;
			this.useAttribute = useAttribute;
			this.useFile = useFile;
			this.useFallback = useFallback;
			this.browseText = browseText;
			this.browseButton = browseButton;
			this.separateFile = separateFile;
		}
	}
}
