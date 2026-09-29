package com.animalloo.ui.home;

import com.animalloo.R;

/**
 * Remote background images for the home stats carousel (Unsplash).
 */
public final class HomeStatSlideImages {

    public static final String MOCK_PET_PROFILE =
            "https://images.unsplash.com/photo-1587300003388-59208cc962cb?auto=format&fit=crop&w=600&q=80";

    public static final String PROTECTED =
            "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?auto=format&fit=crop&w=900&q=80";
    public static final String RESCUED_TODAY =
            "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=900&q=80";
    public static final String LOST_REPORT =
            "https://images.unsplash.com/photo-1587300003388-59208cc962cb?auto=format&fit=crop&w=900&q=80";
    public static final String FACILITIES =
            "https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&w=900&q=80";

    private HomeStatSlideImages() {
    }

    public static int fallbackForProtected() {
        return R.drawable.bg_stat_protected;
    }

    public static int fallbackForRescued() {
        return R.drawable.bg_stat_rescued;
    }

    public static int fallbackForLost() {
        return R.drawable.bg_stat_lost;
    }

    public static int fallbackForFacilities() {
        return R.drawable.bg_stat_facility;
    }
}
