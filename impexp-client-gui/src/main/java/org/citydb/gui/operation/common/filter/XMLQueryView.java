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

package org.citydb.gui.operation.common.filter;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.citydb.config.ConfigUtil;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.exporter.SimpleTiling;
import org.citydb.config.project.exporter.SimpleTilingMode;
import org.citydb.config.project.query.QueryConfig;
import org.citydb.config.project.query.filter.appearance.AppearanceFilter;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.projection.ProjectionFilter;
import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.SelectionFilter;
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.logical.AndOperator;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.config.project.query.filter.selection.spatial.WithinOperator;
import org.citydb.config.project.query.filter.sorting.Sorting;
import org.citydb.config.project.query.filter.tiling.Tiling;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.project.query.simple.SimpleAttributeFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;
import org.citydb.config.util.QueryWrapper;
import org.citydb.core.database.DatabaseController;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.adapter.AbstractSQLAdapter;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.query.Query;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.config.ConfigQueryBuilder;
import org.citydb.core.query.builder.sql.BuildProperties;
import org.citydb.core.query.builder.sql.SQLQueryBuilder;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.Util;
import org.citydb.gui.components.dialog.SQLDialog;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;
import org.citydb.gui.util.RSyntaxTextAreaHelper;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.util.log.Logger;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXWriter;
import org.citygml4j.xml.CityGMLNamespaceContext;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.swing.*;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class XMLQueryView extends FilterView<QueryConfig> {
    private final Logger log = Logger.getInstance();
    private final ViewController viewController;
    private final SchemaMapping schemaMapping;
    private final DatabaseController databaseController;

    private JPanel component;
    private RSyntaxTextArea xmlText;
    private JButton newButton;
    private JButton duplicateButton;
    private JButton validateButton;
    private JButton generateSqlButton;

    private Supplier<SimpleQuery> simpleQuerySupplier;

    public XMLQueryView(ViewController viewController) {
        this.viewController = viewController;
        schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
        databaseController = ObjectRegistry.getInstance().getDatabaseController();

        init();
    }

    public XMLQueryView withSimpleQuerySupplier(Supplier<SimpleQuery> simpleQuerySupplier) {
        this.simpleQuerySupplier = simpleQuerySupplier;
        duplicateButton.addActionListener(e -> SwingUtilities.invokeLater(this::setSimpleSettings));
        duplicateButton.setVisible(true);
        return this;
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        xmlText = new RSyntaxTextArea("", 5, 1);
        xmlText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        xmlText.setAutoIndentEnabled(true);
        xmlText.setHighlightCurrentLine(true);
        xmlText.setCodeFoldingEnabled(true);
        xmlText.setTabSize(2);
        RTextScrollPane scrollPane = new RTextScrollPane(xmlText);

        newButton = new JButton(new FlatSVGIcon("org/citydb/gui/icons/query_new.svg"));
        duplicateButton = new JButton(new FlatSVGIcon("org/citydb/gui/icons/copy.svg"));
        validateButton = new JButton(new FlatSVGIcon("org/citydb/gui/icons/check.svg"));
        generateSqlButton = new JButton(new FlatSVGIcon("org/citydb/gui/icons/code.svg"));

        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(BorderFactory.createEmptyBorder());
        toolBar.add(newButton);
        toolBar.add(duplicateButton);
        toolBar.addSeparator();
        toolBar.add(validateButton);
        toolBar.add(generateSqlButton);
        toolBar.setOrientation(JToolBar.VERTICAL);

        component.add(scrollPane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
        component.add(toolBar, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0, 5, 0, 0));

        newButton.addActionListener(e -> SwingUtilities.invokeLater(this::setEmptyQuery));
        validateButton.addActionListener(e -> SwingUtilities.invokeLater(this::validate));
        generateSqlButton.addActionListener(e -> new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                createSQLQuery();
                return null;
            }
        }.execute());

        duplicateButton.setVisible(false);

        RSyntaxTextAreaHelper.installDefaultTheme(xmlText);
        PopupMenuDecorator.getInstance().decorate(xmlText);
    }

    private void setEmptyQuery() {
        QueryConfig query = new QueryConfig();
        query.setFeatureTypeFilter(new FeatureTypeFilter());
        query.setAppearanceFilter(new AppearanceFilter());
        query.setCounterFilter(new CounterFilter());
        query.setLodFilter(new LodFilter());
        query.setProjectionFilter(new ProjectionFilter());
        query.setSelectionFilter(new SelectionFilter());
        query.setSorting(new Sorting());
        query.setTiling(new Tiling());

        CityGMLNamespaceContext namespaceContext = new CityGMLNamespaceContext();
        namespaceContext.setPrefixes(new ModuleContext(CityGMLVersion.v2_0_0));
        namespaceContext.setDefaultNamespace(ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI);

        xmlText.setText(marshalQuery(query, namespaceContext));
    }

    private void setSimpleSettings() {
        SimpleQuery simpleQuery = simpleQuerySupplier.get();
        CityGMLVersion version = Util.toCityGMLVersion(simpleQuery.getVersion());

        QueryConfig query = new QueryConfig();

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

        if (simpleQuery.isSetTargetSrs() && !isDefaultDatabaseSrs(simpleQuery.getTargetSrs())) {
            DatabaseSrs targetSrs = simpleQuery.getTargetSrs();
            query.setTargetSrs(targetSrs.getSrid(), targetSrs.getGMLSrsName());
        }

        if (simpleQuery.isUseLodFilter()
                && simpleQuery.isSetLodFilter()
                && simpleQuery.getLodFilter().isSetAnyLod())
            query.setLodFilter(simpleQuery.getLodFilter());

        if (simpleQuery.isUseCountFilter()
                && simpleQuery.isSetCounterFilter()
                && (simpleQuery.getCounterFilter().isSetCount()
                || simpleQuery.getCounterFilter().isSetStartIndex()))
            query.setCounterFilter(simpleQuery.getCounterFilter());

        List<AbstractPredicate> predicates = new ArrayList<>();

        if (simpleQuery.isUseFeatureVersionFilter() && simpleQuery.isSetFeatureVersionFilter()) {
            SimpleFeatureVersionFilter featureVersionFilter = simpleQuery.getFeatureVersionFilter();

            AbstractPredicate predicate = featureVersionFilter.toPredicate();
            if (predicate != null) {
                predicates.add(predicate);
            }
        }

        if (simpleQuery.isUseAttributeFilter() && simpleQuery.isSetAttributeFilter()) {
            SimpleAttributeFilter attributeFilter = simpleQuery.getAttributeFilter();

            if (attributeFilter.isSetResourceIdFilter() && attributeFilter.getResourceIdFilter().isSetResourceIds())
                predicates.add(attributeFilter.getResourceIdFilter());

            if (attributeFilter.isSetNameFilter() && attributeFilter.getNameFilter().isSetLiteral()) {
                LikeOperator nameFilter = attributeFilter.getNameFilter();
                nameFilter.setValueReference("gml:name");
                predicates.add(nameFilter);
            }

            if (attributeFilter.isSetLineageFilter() && attributeFilter.getLineageFilter().isSetLiteral()) {
                LikeOperator lineageFilter = attributeFilter.getLineageFilter();
                lineageFilter.setValueReference("citydb:lineage");
                predicates.add(lineageFilter);
            }
        }

        if (simpleQuery.isUseSQLFilter() && simpleQuery.isSetSQLFilter()) {
            predicates.add(simpleQuery.getSQLFilter());
        }

        if (simpleQuery.isUseBboxFilter()
                && simpleQuery.isSetBboxFilter()
                && simpleQuery.getBboxFilter().isSetExtent()) {
            SimpleTiling bboxFilter = simpleQuery.getBboxFilter();

            BoundingBox envelope = simpleQuery.getBboxFilter().getExtent();
            if (envelope.getLowerCorner().isSetX() && envelope.getLowerCorner().isSetY()
                    && envelope.getUpperCorner().isSetX() && envelope.getUpperCorner().isSetY()) {
                if (envelope.isSetSrs() && !isDefaultDatabaseSrs(envelope.getSrs()))
                    envelope.setSrs(envelope.getSrs().getSrid());
                else
                    envelope.unsetSrs();

                if (bboxFilter.getMode() == SimpleTilingMode.TILING) {
                    Tiling tiling = new Tiling();
                    tiling.setExtent(envelope);
                    tiling.setRows(bboxFilter.getRows());
                    tiling.setColumns(bboxFilter.getColumns());
                    query.setTiling(tiling);
                } else {
                    if (bboxFilter.getMode() == SimpleTilingMode.BBOX) {
                        BBOXOperator bbox = new BBOXOperator();
                        bbox.setEnvelope(envelope);
                        predicates.add(bbox);
                    } else {
                        WithinOperator within = new WithinOperator();
                        within.setSpatialOperand(envelope);
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
        viewController.clearConsole();
        log.info("Validating XML query.");

        String query = xmlText.getText().trim();
        if (query.isEmpty()) {
            log.warn("No XML query element defined. Aborting validation.");
            return;
        }

        int[] errors = {0};
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(getClass().getResource("/org/citydb/config/schema/query.xsd"));

            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException exception) {
                    error(exception);
                }

                public void fatalError(SAXParseException exception) {
                    error(exception);
                }

                public void error(SAXParseException exception) {
                    errors[0]++;
                    log.error("Invalid content at [" + (exception.getLineNumber() - 1) + "," +
                            exception.getColumnNumber() + "]: " + exception.getMessage());
                }
            });

            validator.validate(new StreamSource(new StringReader(wrapQuery(query))));
        } catch (SAXException | IOException e) {
            log.error("Validation aborted due to fatal errors.");
        }

        if (errors[0] == 0) {
            if (connectToDatabase(Language.I18N.getString("filter.dialog.xml.validate.connect"))) {
                try {
                    buildQuery();
                } catch (QueryBuildException e) {
                    errors[0]++;
                    log.error("Invalid content: " + e.getMessage(), e.getCause());
                }
            } else {
                log.warn("No database connection established. Aborting validation.");
                return;
            }
        }

        if (errors[0] > 0) {
            log.warn(errors[0] + " error(s) reported while validating the XML query.");
        } else {
            log.info("The XML query is valid.");
        }
    }

    private QueryConfig unmarshalQuery() {
        QueryConfig query = null;

        try {
            Unmarshaller unmarshaller = ConfigUtil.getInstance().getJAXBContext().createUnmarshaller();
            Object object = unmarshaller.unmarshal(new StringReader(wrapQuery(xmlText.getText().trim())));
            if (object instanceof QueryWrapper)
                query = ((QueryWrapper) object).getQueryConfig();
        } catch (Exception e) {
           //
        }

        if (query == null) {
            query = new QueryConfig();
            query.setLocalProperty("unmarshallingFailed", true);
        }

        return query;
    }

    private String marshalQuery(QueryConfig query, NamespaceContext namespaceContext) {
        try {
            StringWriter content = new StringWriter();
            SAXWriter saxWriter = new SAXWriter(new BufferedWriter(content));
            saxWriter.setWriteReportedNamespaces(true);
            saxWriter.setIndentString("  ");

            Marshaller marshaller = ConfigUtil.getInstance().getJAXBContext().createMarshaller();
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

    private String wrapQuery(String query) {
        StringBuilder wrapper = new StringBuilder("<wrapper xmlns=\"")
                .append(ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI).append("\" ");

        ModuleContext context = new ModuleContext(CityGMLVersion.v2_0_0);
        for (Module module : context.getModules()) {
            wrapper.append("xmlns:").append(module.getNamespacePrefix()).append("=\"")
                    .append(module.getNamespaceURI()).append("\" ");
        }

        wrapper.append(">\n").append(query).append("</wrapper>");

        return wrapper.toString();
    }

    private void createSQLQuery() {
        viewController.clearConsole();
        log.info("Generating SQL query expression.");

        if (!connectToDatabase(Language.I18N.getString("filter.dialog.xml.generateSQL.connect"))) {
            log.warn("No database connection established. Aborting SQL query generation.");
            return;
        }

        AbstractDatabaseAdapter databaseAdapter = databaseController.getActiveDatabaseAdapter();
        SQLQueryBuilder sqlBuilder = new SQLQueryBuilder(
                schemaMapping,
                databaseAdapter,
                BuildProperties.defaults().addProjectionColumn(MappingConstants.GMLID));

        String sql = null;
        try {
            Select select = sqlBuilder.buildQuery(buildQuery());
            AbstractSQLAdapter sqlAdapter = databaseAdapter.getSQLAdapter();
            sql = SqlFormatter
                    .extend(cfg -> cfg.plusOperators("&&"))
                    .format(select.toString(), sqlAdapter.getPlaceHolderValues(select));
        } catch (QueryBuildException | SQLException e) {
            log.error("Failed to generate SQL query expression.", e);
        }

        if (sql != null) {
            final SQLDialog sqlDialog = new SQLDialog(sql, viewController.getTopFrame());
            SwingUtilities.invokeLater(() -> {
                sqlDialog.setLocationRelativeTo(viewController.getTopFrame());
                sqlDialog.setVisible(true);
            });
        }
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
    public void doTranslation() {
        newButton.setToolTipText(Language.I18N.getString("filter.label.xml.template"));
        duplicateButton.setToolTipText(Language.I18N.getString("filter.label.xml.duplicate"));
        validateButton.setToolTipText(Language.I18N.getString("filter.label.xml.validate"));
        generateSqlButton.setToolTipText(Language.I18N.getString("filter.label.xml.generateSQL"));
    }

    @Override
    public void setEnabled(boolean enabled) {
        // nothing to do
    }

    @Override
    public void loadSettings(QueryConfig queryConfig) {
        xmlText.setText(marshalQuery(queryConfig, ObjectRegistry.getInstance().getConfig().getNamespaceFilter()));
    }

    @Override
    public QueryConfig toSettings() {
        return unmarshalQuery();
    }

    private boolean isDefaultDatabaseSrs(DatabaseSrs srs) {
        return srs.getSrid() == 0
                || (databaseController.isConnected()
                && srs.getSrid() == databaseController.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid());
    }

    private boolean connectToDatabase(String message) {
        DatabaseConnection conn = ObjectRegistry.getInstance().getConfig().getDatabaseConfig().getActiveConnection();
        if (!databaseController.isConnected()) {
            int option = viewController.showOptionDialog(
                    Language.I18N.getString("common.dialog.dbConnect.title"),
                    MessageFormat.format(Language.I18N.getString("common.dialog.dbConnect.message.generic"),
                            message, conn.getDescription(), conn.toConnectString()),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            return option == JOptionPane.YES_OPTION && databaseController.connect();
        } else {
            return true;
        }
    }

    private Query buildQuery() throws QueryBuildException {
        QueryConfig query = unmarshalQuery();
        if (query.hasLocalProperty("unmarshallingFailed")) {
            throw new QueryBuildException("The XML query is invalid.");
        } else {
            return new ConfigQueryBuilder(schemaMapping, databaseController.getActiveDatabaseAdapter())
                    .buildQuery(query, ObjectRegistry.getInstance().getConfig().getNamespaceFilter());
        }
    }
}

