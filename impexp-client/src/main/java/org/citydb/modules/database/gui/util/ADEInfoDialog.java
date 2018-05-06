package org.citydb.modules.database.gui.util;

import org.citydb.config.i18n.Language;
import org.citydb.database.schema.mapping.AbstractType;
import org.citydb.database.schema.mapping.AppSchema;
import org.citydb.database.schema.mapping.Metadata;
import org.citydb.database.schema.mapping.Namespace;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.jdesktop.swingx.JXTitledSeparator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

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

        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        add(header, GuiUtil.setConstraints(0,0,1,0,GridBagConstraints.HORIZONTAL,0,0,0,0));
        header.setLayout(new GridBagLayout());
        {
            JLabel name = new JLabel(metadata.getIdentifier());
            name.setFont(name.getFont().deriveFont(Font.BOLD));
            header.add(name, GuiUtil.setConstraints(0,0,1,0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,5,5,5,5));
        }

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        add(separator, GuiUtil.setConstraints(0,1,1,0,GridBagConstraints.HORIZONTAL,0,0,0,0));

        JPanel main = new JPanel();
        add(main, GuiUtil.setConstraints(0,2,1,1,GridBagConstraints.BOTH,5,5,5,5));
        main.setLayout(new GridBagLayout());
        {
            // general information
            JLabel nameLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.name"));
            JTextField nameText = createTextField(metadata.getName());
            JLabel versionLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.version"));
            JTextField versionText = createTextField(metadata.getVersion());
            JLabel descriptionLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.description"));
            JTextArea descriptionText = createTextArea(metadata.getDescription());
            JScrollPane descriptionPane = new JScrollPane(descriptionText);
            descriptionPane.setBorder(null);
            JLabel idLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.identifier"));
            JTextField idText = createTextField(adeInfo.getId());

            JLabel cityGMLLabel = new JLabel("CityGML");
            JPanel cityGMLPanel = new JPanel();
            cityGMLPanel.setLayout(new OverlayLayout(cityGMLPanel));
            {
                JPanel glassPane = new JPanel();
                glassPane.addMouseListener(new MouseAdapter() {});
                glassPane.setOpaque(false);
                cityGMLPanel.add(glassPane);

                JPanel content = new JPanel();
                content.setLayout(new GridBagLayout());

                int i = 0;
                for (CityGMLVersion cityGMLVersion : CityGMLVersion.getInstances()) {
                    JCheckBox versionCheck = new JCheckBox(cityGMLVersion.toString());
                    versionCheck.setSelected(adeSchema.getSchemas().stream().anyMatch(s -> s.isAvailableForCityGML(cityGMLVersion)));
                    content.add(versionCheck, GuiUtil.setConstraints(i++,0,0,0,GridBagConstraints.NONE,0,0,0,10));
                }

                content.add(Box.createHorizontalGlue(), GuiUtil.setConstraints(i,0,1,0,GridBagConstraints.HORIZONTAL,0,0,0,0));
                cityGMLPanel.add(content);
            }

            JLabel statusLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.status"));
            JLabel statusText = new JLabel();
            if (adeInfo.hasDatabaseSupport() && adeInfo.hasImpexpSupport()) {
                statusText.setText(Language.I18N.getString("db.dialog.ade.status.ok"));
                statusText.setIcon(new ImageIcon(ADEInfoRow.class.getResource("/org/citydb/gui/images/common/done.png")));
            } else {
                statusText.setText(adeInfo.hasDatabaseSupport() ? Language.I18N.getString("db.dialog.ade.status.noImpExp") :
                        Language.I18N.getString("db.dialog.ade.status.noDB"));
                statusText.setIcon(new ImageIcon(ADEInfoRow.class.getResource("/org/citydb/gui/images/common/error_outline.png")));
            }

            // database information
            JXTitledSeparator dbSeparator = new JXTitledSeparator(Language.I18N.getString("main.tabbedPane.database"));
            JLabel dbPrefixLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.dbPrefix"));
            JTextField dbPrefixText = createTextField(metadata.getDBPrefix());

            int lowerObjectClassId = Integer.MAX_VALUE;
            int upperObjectClassId = -Integer.MAX_VALUE;
            for (AbstractType<?> types : adeSchema.getAbstractTypes()) {
                int objectClassId = types.getObjectClassId();
                if (objectClassId < lowerObjectClassId)
                    lowerObjectClassId = objectClassId;
                else if (objectClassId > upperObjectClassId)
                    upperObjectClassId = objectClassId;
            }

            JLabel objectClassIdLabel = new JLabel("ObjectClassId");
            JTextField objectClassIdText = createTextField(lowerObjectClassId + " .. " + upperObjectClassId);

            // XML schema information
            JXTitledSeparator xmlSeparator = new JXTitledSeparator(Language.I18N.getString("db.dialog.ade.label.xml.schema"));
            JLabel xmlLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.xml.namespaces"));
            JPanel xmlPanel = new JPanel();
            xmlPanel.setLayout(new GridBagLayout());
            {
                int i = 0;
                for (AppSchema appSchema : adeSchema.getSchemas()) {
                    for (Namespace namespace : appSchema.getNamespaces()) {
                        AppSchema tmp = rootSchema.getSchema(namespace.getURI());

                        String prefix;
                        if (tmp != null)
                            prefix = tmp.getXMLPrefix();
                        else if (!appSchema.isGeneratedXMLPrefix())
                            prefix = appSchema.getXMLPrefix();
                        else
                            prefix = "n/a";

                        JTextField prefixText = createTextField(prefix);
                        JTextField uriText = createTextField(namespace.getURI());
                        JLabel prefixLabel = new JLabel(Language.I18N.getString("db.dialog.ade.label.xml.prefix"));

                        xmlPanel.add(uriText, GuiUtil.setConstraints(0,i,1,0,GridBagConstraints.HORIZONTAL,0,0,5,5));
                        xmlPanel.add(prefixLabel, GuiUtil.setConstraints(1,i,0,0,GridBagConstraints.HORIZONTAL,0,10,5,5));
                        xmlPanel.add(prefixText, GuiUtil.setConstraints(2,i++,0,0,GridBagConstraints.HORIZONTAL,0,5,5,0));

                        PopupMenuDecorator.getInstance().decorate(uriText, prefixText);
                    }
                }
            }

            main.add(nameLabel, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(nameText, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(versionLabel, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(versionText, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(descriptionLabel, GuiUtil.setConstraints(0,2,0,0,GridBagConstraints.NORTHWEST,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(descriptionPane, GuiUtil.setConstraints(1,2,1,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(idLabel, GuiUtil.setConstraints(0,3,0,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(idText, GuiUtil.setConstraints(1,3,1,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(cityGMLLabel, GuiUtil.setConstraints(0,4,0,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(cityGMLPanel, GuiUtil.setConstraints(1,4,2,1,1,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(statusLabel, GuiUtil.setConstraints(0,5,0,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(statusText, GuiUtil.setConstraints(1,5,1,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(dbSeparator, GuiUtil.setConstraints(0,6,2,1,1,0,GridBagConstraints.HORIZONTAL,10,5,5,5));
            main.add(dbPrefixLabel, GuiUtil.setConstraints(0,7,0,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(dbPrefixText, GuiUtil.setConstraints(1,7,2,1,1,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(objectClassIdLabel, GuiUtil.setConstraints(0,8,0,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(objectClassIdText, GuiUtil.setConstraints(1,8,2,1,1,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(xmlSeparator, GuiUtil.setConstraints(0,9,2,1,1,0,GridBagConstraints.HORIZONTAL,10,5,5,5));
            main.add(xmlLabel, GuiUtil.setConstraints(0,10,0,0,GridBagConstraints.NORTHWEST,GridBagConstraints.HORIZONTAL,0,5,5,5));
            main.add(xmlPanel, GuiUtil.setConstraints(1,10,2,1,1,0,GridBagConstraints.HORIZONTAL,0,5,0,5));
            main.add(Box.createVerticalGlue(), GuiUtil.setConstraints(0,11,2,1,1,1,GridBagConstraints.BOTH,0,0,0,0));

            PopupMenuDecorator.getInstance().decorate(nameText, versionText, descriptionText, idText,
                    dbPrefixText, objectClassIdText);
        }

        JButton closeButton = new JButton(Language.I18N.getString("common.button.ok"));
        closeButton.addActionListener(l -> dispose());

        closeButton.setMargin(new Insets(closeButton.getMargin().top, 25, closeButton.getMargin().bottom, 25));
        add(closeButton, GuiUtil.setConstraints(0,3,0,0,GridBagConstraints.NONE,5,5,5,5));

        setMinimumSize(new Dimension(500, 400));
        setResizable(true);
        pack();
    }

    private JTextField createTextField(String text) {
        JTextField textField = new JTextField(text);
        textField.setBackground(Color.WHITE);
        textField.setEditable(false);
        textField.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        return textField;
    }

    private JTextArea createTextArea(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setBackground(Color.WHITE);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setRows(2);
        textArea.setFont(UIManager.getFont("Label.font"));
        textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        return textArea;
    }
}
