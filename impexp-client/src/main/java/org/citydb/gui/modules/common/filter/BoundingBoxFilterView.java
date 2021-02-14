package org.citydb.gui.modules.common.filter;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.common.BoundingBoxProvider;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;

import javax.swing.*;
import java.util.function.Supplier;

public class BoundingBoxFilterView extends FilterView {
    private final ViewController viewController;
    private final Supplier<BoundingBoxProvider> configSupplier;

    private BoundingBoxPanel component;

    public BoundingBoxFilterView(ViewController viewController, Supplier<BoundingBoxProvider> configSupplier) {
        this.viewController = viewController;
        this.configSupplier = configSupplier;
        init();
    }

    private void init() {
        component = viewController.getComponentFactory().createBoundingBoxPanel();
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("filter.border.boundingBox");
    }

    @Override
    public BoundingBoxPanel getViewComponent() {
        return component;
    }

    @Override
    public String getToolTip() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return new FlatSVGIcon("org/citydb/gui/filter/bbox.svg");
    }

    @Override
    public void doTranslation() {
        // nothing to do
    }

    @Override
    public void setEnabled(boolean enabled) {
        component.setEnabled(enabled);
    }

    @Override
    public void loadSettings() {
        BoundingBoxProvider bboxFilter = configSupplier.get();
        BoundingBox bbox = bboxFilter.getExtent();
        if (bbox != null) {
            component.setBoundingBox(bboxFilter.getExtent());
        }
    }

    @Override
    public void setSettings() {
        BoundingBoxProvider bboxFilter = configSupplier.get();
        bboxFilter.setExtent(component.getBoundingBox());
    }

}
