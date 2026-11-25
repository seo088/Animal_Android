package com.animalloo.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.R;
import com.animalloo.data.model.Symptom;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Full-width, accessible symptom rows. The API's original symptom name is preserved. */
public class SymptomSelectionAdapter extends RecyclerView.Adapter<SymptomSelectionAdapter.ViewHolder> {

    public interface OnSelectionChangeListener {
        void onSelectionChange(@NonNull Symptom symptom, boolean selected);
    }

    private final List<Symptom> symptoms = new ArrayList<>();
    private final Set<String> selectedIds = new HashSet<>();
    private OnSelectionChangeListener listener;

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.listener = listener;
    }

    public void submit(@NonNull List<Symptom> items, Set<String> selected) {
        symptoms.clear();
        symptoms.addAll(items);
        selectedIds.clear();
        if (selected != null) {
            selectedIds.addAll(selected);
        }
        notifyDataSetChanged();
    }

    public void updateSelection(Set<String> selected) {
        selectedIds.clear();
        if (selected != null) {
            selectedIds.addAll(selected);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_symptom_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Symptom symptom = symptoms.get(position);
        boolean selected = selectedIds.contains(symptom.getId());
        holder.bind(symptom, selected);
        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSelectionChange(symptom, !selected);
            }
        });
    }

    @Override
    public int getItemCount() {
        return symptoms.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView name;
        final TextView check;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_symptom_selection);
            name = itemView.findViewById(R.id.tv_symptom_name);
            check = itemView.findViewById(R.id.tv_symptom_check);
        }

        void bind(@NonNull Symptom symptom, boolean selected) {
            int primary = ContextCompat.getColor(itemView.getContext(), R.color.color_primary);
            int surface = ContextCompat.getColor(itemView.getContext(), R.color.color_surface);
            int onPrimary = ContextCompat.getColor(itemView.getContext(), R.color.color_on_primary);
            int text = ContextCompat.getColor(itemView.getContext(), R.color.color_text_primary);
            name.setText(symptom.getName());
            name.setTextColor(selected ? onPrimary : text);
            check.setVisibility(selected ? View.VISIBLE : View.GONE);
            card.setCardBackgroundColor(selected ? primary : surface);
            card.setStrokeColor(ColorStateList.valueOf(selected ? primary
                    : ContextCompat.getColor(itemView.getContext(), R.color.color_outline)));
            card.setContentDescription(symptom.getName() + (selected ? ", 선택됨" : ", 선택 안 됨"));
        }
    }
}
