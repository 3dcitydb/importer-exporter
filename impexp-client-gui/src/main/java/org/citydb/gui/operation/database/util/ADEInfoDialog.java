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
package org.citydb.gui.operation.database.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import org.citydb.config.i18n.Language;
import org.citydb.core.ade.ADEExtension;
import org.citydb.core.ade.ADEExtensionManager;
import org.citydb.core.database.schema.mapping.*;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citygml4j.builder.cityjson.extension.CityJSONExtension;
import org.citygml4j.builder.cityjson.extension.CityJSONExtensionContext;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.List;
import java.util.stream.Collectors;

public class ADEInfoDialog extends JDialog {

    public ADEInfoDialog(ADEInfoRow adeInfo, SchemaMapping adeSchema, SchemaMapping rootSchema, JFrame frame) {
        super(frame, true);
        initGUI(adeInfo, adeSchema, rootSchema);
    }

    private void initGUI(ADEInfoRow adeInfo, SchemaMapping adeSchema, SchemaMapping rootSchema) {
        setTitle(Language.I18N.getString("db.dialog.ade.title"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        Metadata metadata = adeSchema.getMetadata();

        JTextField nameText = createTextField(metadata.getName());
        JTextField versionText = createTextField(metadata.getVersion());
        JTextArea descriptionText = createTextArea(metadata.getDescription(), 2);
        JTextField idText = createTextField(adeInfo.getId());
        JTextField dbPrefixText = createTextField(metadata.getDBPrefix());

        List<FeatureType> featureTypes = adeSchema.listTopLevelFeatureTypes(true);
        JTextArea featureTypesText = createTextArea(featureTypes.stream()
                        .map(FeatureType::toString)
                        .collect(Collectors.joining("\n")), Math.min(featureTypes.size(), 5));

        int lowerObjectClassId = Integer.MAX_VALUE;
        int upperObjectClassId = -Integer.MAX_VALUE;
        for (AbstractType<?> types : adeSchema.getAbstractTypes()) {
            int objectClassId = types.getObjectClassId();
            if (objectClassId < lowerObjectClassId)
                lowerObjectClassId = objectClassId;
            else if (objectClassId > upperObjectClassId)
                upperObjectClassId = objectClassId;
        }

        JTextField objectClassIdText = createTextField(lowerObjectClassId + " .. " + upperObjectClassId);

        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        {
            // general information
            JLabel nameLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.name"));
            JLabel versionLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.version"));
            JLabel descriptionLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.description"));
            JLabel idLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.identifier"));

            JScrollPane descriptionPane = new JScrollPane(descriptionText);
            descriptionPane.setMinimumSize(descriptionPane.getPreferredSize());

            JLabel cityGMLLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.encoding"));
            JPanel cityGMLPanel = new JPanel();
            cityGMLPanel.setLayout(new OverlayLayout(cityGMLPanel));
            {
                JPanel glassPane = new JPanel();
                glassPane.addMouseListener(new MouseAdapter() { });
                glassPane.setOpaque(false);
                cityGMLPanel.add(glassPane);

                JPanel content = new JPanel();
                content.setLayout(new GridBagLayout());

                int i = 0;
                for (CityGMLVersion cityGMLVersion : CityGMLVersion.getInstances()) {
                    JCheckBox versionCheck = new JCheckBox("CityGML " + cityGMLVersion.toString());
                    versionCheck.setSelected(adeSchema.getSchemas().stream().anyMatch(s -> s.isAvailableForCityGML(cityGMLVersion)));
                    content.add(versionCheck, GuiUtil.setConstraints(i++, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 10));
                }

                JCheckBox cityJSONExtension = new JCheckBox("CityJSON 1.0");
                ADEExtension adeExtension = ADEExtensionManager.getInstance().getExtensionById(adeInfo.getId());
                cityJSONExtension.setSelected(adeExtension != null
                        && adeExtension.getADEContexts().stream()
                        .allMatch(adeContext -> adeContext instanceof CityJSONExtensionContext));

                content.add(cityJSONExtension, GuiUtil.setConstraints(i++, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 10));
                content.add(Box.createHorizontalGlue(), GuiUtil.setConstraints(i, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
                cityGMLPanel.add(content);
            }

            JLabel statusLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.status"));
            JLabel statusText = new JLabel();
            if (adeInfo.hasDatabaseSupport() && adeInfo.hasImpexpSupport()) {
                statusText.setText(Language.I18N.getString("db.dialog.ade.status.ok"));
                statusText.setIcon(new FlatSVGIcon("org/citydb/gui/icons/check.svg"));
            } else {
                statusText.setText(adeInfo.hasDatabaseSupport() ? Language.I18N.getString("db.dialog.ade.status.noImpExp") :
                        Language.I18N.getString("db.dialog.ade.status.noDB"));
                statusText.setIcon(new FlatSVGIcon("org/citydb/gui/icons/yellow_bulb.svg"));
            }

            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                content.add(nameLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                content.add(nameText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
                content.add(versionLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                content.add(versionText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
                content.add(descriptionLabel, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                content.add(descriptionPane, GuiUtil.setConstraints(1, 2, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
                content.add(idLabel, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                content.add(idText, GuiUtil.setConstraints(1, 3, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
                content.add(cityGMLLabel, GuiUtil.setConstraints(0, 4, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                content.add(cityGMLPanel, GuiUtil.setConstraints(1, 4, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
                content.add(statusLabel, GuiUtil.setConstraints(0, 5, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
                content.add(statusText, GuiUtil.setConstraints(1, 5, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            }

            TitledPanel generalPanel = new TitledPanel()
                    .withTitle(Language.I18N.getString("db.dialog.ade.label.general"))
                    .build(content);

            main.add(generalPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
        {
            // Top-level feature types
            JLabel featureTyesLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.feature.types"));
            JScrollPane featureTypesPane = new JScrollPane(featureTypesText);
            featureTypesPane.setMinimumSize(featureTypesPane.getPreferredSize());

            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                content.add(featureTyesLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
                content.add(featureTypesPane, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            }

            TitledPanel featurePanel = new TitledPanel()
                    .withTitle(Language.I18N.getString("db.dialog.ade.label.feature"))
                    .build(content);

            main.add(featurePanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
        {
            // database information
            JLabel dbPrefixLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.dbPrefix"));
            JLabel objectClassIdLabel = new JLabel("ObjectClassId");

            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                content.add(dbPrefixLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                content.add(dbPrefixText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
                content.add(objectClassIdLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
                content.add(objectClassIdText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            }

            TitledPanel databasePanel = new TitledPanel()
                    .withTitle(Language.I18N.getString("main.tabbedPane.database"))
                    .build(content);

            main.add(databasePanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }
        {
            // XML schema information
            JPanel xmlPanel = new JPanel();
            xmlPanel.setLayout(new GridBagLayout());
            {
                int i = 0;
                int max = adeSchema.getSchemas().size() - 1;

                for (AppSchema appSchema : adeSchema.getSchemas()) {
                    for (Namespace namespace : appSchema.getNamespaces()) {
                        AppSchema tmp = rootSchema.getSchema(namespace.getURI());

                        String prefix;
                        if (tmp != null) {
                            prefix = tmp.getXMLPrefix();
                        } else if (!appSchema.isSetXMLPrefix() || appSchema.isGeneratedXMLPrefix()) {
                            prefix = "n/a";
                        } else {
                            prefix = appSchema.getXMLPrefix();
                        }

                        JTextField prefixText = createTextField(prefix);
                        JTextField uriText = createTextField(namespace.getURI());
                        JLabel prefixLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.xml.prefix"));

                        int bottom = i != max ? 5 : 0;
                        xmlPanel.add(new JLabel("URI"), GuiUtil.setConstraints(0, i, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, bottom, 5));
                        xmlPanel.add(uriText, GuiUtil.setConstraints(1, i, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, bottom, 5));
                        xmlPanel.add(prefixLabel, GuiUtil.setConstraints(2, i, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, bottom, 5));
                        xmlPanel.add(prefixText, GuiUtil.setConstraints(3, i++, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, bottom, 0));

                        PopupMenuDecorator.getInstance().decorate(uriText, prefixText);
                    }
                }
            }

            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                content.add(xmlPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
            }

            TitledPanel schemaPanel = new TitledPanel()
                    .withTitle(Language.I18N.getString("db.dialog.ade.label.xml.namespaces"))
                    .build(content);

            main.add(schemaPanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        }

        JButton closeButton = new JButton(Language.I18N.getString("common.button.ok"));
        closeButton.addActionListener(e -> dispose());

        JPanel info = new JPanel();
        info.setLayout(new GridBagLayout());
        {
            info.add(main, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 10, 10, 0, 10));
            info.add(Box.createVerticalGlue(), GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
            info.add(closeButton, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 10, 10, 10));
        }

        JTabbedPane infoPane = new JTabbedPane();
        infoPane.add(metadata.getIdentifier(), info);
        infoPane.setUI(new FlatTabbedPaneUI() {
            protected void paintTabBackground(Graphics g, int p, int i, int x, int y, int w, int h, boolean s) {
                // do not paint tab background
            }
        });

        add(infoPane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));

        PopupMenuDecorator.getInstance().decorate(nameText, versionText, descriptionText, idText,
                featureTypesText, dbPrefixText, objectClassIdText);

        pack();
    }

    private JTextField createTextField(String text) {
        JTextField textField = new JTextField(text);
        textField.setEditable(false);
        textField.setBackground(UIManager.getColor("TextField.background"));
        return textField;
    }

    private JTextArea createTextArea(String text, int rows) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setRows(rows);
        textArea.setFont(UIManager.getFont("Label.font"));
        textArea.setBackground(UIManager.getColor("TextField.background"));
        return textArea;
    }
}
