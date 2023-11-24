package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelegramBotRunner {
    private static final ResourceBundle resource = ResourceBundle.getBundle("application");
    private static final ThreadLocal<TelegramBotUpdateHandler> updateHandler = ThreadLocal.withInitial(TelegramBotUpdateHandler::new);
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static void main(String[] args) {
        String token = resource.getString("bot.token");
        TelegramBot bot = new TelegramBot(token);
        bot.setUpdatesListener(list -> {
            for (Update update : list) {
                CompletableFuture.runAsync(() -> updateHandler.get().handle(update), executor);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        },
                Throwable::printStackTrace
        );
    }
}
enum State {
    ROW_COUNT,
    FILE_NAME,
    FIELDS,
    FILETYPE
}
