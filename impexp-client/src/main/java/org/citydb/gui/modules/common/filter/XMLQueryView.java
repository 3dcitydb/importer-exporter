/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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

package org.citydb.gui.modules.common.filter;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.citydb.config.ConfigUtil;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
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
import org.citydb.config.project.query.filter.selection.comparison.GreaterThanOperator;
import org.citydb.config.project.query.filter.selection.comparison.LessThanOrEqualToOperator;
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.comparison.NullOperator;
import org.citydb.config.project.query.filter.selection.logical.AndOperator;
import org.citydb.config.project.query.filter.selection.logical.OrOperator;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.config.project.query.filter.selection.spatial.WithinOperator;
import org.citydb.config.project.query.filter.sorting.Sorting;
import org.citydb.config.project.query.filter.tiling.Tiling;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.project.query.simple.SimpleAttributeFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilterMode;
import org.citydb.config.util.QueryWrapper;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.factory.RSyntaxTextAreaHelper;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
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
import javax.xml.datatype.XMLGregorianCalendar;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class XMLQueryView extends FilterView {
    private final Logger log = Logger.getInstance();
    private final ViewController viewController;
    private final SchemaMapping schemaMapping;
    private final DatabaseConnectionPool connectionPool;
    private final Supplier<QueryConfig> queryConfigSupplier;
    private final Consumer<QueryConfig> queryConfigConsumer;

    private JPanel component;
    private RSyntaxTextArea xmlText;

    private JButton newButton;
    private JButton duplicateButton;
    private JButton validateButton;

    public XMLQueryView(ViewController viewController,
                        Supplier<SimpleQuery> simpleQuerySupplier,
                        Supplier<QueryConfig> queryConfigSupplier,
                        Consumer<QueryConfig> queryConfigConsumer) {
        super(simpleQuerySupplier);
        this.viewController = viewController;
        this.queryConfigSupplier = queryConfigSupplier;
        this.queryConfigConsumer = queryConfigConsumer;

        schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
        connectionPool = DatabaseConnectionPool.getInstance();

        init();
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

        JToolBar toolBar = new JToolBar();
        toolBar.add(newButton);
        toolBar.add(duplicateButton);
        toolBar.addSeparator();
        toolBar.add(validateButton);
        toolBar.setFloatable(false);
        toolBar.setOrientation(JToolBar.VERTICAL);

        component.add(scrollPane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
        component.add(toolBar, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0, 5, 0, 0));

        newButton.addActionListener(e -> SwingUtilities.invokeLater(this::setEmptyQuery));
        duplicateButton.addActionListener(e -> SwingUtilities.invokeLater(this::setSimpleSettings));
        validateButton.addActionListener(e -> SwingUtilities.invokeLater(this::validate));

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

            if (featureVersionFilter.getMode() == SimpleFeatureVersionFilterMode.LATEST)
                predicates.add(new NullOperator("core:terminationDate"));
            else if (featureVersionFilter.isSetStartDate()
                    && (featureVersionFilter.getMode() == SimpleFeatureVersionFilterMode.AT
                    || featureVersionFilter.isSetEndDate())) {
                XMLGregorianCalendar creationDate = featureVersionFilter.getMode() == SimpleFeatureVersionFilterMode.AT ?
                        featureVersionFilter.getStartDate() :
                        featureVersionFilter.getEndDate();

                predicates.add(new AndOperator(
                        new LessThanOrEqualToOperator("core:creationDate", creationDate.toXMLFormat()),
                        new OrOperator(
                                new GreaterThanOperator("core:terminationDate", featureVersionFilter.getStartDate().toString()),
                                new NullOperator("core:terminationDate")
                        )
                ));
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
                if (!isDefaultDatabaseSrs(envelope.getSrs()))
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

        if (errors[0] > 0)
            log.warn(errors[0] + " error(s) reported while validating the XML query.");
        else
            log.info("The XML query is valid.");
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
        QueryConfig query = queryConfigSupplier.get();
        xmlText.setText(marshalQuery(query, ObjectRegistry.getInstance().getConfig().getNamespaceFilter()));
    }

    @Override
    public void setSettings() {
        QueryConfig query = unmarshalQuery();
        queryConfigConsumer.accept(query);
    }

    private boolean isDefaultDatabaseSrs(DatabaseSrs srs) {
        return srs.getSrid() == 0 || (connectionPool.isConnected()
                && srs.getSrid() == connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid());
    }
}

