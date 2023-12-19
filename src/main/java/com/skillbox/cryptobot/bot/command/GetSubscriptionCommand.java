package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.User;
import com.skillbox.cryptobot.model.UserRepository;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class GetSubscriptionCommand implements IBotCommand {

    private UserRepository userRepository;

    @Override
    public String getCommandIdentifier() {
        return "get_subscription";
    }

    @Override
    public String getDescription() {
        return "Возвращает текущую подписку";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        var chatId = message.getChatId();
        answer.setChatId(chatId);

        Optional<User> userList = userRepository.findById(chatId);
        userList.ifPresent(user -> {
            if (user.getPrice() != null) {
                answer.setText("Вы подписаны на стоимость биткоина " + TextUtil.toString(user.getPrice()) + " USD");
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