package com.example.expense_tracker;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<Expense> expenseList;
    private final OnExpenseItemClickListener listener;
    private final Context context;

    public ExpenseAdapter(Context context, List<Expense> expenseList, OnExpenseItemClickListener listener) {
        this.context = context;
        this.expenseList = expenseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        holder.titleTextView.setText(expense.getTitle());
        holder.titleTextView.setTypeface(null, Typeface.BOLD);
        holder.participantsTextView.setText("Participants: " + expense.getParticipants().size());

        holder.viewButton.setOnClickListener(v -> listener.onViewExpense(expense));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteExpense(expense));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView amountTextView;
        TextView participantsTextView;
        ImageButton viewButton;
        ImageButton deleteButton;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            //amountTextView = itemView.findViewById(R.id.amountTextView);
            participantsTextView = itemView.findViewById(R.id.participantsTextView);
            viewButton = itemView.findViewById(R.id.viewButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    public interface OnExpenseItemClickListener {
        void onViewExpense(Expense expense);
        void onDeleteExpense(Expense expense);
    }
}
