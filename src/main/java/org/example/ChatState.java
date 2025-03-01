package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatState {
    public State state;
    public String data = null;
    public Map<String, List<Integer>> expenses = new HashMap<>();

    public ChatState(State initialState) {
        this.state = initialState;
    }

    public String getFormattedCategories() {
        Set<String> categories = expenses.keySet();
        if (categories.isEmpty()) return "Категории отсутствуют";
        return String.join("\n", categories);
    }

    public String getFormattedExpenses() {
        if (expenses.isEmpty()) return "Категории отсутствуют";

        StringBuilder result = new StringBuilder();
        expenses.forEach((category, expensesList) -> result.append(category).append(": ").append(expensesList.toString()).append("\n"));
        return result.toString();
    }
}
