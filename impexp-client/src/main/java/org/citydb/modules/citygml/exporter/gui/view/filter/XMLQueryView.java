package org.citydb.modules.citygml.exporter.gui.view.filter;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.query.Query;
import org.citydb.config.project.query.filter.appearance.AppearanceFilter;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.projection.ProjectionFilter;
import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.SelectionFilter;
import org.citydb.config.project.query.filter.selection.logical.AndOperator;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.config.project.query.filter.selection.spatial.WithinOperator;
import org.citydb.config.project.query.filter.tiling.Tiling;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.project.query.simple.SimpleBBOXMode;
import org.citydb.config.project.query.simple.SimpleSelectionFilter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.exporter.gui.view.FilterPanel;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXWriter;
import org.citygml4j.xml.CityGMLNamespaceContext;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.xml.sax.SAXException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class XMLQueryView extends FilterView {
    private final Logger log = Logger.getInstance();
    private final FilterPanel filterPanel;
    private final JAXBContext projectContext;
    private final SchemaMapping schemaMapping;
    private final DatabaseConnectionPool connectionPool;

    private JPanel component;
    private RSyntaxTextArea xmlText;

    private JButton newButton;
    private JButton duplicateButton;
    private JButton validateButton;

    public XMLQueryView(FilterPanel filterPanel, JAXBContext projectContext, Config config) {
        super(config);
        this.filterPanel = filterPanel;
        this.projectContext = projectContext;

        schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
        connectionPool = DatabaseConnectionPool.getInstance();

        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        xmlText = new RSyntaxTextArea("", 5, 1);
        try (InputStream in = getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml")) {
            Theme.load(in).apply(xmlText);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize SQL editor.", e);
        }

        xmlText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        xmlText.setAutoIndentEnabled(true);
        xmlText.setHighlightCurrentLine(true);
        xmlText.setCodeFoldingEnabled(true);
        xmlText.setTabSize(2);
        RTextScrollPane scrollPane = new RTextScrollPane(xmlText);

        newButton = new JButton();
        ImageIcon add = new ImageIcon(getClass().getResource("/org/citydb/gui/images/common/filter_new.png"));
        newButton.setIcon(add);
        newButton.setMargin(new Insets(0, 0, 0, 0));

        duplicateButton = new JButton();
        ImageIcon remove = new ImageIcon(getClass().getResource("/org/citydb/gui/images/common/filter_duplicate.png"));
        duplicateButton.setIcon(remove);
        duplicateButton.setMargin(new Insets(0, 0, 0, 0));

        validateButton = new JButton();
        ImageIcon validate = new ImageIcon(getClass().getResource("/org/citydb/gui/images/common/done.png"));
        validateButton.setIcon(validate);
        validateButton.setMargin(new Insets(0, 0, 0, 0));

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridBagLayout());
        buttons.add(newButton, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,0,5,5,0));
        buttons.add(duplicateButton, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.NONE,0,5,5,0));
        buttons.add(validateButton, GuiUtil.setConstraints(0,2,0,0,GridBagConstraints.NONE,0,5,0,0));

        component.add(scrollPane, GuiUtil.setConstraints(0,0,1,1,GridBagConstraints.BOTH,10,5,5,0));
        component.add(buttons, GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.NORTH,GridBagConstraints.NONE,10,0,5,5));

        newButton.addActionListener(e -> SwingUtilities.invokeLater(this::setEmptyQuery));
        duplicateButton.addActionListener(e -> SwingUtilities.invokeLater(this::setSimpleSettings));
        validateButton.addActionListener(e -> SwingUtilities.invokeLater(this::validate));

        PopupMenuDecorator.getInstance().decorate(xmlText);
    }

    private void setEmptyQuery() {
        Query query = new Query();
        query.setFeatureTypeFilter(new FeatureTypeFilter());
        query.setAppearanceFilter(new AppearanceFilter());
        query.setCounterFilter(new CounterFilter());
        query.setLodFilter(new LodFilter());
        query.setProjectionFilter(new ProjectionFilter());
        query.setSelectionFilter(new SelectionFilter());
        query.setTiling(new Tiling());

        CityGMLNamespaceContext namespaceContext = new CityGMLNamespaceContext();
        namespaceContext.setPrefixes(new ModuleContext(CityGMLVersion.v2_0_0));
        namespaceContext.setDefaultNamespace(ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI);

        xmlText.setText(marshalQuery(query, namespaceContext));
    }

    private void setSimpleSettings() {
        filterPanel.setSimpleQuerySettings();
        SimpleQuery simpleQuery = config.getProject().getExporter().getSimpleQuery();
        CityGMLVersion version = Util.toCityGMLVersion(simpleQuery.getVersion());

        Query query = new Query();

        FeatureTypeFilter typeFilter = new FeatureTypeFilter();
        if (simpleQuery.isUseTypeNames()) {
            if (simpleQuery.isSetFeatureTypeFilter() && !simpleQuery.getFeatureTypeFilter().isEmpty()) {
                for (QName typeName : simpleQuery.getFeatureTypeFilter().getTypeNames()) {
                    FeatureType featureType = schemaMapping.getFeatureType(typeName);
                    if (featureType == null || !featureType.isAvailableForCityGML(version))
                        continue;

                    typeFilter.addTypeName(typeName);
                }
            }
        } else
            typeFilter.addTypeName(new QName(version.getCityGMLModule(CityGMLModuleType.CORE).getNamespaceURI(), "_CityObject"));

        if (!typeFilter.isEmpty())
            query.setFeatureTypeFilter(typeFilter);

        if (simpleQuery.isUseLodFilter()
                && simpleQuery.isSetLodFilter()
                && simpleQuery.getLodFilter().isSetAnyLod())
            query.setLodFilter(simpleQuery.getLodFilter());

        if (simpleQuery.isUseCountFilter()
                && simpleQuery.isSetCounterFilter()
                && simpleQuery.getCounterFilter().isSetUpperLimit())
            query.setCounterFilter(simpleQuery.getCounterFilter());

        List<AbstractPredicate> predicates = new ArrayList<>();
        if (simpleQuery.isUseSelectionFilter()) {
            SimpleSelectionFilter selectionFilter = simpleQuery.getSelectionFilter();

            if (!selectionFilter.isUseSQLFilter()) {
                if (selectionFilter.isSetGmlIdFilter() && selectionFilter.getGmlIdFilter().isSetResourceIds())
                    predicates.add(selectionFilter.getGmlIdFilter());

                if (selectionFilter.isSetGmlNameFilter() && selectionFilter.getGmlNameFilter().isSetLiteral())
                    predicates.add(selectionFilter.getGmlNameFilter());
            } else if (selectionFilter.isSetSQLFilter() && selectionFilter.getSQLFilter().isSetValue())
                predicates.add(selectionFilter.getSQLFilter());
        }

        if (simpleQuery.isUseBboxFilter()
                && simpleQuery.isSetBboxFilter()
                && simpleQuery.getBboxFilter().isSetEnvelope()) {
            BoundingBox envelope = simpleQuery.getBboxFilter().getEnvelope();
            if (envelope.getLowerCorner().isSetX() && envelope.getLowerCorner().isSetY()
                    && envelope.getUpperCorner().isSetX() && envelope.getUpperCorner().isSetY()) {
                if (!isDefaultDatabaseSrs(envelope.getSrs()))
                    envelope.setSrs(envelope.getSrs().getSrid());
                else
                    envelope.unsetSrs();

                if (simpleQuery.isUseTiling()) {
                    Tiling tiling = new Tiling();
                    tiling.setExtent(envelope);
                    tiling.setRows(simpleQuery.getTilingOptions().getRows());
                    tiling.setColumns(simpleQuery.getTilingOptions().getColumns());
                    query.setTiling(tiling);
                } else {
                    if (simpleQuery.getBboxFilter().getBboxMode() == SimpleBBOXMode.BBOX) {
                        BBOXOperator bbox = new BBOXOperator();
                        bbox.setEnvelope(envelope);
                        bbox.setValueReference("gml:boundedBy");
                        predicates.add(bbox);
                    } else {
                        WithinOperator within = new WithinOperator();
                        within.setSpatialOperand(envelope);
                        within.setValueReference("gml:boundedBy");
                        predicates.add(within);
                    }
                }
            }
        }

        AbstractPredicate predicate = null;
        if (predicates.size() == 1) {
            predicate = predicates.get(0);
        } else if (predicates.size() > 1) {
            predicate = new AndOperator();
            ((AndOperator) predicate).setOperands(predicates);
        }

        if (predicate != null) {
            SelectionFilter selectionFilter = new SelectionFilter();
            selectionFilter.setPredicate(predicate);
            query.setSelectionFilter(selectionFilter);
        }

        CityGMLNamespaceContext namespaceContext = new CityGMLNamespaceContext();
        namespaceContext.setPrefixes(new ModuleContext(version));
        namespaceContext.setDefaultNamespace(ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI);

        xmlText.setText(marshalQuery(query, namespaceContext));
    }

    private void validate() {
        log.info("Validating XML query.");

        if (xmlText.getText().trim().isEmpty()) {
            log.warn("No XML query element defined. Aborting validation.");
            return;
        }

        Query query = unmarshalQuery(true);
        if (query == null) {
            log.error("Failed to unmarshal XML query.");
            return;
        }

        System.out.println(query);
    }

    private Query unmarshalQuery(boolean validate) {
        try {
            Boolean[] success = {true};
            Unmarshaller unmarshaller = projectContext.createUnmarshaller();

            if (validate) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(getClass().getResource("/org/citydb/config/schema/project.xsd"));

                unmarshaller.setSchema(schema);
                unmarshaller.setEventHandler(event -> {
                    success[0] = false;
                    log.error("Invalid content at [" + event.getLocator().getLineNumber() + ", " +
                            event.getLocator().getColumnNumber() + "]: " +
                            event.getMessage());
                    return true;
                });
            }

            Object object = unmarshaller.unmarshal(new StringReader(xmlText.getText()));
            return success[0] && object instanceof Query ? (Query) object : null;
        } catch (JAXBException | SAXException e) {
            return null;
        }
    }

    private String marshalQuery(Query query, NamespaceContext namespaceContext) {
        try {
            StringWriter content = new StringWriter();
            SAXWriter saxWriter = new SAXWriter(new BufferedWriter(content));
            saxWriter.setIndentString("\t");

            Marshaller marshaller = projectContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                    String prefix = namespaceContext.getPrefix(namespaceUri);
                    return prefix != null ? prefix : suggestion;
                }
            });

            marshaller.marshal(query, saxWriter);
            saxWriter.close();

            return content.toString();
        } catch (JAXBException | IOException | SAXException e) {
            return "";
        }
    }

    @Override
    public void doTranslation() {
        newButton.setToolTipText(Language.I18N.getString("filter.label.xml.template"));
        duplicateButton.setToolTipText(Language.I18N.getString("filter.label.xml.duplicate"));
        validateButton.setToolTipText(Language.I18N.getString("filter.label.xml.validate"));
    }

    @Override
    public void setEnabled(boolean enable) {

    }

    @Override
    public String getLocalizedTitle() {
        return null;
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
        return null;
    }

    @Override
    public void loadSettings() {
        Query query = config.getProject().getExporter().getQuery();
        xmlText.setText(marshalQuery(query, config.getProject().getNamespaceFilter()));
    }

    @Override
    public void setSettings() {
        Query query = unmarshalQuery(false);
        config.getProject().getExporter().setQuery(query != null ? query : new Query());
    }

    private boolean isDefaultDatabaseSrs(DatabaseSrs srs) {
        return srs.getSrid() == 0 || (connectionPool.isConnected()
                && srs.getSrid() == connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid());
    }
}

