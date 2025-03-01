package org.example;

import java.util.ArrayList;
import java.util.List;

public final class ButtonMessageConstants {
    private ButtonMessageConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    public static final String ADD_EXPENSE_BTN = "Добавить расход";
    public static final String SHOW_CATEGORIES_BTN = "Показать категории";
    public static final String SHOW_EXPENSES_BTN = "Показать расходы";

    public static final List<String> getButtonsForState(State state) {
        List<String> buttons = new ArrayList<>();
        if (state.equals(State.IDLE_STATE) || state.equals(State.AWAITS_EXPENSE_STATE)) {
            buttons.add(ADD_EXPENSE_BTN);
            buttons.add(SHOW_CATEGORIES_BTN);
            buttons.add(SHOW_EXPENSES_BTN);
        }
        return buttons;
    }
}
