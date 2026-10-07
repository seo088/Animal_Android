package com.animalloo.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.R;
import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.AlertType;
import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.databinding.ItemAlertBinding;
import com.animalloo.databinding.ItemRescueMyReportBinding;
import com.animalloo.databinding.ItemRescueSectionHeaderBinding;
import com.animalloo.ui.rescue.RescueTimelineItem;
import com.animalloo.ui.rescue.RescueTimelineItemType;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class RescueTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MY_REPORT = 0;
    private static final int VIEW_TYPE_SECTION_HEADER = 1;
    private static final int VIEW_TYPE_TIMELINE_EVENT = 2;

    public interface OnTimelineActionListener {
        void onMyReportClick(LostAnimalReport report);

        void onViewMatchesClick(LostAnimalReport report);

        void onTimelineEventClick(AlertNotification alert);
    }

    private final List<RescueTimelineItem> items = new ArrayList<>();
    private OnTimelineActionListener listener;

    public void setItems(List<RescueTimelineItem> timelineItems) {
        items.clear();
        if (timelineItems != null) {
            items.addAll(timelineItems);
        }
        notifyDataSetChanged();
    }

    public void setOnTimelineActionListener(OnTimelineActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        RescueTimelineItem item = items.get(position);
        switch (item.getType()) {
            case MY_REPORT:
                return VIEW_TYPE_MY_REPORT;
            case SECTION_HEADER:
                return VIEW_TYPE_SECTION_HEADER;
            case TIMELINE_EVENT:
            default:
                return VIEW_TYPE_TIMELINE_EVENT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_MY_REPORT) {
            return new MyReportViewHolder(
                    ItemRescueMyReportBinding.inflate(inflater, parent, false));
        }
        if (viewType == VIEW_TYPE_SECTION_HEADER) {
            return new SectionHeaderViewHolder(
                    ItemRescueSectionHeaderBinding.inflate(inflater, parent, false));
        }
        return new TimelineEventViewHolder(
                ItemAlertBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RescueTimelineItem item = items.get(position);
        if (holder instanceof MyReportViewHolder) {
            ((MyReportViewHolder) holder).bind(item);
        } else if (holder instanceof SectionHeaderViewHolder) {
            ((SectionHeaderViewHolder) holder).bind(item);
        } else if (holder instanceof TimelineEventViewHolder) {
            ((TimelineEventViewHolder) holder).bind(item.getAlert());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MyReportViewHolder extends RecyclerView.ViewHolder {

        private final ItemRescueMyReportBinding binding;

        MyReportViewHolder(ItemRescueMyReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RescueTimelineItem item) {
            LostAnimalReport report = item.getMyReport();
            if (report == null) {
                return;
            }

            binding.tvMyReportName.setText(binding.getRoot().getContext().getString(
                    R.string.rescue_my_report_name_format,
                    report.getName(),
                    report.getBreed()));
            binding.tvMyReportRegion.setText(report.getRegion());
            binding.tvMyReportMatchCount.setText(binding.getRoot().getContext().getString(
                    R.string.rescue_match_count_format,
                    item.getMatchCount()));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMyReportClick(report);
                }
            });
            binding.btnViewMatches.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewMatchesClick(report);
                }
            });
        }
    }

    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {

        private final ItemRescueSectionHeaderBinding binding;

        SectionHeaderViewHolder(ItemRescueSectionHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RescueTimelineItem item) {
            String title = item.getSectionTitle();
            if (title != null) {
                binding.getRoot().setText(title);
            }
        }
    }

    class TimelineEventViewHolder extends RecyclerView.ViewHolder {

        private final ItemAlertBinding binding;

        TimelineEventViewHolder(ItemAlertBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AlertNotification alert) {
            if (alert == null) {
                return;
            }

            binding.tvAnimalType.setText(alert.getAnimalType());
            binding.tvRegion.setText(alert.getRegion());
            binding.tvOccurredAt.setText(alert.getOccurredAt());

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

            ViewGroup.MarginLayoutParams layoutParams = (MarginLayoutParams) binding.getRoot().getLayoutParams();
            if (layoutParams != null) {
                int horizontal = binding.getRoot().getResources()
                        .getDimensionPixelSize(com.animalloo.R.dimen.spacing_md);
                layoutParams.setMargins(horizontal, 0, horizontal, layoutParams.bottomMargin);
                binding.getRoot().setLayoutParams(layoutParams);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTimelineEventClick(alert);
                }
            });
            binding.getRoot().setOnLongClickListener(null);
        }
    }
}
