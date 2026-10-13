package com.animalloo.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.animalloo.R;
import com.animalloo.data.model.Facility;
import com.animalloo.data.model.FacilityCategory;
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
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kakao MapView lifecycle and facility marker rendering helper.
 * Markers are colored per FacilityCategory and grouped into count bubbles on a
 * zoom-dependent grid; tapping a bubble zooms in on it.
 */
public final class FacilityMapController {

    public interface OnFacilityClickListener {
        void onFacilityClick(@NonNull String facilityId);
    }

    private static final LatLng DEFAULT_SEOUL = LatLng.from(37.5665, 126.9780);
    private static final int DEFAULT_ZOOM = 11;
    private static final int MAP_PADDING_PX = 120;
    private static final int MARKER_SIZE_DP = 36;
    private static final int CLUSTER_SIZE_DP = 40;
    // Grid cell edge in dp; facilities closer than this on screen are grouped.
    private static final int CLUSTER_CELL_DP = 64;
    // From this zoom level on, every facility is shown individually.
    private static final int NO_CLUSTER_ZOOM = 16;
    private static final int CLUSTER_ZOOM_STEP = 2;
    private static final String CLUSTER_TAG_PREFIX = "cluster:";

    private final FrameLayout container;
    private MapView mapView;
    private KakaoMap kakaoMap;
    private LabelManager labelManager;
    private LabelLayer labelLayer;
    private final Map<FacilityCategory, LabelStyles> markerStyles = new EnumMap<>(FacilityCategory.class);
    private LabelStyles defaultMarkerStyles;
    private final Map<String, LabelStyles> clusterStyles = new HashMap<>();
    private final Map<String, LatLng> clusterCenters = new HashMap<>();
    private final List<Label> labels = new ArrayList<>();
    private final List<Facility> facilities = new ArrayList<>();
    private OnFacilityClickListener facilityClickListener;
    private List<Facility> pendingFacilities;
    private boolean mapStarted;
    private int renderedZoom = -1;

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
                labelManager = map.getLabelManager();
                if (labelManager != null) {
                    labelLayer = labelManager.getLayer();
                    for (FacilityCategory category : FacilityCategory.values()) {
                        markerStyles.put(category, labelManager.addLabelStyles(LabelStyles.from(
                                LabelStyle.from(createMarkerBitmap(colorFor(category))))));
                    }
                    defaultMarkerStyles = labelManager.addLabelStyles(LabelStyles.from(
                            LabelStyle.from(createMarkerBitmap(color(R.color.color_primary)))));
                }

