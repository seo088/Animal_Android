package com.animalloo.util;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.animalloo.R;
import com.animalloo.data.model.Facility;
import com.animalloo.data.model.Hospital;
import com.google.android.material.snackbar.Snackbar;

/** External, user initiated actions for mock location records. */
public final class FacilityActions {

    private FacilityActions() { }

    public static void dial(Fragment fragment, View anchor, Facility facility) {
        dial(fragment, anchor, facility.getPhone());
    }

    public static void dial(Fragment fragment, View anchor, Hospital hospital) {
        dial(fragment, anchor, hospital.getPhone());
    }

    public static void dial(Fragment fragment, View anchor, String phone) {
        if (TextUtils.isEmpty(phone)) {
            Snackbar.make(anchor, R.string.facility_phone_unavailable, Snackbar.LENGTH_SHORT).show();
            return;
        }
        launch(fragment, anchor, new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", phone, null)));
    }

    public static void directions(Fragment fragment, View anchor, Facility facility) {
        directions(fragment, anchor, facility.getLatitude(), facility.getLongitude());
    }

    public static void directions(Fragment fragment, View anchor, Hospital hospital) {
        directions(fragment, anchor, hospital.getLatitude(), hospital.getLongitude());
    }

    public static void directions(Fragment fragment, View anchor,
                                  double latitude, double longitude) {
        String coordinates = latitude + "," + longitude;
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
