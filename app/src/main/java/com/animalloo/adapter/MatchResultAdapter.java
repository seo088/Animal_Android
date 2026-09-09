package com.animalloo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.R;
import com.animalloo.data.model.MatchGrade;
import com.animalloo.data.model.MatchResult;
import com.animalloo.databinding.ItemMatchResultBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class MatchResultAdapter extends RecyclerView.Adapter<MatchResultAdapter.MatchViewHolder> {

    public interface OnMatchClickListener {
        void onMatchClick(MatchResult result);

        void onMatchLongClick(MatchResult result, View anchorView);
    }

    private final List<MatchResult> items = new ArrayList<>();
    private OnMatchClickListener listener;

    public void setItems(List<MatchResult> results) {
        items.clear();
        if (results != null) {
            items.addAll(results);
        }
        notifyDataSetChanged();
    }

    public void setOnMatchClickListener(OnMatchClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMatchResultBinding binding = ItemMatchResultBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MatchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MatchViewHolder extends RecyclerView.ViewHolder {

        private final ItemMatchResultBinding binding;

        MatchViewHolder(ItemMatchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MatchResult result) {
            binding.tvBreed.setText(result.getBreed());
            binding.tvGender.setText(result.getGender());
            binding.tvFoundRegion.setText(result.getFoundRegion());
            binding.tvFoundDate.setText(result.getFoundDate());
            binding.tvSimilarity.setText(
                    binding.getRoot().getContext().getString(
                            R.string.matching_similarity_format, result.getSimilarityPercent()));

            MatchGrade grade = result.getGrade();
            binding.tvMatchGrade.setText(grade.getGradeLabel());
            binding.tvMatchDescription.setText(grade.getDescription());

            int gradeColor;
            if (grade == MatchGrade.A) {
                gradeColor = R.color.color_match_a;
            } else if (grade == MatchGrade.B) {
                gradeColor = R.color.color_match_b;
            } else {
                gradeColor = R.color.color_match_c;
            }
            binding.tvMatchGrade.setTextColor(
                    ContextCompat.getColor(binding.getRoot().getContext(), gradeColor));

            Glide.with(binding.ivMatchImage.getContext())
                    .load(result.getImageUrl())
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivMatchImage);

            binding.btnDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMatchClick(result);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMatchClick(result);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onMatchLongClick(result, v);
                }
                return true;
            });
        }
    }
}
