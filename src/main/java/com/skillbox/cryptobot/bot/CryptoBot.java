package com.skillbox.cryptobot.bot;

import com.skillbox.cryptobot.model.User;
import com.skillbox.cryptobot.model.UserRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@EnableScheduling
public class CryptoBot extends TelegramLongPollingCommandBot {
    @Autowired
    private CryptoCurrencyService service;
    Map<Long, String> awaitingMap = new HashMap<>();
    @Autowired
    private UserRepository userRepository;
    private final String botUsername;
    @Autowired
    private List<IBotCommand> commandList;

    public CryptoBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList
    ) {
        super(botToken);
        this.botUsername = botUsername;
        commandList.forEach(this::register);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        boolean commandNotFound = true;
        for (IBotCommand command : commandList) {
            if (messageText.contains(command.getCommandIdentifier())) {
                commandNotFound = false;
                break;
            }
        }
        if (commandNotFound && !awaitingNumber(chatId)) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("""
                    Данная команда не поддерживается.
                    Для вывода всех команд нажмите /start
                    """);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Ошибка " + e.getMessage());
            }
        }
    }


    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
        for (Update update : updates) {
            Message message = update.getMessage();
            String[] msgText = message.getText().split(" ");
            if (message != null && msgText != null) {
                Long chatId = message.getChatId();
                if (message.getText().equals("/subscribe")) {
                    // Отправляем пользователю запрос на ввод числа
                    try {
                        execute(new SendMessage(chatId.toString(), "Введите желаемую стоимость биткоина:"));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    // добавление ожидания ввода числа
                    awaitingMap.put(message.getChatId(), "AWAITING_NUMBER");
                } else {
                    // проверка ожидания
                    if (awaitingNumber(chatId)) {
                        try {
                            String text = msgText[0].replace(',', '.');
                            double desiredPrice = Double.parseDouble(text);
                            // Сохраняем подписку в базу данных для данного пользователя
                            User user = findOrCreateUserById(chatId);
                            user.setPrice(desiredPrice);
                            userRepository.save(user);
                            // Удаляем состояние ожидания ввода числа для данного пользователя
                            awaitingMap.remove(chatId);
                            // Отправляем сообщение об успешном сохранении числа
                            Double bitcoinPrice = null;
                            try {
                                bitcoinPrice = service.getBitcoinPrice();
                            } catch (IOException e) {
                                log.error("Ошибка в методе service.getBitcoinPrice(): " + e.getMessage());
                            }
                            String subscribeText = "Новая подписка создана на стоимость " + TextUtil.toString(desiredPrice) + " USD \n" +
                                    "Текущий курс биткоина: " + TextUtil.toString(bitcoinPrice) + " USD";
                            execute(new SendMessage(chatId.toString(), subscribeText));

                        } catch (NumberFormatException e) {
                            // Отправляем сообщение об ошибке в случае неверного формата числа
                            try {
                                execute(new SendMessage(chatId.toString(), "Введите корректную стоимость."));
                            } catch (TelegramApiException ex) {
                                throw new RuntimeException(ex);
                            }
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    private boolean awaitingNumber(long chatId) {
        return awaitingMap.getOrDefault(chatId, " ").equals("AWAITING_NUMBER");
    }

    public User findOrCreateUserById(long chatId) {
        return userRepository.findById(chatId).orElseGet(() -> {
            User user = new User();
            user.setTelegramId(chatId);
            user.setUserId(UUID.randomUUID());
            return user;
        });
    }
}
