package com.yxtech.smartlife.service.impl;

import com.yxtech.smartlife.service.RentalTradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RentalTradeScheduler {

    private final RentalTradeService rentalTradeService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendExpirationReminders() {
        rentalTradeService.sendExpirationReminders();
    }

    @Scheduled(cron = "0 10 0 * * *")
    public void completeExpiredOrders() {
        rentalTradeService.completeExpiredOrders();
    }
}
