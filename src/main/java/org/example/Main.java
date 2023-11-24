package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TelegramBot bot =
                new TelegramBot("6344060129:AAG5LYVwgwy015AeGCMgwjXThsnrLrYBXXM");
        //sendingMessageAndButtonAdding(bot);

        bot.setUpdatesListener(new UpdatesListener() {
            @Override
            public int process(List<Update> list) {
                for (Update update : list) {
                    Message message = update.message();
                    Chat chat = message.chat();
                    String text = message.text();
                    Long id = chat.id();
                    SendMessage sendMessage = new SendMessage(id, "Replied to " + text);
                    bot.execute(sendMessage);
                    System.out.println(text);
                }
                return CONFIRMED_UPDATES_ALL;
            }
        });





    }

    private static void sendingMessageAndButtonAdding(TelegramBot bot) {
        SendMessage sendMessage = new SendMessage("1141708553", "hello, I am happy to see you here");
        /*ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(new String[][] {
                {"contact"},
                {"location"},
                {"home"}
        });*/

        KeyboardButton contact = new KeyboardButton("contact");
        contact.requestContact(true);
        KeyboardButton location = new KeyboardButton("location");
        location.requestLocation(true);
        KeyboardButton[] button = new KeyboardButton[]{
                contact,
                location,
                new KeyboardButton("home")
        };
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(button);

        markup.resizeKeyboard(true);
        sendMessage.replyMarkup(markup);
        bot.execute(sendMessage);
    }
}