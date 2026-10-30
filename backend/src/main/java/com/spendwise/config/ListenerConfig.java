package com.spendwise.config;

import com.spendwise.service.BudgetService;
import com.spendwise.service.NotificationService;
import org.springframework.context.annotation.Configuration;

/** Mendaftarkan {@link NotificationService} sebagai listener {@link BudgetService} (Observer), sekali saat start. */
@Configuration
public class ListenerConfig {

    public ListenerConfig(BudgetService budgetService, NotificationService notificationService) {
        budgetService.addListener(notificationService);
    }
}