                map.setOnLabelClickListener((kakaoMap, layer, label) -> {
                    Object tag = label.getTag();
                    if (!(tag instanceof String)) {
                        return false;
                    }
                    String id = (String) tag;
                    LatLng clusterCenter = clusterCenters.get(id);
                    if (clusterCenter != null) {
                        int zoom = Math.min(kakaoMap.getZoomLevel() + CLUSTER_ZOOM_STEP, NO_CLUSTER_ZOOM);
                        kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(clusterCenter, zoom));
                        return true;
                    }
                    if (facilityClickListener != null) {
                        facilityClickListener.onFacilityClick(id);
                        return true;
                    }
                    return false;
                });

                map.setOnCameraMoveEndListener((kakaoMap, cameraPosition, gestureType) -> {
                    // Panning keeps the grid valid; only regroup when the zoom changes.
                    if (cameraPosition.getZoomLevel() != renderedZoom) {
                        renderMarkers(cameraPosition.getZoomLevel());
                    }
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
        clusterCenters.clear();
    }

    public void updateFacilities(@Nullable List<Facility> newFacilities) {
        if (kakaoMap == null || labelLayer == null || defaultMarkerStyles == null) {
            pendingFacilities = newFacilities;
            return;
        }

        facilities.clear();
        if (newFacilities != null) {
            facilities.addAll(newFacilities);
        }
        if (facilities.isEmpty()) {
            clearMarkers();
            kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(DEFAULT_SEOUL, DEFAULT_ZOOM));
            return;
        }

        List<LatLng> positions = new ArrayList<>();
        for (Facility facility : facilities) {
            positions.add(LatLng.from(facility.getLatitude(), facility.getLongitude()));
        }
        kakaoMap.moveCamera(CameraUpdateFactory.fitMapPoints(
                positions.toArray(new LatLng[0]),
                MAP_PADDING_PX));
        // fitMapPoints also fires the camera-move-end listener; render now in case
        // the zoom didn't change (same zoom => listener skips the regroup).
        renderMarkers(kakaoMap.getZoomLevel());
    }

    public void destroy() {
        clearMarkers();
        facilities.clear();
        kakaoMap = null;
        labelManager = null;
        labelLayer = null;
        markerStyles.clear();
        defaultMarkerStyles = null;
        clusterStyles.clear();
        pendingFacilities = null;
        renderedZoom = -1;
        if (mapView != null) {
            container.removeView(mapView);
            mapView = null;
        }
        mapStarted = false;
    }

    // --- rendering ------------------------------------------------------------------

    private void renderMarkers(int zoom) {
        if (labelLayer == null) {
            return;
        }
        clearMarkers();
        renderedZoom = zoom;

        if (zoom >= NO_CLUSTER_ZOOM) {
            for (Facility facility : facilities) {
                addFacilityLabel(facility);
            }
            return;
        }

        for (List<Facility> cell : groupByGridCell(zoom).values()) {
            if (cell.size() == 1) {
                addFacilityLabel(cell.get(0));
            } else {
                addClusterLabel(cell);
            }
        }
    }

    /** Buckets facilities into square-ish grid cells about CLUSTER_CELL_DP wide at this zoom. */
    private Map<String, List<Facility>> groupByGridCell(int zoom) {
        // Kakao zoom levels follow web-mercator tiles of 256dp: 360° spans 256 * 2^zoom dp.
        double lngPerCell = CLUSTER_CELL_DP * 360.0 / (256.0 * Math.pow(2, zoom));
        Map<String, List<Facility>> cells = new LinkedHashMap<>();
        for (Facility facility : facilities) {
            double latPerCell = lngPerCell * Math.cos(Math.toRadians(facility.getLatitude()));
            long row = (long) Math.floor(facility.getLatitude() / latPerCell);
            long col = (long) Math.floor(facility.getLongitude() / lngPerCell);
            cells.computeIfAbsent(row + ":" + col, key -> new ArrayList<>()).add(facility);
        }
        return cells;
    }

    private void addFacilityLabel(Facility facility) {
        LabelStyles styles = facility.getCategory() != null
                ? markerStyles.get(facility.getCategory())
                : defaultMarkerStyles;
        labels.add(labelLayer.addLabel(LabelOptions.from(
                        LatLng.from(facility.getLatitude(), facility.getLongitude()))
                .setStyles(styles != null ? styles : defaultMarkerStyles)
                .setTag(facility.getId())
                .setClickable(true)));
    }

    private void addClusterLabel(List<Facility> cell) {
        double lat = 0;
        double lng = 0;
        Map<FacilityCategory, Integer> categoryCounts = new EnumMap<>(FacilityCategory.class);
        for (Facility facility : cell) {
            lat += facility.getLatitude();
            lng += facility.getLongitude();
            if (facility.getCategory() != null) {
                categoryCounts.merge(facility.getCategory(), 1, Integer::sum);
            }
        }
        LatLng center = LatLng.from(lat / cell.size(), lng / cell.size());

        // Bubble takes the color of the most common category in it.
        FacilityCategory dominant = null;
        for (Map.Entry<FacilityCategory, Integer> entry : categoryCounts.entrySet()) {
            if (dominant == null || entry.getValue() > categoryCounts.get(dominant)) {
                dominant = entry.getKey();
            }
        }
        int fill = dominant != null ? colorFor(dominant) : color(R.color.color_primary);

        String tag = CLUSTER_TAG_PREFIX + clusterCenters.size();
        clusterCenters.put(tag, center);
        labels.add(labelLayer.addLabel(LabelOptions.from(center)
                .setStyles(clusterStyle(fill, cell.size()))
                .setTag(tag)
                .setClickable(true)));
    }

    private LabelStyles clusterStyle(@ColorInt int fill, int count) {
        String text = count > 99 ? "99+" : String.valueOf(count);
        String key = fill + "/" + text;
        LabelStyles styles = clusterStyles.get(key);
        if (styles == null && labelManager != null) {
            styles = labelManager.addLabelStyles(LabelStyles.from(
                    LabelStyle.from(createClusterBitmap(fill, text))));
            clusterStyles.put(key, styles);
        }
        return styles;
    }

    // --- bitmaps & colors -----------------------------------------------------------

    @ColorInt
    private int colorFor(@NonNull FacilityCategory category) {
        switch (category) {
            case HOSPITAL:
                return color(R.color.map_marker_hospital);
            case PHARMACY:
                return color(R.color.map_marker_pharmacy);
            case SHELTER:
                return color(R.color.map_marker_shelter);
            case RESTAURANT:
                return color(R.color.map_marker_restaurant);
            case CAFE:
                return color(R.color.map_marker_cafe);
            case HOTEL:
                return color(R.color.map_marker_hotel);
            case TOURISM:
            default:
                return color(R.color.map_marker_tourism);
        }
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(container.getContext(), res);
    }

    private int dp(int value) {
        return (int) (value * container.getResources().getDisplayMetrics().density);
    }

    @NonNull
    private Bitmap createMarkerBitmap(@ColorInt int tint) {
        int sizePx = dp(MARKER_SIZE_DP);
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Drawable drawable = AppCompatResources.getDrawable(container.getContext(), R.drawable.ic_map_marker);
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable.mutate());
            DrawableCompat.setTint(drawable, tint);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, sizePx, sizePx);
            drawable.draw(canvas);
        }
        return bitmap;
    }

    @NonNull
    private Bitmap createClusterBitmap(@ColorInt int fill, @NonNull String text) {
        int sizePx = dp(CLUSTER_SIZE_DP);
        float radius = sizePx / 2f;
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);
        circle.setColor(Color.WHITE);
        canvas.drawCircle(radius, radius, radius, circle);
        circle.setColor(fill);
        canvas.drawCircle(radius, radius, radius - dp(3), circle);

        Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
        label.setColor(Color.WHITE);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setTextAlign(Paint.Align.CENTER);
        label.setTextSize(text.length() >= 3 ? dp(12) : dp(15));
        float baseline = radius - (label.descent() + label.ascent()) / 2f;
        canvas.drawText(text, radius, baseline, label);
        return bitmap;
    }
}
