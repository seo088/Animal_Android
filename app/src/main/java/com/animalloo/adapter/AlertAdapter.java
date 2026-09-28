package com.animalloo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.R;
import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.AlertType;
import com.animalloo.databinding.ItemAlertBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    public interface OnAlertClickListener {
        void onAlertClick(AlertNotification alert);

        void onAlertLongClick(AlertNotification alert, View anchorView);

        void onFavoriteClick(AlertNotification alert);
    }

    private final List<AlertNotification> items = new ArrayList<>();
    private final Set<String> favoriteAlertIds = new HashSet<>();
    private OnAlertClickListener listener;

    public void setItems(List<AlertNotification> alerts) {
        items.clear();
        if (alerts != null) {
            items.addAll(alerts);
        }
        notifyDataSetChanged();
    }

    public void setOnAlertClickListener(OnAlertClickListener listener) {
        this.listener = listener;
    }

    public void setFavoriteAlertIds(Set<String> favoriteIds) {
        favoriteAlertIds.clear();
        if (favoriteIds != null) {
            favoriteAlertIds.addAll(favoriteIds);
        }
        notifyDataSetChanged();
    }

    public AlertNotification getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAlertBinding binding = ItemAlertBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AlertViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class AlertViewHolder extends RecyclerView.ViewHolder {

        private final ItemAlertBinding binding;

        AlertViewHolder(ItemAlertBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AlertNotification alert) {
            boolean isFavorite = favoriteAlertIds.contains(alert.getId());

            binding.tvAnimalType.setText(alert.getAnimalType());
            binding.tvRegion.setText(alert.getRegion());
            binding.tvOccurredAt.setText(alert.getOccurredAt());
            binding.tvStatus.setText(alert.getStatus());
            binding.tvFavoriteBadge.setVisibility(isFavorite ? View.VISIBLE : View.GONE);
            binding.btnFavorite.setImageResource(isFavorite
                    ? R.drawable.ic_favorite_filled
                    : R.drawable.ic_favorite);
            binding.btnFavorite.setContentDescription(binding.getRoot().getContext().getString(
                    isFavorite ? R.string.context_unfavorite : R.string.context_favorite));

            if (alert.getType() == AlertType.RESCUE) {
                binding.tvAlertType.setText(R.string.alert_type_rescue);
                binding.tvAlertType.setBackgroundResource(R.drawable.bg_badge_success);
            } else {
                binding.tvAlertType.setText(R.string.alert_type_lost);
                binding.tvAlertType.setBackgroundResource(R.drawable.bg_badge_warning);
            }
            binding.tvAlertType.setTextColor(
                    binding.getRoot().getContext().getColor(R.color.color_on_primary));

            Glide.with(binding.ivAlertImage.getContext())
                    .load(alert.getImageUrl())
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivAlertImage);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlertClick(alert);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onAlertLongClick(alert, v);
                }
                return true;
            });

            binding.btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(alert);
                }
            });
        }
    }
}
