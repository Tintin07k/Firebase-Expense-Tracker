package com.example.expense_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText expenseTitleInput, totalAmountInput;
    private LinearLayout participantsLayout;
    private Button addParticipantButton, saveExpenseButton;

    private List<EditText> participantNames;
    private List<EditText> participantContributions;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        firestore = FirebaseFirestore.getInstance();

        expenseTitleInput = findViewById(R.id.expenseTitleInput);
        totalAmountInput = findViewById(R.id.totalAmountInput);
        participantsLayout = findViewById(R.id.participantsLayout);
        addParticipantButton = findViewById(R.id.addParticipantButton);
        saveExpenseButton = findViewById(R.id.saveExpenseButton);

        participantNames = new ArrayList<>();
        participantContributions = new ArrayList<>();

        addParticipantButton.setOnClickListener(view -> addParticipantInputFields());
        saveExpenseButton.setOnClickListener(view -> saveExpenseData());
    }

    private void addParticipantInputFields() {
        EditText participantNameInput = new EditText(this);
        participantNameInput.setHint("Participant Name");
        participantsLayout.addView(participantNameInput);
        participantNames.add(participantNameInput);

        EditText participantContributionInput = new EditText(this);
        participantContributionInput.setHint("Contribution Amount");
        participantContributionInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        participantsLayout.addView(participantContributionInput);
        participantContributions.add(participantContributionInput);
    }

    private void saveExpenseData() {
        String title = expenseTitleInput.getText().toString();
        String totalAmountStr = totalAmountInput.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(totalAmountStr)) {
            Toast.makeText(this, "Please enter both title and total amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalAmount = Double.parseDouble(totalAmountStr);

        List<String> names = new ArrayList<>();
        Map<String, Double> contributions = new HashMap<>();
        double sumContributions = 0.0;

        for (int i = 0; i < participantNames.size(); i++) {
            String name = participantNames.get(i).getText().toString();
            String contributionStr = participantContributions.get(i).getText().toString();

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(contributionStr)) {
                double contribution = Double.parseDouble(contributionStr);
                names.add(name);
                contributions.put(name, contribution);
                sumContributions += contribution;
            }
        }

        if (names.isEmpty() || contributions.isEmpty()) {
            Toast.makeText(this, "Please add at least one participant with a valid contribution", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Math.abs(sumContributions - totalAmount) > 0.01) {
            Toast.makeText(this, "Total amount doesn't match sum of individual contributions", Toast.LENGTH_SHORT).show();
            return;
        }

        Expense expense = new Expense(title, totalAmount, names, contributions);
        saveExpenseToFirestore(expense);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("expense", expense);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void saveExpenseToFirestore(Expense expense) {
        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("title", expense.getTitle());
        expenseData.put("totalAmount", expense.getTotalAmount());
        expenseData.put("participants", expense.getParticipants());
        expenseData.put("contributions", expense.getContributions());

        firestore.collection("expenses").document(expense.getTitle())
                .set(expenseData)
                .addOnSuccessListener(aVoid -> Toast.makeText(AddExpenseActivity.this, "Expense saved to Firestore", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AddExpenseActivity.this, "Failed to save expense", Toast.LENGTH_SHORT).show());
    }
}
