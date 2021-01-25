package org.citydb.gui.modules.exporter.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.CityJSONOptions;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class CityJSONOptionsPanel extends AbstractPreferencesComponent {
    private TitledPanel generalOptionsPanel;
    private JCheckBox prettyPrint;
    private JCheckBox useGeometryCompression;
    private JCheckBox removeDuplicateChildGeometries;
    private JCheckBox addSequenceIdWhenSorting;

    private TitledPanel precisionPanel;
    private JLabel significantDigitsLabel;
    private JLabel significantDigitsUnitLabel;
    private JSpinner significantDigits;
    private JLabel significantTextureDigitsLabel;
    private JLabel significantTextureDigitsUnitLabel;
    private JSpinner significantTextureDigits;

    protected CityJSONOptionsPanel(Config config) {
        super(config);
        initGui();
    }

    @Override
    public boolean isModified() {
        CityJSONOptions cityJSONOptions = config.getExportConfig().getCityJSONOptions();
        if (prettyPrint.isSelected() != cityJSONOptions.isPrettyPrint()) return true;
        if (useGeometryCompression.isSelected() != cityJSONOptions.isUseGeometryCompression()) return true;
        if (removeDuplicateChildGeometries.isSelected() != cityJSONOptions.isRemoveDuplicateChildGeometries()) return true;
        if (addSequenceIdWhenSorting.isSelected() != cityJSONOptions.isAddSequenceIdWhenSorting()) return true;
        if (((Number) significantDigits.getValue()).intValue() != cityJSONOptions.getSignificantDigits()) return true;
        if (((Number) significantTextureDigits.getValue()).intValue() != cityJSONOptions.getSignificantTextureDigits()) return true;
        return false;
    }

    private void initGui() {
        prettyPrint = new JCheckBox();
        useGeometryCompression = new JCheckBox();
        removeDuplicateChildGeometries = new JCheckBox();
        addSequenceIdWhenSorting = new JCheckBox();

        significantDigitsLabel = new JLabel();
        significantDigitsUnitLabel = new JLabel();
        significantTextureDigitsLabel = new JLabel();
        significantTextureDigitsUnitLabel = new JLabel();
        significantDigits = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        significantTextureDigits = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));

        PopupMenuDecorator.getInstance().decorate(significantDigits, significantTextureDigits,
                ((JSpinner.DefaultEditor) significantDigits.getEditor()).getTextField(),
                ((JSpinner.DefaultEditor) significantTextureDigits.getEditor()).getTextField());

        setLayout(new GridBagLayout());
        {
            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                content.add(prettyPrint, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 5, 0));
                content.add(useGeometryCompression, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 5, 0));
                content.add(removeDuplicateChildGeometries, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 0, 0, 5, 0));
                content.add(addSequenceIdWhenSorting, GuiUtil.setConstraints(0, 3, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
            }

            generalOptionsPanel = new TitledPanel().build(content);
        }
        {
            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                content.add(significantDigitsLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
                content.add(significantDigits, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
                content.add(significantDigitsUnitLabel, GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
                content.add(significantTextureDigitsLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
                content.add(significantTextureDigits, GuiUtil.setConstraints(1, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 5));
                content.add(significantTextureDigitsUnitLabel, GuiUtil.setConstraints(2, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
            }

            precisionPanel = new TitledPanel().build(content);
        }

        add(generalOptionsPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        add(precisionPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
    }

    @Override
    public void setSettings() {
        CityJSONOptions cityJSONOptions = config.getExportConfig().getCityJSONOptions();
        cityJSONOptions.setPrettyPrint(prettyPrint.isSelected());
        cityJSONOptions.setUseGeometryCompression(useGeometryCompression.isSelected());
        cityJSONOptions.setRemoveDuplicateChildGeometries(removeDuplicateChildGeometries.isSelected());
        cityJSONOptions.setAddSequenceIdWhenSorting(addSequenceIdWhenSorting.isSelected());
        cityJSONOptions.setSignificantDigits(((Number) significantDigits.getValue()).intValue());
        cityJSONOptions.setSignificantTextureDigits(((Number) significantTextureDigits.getValue()).intValue());
    }

    @Override
    public void loadSettings() {
        CityJSONOptions cityJSONOptions = config.getExportConfig().getCityJSONOptions();
        prettyPrint.setSelected(cityJSONOptions.isPrettyPrint());
        useGeometryCompression.setSelected(cityJSONOptions.isUseGeometryCompression());
        removeDuplicateChildGeometries.setSelected(cityJSONOptions.isRemoveDuplicateChildGeometries());
        addSequenceIdWhenSorting.setSelected(cityJSONOptions.isAddSequenceIdWhenSorting());
        significantDigits.setValue(cityJSONOptions.getSignificantDigits());
        significantTextureDigits.setValue(cityJSONOptions.getSignificantTextureDigits());
    }

    @Override
    public void doTranslation() {
        generalOptionsPanel.setTitle(Language.I18N.getString("pref.export.cityjson.border.general"));
        prettyPrint.setText(Language.I18N.getString("pref.export.cityjson.label.prettyPrint"));
        useGeometryCompression.setText(Language.I18N.getString("pref.export.cityjson.label.geometryCompression"));
        removeDuplicateChildGeometries.setText(Language.I18N.getString("pref.export.cityjson.label.removeDuplicates"));
        addSequenceIdWhenSorting.setText(Language.I18N.getString("pref.export.cityjson.label.sequenceId"));
        precisionPanel.setTitle(Language.I18N.getString("pref.export.cityjson.border.precision"));
        significantDigitsLabel.setText(Language.I18N.getString("pref.export.cityjson.label.digits"));
        significantDigitsUnitLabel.setText(Language.I18N.getString("pref.export.cityjson.label.digitsUnits"));
        significantTextureDigitsLabel.setText(Language.I18N.getString("pref.export.cityjson.label.textureDigits"));
        significantTextureDigitsUnitLabel.setText(Language.I18N.getString("pref.export.cityjson.label.textureDigitsUnits"));
    }

    @Override
    public String getTitle() {
        return Language.I18N.getString("pref.tree.export.cityJSONOptions");
    }
}
