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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RescuedAnimalAdapter extends RecyclerView.Adapter<RescuedAnimalAdapter.AnimalViewHolder> {

    public interface OnAnimalClickListener {
        void onAnimalClick(RescuedAnimal animal);

        void onAnimalLongClick(RescuedAnimal animal, View anchorView);
    }

    public interface OnAnimalActionListener {
        void onFavoriteClick(RescuedAnimal animal);

        void onShareClick(RescuedAnimal animal);
    }

    private final List<RescuedAnimal> items = new ArrayList<>();
    private final Set<String> favoriteAnimalIds = new HashSet<>();
    private OnAnimalClickListener listener;
    private OnAnimalActionListener actionListener;

    public void setItems(List<RescuedAnimal> animals) {
        items.clear();
        if (animals != null) {
            items.addAll(animals);
        }
        notifyDataSetChanged();
    }

    public void setFavoriteAnimalIds(Set<String> favoriteIds) {
        favoriteAnimalIds.clear();
        if (favoriteIds != null) {
            favoriteAnimalIds.addAll(favoriteIds);
        }
        notifyDataSetChanged();
    }

    public void setOnAnimalClickListener(OnAnimalClickListener listener) {
        this.listener = listener;
    }

    public void setOnAnimalActionListener(OnAnimalActionListener actionListener) {
        this.actionListener = actionListener;
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
            boolean isFavorite = favoriteAnimalIds.contains(animal.getId());

            binding.tvBreed.setText(animal.getBreed());
            binding.tvGender.setText(animal.getGender());
            binding.tvRegion.setText(animal.getRegion());
            binding.tvRescuedDate.setText(animal.getRescuedDate());
            binding.tvProtectionStatus.setText(animal.getProtectionStatus());
            binding.tvFavoriteBadge.setVisibility(isFavorite ? View.VISIBLE : View.GONE);
            binding.btnFavorite.setImageResource(isFavorite
                    ? R.drawable.ic_favorite_filled
                    : R.drawable.ic_favorite);
            binding.btnFavorite.setContentDescription(binding.getRoot().getContext().getString(
                    isFavorite ? R.string.context_unfavorite : R.string.context_favorite));

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

            binding.btnFavorite.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onFavoriteClick(animal);
                }
            });

            binding.btnShare.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onShareClick(animal);
                }
            });
        }
    }
}
