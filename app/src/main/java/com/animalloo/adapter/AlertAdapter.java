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
import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    public interface OnAlertClickListener {
        void onAlertClick(AlertNotification alert);

        void onAlertLongClick(AlertNotification alert, View anchorView);
    }

    private final List<AlertNotification> items = new ArrayList<>();
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
            binding.tvAnimalType.setText(alert.getAnimalType());
            binding.tvRegion.setText(alert.getRegion());
            binding.tvOccurredAt.setText(alert.getOccurredAt());
            binding.tvStatus.setText(alert.getStatus());

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

            binding.btnDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlertClick(alert);
                }
            });

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
        }
    }
}
