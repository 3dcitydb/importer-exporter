package org.citydb.gui.components.srs;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.event.Event;
import org.citydb.core.event.EventHandler;
import org.citydb.core.event.global.EventType;
import org.citydb.core.registry.ObjectRegistry;

import javax.swing.*;
import java.awt.*;

public class SrsComboBox extends JComboBox<DatabaseSrs> implements EventHandler {
    private final DatabaseSrs databaseSrs;
    private final DatabaseConnectionPool connectionPool;
    private final Config config;

    private boolean showOnlySupported;
    private boolean showOnlySameDimension;

    SrsComboBox(boolean onlyShowSupported, Config config) {
        this.showOnlySupported = onlyShowSupported;
        this.config = config;

        databaseSrs = DatabaseSrs.createDefaultSrs();
        databaseSrs.setSupported(true);
        connectionPool = DatabaseConnectionPool.getInstance();

        setRenderer(new SrsComboBoxRenderer());
        ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
    }

    @Override
    public void setSelectedItem(Object anObject) {
        if (anObject instanceof DatabaseSrs) {
            DatabaseSrs srs = (DatabaseSrs) anObject;

            if (srs == databaseSrs || config.getDatabaseConfig().getReferenceSystems().contains(srs)) {
                super.setSelectedItem(srs);
            } else {
                DatabaseSrs candidate = null;
                for (int i = 0; i < getItemCount(); i++) {
                    DatabaseSrs item = getItemAt(i);
                    if (item != null) {
                        if (item.getId() != null && item.getId().equals(srs.getId())) {
                            super.setSelectedItem(item);
                            candidate = null;
                            break;
                        } else if (candidate == null && item.getSrid() == srs.getSrid()) {
                            candidate = srs;
                        }
                    }
                }

                if (candidate != null) {
                    super.setSelectedItem(candidate);
                }
            }
        }
    }

    public void setShowOnlySupported(boolean show) {
        showOnlySupported = show;
    }

    public void setShowOnlySameDimension(boolean show) {
        showOnlySameDimension = show;
    }

    public void setDBReferenceSystem() {
        setSelectedItem(databaseSrs);
    }

    public boolean isDBReferenceSystemSelected() {
        return getSelectedItem() == databaseSrs;
    }

    void init() {
        addItem(databaseSrs);

        // user-defined reference systems
        for (DatabaseSrs srs : config.getDatabaseConfig().getReferenceSystems()) {
            if (showOnlySupported && !srs.isSupported()) {
                continue;
            }

            if (showOnlySameDimension && srs.is3D() != databaseSrs.is3D()) {
                continue;
            }

            addItem(srs);
        }
    }

    void reset() {
        DatabaseSrs srs = connectionPool.isConnected() ?
                connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem() :
                DatabaseSrs.createDefaultSrs();

        databaseSrs.setSrid(srs.getSrid());
        databaseSrs.setGMLSrsName(srs.getGMLSrsName());
        databaseSrs.setDatabaseSrsName(srs.getDatabaseSrsName());
        databaseSrs.setType(srs.getType());

        removeAllItems();
        init();
    }

    void updateContent() {
        DatabaseSrs selectedItem = getSelectedItem();
        if (selectedItem == null) {
            selectedItem = databaseSrs;
        }

        reset();
        setSelectedItem(selectedItem);
    }

    private void doTranslation() {
        databaseSrs.setDescription(Language.I18N.getString("common.label.boundingBox.crs.sameAsInDB"));
        DatabaseSrs selectedItem = getSelectedItem();
        if (selectedItem == null) {
            selectedItem = databaseSrs;
        }

        removeItemAt(0);
        insertItemAt(databaseSrs, 0);

        if (selectedItem == databaseSrs) {
            setSelectedItem(selectedItem);
        }

        repaint();
        fireActionEvent();
    }

    @Override
    public DatabaseSrs getSelectedItem() {
        Object object = super.getSelectedItem();
        return (object instanceof DatabaseSrs) ? (DatabaseSrs) object : null;
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        doTranslation();
    }

    private class SrsComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                SrsComboBox.this.setToolTipText(value.toString());
            }

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
