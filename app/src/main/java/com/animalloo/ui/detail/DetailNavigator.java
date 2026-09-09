package com.animalloo.ui.detail;

import com.animalloo.data.model.DetailType;

public interface DetailNavigator {

    void navigateToDetail(DetailType type, String itemId);

    void closeDetail();
}
