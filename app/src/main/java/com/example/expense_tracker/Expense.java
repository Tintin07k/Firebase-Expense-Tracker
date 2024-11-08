package com.example.expense_tracker;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Expense implements Serializable {
    private String title;
    private double totalAmount;
    private List<String> participants;
    private Map<String, Double> contributions;

    public Expense(String title, double totalAmount, List<String> participants, Map<String, Double> contributions) {
        this.title = title;
        this.totalAmount = totalAmount;
        this.participants = participants;
        this.contributions = contributions;
    }

    public String getTitle() {
        return title;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public Map<String, Double> getContributions() {
        return contributions;
    }
}
