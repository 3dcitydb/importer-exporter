package org.citydb.gui.modules.common.filter;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;

import javax.swing.*;

public class BoundingBoxFilterView extends FilterView<BoundingBox> {
    private final ViewController viewController;
    private BoundingBoxPanel component;

    public BoundingBoxFilterView(ViewController viewController) {
        this.viewController = viewController;
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
    public void loadSettings(BoundingBox bbox) {
        component.setBoundingBox(bbox);
    }

    @Override
    public BoundingBox toSettings() {
        return component.getBoundingBox();
    }

}
