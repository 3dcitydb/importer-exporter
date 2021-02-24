package org.citydb.gui.modules.common.filter;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.ade.ADEExtension;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.project.query.filter.version.CityGMLVersionType;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.event.global.PropertyChangeEvent;
import org.citydb.gui.components.checkboxtree.DefaultCheckboxTreeCellRenderer;
import org.citydb.gui.components.feature.FeatureTypeTree;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FeatureTypeFilterView extends FilterView<FeatureTypeFilter> implements EventHandler {
    private JPanel component;
    private FeatureTypeTree featureTypeTree;
    private boolean enabled = true;
    private Consumer<FeatureTypeFilter> featureTypeFilterConsumer;

    public FeatureTypeFilterView(CityGMLVersion version, Predicate<ADEExtension> adeFilter) {
        init(version, adeFilter);
    }

    public FeatureTypeFilterView(CityGMLVersionType version, Predicate<ADEExtension> adeFilter) {
        this(Util.toCityGMLVersion(version), adeFilter);
    }

    public FeatureTypeFilterView(Predicate<ADEExtension> adeFilter) {
        this(CityGMLVersion.v2_0_0, adeFilter);
    }

    public FeatureTypeFilterView(CityGMLVersion version) {
        this(version, e -> true);
    }

    public FeatureTypeFilterView(CityGMLVersionType version) {
        this(Util.toCityGMLVersion(version));
    }

    public FeatureTypeFilterView() {
        this(CityGMLVersion.v2_0_0);
    }

    public FeatureTypeFilterView adaptToCityGMLVersionChange(Consumer<FeatureTypeFilter> featureTypeFilterConsumer) {
        this.featureTypeFilterConsumer = featureTypeFilterConsumer;
        ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.PROPERTY_CHANGE_EVENT, this);
        return this;
    }

    public FeatureTypeTree getFeatureTypeTree() {
        return featureTypeTree;
    }

    private void init(CityGMLVersion version, Predicate<ADEExtension> adeFilter) {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        featureTypeTree = new FeatureTypeTree(version, adeFilter);
        featureTypeTree.setRowHeight((int)(new JCheckBox().getPreferredSize().getHeight()) - 1);

        // get rid of standard icons
        DefaultCheckboxTreeCellRenderer renderer = (DefaultCheckboxTreeCellRenderer) featureTypeTree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);

        component.add(featureTypeTree, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));

        PopupMenuDecorator.getInstance().decorate(featureTypeTree);

        UIManager.addPropertyChangeListener(e -> {
            if ("lookAndFeel".equals(e.getPropertyName())) {
                SwingUtilities.invokeLater(this::updateComponentUI);
            }
        });

        updateComponentUI();
    }

    private void updateComponentUI() {
        component.setBorder(UIManager.getBorder("ScrollPane.border"));
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("filter.border.featureClass");
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
        return new FlatSVGIcon("org/citydb/gui/filter/featureType.svg");
    }

    @Override
    public void doTranslation() {
        // nothing to do
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            featureTypeTree.expandRow(0);
        } else {
            featureTypeTree.collapseRow(0);
            featureTypeTree.setSelectionPath(null);
        }

        featureTypeTree.setPathsEnabled(enabled);
        featureTypeTree.setEnabled(enabled);
    }

    @Override
    public void loadSettings(FeatureTypeFilter featureTypeFilter) {
        featureTypeTree.getCheckingModel().clearChecking();
        featureTypeTree.setSelected(featureTypeFilter.getTypeNames());
    }

    @Override
    public FeatureTypeFilter toSettings() {
        FeatureTypeFilter featureTypeFilter = new FeatureTypeFilter();
        featureTypeFilter.setTypeNames(featureTypeTree.getSelectedTypeNames());
        return featureTypeFilter;
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        PropertyChangeEvent e = (PropertyChangeEvent)event;
        if (e.getPropertyName().equals("citygml.version")) {
            featureTypeTree.updateCityGMLVersion((CityGMLVersion) e.getNewValue(), enabled);
            featureTypeFilterConsumer.accept(toSettings());
        }
    }
}
