package com.animalloo.util;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.animalloo.R;
import com.animalloo.data.model.Facility;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelManager;
import com.kakao.vectormap.label.LabelOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Kakao MapView lifecycle and facility marker rendering helper.
 */
public final class FacilityMapController {

    public interface OnFacilityClickListener {
        void onFacilityClick(@NonNull String facilityId);
    }

    private static final LatLng DEFAULT_SEOUL = LatLng.from(37.5665, 126.9780);
    private static final int DEFAULT_ZOOM = 11;
    private static final int MAP_PADDING_PX = 120;

    private final FrameLayout container;
    private MapView mapView;
    private KakaoMap kakaoMap;
    private LabelLayer labelLayer;
    private final List<Label> labels = new ArrayList<>();
    private OnFacilityClickListener facilityClickListener;
    private List<Facility> pendingFacilities;
    private boolean mapStarted;

    public FacilityMapController(@NonNull FrameLayout container) {
        this.container = container;
    }

    public void setOnFacilityClickListener(@Nullable OnFacilityClickListener listener) {
        this.facilityClickListener = listener;
    }

    public void start() {
        if (mapStarted) {
            return;
        }
        mapStarted = true;
        container.removeAllViews();
        mapView = new MapView(container.getContext());
        container.addView(mapView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                // no-op
            }

            @Override
            public void onMapError(@NonNull Exception error) {
                mapStarted = false;
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                LabelManager labelManager = map.getLabelManager();
                if (labelManager != null) {
                    labelLayer = labelManager.getLayer();
                }

                map.setOnLabelClickListener((kakaoMap, layer, label) -> {
                    Object tag = label.getTag();
                    if (tag instanceof String && facilityClickListener != null) {
                        facilityClickListener.onFacilityClick((String) tag);
                        return true;
                    }
                    return false;
                });

                if (pendingFacilities != null) {
                    updateFacilities(pendingFacilities);
                    pendingFacilities = null;
                }
            }

            @NonNull
            @Override
            public LatLng getPosition() {
                return DEFAULT_SEOUL;
            }

            @Override
            public int getZoomLevel() {
                return DEFAULT_ZOOM;
            }
        });
    }

    public void resume() {
        if (mapView != null) {
            mapView.resume();
        }
    }

    public void pause() {
        if (mapView != null) {
            mapView.pause();
        }
    }

    public void clearMarkers() {
        for (Label label : labels) {
            label.remove();
        }
        labels.clear();
    }

    public void updateFacilities(@Nullable List<Facility> facilities) {
        if (kakaoMap == null || labelLayer == null) {
            pendingFacilities = facilities;
            return;
        }

        clearMarkers();
        if (facilities == null || facilities.isEmpty()) {
            kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(DEFAULT_SEOUL, DEFAULT_ZOOM));
            return;
        }

        List<LatLng> positions = new ArrayList<>();
        for (Facility facility : facilities) {
            LatLng position = LatLng.from(facility.getLatitude(), facility.getLongitude());
            Label label = labelLayer.addLabel(LabelOptions.from(position)
                    .setStyles(R.drawable.ic_map_marker)
                    .setTag(facility.getId())
                    .setClickable(true));
            labels.add(label);
            positions.add(position);
        }

        kakaoMap.moveCamera(CameraUpdateFactory.fitMapPoints(
                positions.toArray(new LatLng[0]),
                MAP_PADDING_PX));
    }

    public void destroy() {
        clearMarkers();
        kakaoMap = null;
        labelLayer = null;
        pendingFacilities = null;
        if (mapView != null) {
            container.removeView(mapView);
            mapView = null;
        }
        mapStarted = false;
    }
}
