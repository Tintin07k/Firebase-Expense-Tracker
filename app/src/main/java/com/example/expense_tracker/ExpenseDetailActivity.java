package com.example.expense_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseDetailActivity extends AppCompatActivity {

    private TextView resultTextView;
    private Button shareButton;
    private Expense expense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        resultTextView = findViewById(R.id.resultTextView);
        shareButton = findViewById(R.id.shareButton);

        // Get the expense object from the intent
        expense = (Expense) getIntent().getSerializableExtra("expense");

        if (expense != null) {
            displaySplitResults();
        }

        shareButton.setOnClickListener(view -> shareResults());
    }

    private void displaySplitResults() {
        // Calculate the split and display the results
        Map<String, Double> balances = calculateBalances();
        List<String> transactions = calculateTransactions(balances);

        StringBuilder resultText = new StringBuilder();
        resultText.append("Expense Split Results:\n");
        resultText.append("Expense Title: ").append(expense.getTitle()).append("\n\n");

        for (String transaction : transactions) {
            resultText.append(transaction).append("\n");
        }

        resultTextView.setText(resultText.toString());
    }

    private Map<String, Double> calculateBalances() {
        Map<String, Double> balances = new HashMap<>();
        double totalAmount = expense.getTotalAmount();
        int participantCount = expense.getParticipants().size();
        double perPersonShare = totalAmount / participantCount;

        // Calculate each participant's balance
        for (String participant : expense.getParticipants()) {
            double contribution = expense.getContributions().getOrDefault(participant, 0.0);
            balances.put(participant, contribution - perPersonShare);
        }
        return balances;
    }

    private List<String> calculateTransactions(Map<String, Double> balances) {
        List<String> transactions = new ArrayList<>();
        List<Map.Entry<String, Double>> creditors = new ArrayList<>();
        List<Map.Entry<String, Double>> debtors = new ArrayList<>();

        // Split participants into creditors and debtors based on balances
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            if (entry.getValue() > 0) {
                creditors.add(entry);
            } else if (entry.getValue() < 0) {
                debtors.add(entry);
            }
        }

        // Simplify transactions to settle debts
        int creditorIndex = 0, debtorIndex = 0;
        while (creditorIndex < creditors.size() && debtorIndex < debtors.size()) {
            Map.Entry<String, Double> creditor = creditors.get(creditorIndex);
            Map.Entry<String, Double> debtor = debtors.get(debtorIndex);

            double creditAmount = creditor.getValue();
            double debitAmount = Math.abs(debtor.getValue());
            double settlementAmount = Math.min(creditAmount, debitAmount);

            transactions.add(debtor.getKey() + " owes " + creditor.getKey() + " $" + String.format("%.2f", settlementAmount));

            // Update balances after settlement
            creditors.set(creditorIndex, Map.entry(creditor.getKey(), creditAmount - settlementAmount));
            debtors.set(debtorIndex, Map.entry(debtor.getKey(), -(debitAmount - settlementAmount)));

            if (creditors.get(creditorIndex).getValue() == 0) {
                creditorIndex++;
            }
            if (debtors.get(debtorIndex).getValue() == 0) {
                debtorIndex++;
            }
        }
        return transactions;
    }

    private void shareResults() {
        StringBuilder shareText = new StringBuilder();
        shareText.append("Expense Split Summary\n\n")
                .append("Expense Title: ").append(expense.getTitle()).append("\n\n");

        for (String transaction : calculateTransactions(calculateBalances())) {
            shareText.append(transaction).append("\n");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Expense Summary"));
    }
}
