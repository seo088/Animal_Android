package com.animalloo.util;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.animalloo.R;
import com.animalloo.data.model.Facility;
import com.google.android.material.snackbar.Snackbar;

/** External, user initiated actions for mock facility records. */
public final class FacilityActions {

    private FacilityActions() { }

    public static void dial(Fragment fragment, View anchor, Facility facility) {
        if (TextUtils.isEmpty(facility.getPhone())) {
            Snackbar.make(anchor, R.string.facility_phone_unavailable, Snackbar.LENGTH_SHORT).show();
            return;
        }
        launch(fragment, anchor, new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", facility.getPhone(), null)));
    }

    public static void directions(Fragment fragment, View anchor, Facility facility) {
        String coordinates = facility.getLatitude() + "," + facility.getLongitude();
        Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                + Uri.encode(coordinates));
        launch(fragment, anchor, new Intent(Intent.ACTION_VIEW, uri));
    }

    private static void launch(Fragment fragment, View anchor, Intent intent) {
        try {
            fragment.startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException exception) {
            Snackbar.make(anchor, R.string.facility_action_unavailable, Snackbar.LENGTH_SHORT).show();
        }
    }
}
