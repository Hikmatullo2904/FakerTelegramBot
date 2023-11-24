package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.example.faker_p.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramBotUpdateHandler {
    private final TelegramBot telegramBot = new TelegramBot(ResourceBundle.getBundle("application").getString("bot.token"));
    private  static final ConcurrentHashMap<Long, State> userState = new ConcurrentHashMap<>();
    private  static final ConcurrentHashMap<Long, FakerApplicationGenerateRequest> generateRequest = new ConcurrentHashMap<>();

    public void handle(Update update) {
        Message message = update.message();
        CallbackQuery callbackQuery = update.callbackQuery();
        System.out.println(message);
        if (message != null) {
            String text = message.text();
            Chat chat = message.chat();
            Long id = chat.id();
            if(text.equals("/start")) {
                SendMessage sendMessage =
                        new SendMessage(id,
                                "welcome to fake data generator \nTo generate data, send /generate command");
                telegramBot.execute(sendMessage);
            } else if(text.equals("/generate")) {
                SendMessage sendMessage =
                        new SendMessage(id,
                                "Send File Name");
                telegramBot.execute(sendMessage);
                userState.put(id, State.FILE_NAME);
                generateRequest.put(id, new FakerApplicationGenerateRequest());
            }
            else if(State.FILE_NAME.equals(userState.get(id))) {
                System.out.println("file name is : " + text);
                SendMessage sendMessage =
                        new SendMessage(id,
                                "Send Row Count");
                telegramBot.execute(sendMessage);
                userState.put(id, State.ROW_COUNT);
                generateRequest.get(id).setFileName(text);
            } else if (State.ROW_COUNT.equals(userState.get(id))) {
                System.out.println("row count is : " + text);

                SendMessage sendMessage =
                        new SendMessage(id,
                                "Send Fields");
                //telegramBot.execute(sendMessage);
                sendMessage.replyMarkup(getInlineMarkupKeyboard());
                telegramBot.execute(sendMessage);
                userState.put(id, State.FIELDS);
                generateRequest.get(id).setCount(Integer.parseInt(text));

            }
            else {
                DeleteMessage deleteMessage = new DeleteMessage(id, message.messageId());
                telegramBot.execute(deleteMessage);
            }
        } else {
            FieldType[] fieldTypes = FieldType.values();
            String data = callbackQuery.data();
            //System.out.println(data);
            Chat chat = callbackQuery.message().chat();
            Long id = chat.id();
            System.out.println(id);

            if(data.equals("json") || data.equals("csv") || data.equals("sql")) {

                FileType validFileType = getValidFileType(data);
                generateRequest.get(id).setFileType(validFileType);

                var service = new FakerApplicationService();
                FakerApplicationGenerateRequest req = generateRequest.get(id);
                String path = service.processRequest(req);
                try {
                    SendDocument doc = new SendDocument(id, Files.readAllBytes(Path.of(path)));
                    telegramBot.execute(doc);
                } catch (IOException e) {
                    AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.id());
                    answerCallbackQuery.text("Error: please try again");
                    answerCallbackQuery.showAlert(true);
                    telegramBot.execute(answerCallbackQuery);
                }
            }

            else if(data.equals("g")) {
                InlineKeyboardMarkup markup = showFileTypes();
                SendMessage sendMessage = new SendMessage(id, "choose type");
                sendMessage.replyMarkup(markup);
                telegramBot.execute(sendMessage);
                userState.put(id, State.FILETYPE);
                System.out.println(id);

            } else {
                FieldType fieldType = fieldTypes[Integer.parseInt(data)];
                generateRequest.get(id).getFields().add(new Field(fieldType.name().toLowerCase(), fieldType, 0, 60));
            }

        }
    }
    private InlineKeyboardMarkup showFileTypes() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton json = new InlineKeyboardButton("JSON");
        json.callbackData("json");
        InlineKeyboardButton csv = new InlineKeyboardButton("CSV");
        csv.callbackData("csv");
        InlineKeyboardButton sql = new InlineKeyboardButton("SQL");
        sql.callbackData("sql");
        markup.addRow(json);
        markup.addRow(csv);
        markup.addRow(sql);
        return markup;
    }

    private Keyboard getInlineMarkupKeyboard() {
        FieldType[] fields = FieldType.values();
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[12][2];
        for (int i = 0; i < fields.length/2; i++) {
            InlineKeyboardButton btn1 = new InlineKeyboardButton(fields[i*2].name());
            InlineKeyboardButton btn2 = new InlineKeyboardButton(fields[i*2 + 1].name());
            btn1.callbackData("" + i*2);
            btn2.callbackData("" + (i*2 + 1));
            buttons[i][0] = btn1;
            buttons[i][1] = btn2;

        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(buttons);
        InlineKeyboardButton gen = new InlineKeyboardButton("generate");
        gen.callbackData("g");
        markup.addRow(gen);
        return markup;
    }
    private  FileType getValidFileType(String i) {

        String res = "";
        switch (i) {
            case "json" -> res = "JSON";
            case "csv" -> res = "CSV";
            case "sql" -> res = "SQL";
        }

        return FileType.findByName(res);
    }
}
