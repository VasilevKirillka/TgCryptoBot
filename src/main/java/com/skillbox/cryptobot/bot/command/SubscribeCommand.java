package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.User;
import com.skillbox.cryptobot.model.UserRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Обработка команды подписки на курс валюты
 */
@Service
@Slf4j
@AllArgsConstructor
public class SubscribeCommand implements IBotCommand {

    Map<Long, String> userStates = new HashMap<>();
    private UserRepository userRepository;
    private final CryptoCurrencyService service;

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }


    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        arguments = message.getText().split(" ");
        SendMessage answer = new SendMessage();
        Long chatId = message.getChatId();
        answer.setChatId(chatId);
        if (arguments.length < 2 || !"/subscribe".equals(arguments[0])) {
            return;
        }
        User user = findOrCreateUserById(chatId);
        Double desiredPrice;
        try {
            desiredPrice = Double.parseDouble(arguments[1].replace(',', '.'));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            messageExecute(absSender, new SendMessage(String.valueOf(chatId), "Введите корректную стоимость."));
            return;
        }
        user.setPrice(desiredPrice);
        userRepository.save(user);
        Double bitcoinPrice = null;
        try {
            bitcoinPrice = service.getBitcoinPrice();
        } catch (IOException e) {
            log.error("Ошибка в методе service.getBitcoinPrice(): " + e.getMessage());
        }
        String subscribeText = "Новая подписка создана на стоимость " + TextUtil.toString(desiredPrice) + " USD \n" +
                "Текущий курс биткоина: " + TextUtil.toString(bitcoinPrice) + " USD";
        SendMessage subscribeMsg = new SendMessage(String.valueOf(message.getChatId()), subscribeText);
        messageExecute(absSender, subscribeMsg);
    }

    public User findOrCreateUserById(long chatId) {
        return userRepository.findById(chatId).orElseGet(() -> {
            User user = new User();
            user.setTelegramId(chatId);
            user.setUserId(UUID.randomUUID());
            return user;
        });
    }

    private void messageExecute(AbsSender absSender, SendMessage sendMessage) {
        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage(), e);
        }
    }
}

