package com.animalloo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.R;
import com.animalloo.data.model.Facility;
import com.animalloo.databinding.ItemFacilityBinding;

import java.util.ArrayList;
import java.util.List;

public class FacilityListAdapter extends RecyclerView.Adapter<FacilityListAdapter.FacilityViewHolder> {

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
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFacilityBinding binding = ItemFacilityBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FacilityViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class FacilityViewHolder extends RecyclerView.ViewHolder {

        private final ItemFacilityBinding binding;

        FacilityViewHolder(ItemFacilityBinding binding) {
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

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFacilityClick(facility);
                }
            });
        }
    }
}
