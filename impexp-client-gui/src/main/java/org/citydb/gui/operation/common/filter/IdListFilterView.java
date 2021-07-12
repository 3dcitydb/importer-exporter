package org.citydb.gui.operation.common.filter;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.common.IdColumnType;
import org.citydb.config.project.common.IdList;
import org.citydb.core.operation.common.csv.IdListPreviewer;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class IdListFilterView<T extends IdList> extends FilterView<T> {
    private final Logger log = Logger.getInstance();
    private final ReentrantLock mainLock = new ReentrantLock();
    private final ViewController viewController;
    private final Supplier<T> idListSupplier;

    private JPanel component;
    private JLabel idListLabel;
    private JTextField idListFile;
    private JButton idListBrowseButton;
    private JButton idListPreviewButton;
    private JLabel idColumnLabel;
    private JRadioButton idColumnNameButton;
    private JTextField idColumnName;
    private JRadioButton idColumnIndexButton;
    private JSpinner idColumnIndex;
    private JCheckBox skipHeader;
    private JComboBox<IdColumnType> idColumnType;
    private JLabel delimiterLabel;
    private JComboBox<Delimiter> delimiter;
    private JLabel quoteLabel;
    private JTextField quote;
    private JLabel commentLabel;
    private JTextField comment;
    private JLabel encodingLabel;
    private JComboBox<String> encoding;

    private Supplier<String> titleSupplier;
    private Supplier<String> errorDialogTitleSupplier;
    private boolean enabled = true;
    private int row;

    public IdListFilterView(ViewController viewController, Supplier<T> idListSupplier) {
        this.viewController = viewController;
        this.idListSupplier = idListSupplier;
        init();
    }

    public IdListFilterView<T> withHeaderRow(JComponent label, JComponent content) {
        component.add(label, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 10));
        component.add(content, GuiUtil.setConstraints(1, 0, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 0));
        return this;
    }

    public IdListFilterView<T> addFooterRow(JComponent label, JComponent content) {
        component.add(label, GuiUtil.setConstraints(0, row, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
        component.add(content, GuiUtil.setConstraints(1, row++, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
        return this;
    }

    public IdListFilterView<T> withIdColumnTypeSelection() {
        idColumnType = new JComboBox<>();
        Arrays.stream(IdColumnType.values()).forEach(idColumnType::addItem);
        component.add(idColumnType, GuiUtil.setConstraints(1, 2, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 15));
        return this;
    }

    public IdListFilterView<T> withLocalizedTitle(Supplier<String> titleSupplier) {
        this.titleSupplier = titleSupplier;
        return this;
    }

    public IdListFilterView<T> withLocalizedErrorDialogTitle(Supplier<String> errorDialogTitleSupplier) {
        this.errorDialogTitleSupplier = errorDialogTitleSupplier;
        return this;
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        idListLabel = new JLabel();
        idListFile = new JTextField();
        idListFile.setPreferredSize(new Dimension(0, idListFile.getPreferredSize().height));
        idListBrowseButton = new JButton();
        idListPreviewButton = new JButton();

        idColumnLabel = new JLabel();
        idColumnNameButton = new JRadioButton();
        idColumnIndexButton = new JRadioButton();
        skipHeader = new JCheckBox();

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(idColumnNameButton);
        buttonGroup.add(idColumnIndexButton);

        idColumnName = new JTextField();
        idColumnIndex = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

        delimiterLabel = new JLabel();
        quoteLabel = new JLabel();
        commentLabel = new JLabel();

        delimiter = new JComboBox<>();
        delimiter.setEditable(true);
        Arrays.stream(Delimiter.values()).forEach(delimiter::addItem);

        quote = new JTextField();
        quote.setColumns(2);
        quote.setMinimumSize(quote.getPreferredSize());
        comment = new JTextField();
        comment.setColumns(2);
        comment.setMinimumSize(comment.getPreferredSize());

        encodingLabel = new JLabel();
        encoding = new JComboBox<>();
        encoding.setEditable(true);

        Set<String> encodings = new TreeSet<>();
        encodings.add(System.getProperty("file.encoding"));
        encodings.add(StandardCharsets.UTF_8.name());
        encodings.add(StandardCharsets.UTF_16.name());
        encodings.add(StandardCharsets.ISO_8859_1.name());
        encodings.add(StandardCharsets.US_ASCII.name());
        encodings.forEach(encoding::addItem);

        JPanel idColumnNamePanel = new JPanel();
        idColumnNamePanel.setLayout(new GridBagLayout());
        {
            idColumnNamePanel.add(idColumnNameButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
            idColumnNamePanel.add(idColumnName, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        }

        JPanel idColumnIndexPanel = new JPanel();
        idColumnIndexPanel.setLayout(new GridBagLayout());
        {
            idColumnIndexPanel.add(idColumnIndexButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
            idColumnIndexPanel.add(idColumnIndex, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            idColumnIndexPanel.add(skipHeader, GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 15, 0, 0));
        }

        JPanel charactersPanel = new JPanel();
        charactersPanel.setLayout(new GridBagLayout());
        {
            charactersPanel.add(delimiter, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
            charactersPanel.add(quoteLabel, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 15, 0, 5));
            charactersPanel.add(quote, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            charactersPanel.add(commentLabel, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 15, 0, 5));
            charactersPanel.add(comment, GuiUtil.setConstraints(4, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            charactersPanel.add(encodingLabel, GuiUtil.setConstraints(5, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 15, 0, 5));
            charactersPanel.add(encoding, GuiUtil.setConstraints(6, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        }

        row = 1;
        component.add(idListLabel, GuiUtil.setConstraints(0, row, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 10));
        component.add(idListFile, GuiUtil.setConstraints(1, row, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
        component.add(idListBrowseButton, GuiUtil.setConstraints(3, row++, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, 0, 0));
        component.add(idColumnLabel, GuiUtil.setConstraints(0, row, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
        component.add(idColumnNamePanel, GuiUtil.setConstraints(2, row, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
        component.add(idListPreviewButton, GuiUtil.setConstraints(3, row++, 0, 0, GridBagConstraints.HORIZONTAL, 5, 10, 0, 0));
        component.add(idColumnIndexPanel, GuiUtil.setConstraints(2, row++, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
        component.add(delimiterLabel, GuiUtil.setConstraints(0, row, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
        component.add(charactersPanel, GuiUtil.setConstraints(1, row++, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));

        idListBrowseButton.addActionListener(e -> browseIdList(titleSupplier != null ?
                titleSupplier.get() :
                Language.I18N.getString("filter.border.idList")));
        idColumnNameButton.addActionListener(e -> setEnabledIdColumn());
        idColumnIndexButton.addActionListener(e -> setEnabledIdColumn());

        idListPreviewButton.addActionListener(e -> new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                doIdListPreview();
                return null;
            }
        }.execute());

        PopupMenuDecorator.getInstance().decorate(idListFile, idColumnName, quote, comment,
                ((JSpinner.DefaultEditor) idColumnIndex.getEditor()).getTextField(),
                (JTextField) delimiter.getEditor().getEditorComponent(),
                (JTextField) encoding.getEditor().getEditorComponent());
    }

    @Override
    public String getLocalizedTitle() {
        return titleSupplier != null ? titleSupplier.get() : Language.I18N.getString("filter.border.idList");
    }

    @Override
    public Component getViewComponent() {
        return component;
    }

    @Override
    public String getToolTip() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return new FlatSVGIcon("org/citydb/gui/filter/list.svg");
    }

    @Override
    public void doTranslation() {
        idListLabel.setText(Language.I18N.getString("filter.idList.label.csvFile"));
        idListBrowseButton.setText(Language.I18N.getString("common.button.browse"));
        idListPreviewButton.setText(Language.I18N.getString("filter.idList.button.preview"));
        idColumnLabel.setText(Language.I18N.getString("filter.idList.label.idColumn"));
        idColumnNameButton.setText(Language.I18N.getString("filter.idList.label.idColumn.name"));
        idColumnIndexButton.setText(Language.I18N.getString("filter.idList.label.idColumn.index"));
        skipHeader.setText(Language.I18N.getString("filter.idList.label.skipHeader"));
        delimiterLabel.setText(Language.I18N.getString("filter.idList.label.delimiter"));
        quoteLabel.setText(Language.I18N.getString("filter.idList.label.quote"));
        commentLabel.setText(Language.I18N.getString("filter.idList.label.comment"));
        encodingLabel.setText(Language.I18N.getString("filter.idList.label.encoding"));

        Object item = delimiter.getSelectedItem();
        delimiter.removeAllItems();
        Arrays.stream(Delimiter.values()).forEach(delimiter::addItem);
        delimiter.setSelectedItem(item);

        if (idColumnType != null) {
            int selected = idColumnType.getSelectedIndex();
            idColumnType.removeAllItems();
            Arrays.stream(IdColumnType.values()).forEach(idColumnType::addItem);
            idColumnType.setSelectedIndex(selected);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        idListLabel.setEnabled(enabled);
        idListFile.setEnabled(enabled);
        idListBrowseButton.setEnabled(enabled);
        idListPreviewButton.setEnabled(enabled);
        idColumnLabel.setEnabled(enabled);
        idColumnNameButton.setEnabled(enabled);
        idColumnIndexButton.setEnabled(enabled);
        delimiterLabel.setEnabled(enabled);
        delimiter.setEnabled(enabled);
        quoteLabel.setEnabled(enabled);
        quote.setEnabled(enabled);
        commentLabel.setEnabled(enabled);
        comment.setEnabled(enabled);
        encodingLabel.setEnabled(enabled);
        encoding.setEnabled(enabled);
        setEnabledIdColumn();

        if (idColumnType != null) {
            idColumnType.setEnabled(enabled);
        }
    }

    private void setEnabledIdColumn() {
        boolean nameEnabled = enabled && idColumnNameButton.isSelected();
        boolean indexEnabled = enabled && idColumnIndexButton.isSelected();
        idColumnName.setEnabled(nameEnabled);
        idColumnIndex.setEnabled(indexEnabled);
        skipHeader.setEnabled(indexEnabled);
    }

    @Override
    public void loadSettings(IdList idList) {
        idListFile.setText(idList.getFile());
        idColumnName.setText(idList.getIdColumnName());
        idColumnIndex.setValue(idList.getIdColumnIndex());
        skipHeader.setSelected(idList.hasHeader());
        quote.setText(String.valueOf(idList.getQuoteCharacter()));
        comment.setText(idList.getCommentCharacter() != null ? String.valueOf(idList.getCommentCharacter()) : "");
        encoding.setSelectedItem(idList.getEncoding());

        if (idList.getIdColumnName() != null) {
            idColumnNameButton.setSelected(true);
        } else {
            idColumnIndexButton.setSelected(true);
        }

        Delimiter temp = Delimiter.fromValue(idList.getDelimiter());
        delimiter.setSelectedItem(temp != null ? temp : idList.getDelimiter());

        if (idColumnType != null) {
            idColumnType.setSelectedItem(idList.getIdColumnType());
        }
    }

    @Override
    public T toSettings() {
        T idList = idListSupplier.get();
        idList.setFile(idListFile.getText());
        idList.setIdColumnName(idColumnNameButton.isSelected() ? idColumnName.getText() : null);
        idList.setIdColumnIndex(((Number) idColumnIndex.getValue()).intValue());
        idList.setHasHeader(skipHeader.isSelected());

        Object item = delimiter.getSelectedItem();
        if (item instanceof Delimiter) {
            idList.setDelimiter(((Delimiter) item).getDelimiter());
        } else if (item instanceof String && !((String) item).isEmpty()) {
            idList.setDelimiter(((String) item));
        } else {
            idList.setDelimiter(IdList.DEFAULT_DELIMITER);
        }

        idList.setQuoteCharacter(!quote.getText().isEmpty() ?
                quote.getText().charAt(0) :
                IdList.DEFAULT_QUOTE_CHARACTER);

        idList.setCommentCharacter(!comment.getText().isEmpty() ?
                comment.getText().charAt(0) :
                null);

        idList.setEncoding(encoding.getSelectedItem() != null && !((String) encoding.getSelectedItem()).isEmpty() ?
                (String) encoding.getSelectedItem() :
                IdList.DEFAULT_ENCODING);

        if (idColumnType != null) {
            idList.setIdColumnType((IdColumnType) idColumnType.getSelectedItem());
        }

        return idList;
    }

    public boolean checkSettings(boolean preview) {
        String title = errorDialogTitleSupplier != null ?
                errorDialogTitleSupplier.get() :
                Language.I18N.getString("common.dialog.error.incorrectFilter");

        if (idListFile.getText().trim().isEmpty()) {
            viewController.errorMessage(title, Language.I18N.getString("filter.idList.dialog.error.file"));
            return false;
        }

        if (!preview && idColumnNameButton.isSelected() && idColumnName.getText().trim().isEmpty()) {
            viewController.errorMessage(title, Language.I18N.getString("filter.idList.dialog.error.columnName"));
            return false;
        }

        Object selectedDelimiter = delimiter.getSelectedItem();
        if (selectedDelimiter instanceof String) {
            if (((String) selectedDelimiter).isEmpty()) {
                viewController.errorMessage(title, MessageFormat.format(
                        Language.I18N.getString("filter.idList.dialog.error.delimiter.empty"),
                        IdList.DEFAULT_DELIMITER));
                return false;
            } else if (((String) selectedDelimiter).length() > 1) {
                viewController.errorMessage(title, Language.I18N.getString("filter.idList.dialog.error.delimiter.char"));
                return false;
            }
        }

        if (quote.getText().isEmpty()) {
            viewController.errorMessage(title, MessageFormat.format(
                    Language.I18N.getString("filter.idList.dialog.error.quote.empty"),
                    IdList.DEFAULT_QUOTE_CHARACTER));
            return false;
        } else if (quote.getText().length() > 1) {
            viewController.errorMessage(title, Language.I18N.getString("filter.idList.dialog.error.quote.char"));
            return false;
        }

        if (comment.getText().length() > 1) {
            viewController.errorMessage(title, Language.I18N.getString("filter.idList.dialog.error.comment.char"));
            return false;
        }

        if (encoding.getSelectedItem() == null || ((String) encoding.getSelectedItem()).isEmpty()) {
            viewController.errorMessage(title, MessageFormat.format(
                    Language.I18N.getString("filter.idList.dialog.error.encoding"),
                    IdList.DEFAULT_ENCODING));
            return false;
        }

        return true;
    }

    private void doIdListPreview() {
        final ReentrantLock lock = this.mainLock;
        lock.lock();

        try {
            viewController.clearConsole();
            if (!checkSettings(true)) {
                return;
            }

            try {
                IdListPreviewer.of(toSettings())
                        .withNumberOfRecords(20)
                        .printToConsole();
                log.info("Identifier list preview successfully finished.");
            } catch (Exception e) {
                log.error("Failed to create a preview of the identifier list.", e);
            }
        } finally {
            lock.unlock();
        }
    }

    private void browseIdList(String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv, *.txt, *.log)", "csv", "txt", "log");
        chooser.addChoosableFileFilter(filter);
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.setFileFilter(filter);

        if (!idListFile.getText().trim().isEmpty()) {
            File file = new File(idListFile.getText().trim());
            if (!file.isDirectory()) {
                file = file.getParentFile();
            }

            chooser.setCurrentDirectory(file);
        }

        if (chooser.showSaveDialog(viewController.getTopFrame()) == JFileChooser.APPROVE_OPTION) {
            try {
                idListFile.setText(chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception e) {
                //
            }
        }
    }

    private enum Delimiter {
        COMMA(','),
        SEMICOLON(';'),
        COLON(':'),
        SPACE(' '),
        TAB('\t');

        private final Character delimiter;

        Delimiter(Character delimiter) {
            this.delimiter = delimiter;
        }

        public Character getDelimiter() {
            return delimiter;
        }

        public static Delimiter fromValue(String delimiter) {
            return (delimiter != null && !delimiter.isEmpty()) ? fromValue(delimiter.charAt(0)) : null;
        }

        public static Delimiter fromValue(Character delimiter) {
            for (Delimiter value : values()) {
                if (value.delimiter.equals(delimiter)) {
                    return value;
                }
            }

            return null;
        }

        @Override
        public String toString() {
            switch (this) {
                case COMMA:
                    return Language.I18N.getString("filter.idList.delimiter.comma");
                case SEMICOLON:
                    return Language.I18N.getString("filter.idList.delimiter.semicolon");
                case COLON:
                    return Language.I18N.getString("filter.idList.delimiter.colon");
                case SPACE:
                    return Language.I18N.getString("filter.idList.delimiter.space");
                case TAB:
                    return Language.I18N.getString("filter.idList.delimiter.tab");
                default:
                    return "";
            }
        }
    }
}
