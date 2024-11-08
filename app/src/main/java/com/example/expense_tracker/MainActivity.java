package com.example.expense_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView expenseRecyclerView;
    private FloatingActionButton addExpenseFab;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firestore = FirebaseFirestore.getInstance();
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);
        addExpenseFab = findViewById(R.id.addExpenseFab);

        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this, expenseList, new ExpenseAdapter.OnExpenseItemClickListener() {
            @Override
            public void onViewExpense(Expense expense) {
                Intent intent = new Intent(MainActivity.this, ExpenseDetailActivity.class);
                intent.putExtra("expense", expense);
                startActivity(intent);
            }

            @Override
            public void onDeleteExpense(Expense expense) {
                deleteExpenseFromFirestore(expense);
            }
        });

        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseRecyclerView.setAdapter(expenseAdapter);

        addExpenseFab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivityForResult(intent, 1);
        });

        loadExpensesFromFirestore();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Expense newExpense = (Expense) data.getSerializableExtra("expense");
            if (newExpense != null) {
                expenseList.add(newExpense);
                expenseAdapter.notifyDataSetChanged();
            }
        }
    }

    private void loadExpensesFromFirestore() {
        firestore.collection("expenses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        expenseList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            double totalAmount = document.getDouble("totalAmount");
                            List<String> participants = (List<String>) document.get("participants");
                            Map<String, Double> contributions = (Map<String, Double>) document.get("contributions");

                            Expense expense = new Expense(title, totalAmount, participants, contributions);
                            expenseList.add(expense);
                        }
                        expenseAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteExpenseFromFirestore(Expense expense) {
        firestore.collection("expenses").document(expense.getTitle())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    expenseList.remove(expense);
                    expenseAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Expense deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to delete expense", Toast.LENGTH_SHORT).show());
    }
}
