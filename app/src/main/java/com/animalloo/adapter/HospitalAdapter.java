package com.animalloo.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.R;
import com.animalloo.data.model.Hospital;
import com.animalloo.databinding.ItemHospitalBinding;

import java.util.ArrayList;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    public interface OnHospitalClickListener {
        void onHospitalClick(Hospital hospital);
    }

    private final List<Hospital> items = new ArrayList<>();
    private OnHospitalClickListener listener;

    public void setItems(List<Hospital> hospitals) {
        items.clear();
        if (hospitals != null) {
            items.addAll(hospitals);
        }
        notifyDataSetChanged();
    }

    public void setOnHospitalClickListener(OnHospitalClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHospitalBinding binding = ItemHospitalBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HospitalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HospitalViewHolder extends RecyclerView.ViewHolder {

        private final ItemHospitalBinding binding;

        HospitalViewHolder(ItemHospitalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Hospital hospital) {
            binding.tvHospitalName.setText(hospital.getName());
            binding.tvHospitalAddress.setText(hospital.getAddress());
            binding.tvHospitalPhone.setText(hospital.getPhone());
            binding.tvHospitalDistance.setText(
                    binding.getRoot().getContext().getString(
                            R.string.map_distance_format, hospital.getDistanceKm()));
            binding.tvHospitalOpenStatus.setText(
                    binding.getRoot().getContext().getString(
                            hospital.isOpenNow() ? R.string.hospital_open : R.string.hospital_closed));
            if (hospital.isOpenNow()) {
                binding.tvHospitalOpenStatus.setBackgroundResource(R.drawable.bg_badge_success);
            } else {
                binding.tvHospitalOpenStatus.setBackgroundResource(R.drawable.bg_badge_warning);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHospitalClick(hospital);
                }
            });
        }
    }
}
