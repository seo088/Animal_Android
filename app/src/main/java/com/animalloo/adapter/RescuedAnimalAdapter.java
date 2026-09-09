package com.animalloo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.R;
import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.databinding.ItemRescuedAnimalBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class RescuedAnimalAdapter extends RecyclerView.Adapter<RescuedAnimalAdapter.AnimalViewHolder> {

    public interface OnAnimalClickListener {
        void onAnimalClick(RescuedAnimal animal);

        void onAnimalLongClick(RescuedAnimal animal, View anchorView);
    }

    private final List<RescuedAnimal> items = new ArrayList<>();
    private OnAnimalClickListener listener;

    public void setItems(List<RescuedAnimal> animals) {
        items.clear();
        if (animals != null) {
            items.addAll(animals);
        }
        notifyDataSetChanged();
    }

    public void setOnAnimalClickListener(OnAnimalClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRescuedAnimalBinding binding = ItemRescuedAnimalBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AnimalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class AnimalViewHolder extends RecyclerView.ViewHolder {

        private final ItemRescuedAnimalBinding binding;

        AnimalViewHolder(ItemRescuedAnimalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RescuedAnimal animal) {
            binding.tvBreed.setText(animal.getBreed());
            binding.tvGender.setText(animal.getGender());
            binding.tvRegion.setText(animal.getRegion());
            binding.tvRescuedDate.setText(animal.getRescuedDate());
            binding.tvProtectionStatus.setText(animal.getProtectionStatus());

            Glide.with(binding.ivAnimalImage.getContext())
                    .load(animal.getImageUrl())
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivAnimalImage);

            binding.btnDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAnimalClick(animal);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAnimalClick(animal);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onAnimalLongClick(animal, v);
                }
                return true;
            });
        }
    }
}
