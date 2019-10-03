package org.citydb.gui.components.common;

import org.citydb.config.i18n.Language;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.event.global.SwitchLocaleEvent;
import org.citydb.registry.ObjectRegistry;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.plaf.basic.CalendarHeaderHandler;
import org.jdesktop.swingx.plaf.basic.SpinningCalendarHeaderHandler;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePicker extends JXDatePicker implements EventHandler {
    private JPanel linkPanel;
    private JLabel todayLink;

    static {
        UIManager.put("JXDatePicker.border", UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        UIManager.put("JXMonthView.monthDownFileName", new ImageIcon(DatePicker.class.getResource("/org/citydb/gui/images/common/left_arrow.png")));
        UIManager.put("JXMonthView.monthUpFileName", new ImageIcon(DatePicker.class.getResource("/org/citydb/gui/images/common/right_arrow.png")));
        UIManager.put(CalendarHeaderHandler.uiControllerID, SpinningCalendarHeaderHandler.class.getName());
        UIManager.put(SpinningCalendarHeaderHandler.ARROWS_SURROUND_MONTH, Boolean.TRUE);
    }

    public DatePicker() {
        ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
        setFormats("yyyy-MM-dd", "dd.MM.yyyy");
        getMonthView().setZoomable(true);
        getEditor().setPromptForeground(Color.LIGHT_GRAY);
        getEditor().setFocusBehavior(PromptSupport.FocusBehavior.SHOW_PROMPT);
        getEditor().setPrompt("YYYY-MM-DD");
    }

    @Override
    public JPanel getLinkPanel() {
        return linkPanel != null ? linkPanel : createLinkPanel();
    }

    private JPanel createLinkPanel() {
        linkPanel = new JPanel();
        linkPanel.setLayout(new FlowLayout());

        todayLink = new JLabel();
        todayLink.setForeground(new Color(16, 66, 104));
        todayLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkPanel.add(todayLink);

        todayLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Action delegate = getActionMap().get(e.getClickCount() != 2 ? JXDatePicker.HOME_NAVIGATE_KEY : JXDatePicker.HOME_COMMIT_KEY);
                    if (delegate != null && delegate.isEnabled())
                        delegate.actionPerformed(null);
                }
            }
        });

        return linkPanel;
    }

    @Override
    public void handleEvent(Event event) {
        SwitchLocaleEvent localeEvent = (SwitchLocaleEvent) event;
        setLocale(localeEvent.getLocale());

        String[] daysOfTheWeek = new String[JXMonthView.DAYS_IN_WEEK];
        String[] dateFormatSymbols = DateFormatSymbols.getInstance(localeEvent.getLocale()).getShortWeekdays();
        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            daysOfTheWeek[i - 1] = dateFormatSymbols[i].replaceAll("\\.$", "");
            while (daysOfTheWeek[i - 1].length() < 3)
                daysOfTheWeek[i - 1] = " " + daysOfTheWeek[i - 1];
        }

        getMonthView().setDaysOfTheWeek(daysOfTheWeek);
        getMonthView().setFirstDayOfWeek(Calendar.MONDAY);
        setDate(getDate());

        MessageFormat format = new MessageFormat(Language.I18N.getString("common.label.datePicker.today"));
        String text = format.format(new Object[] {new SimpleDateFormat("dd. MMMM yyyy", localeEvent.getLocale()).format(getLinkDay())});
        todayLink.setText("<html><u>" + text + "</u></html>");
    }
}
