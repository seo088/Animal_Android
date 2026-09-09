package com.animalloo.ui.detail;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.animalloo.R;
import com.animalloo.data.model.DetailField;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.DetailUiModel;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentDetailBinding;
import com.animalloo.databinding.ItemDetailRowBinding;
import com.animalloo.ui.common.BaseFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.List;

public class DetailFragment extends BaseFragment {

    private static final String ARG_DETAIL_TYPE = "detail_type";
    private static final String ARG_ITEM_ID = "item_id";

    private FragmentDetailBinding binding;
    private DetailViewModel viewModel;
    private DetailNavigator detailNavigator;
    private DetailType detailType;
    private String itemId;

    public static DetailFragment newInstance(DetailType type, String itemId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DETAIL_TYPE, type.name());
        args.putString(ARG_ITEM_ID, itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof DetailNavigator) {
            detailNavigator = (DetailNavigator) context;
        } else {
            throw new IllegalStateException("Host Activity must implement DetailNavigator");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments == null) {
            detailNavigator.closeDetail();
            return;
        }

        String typeName = arguments.getString(ARG_DETAIL_TYPE);
        itemId = arguments.getString(ARG_ITEM_ID);
        if (typeName == null || itemId == null) {
            detailNavigator.closeDetail();
            return;
        }

        detailType = DetailType.valueOf(typeName);
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        binding.btnBack.setOnClickListener(v -> detailNavigator.closeDetail());
        observeViewModel();

        if (savedInstanceState == null) {
            viewModel.loadDetail(detailType, itemId);
        } else {
            viewModel.loadDetail(detailType, itemId);
        }
    }

    private void observeViewModel() {
        viewModel.getDetailState().observe(getViewLifecycleOwner(), this::renderDetailState);
    }

    private void renderDetailState(UiState<DetailUiModel> state) {
        if (state == null) {
            return;
        }

        if (state.isLoading()) {
            binding.layoutContent.setVisibility(View.GONE);
            showOverlayState(R.layout.layout_loading, null);
            return;
        }

        hideOverlayState();

        if (state.isError()) {
            binding.layoutContent.setVisibility(View.GONE);
            showOverlayState(R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> viewModel.loadDetail(detailType, itemId));
            });
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.layoutContent.setVisibility(View.VISIBLE);
            bindDetail(state.getData());
        }
    }

    private void bindDetail(DetailUiModel model) {
        binding.tvDetailTitle.setText(model.getTitle());

        if (!TextUtils.isEmpty(model.getSubtitle())) {
            binding.tvDetailSubtitle.setVisibility(View.VISIBLE);
            binding.tvDetailSubtitle.setText(model.getSubtitle());
        } else {
            binding.tvDetailSubtitle.setVisibility(View.GONE);
        }

        bindImage(model.getImageUrl());
        bindFields(model.getFields());

        if (!TextUtils.isEmpty(model.getDescription())) {
            binding.tvDescriptionLabel.setVisibility(View.VISIBLE);
            binding.tvDescription.setVisibility(View.VISIBLE);
            binding.tvDescription.setText(model.getDescription());
        } else {
            binding.tvDescriptionLabel.setVisibility(View.GONE);
            binding.tvDescription.setVisibility(View.GONE);
        }

        if (model.hasRelatedItem()) {
            binding.btnRelatedDetail.setVisibility(View.VISIBLE);
            binding.btnRelatedDetail.setOnClickListener(v ->
                    detailNavigator.navigateToDetail(model.getRelatedItemType(), model.getRelatedItemId()));
        } else {
            binding.btnRelatedDetail.setVisibility(View.GONE);
        }
    }

    private void bindImage(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            binding.ivDetailImage.setVisibility(View.GONE);
            return;
        }

        binding.ivDetailImage.setVisibility(View.VISIBLE);
        if (imageUrl.startsWith("/") || imageUrl.startsWith("file:")) {
            File imageFile = imageUrl.startsWith("file:")
                    ? new File(Uri.parse(imageUrl).getPath())
                    : new File(imageUrl);
            Glide.with(this)
                    .load(imageFile)
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .centerCrop()
                    .into(binding.ivDetailImage);
        } else {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .centerCrop()
                    .into(binding.ivDetailImage);
        }
    }

    private void bindFields(List<DetailField> fields) {
        binding.layoutFields.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (DetailField field : fields) {
            ItemDetailRowBinding rowBinding = ItemDetailRowBinding.inflate(inflater, binding.layoutFields, false);
            rowBinding.tvDetailLabel.setText(field.getLabel());
            rowBinding.tvDetailValue.setText(field.getValue());
            binding.layoutFields.addView(rowBinding.getRoot());
        }
    }

    private void showOverlayState(int layoutRes, StateViewSetup setup) {
        binding.stateContainer.removeAllViews();
        binding.stateContainer.setVisibility(View.VISIBLE);
        View stateView = getLayoutInflater().inflate(layoutRes, binding.stateContainer, false);
        binding.stateContainer.addView(stateView);
        if (setup != null) {
            setup.setup(stateView);
        }
    }

    private void hideOverlayState() {
        binding.stateContainer.removeAllViews();
        binding.stateContainer.setVisibility(View.GONE);
    }

    private interface StateViewSetup {
        void setup(View stateView);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
