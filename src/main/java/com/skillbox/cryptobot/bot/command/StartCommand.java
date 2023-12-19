package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.User;
import com.skillbox.cryptobot.model.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Обработка команды начала работы с ботом
 */
@Service
@AllArgsConstructor
@Slf4j
public class StartCommand implements IBotCommand {
    private UserRepository userRepository;

    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Запускает бота";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        long chatId = message.getChatId();
        answer.setChatId(chatId);
        answer.setReplyMarkup(virtualTgKeyboard());
        answer.setText("""
                Привет! Данный бот помогает отслеживать стоимость биткоина.
                Поддерживаемые команды:
                 /get_price - получить стоимость биткоина \n
                 /get_subscription - выводит текущую подписку \n
                 /subscribe <стоимость биктоина> - подписывает пользователя на стоимость биткоина \n
                 /unsubscribe - отменяет подписку пользователя
                """);
        registerUser(chatId);
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
    }

    private void registerUser(long chatId) {
        userRepository.findById(chatId).ifPresentOrElse(
                user -> log.info("Пользователь c id = " + user.getTelegramId() + " уже существует"),
                () -> {
                    User user = new User();
                    user.setTelegramId(chatId);
                    user.setUserId(UUID.randomUUID());
                    userRepository.save(user);
                    log.info("Пользователь c id =" + user.getTelegramId() + " сохранен");
                });

    }

    private ReplyKeyboardMarkup virtualTgKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("/get_price");
        row.add("/get_subscription");

        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("/subscribe");
        row.add("/unsubscribe");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }
}