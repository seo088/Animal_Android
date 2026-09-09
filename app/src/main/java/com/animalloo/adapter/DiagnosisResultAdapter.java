package com.animalloo.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.databinding.ItemDiagnosisResultBinding;
import com.animalloo.data.model.DiagnosisResult;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisResultAdapter extends RecyclerView.Adapter<DiagnosisResultAdapter.ResultViewHolder> {

    private final List<DiagnosisResult> items = new ArrayList<>();

    public void setItems(List<DiagnosisResult> results) {
        items.clear();
        if (results != null) {
            items.addAll(results);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDiagnosisResultBinding binding = ItemDiagnosisResultBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ResultViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {

        private final ItemDiagnosisResultBinding binding;

        ResultViewHolder(ItemDiagnosisResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DiagnosisResult result) {
            binding.tvDiseaseName.setText(result.getDiseaseName());
            binding.tvProbability.setText(
                    binding.getRoot().getContext().getString(
                            com.animalloo.R.string.diagnosis_probability_format,
                            result.getProbabilityPercent()));
            binding.tvRelatedSymptoms.setText(
                    binding.getRoot().getContext().getString(
                            com.animalloo.R.string.diagnosis_related_symptoms,
                            android.text.TextUtils.join(", ", result.getRelatedSymptoms())));
            binding.tvDescription.setText(result.getDescription());
            binding.tvCaution.setText(result.getCaution());
        }
    }
}
