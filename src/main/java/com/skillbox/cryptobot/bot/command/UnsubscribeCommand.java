package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.User;
import com.skillbox.cryptobot.model.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

/**
 * Обработка команды отмены подписки на курс валюты
 */
@Service
@Slf4j
@AllArgsConstructor
public class UnsubscribeCommand implements IBotCommand {
    private UserRepository userRepository;

    @Override
    public String getCommandIdentifier() {
        return "unsubscribe";
    }

    @Override
    public String getDescription() {
        return "Отменяет подписку пользователя";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        Long chatId = message.getChatId();
        answer.setChatId(chatId);
        userRepository.findById(chatId)
                .ifPresent(user -> {
                    if (user.getPrice() != null) {
                        user.setPrice(null);
                        userRepository.save(user);
                        answer.setText("Подписка отменена");
                    } else {
                        answer.setText("Активные подписки отсутствуют");
                    }
                    try {
                        absSender.execute(answer);
                    } catch (TelegramApiException e) {
                        log.error("Ошибка " + e.getMessage());
                    }
                });
    }
}