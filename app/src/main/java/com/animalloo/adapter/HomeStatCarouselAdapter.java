package com.animalloo.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.databinding.ItemHomeStatSlideBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class HomeStatCarouselAdapter extends RecyclerView.Adapter<HomeStatCarouselAdapter.StatViewHolder> {

    private static final int BLUR_RADIUS = 22;
    private static final int BLUR_SAMPLING = 3;

    public static final class StatSlide {
        private final String value;
        private final String label;
        private final String imageUrl;
        @DrawableRes
        private final int fallbackBackgroundResId;
        @ColorInt
        private final int scrimColor;

        public StatSlide(String value, String label, String imageUrl,
                         int fallbackBackgroundResId, int scrimColor) {
            this.value = value;
            this.label = label;
            this.imageUrl = imageUrl;
            this.fallbackBackgroundResId = fallbackBackgroundResId;
            this.scrimColor = scrimColor;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public int getFallbackBackgroundResId() {
            return fallbackBackgroundResId;
        }

        public int getScrimColor() {
            return scrimColor;
        }
    }

    private final List<StatSlide> items = new ArrayList<>();

    public void setItems(List<StatSlide> slides) {
        items.clear();
        if (slides != null) {
            items.addAll(slides);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeStatSlideBinding binding = ItemHomeStatSlideBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        binding.getRoot().setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return new StatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StatViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class StatViewHolder extends RecyclerView.ViewHolder {

        private final ItemHomeStatSlideBinding binding;

        StatViewHolder(ItemHomeStatSlideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(StatSlide slide) {
            binding.tvStatValue.setText(slide.getValue());
            binding.tvStatLabel.setText(slide.getLabel());
            binding.viewStatScrim.setBackgroundColor(slide.getScrimColor());

            Glide.with(binding.ivStatBackground.getContext())
                    .load(slide.getImageUrl())
                    .apply(new RequestOptions()
                            .transform(new BlurTransformation(BLUR_RADIUS, BLUR_SAMPLING))
                            .placeholder(slide.getFallbackBackgroundResId())
                            .error(slide.getFallbackBackgroundResId()))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivStatBackground);
        }
    }
}
