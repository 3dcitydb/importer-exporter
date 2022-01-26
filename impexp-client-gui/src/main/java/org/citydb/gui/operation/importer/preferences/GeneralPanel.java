package org.citydb.gui.operation.importer.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.GeneralOptions;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.operation.common.DefaultPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class GeneralPanel extends DefaultPreferencesComponent {
    private TitledPanel generalPanel;
    private JCheckBox failFastOnErrors;

    public GeneralPanel(Config config) {
        super(config);
        initGui();
    }

    @Override
    public boolean isModified() {
        GeneralOptions generalOptions = config.getImportConfig().getGeneralOptions();

        if (failFastOnErrors.isSelected() != generalOptions.isFailFastOnErrors()) return true;

        return false;
    }

    private void initGui() {
        failFastOnErrors = new JCheckBox();

        setLayout(new GridBagLayout());
        {
            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                content.add(failFastOnErrors, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
            }

            generalPanel = new TitledPanel().build(content);
        }

        add(generalPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
    }

    @Override
    public void setSettings() {
        GeneralOptions generalOptions = config.getImportConfig().getGeneralOptions();
        generalOptions.setFailFastOnErrors(failFastOnErrors.isSelected());
    }

    @Override
    public void loadSettings() {
        GeneralOptions generalOptions = config.getImportConfig().getGeneralOptions();
        failFastOnErrors.setSelected(generalOptions.isFailFastOnErrors());
    }

    @Override
    public void doTranslation() {
        generalPanel.setTitle(Language.I18N.getString("pref.import.general.border.general"));
        failFastOnErrors.setText(Language.I18N.getString("pref.import.general.failFastOnError"));
    }

    @Override
    public String getTitle() {
        return Language.I18N.getString("pref.tree.import.general");
    }
}
