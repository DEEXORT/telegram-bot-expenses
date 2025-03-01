package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class TelegramBot extends TelegramLongPollingBot {
    private static final String ADD_EXPENSE_BTN = "Добавить расход";
    private static final String SHOW_CATEGORIES_BTN = "Показать категории";
    private static final String SHOW_EXPENSES_BTN = "Показать расходы";

//    private static final String IDLE_STATE = "IDLE";
//    private static final String AWAITS_CATEGORY_STATE = "AWAITS_CATEGORY";
//    private static final String AWAITS_EXPENSE_STATE = "AWAITS_EXPENSE";

    private static final Map<Long, ChatState> CHATS = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "TelegramBot.Expenses";
    }

    @Override
    public String getBotToken() {
        return System.getenv("TG_BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            System.out.println("Unsupported update");
            return;
        }


        Message message = update.getMessage();
        Long chatId = message.getChatId();
        CHATS.putIfAbsent(chatId, new ChatState(State.IDLE_STATE));

        // Лог
        User from = message.getFrom();
        String text = message.getText();
        String logMessage = from.getUserName() + ": " + text;
        System.out.println(logMessage);

        ChatState currentChat = CHATS.get(chatId);
        switch (currentChat.state) {
            case State.IDLE_STATE -> handleIdle(message, currentChat);
            case AWAITS_CATEGORY_STATE -> handleAwaitCategory(message, currentChat);
            case AWAITS_EXPENSE_STATE -> handleAwaitsExpense(message, currentChat);
        }
    }

    private void handleIdle(Message incomingMessage, ChatState currentChat) {
        String incomingText = incomingMessage.getText();
        Long chatId = incomingMessage.getChatId();

        final List<String> defaultButtons = List.of(
                ADD_EXPENSE_BTN,
                SHOW_EXPENSES_BTN,
                SHOW_CATEGORIES_BTN
        );

        switch (incomingText) {
            case SHOW_CATEGORIES_BTN -> changeState(State.IDLE_STATE, chatId, currentChat, currentChat.getFormattedCategories(), defaultButtons);
            case SHOW_EXPENSES_BTN -> changeState(State.IDLE_STATE, chatId, currentChat, currentChat.getFormattedExpenses(), defaultButtons);
            case ADD_EXPENSE_BTN -> changeState(State.AWAITS_CATEGORY_STATE, chatId, currentChat, "Укажите категорию", null);
            default -> changeState(State.IDLE_STATE, chatId, currentChat,"Я не знаю такой команды", defaultButtons);
        }
    }

    private void handleAwaitCategory(Message incomingMessage, ChatState currentChat) {
        String incomingText = incomingMessage.getText().substring(0, 1).toUpperCase() + incomingMessage.getText().substring(1);
        Long chatId = incomingMessage.getChatId();

        currentChat.expenses.putIfAbsent(incomingText, new ArrayList<>());
        currentChat.data = incomingText;
        changeState(State.AWAITS_EXPENSE_STATE, chatId, currentChat, "Введите сумму", null);
    }

    private void handleAwaitsExpense(Message incomingMessage, ChatState currentChat) {
        Long chatId = incomingMessage.getChatId();

        if (currentChat.data == null) {
            changeState(State.IDLE_STATE, chatId, currentChat, "Что-то пошло не так. Попробуйте сначала", List.of(
                    ADD_EXPENSE_BTN,
                    SHOW_EXPENSES_BTN,
                    SHOW_CATEGORIES_BTN
            ));
        }
        String incomingText = incomingMessage.getText();
        Integer expense = Integer.parseInt(incomingText);
        currentChat.expenses.get(currentChat.data).add(expense);
        changeState(State.IDLE_STATE, chatId, currentChat, "Расход успешно добавлен", List.of(
                ADD_EXPENSE_BTN,
                SHOW_EXPENSES_BTN,
                SHOW_CATEGORIES_BTN
        ));
    }

    private void changeState(State newState,
                             Long chatId,
                             ChatState currentChat,
                             String messageText,
                             List<String> buttonNames) {
        System.out.println(currentChat.state + " -> " + newState);
        currentChat.state = newState;

        ReplyKeyboard keyboard = buildKeyboard(buttonNames);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageText);
        sendMessage.setReplyMarkup(keyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("!!!ERROR!!!");
            System.out.println(e);
        }
    }

    private ReplyKeyboard buildKeyboard(List<String> buttonNames) {
        if (buttonNames == null || buttonNames.isEmpty()) return new ReplyKeyboardRemove(true);

        List<KeyboardRow> rows = new ArrayList<>();
        for (String buttonName : buttonNames) {
            KeyboardRow row = new KeyboardRow();
            row.add(buttonName);
            rows.add(row);
        }
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(rows);
        return keyboard;
    }
}
