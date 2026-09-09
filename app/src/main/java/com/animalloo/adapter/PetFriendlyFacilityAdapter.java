package com.animalloo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.R;
import com.animalloo.data.model.Facility;
import com.animalloo.databinding.ItemPetFriendlyFacilityBinding;

import java.util.ArrayList;
import java.util.List;

public class PetFriendlyFacilityAdapter
        extends RecyclerView.Adapter<PetFriendlyFacilityAdapter.PetFriendlyViewHolder> {

    public interface OnFacilityClickListener {
        void onFacilityClick(Facility facility);
    }

    private final List<Facility> items = new ArrayList<>();
    private OnFacilityClickListener listener;

    public void setItems(List<Facility> facilities) {
        items.clear();
        if (facilities != null) {
            items.addAll(facilities);
        }
        notifyDataSetChanged();
    }

    public void setOnFacilityClickListener(OnFacilityClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PetFriendlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPetFriendlyFacilityBinding binding = ItemPetFriendlyFacilityBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PetFriendlyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PetFriendlyViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class PetFriendlyViewHolder extends RecyclerView.ViewHolder {

        private final ItemPetFriendlyFacilityBinding binding;

        PetFriendlyViewHolder(ItemPetFriendlyFacilityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Facility facility) {
            binding.tvFacilityName.setText(facility.getName());
            binding.tvFacilityAddress.setText(facility.getAddress());
            binding.tvFacilityCategory.setText(facility.getCategory().getDisplayName());
            binding.tvFacilityDistance.setText(
                    binding.getRoot().getContext().getString(
                            R.string.map_distance_format, facility.getDistanceKm()));
            binding.tvPetType.setText(
                    binding.getRoot().getContext().getString(
                            R.string.pet_friendly_pet_type_format, facility.getAllowedPetType()));
            binding.tvSizeLimit.setText(
                    binding.getRoot().getContext().getString(
                            R.string.pet_friendly_size_format, facility.getSizeLimit()));

            binding.tvIndoorBadge.setVisibility(
                    facility.isIndoorAllowed() ? View.VISIBLE : View.GONE);
            binding.tvCarrierBadge.setVisibility(
                    facility.isCarrierRequired() ? View.VISIBLE : View.GONE);
            binding.tvLeashBadge.setVisibility(
                    facility.isLeashRequired() ? View.VISIBLE : View.GONE);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFacilityClick(facility);
                }
            });
        }
    }
}
