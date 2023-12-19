package com.skillbox.cryptobot.bot;

import com.skillbox.cryptobot.model.User;
import com.skillbox.cryptobot.model.UserRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;


import static java.lang.System.currentTimeMillis;

/**
 * Отправка уведомлений о покупке
 */
@Service
@Slf4j
@EnableScheduling
public class SendNotification {
    @Autowired
    private AbsSender absSender;
    @Value("${cron.timeBetweenNotification}")
    private int timeBetweenNotification;
    @Autowired
    private CryptoCurrencyService service;
    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "${cron.scheduler}")
    private void sendNotification() {
        double actualPrice;
        try {
            actualPrice = service.getBitcoinPrice();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        userRepository.findAll().forEach(user -> {
            Double subscriptionPrice = user.getPrice();
            if (subscriptionPrice != null && subscriptionPrice > actualPrice) {
                double diff = diffTimeBetweenNotifications(user);
                if (diff >= timeBetweenNotification) {
                    sendMessage(user.getTelegramId(), "Пора покупать, стоимость биткоина "
                            + TextUtil.toString(actualPrice) + " USD");
                    setTimeLastNotification(user);
                }
            }
        });
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        messageExecute(message);
    }

    private void messageExecute(SendMessage message) {
        try {
            absSender.execute(message);
        } catch (TelegramApiException ex) {
            log.error("Ошибка: " + ex.getMessage());
        }
    }

    private double diffTimeBetweenNotifications(User user) {
        Timestamp last = user.getLastPriceNotification();
        if (last == null) {
            last = new Timestamp(currentTimeMillis());
            setTimeLastNotification(user);
        }
        Timestamp current = new Timestamp(System.currentTimeMillis());
        long diffInSeconds = Duration.between(last.toInstant(), current.toInstant()).getSeconds();
        return Math.round(diffInSeconds / 60.0);
    }

    private void setTimeLastNotification(User user) {
        user.setLastPriceNotification(new Timestamp(currentTimeMillis()));
        userRepository.save(user);
    }
}
