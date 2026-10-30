package com.spendwise.service;

import com.spendwise.dao.NotificationDAO;
import com.spendwise.model.Budget;
import com.spendwise.model.BudgetAlertListener;
import com.spendwise.model.Notification;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/** Observer: membuat dan menyimpan {@link Notification} saat ambang anggaran dilewati. */
@Service
public class NotificationService implements BudgetAlertListener {

    private static final Locale ID = Locale.forLanguageTag("id-ID");

    private final NotificationDAO notificationDAO;

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    @Override
    public void onBudgetWarning(Budget b, double percent) {
        String message = label(b) + " sudah terpakai " + Math.round(percent) + "%";
        notificationDAO.save(new Notification(b.getUserId(), message, Notification.WARNING));
    }

    @Override
    public void onBudgetExceeded(Budget b) {
        String message = label(b) + " terlampaui (" + rupiah(b.getSpentAmount())
                + " dari " + rupiah(b.getTargetAmount()) + ")";
        notificationDAO.save(new Notification(b.getUserId(), message, Notification.DANGER));
    }

    public List<Notification> getUnread(int userId) {
        return notificationDAO.findUnread(userId);
    }

    /** Notifikasi terbaru, dibaca maupun belum. */
    public List<Notification> getRecent(int userId, int limit) {
        return notificationDAO.findRecent(userId, limit);
    }

    public int countUnread(int userId) {
        return notificationDAO.countUnread(userId);
    }

    public void markAllRead(int userId) {
        notificationDAO.markAllRead(userId);
    }

    private String label(Budget b) {
        String month = b.getPeriod().getMonth().getDisplayName(TextStyle.FULL, ID);
        return b.getName() + " " + month + " " + b.getPeriod().getYear();
    }

    private String rupiah(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ID);
        symbols.setGroupingSeparator('.');
        return "Rp " + new DecimalFormat("#,##0", symbols).format(Math.round(amount));
    }
}
