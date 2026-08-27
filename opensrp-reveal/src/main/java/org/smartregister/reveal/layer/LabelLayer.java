package org.smartregister.reveal.layer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;

public class LabelLayer {

    private static final String TAG = LabelLayer.class.getName();

    protected LabelLayer.Builder builder;

    protected String BOUNDARY_LABEL_SOURCE_ID = UUID.randomUUID().toString();
    protected String BOUNDARY_LINE_LAYER_ID = UUID.randomUUID().toString();
    protected String BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();

    private GeoJsonSource boundaryLabelsSource;
    private SymbolLayer boundaryLabelLayer;

    private boolean visible = false;
    private boolean removed = false;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public LabelLayer(@NonNull LabelLayer.Builder builder) {
        this.builder = builder;
    }

    private void createBoundaryLabelLayer(@NonNull LabelLayer.Builder builder) {
        boundaryLabelLayer = new SymbolLayer(BOUNDARY_LABEL_LAYER_ID, BOUNDARY_LABEL_SOURCE_ID)
                .withProperties(
                        PropertyFactory.textField(Expression.toString(Expression.get(builder.getLabelProperty()))),
                        PropertyFactory.textPadding(35f),
                        PropertyFactory.textColor(builder.getLabelColorInt()),
                        PropertyFactory.textAllowOverlap(true),
                        PropertyFactory.textSize(Expression.interpolate(
                                Expression.linear(),
                                Expression.zoom(),
                                Expression.stop(14, 2f),
                                Expression.stop(17, 8f),
                                Expression.stop(19, 12f)
                        )),
                        PropertyFactory.textOpacity(Expression.interpolate(
                                Expression.linear(),
                                Expression.zoom(),
                                Expression.stop(17.5, 0f),
                                Expression.stop(18.5, 1f)
                        )),
                        PropertyFactory.textOffset(this.builder.getTextOffSetTuple())
                );
    }

    private void createBoundaryLabelSource() {
        boundaryLabelsSource = new GeoJsonSource(BOUNDARY_LABEL_SOURCE_ID);
    }

    public void addLayerToMap(@NonNull MapboxMap mapboxMap) {
        mapboxMap.getStyle(style -> {
            if (!style.isFullyLoaded()) return;

            createLayers(style);

            executorService.execute(() -> {
                FeatureCollection boundaryCenterFeatures = calculateCenterPoints(builder.getFeatureCollection());

                mainHandler.post(() -> {
                    mapboxMap.getStyle(activeStyle -> {
                        if (activeStyle != null && activeStyle.isFullyLoaded()) {
                            if (boundaryLabelsSource != null) {
                                boundaryLabelsSource.setGeoJson(boundaryCenterFeatures);
                                if (activeStyle.getSource(BOUNDARY_LABEL_SOURCE_ID) == null) {
                                    activeStyle.addSource(boundaryLabelsSource);
                                }
                            }

                            if (boundaryLabelLayer != null && activeStyle.getLayer(BOUNDARY_LABEL_LAYER_ID) == null) {
                                if (builder.getBelowLayerId() != null && activeStyle.getLayer(builder.getBelowLayerId()) != null) {
                                    activeStyle.addLayerBelow(boundaryLabelLayer, builder.getBelowLayerId());
                                } else {
                                    activeStyle.addLayer(boundaryLabelLayer);
                                }
                            }
                            visible = true;
                        }
                    });
                });
            });
        });
    }

    protected void createLayers(@NonNull Style style) {
        if (style.getSource(BOUNDARY_LABEL_SOURCE_ID) != null) {
            BOUNDARY_LABEL_SOURCE_ID = UUID.randomUUID().toString();
        }
        createBoundaryLabelSource();

        if (style.getLayer(BOUNDARY_LABEL_LAYER_ID) != null) {
            BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();
        }
        createBoundaryLabelLayer(builder);
    }

    private FeatureCollection calculateCenterPoints(@NonNull FeatureCollection featureCollection) {
        ArrayList<Feature> centerPoints = new ArrayList<>();

        List<Feature> featureList = featureCollection.features();
        if (featureList != null) {
            for (Feature feature : featureList) {
                Geometry featureGeometry = feature.geometry();
                if (featureGeometry != null) {
                    Point featurePoint;
                    if (featureGeometry instanceof Point) {
                        featurePoint = (Point) featureGeometry;
                    } else {
                        featurePoint = getCenter(featureGeometry);
                    }

                    centerPoints.add(Feature.fromGeometry(featurePoint, feature.properties()));
                }
            }
        }

        return FeatureCollection.fromFeatures(centerPoints);
    }

    private Point getCenter(@NonNull Geometry featureGeometry) {
        double[] bbox = TurfMeasurement.bbox(featureGeometry);
        LatLng centerLatLng = LatLngBounds.from(bbox[3], bbox[2], bbox[1], bbox[0]).getCenter();
        return Point.fromLngLat(centerLatLng.getLongitude(), centerLatLng.getLatitude());
    }

    protected ArrayList<Layer> getLayers(@NonNull Style style) {
        ArrayList<Layer> layers = new ArrayList<>();
        Layer layer = style.getLayer(BOUNDARY_LABEL_LAYER_ID);
        if (layer != null) {
            layers.add(layer);
        }
        return layers;
    }

    public void enableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        mapboxMap.getStyle(style -> {
            for (Layer layer : getLayers(style)) {
                if (layer != null && NONE.equals(layer.getVisibility().getValue())) {
                    layer.setProperties(PropertyFactory.visibility(VISIBLE));
                    visible = true;
                }
            }
        });
    }

    public void disableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        mapboxMap.getStyle(style -> {
            for (Layer layer : getLayers(style)) {
                if (layer != null && VISIBLE.equals(layer.getVisibility().getValue())) {
                    layer.setProperties(PropertyFactory.visibility(NONE));
                    visible = false;
                }
            }
        });
    }

    @NonNull
    public String[] getLayerIds() {
        return new String[]{BOUNDARY_LABEL_LAYER_ID, BOUNDARY_LINE_LAYER_ID};
    }

    public boolean removeLayerOnMap(@NonNull MapboxMap mapboxMap) {
        Style style = mapboxMap.getStyle();
        if (style != null && style.isFullyLoaded()) {
            return removeLayerOnMap(style);
        }
        return false;
    }

    public boolean removeLayerOnMap(@NonNull Style style) {
        setRemoved(true);
        if (style.isFullyLoaded()) {
            removeLayers(style);
            removeSources(style);
            return true;
        } else {
            Log.e(TAG, "Could not remove layers/sources because style is not fully loaded");
            return false;
        }
    }

    protected void removeLayers(@NonNull Style style) {
        if (boundaryLabelLayer != null && style.getLayer(BOUNDARY_LABEL_LAYER_ID) != null) {
            style.removeLayer(BOUNDARY_LABEL_LAYER_ID);
        }
    }

    protected void removeSources(@NonNull Style style) {
        if (boundaryLabelsSource != null && style.getSource(BOUNDARY_LABEL_SOURCE_ID) != null) {
            style.removeSource(BOUNDARY_LABEL_SOURCE_ID);
        }
    }

    public void updateFeatures(@NonNull FeatureCollection featureCollection) {
        this.builder.setFeatureCollection(featureCollection);

        if (boundaryLabelLayer != null) {
            executorService.execute(() -> {
                FeatureCollection boundaryCenterFeatures = calculateCenterPoints(builder.getFeatureCollection());
                mainHandler.post(() -> {
                    if (boundaryLabelsSource != null) {
                        boundaryLabelsSource.setGeoJson(boundaryCenterFeatures);
                    }
                });
            });
        }
    }

    public FeatureCollection getFeatureCollection() {
        return this.builder.getFeatureCollection();
    }

    public void setFeatureCollection(FeatureCollection featureCollection) {
        this.builder.setFeatureCollection(featureCollection);
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    // Builder Pattern
    public static class Builder {

        private FeatureCollection featureCollection;
        private String labelProperty;
        private float labelTextSize;
        private int labelColorInt;
        private Expression labelTextSizeExpression;
        private String belowLayerId;
        private Float[] textOffSetTuple;

        public Builder(@NonNull FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        public LabelLayer build() {
            return new LabelLayer(this);
        }

        public FeatureCollection getFeatureCollection() {
            return featureCollection;
        }

        public Builder setFeatureCollection(FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
            return this;
        }

        public String getLabelProperty() {
            return labelProperty;
        }

        public Builder setLabelProperty(String labelProperty) {
            this.labelProperty = labelProperty;
            return this;
        }

        public float getLabelTextSize() {
            return labelTextSize;
        }

        public Builder setLabelTextSize(float labelTextSize) {
            this.labelTextSize = labelTextSize;
            return this;
        }

        public int getLabelColorInt() {
            return labelColorInt;
        }

        public Builder setLabelColorInt(int labelColorInt) {
            this.labelColorInt = labelColorInt;
            return this;
        }

        public Expression getLabelTextSizeExpression() {
            return labelTextSizeExpression;
        }

        public Builder setLabelTextSizeExpression(Expression labelTextSizeExpression) {
            this.labelTextSizeExpression = labelTextSizeExpression;
            return this;
        }

        public String getBelowLayerId() {
            return belowLayerId;
        }

        public Builder setBelowLayerId(String belowLayerId) {
            this.belowLayerId = belowLayerId;
            return this;
        }

        public Float[] getTextOffSetTuple() {
            return textOffSetTuple;
        }

        public Builder setTextOffSetTuple(Float[] textOffSetTuple) {
            this.textOffSetTuple = textOffSetTuple;
            return this;
        }
    }
}
//package org.smartregister.reveal.layer;
//
//
//import android.os.AsyncTask;
//
//import android.util.Log;
//
//import com.mapbox.geojson.Feature;
//import com.mapbox.geojson.FeatureCollection;
//import com.mapbox.geojson.Geometry;
//import com.mapbox.geojson.Point;
//import com.mapbox.mapboxsdk.geometry.LatLng;
//import com.mapbox.mapboxsdk.geometry.LatLngBounds;
//import com.mapbox.mapboxsdk.maps.MapboxMap;
//import com.mapbox.mapboxsdk.maps.Style;
//import com.mapbox.mapboxsdk.style.expressions.Expression;
//import com.mapbox.mapboxsdk.style.layers.Layer;
//import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
//import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
//import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
//import com.mapbox.turf.TurfMeasurement;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
//import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
//
//import androidx.annotation.NonNull;
//
//import io.ona.kujaku.callables.AsyncTaskCallable;
//import io.ona.kujaku.layers.KujakuLayer;
//import io.ona.kujaku.listeners.OnFinishedListener;
//import io.ona.kujaku.tasks.GenericAsyncTask;
//
///**
// * This layer enables one to add labelled foci boundaries to the {@link io.ona.kujaku.views.KujakuMapView}
// * <p>
// * Sample usage:
// * <code>
// * <p>
// * BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
// * .setLabelProperty("name")
// * .setLabelTextSize(20f)
// * .setLabelColorInt(Color.RED)
// * .setBoundaryColor(Color.RED)
// * .setBoundaryWidth(6f);
// * <p>
// * kujakuMapView.addLayer(builder.build());
// * </code>
// * <p>
// * Created by Ephraim Kigamba - ekigamba@ona.io on 18/02/2019
// */
//public class LabelLayer extends KujakuLayer {
//
//    private static final String TAG = LabelLayer.class.getName();
//
//    protected LabelLayer.Builder builder;
//
//    protected String BOUNDARY_LABEL_SOURCE_ID = UUID.randomUUID().toString();
//    protected String BOUNDARY_LINE_LAYER_ID = UUID.randomUUID().toString();
//    protected String BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();
//
//    private GeoJsonSource boundaryLabelsSource;
//
//    private SymbolLayer boundaryLabelLayer;
//
//    LabelLayer(@NonNull LabelLayer.Builder builder) {
//        this.builder = builder;
//    }
//
//    private void createBoundaryLabelLayer(@NonNull LabelLayer.Builder builder) {
//
//        boundaryLabelLayer = new SymbolLayer(BOUNDARY_LABEL_LAYER_ID, BOUNDARY_LABEL_SOURCE_ID)
//                .withProperties(
//                        PropertyFactory.textField(Expression.toString(Expression.get(builder.getLabelProperty()))),
//                        PropertyFactory.textPadding(35f),
//                        PropertyFactory.textColor(builder.getLabelColorInt()),
//                        PropertyFactory.textAllowOverlap(true),
//                        PropertyFactory.textSize(Expression.interpolate(
//                                Expression.linear(),
//                                Expression.zoom(),          // Use zoom level as the input
//                                Expression.stop(14, 2f),     // At zoom level 5, text size will be 12
//                                Expression.stop(17, 8f),    // At zoom level 10, text size will be 24
//                                Expression.stop(19, 12f)     // At zoom level 15, text size will be 36
//                        )),
//                        PropertyFactory.textOpacity(Expression.interpolate(
//                                Expression.linear(),
//                                Expression.zoom(),
//                                Expression.stop(17.5, 0f),  // When zoom < 17, opacity is 0 (invisible)
//                                Expression.stop(18.5, 1f)   // When zoom >= 17, opacity is 1 (fully visible)
//                        )),
//                        PropertyFactory.textOffset(this.builder.getTextOffSetTuple())
//                );
//
//
//    }
//
//
//
//    private void createBoundaryLabelSource() {
//        boundaryLabelsSource = new GeoJsonSource(BOUNDARY_LABEL_SOURCE_ID);
//    }
//
//
//    @Override
//    public void addLayerToMap(@NonNull MapboxMap mapboxMap) {
//
//        createLayers(mapboxMap);
//
//        GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
//            @Override
//            public Object[] call() throws Exception {
//                return new Object[]{calculateCenterPoints(builder.getFeatureCollection())};
//            }
//        });
//
//        genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
//            @Override
//            public void onSuccess(Object[] objects) {
//                FeatureCollection boundaryCenterFeatures = (FeatureCollection) objects[0];
//
//                boundaryLabelsSource.setGeoJson(boundaryCenterFeatures);
//
//                mapboxMap.getStyle().addSource(boundaryLabelsSource);
//
//                if (builder.getBelowLayerId() != null) {
//                    addLayersBelow(mapboxMap);
//                } else {
//                    addLayers(mapboxMap);
//                }
//
//                visible = true;
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.e(TAG, Log.getStackTraceString(e));
//            }
//        });
//
//        genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }
//
//    protected void createLayers(@NonNull MapboxMap mapboxMap) {
//        // Create the sources
//        if (mapboxMap.getStyle().getSource(BOUNDARY_LABEL_SOURCE_ID) != null) {
//            BOUNDARY_LABEL_SOURCE_ID = UUID.randomUUID().toString();
//        }
//        createBoundaryLabelSource();
//
//        // Create the layers
//        if (mapboxMap.getStyle().getLayer(BOUNDARY_LABEL_LAYER_ID) != null) {
//            BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();
//        }
//        createBoundaryLabelLayer(builder);
//
//    }
//
//    protected void addLayersBelow(@NonNull MapboxMap mapboxMap) {
//        mapboxMap.getStyle().addLayerBelow(boundaryLabelLayer, builder.getBelowLayerId());
//    }
//
//    protected void addLayers(@NonNull MapboxMap mapboxMap) {
//        mapboxMap.getStyle().addLayer(boundaryLabelLayer);
//    }
//
//    private FeatureCollection calculateCenterPoints(@NonNull FeatureCollection featureCollection) {
//        ArrayList<Feature> centerPoints = new ArrayList<>();
//
//        List<Feature> featureList = featureCollection.features();
//        if (featureList != null) {
//            for (Feature feature : featureList) {
//                Geometry featureGeometry = feature.geometry();
//                if (featureGeometry != null) {
//                    Point featurePoint;
//                    if (featureGeometry instanceof Point) {
//                        featurePoint = (Point) featureGeometry;
//                    } else {
//                        featurePoint = getCenter(featureGeometry);
//                    }
//
//                    centerPoints.add(Feature.fromGeometry(featurePoint, feature.properties()));
//                }
//            }
//        }
//
//        return FeatureCollection.fromFeatures(centerPoints);
//    }
//
//    /**
//     * Generates the center from the {@link Geometry} of a given {@link Feature} for {@link Geometry}
//     * of types {@link com.mapbox.geojson.MultiPolygon}, {@link com.mapbox.geojson.Polygon} and
//     * {@link com.mapbox.geojson.MultiPoint}
//     *
//     * @param featureGeometry
//     * @return
//     */
//    private Point getCenter(@NonNull Geometry featureGeometry) {
//        double[] bbox = TurfMeasurement.bbox(featureGeometry);
//
//        LatLng centerLatLng  = LatLngBounds.from(bbox[3], bbox[2], bbox[1], bbox[0]).getCenter();
//        return Point.fromLngLat(centerLatLng.getLongitude(), centerLatLng.getLatitude());
//    }
//
//    /**
//     * Return a list of Layers
//     * @param mapboxMap
//     * @return
//     */
//    protected ArrayList<Layer> getLayers(@NonNull MapboxMap mapboxMap) {
//        ArrayList<Layer> layers = new ArrayList<Layer>();
//        layers.add(mapboxMap.getStyle().getLayerAs(BOUNDARY_LABEL_LAYER_ID));
//
//        return layers;
//    }
//
//    @Override
//    public void enableLayerOnMap(@NonNull MapboxMap mapboxMap) {
//        for (Layer layer: getLayers(mapboxMap)) {
//            if (layer != null && NONE.equals(layer.getVisibility().getValue())) {
//                layer.setProperties(PropertyFactory.visibility(VISIBLE));
//                visible = true;
//            }
//        }
//    }
//
//    @Override
//    public void disableLayerOnMap(@NonNull MapboxMap mapboxMap) {
//        for (Layer layer: getLayers(mapboxMap)) {
//            if (layer != null && VISIBLE.equals(layer.getVisibility().getValue())) {
//                layer.setProperties(PropertyFactory.visibility(NONE));
//                visible = false;
//            }
//        }
//    }
//
//    @Override @NonNull
//    public String[] getLayerIds() {
//        return new String[] {BOUNDARY_LABEL_LAYER_ID, BOUNDARY_LINE_LAYER_ID};
//    }
//
//    @Override
//    public boolean removeLayerOnMap(@NonNull MapboxMap mapboxMap) {
//        setRemoved(true);
//
//        // Remove the layers & sources
//        Style style = mapboxMap.getStyle();
//        if (style != null && style.isFullyLoaded()) {
//            removeLayers(style) ;
//            removeSources(style);
//
//            return true;
//        } else {
//            Log.e(TAG, "Could not remove the layers & source because the the style is null or not fully loaded");
//            return false;
//        }
//    }
//
//    protected void removeLayers(@NonNull Style style) {
//        style.removeLayer(boundaryLabelLayer);
//    }
//
//    protected void removeSources(@NonNull Style style) {
//        style.removeSource(boundaryLabelsSource);
//    }
//
//    @Override
//    public void updateFeatures(@NonNull FeatureCollection featureCollection) {
//        this.builder.setFeatureCollection(featureCollection);
//
//        if (boundaryLabelLayer != null) {
//            GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
//                @Override
//                public Object[] call() throws Exception {
//                    return new Object[]{calculateCenterPoints(builder.getFeatureCollection())};
//                }
//            });
//
//            genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
//                @Override
//                public void onSuccess(Object[] objects) {
//                    FeatureCollection boundaryCenterFeatures = (FeatureCollection) objects[0];
//
//                    boundaryLabelsSource.setGeoJson(boundaryCenterFeatures);
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    Log.e(TAG, Log.getStackTraceString(e));
//                }
//            });
//
//            genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }
//    }
//
//    @Override
//    public FeatureCollection getFeatureCollection() {
//        return this.builder.getFeatureCollection();
//    }
//
//    public void setFeatureCollection(FeatureCollection featureCollection){
//        this.builder.setFeatureCollection(featureCollection);
//    }
//
//    public static class Builder extends KujakuLayer.Builder<LabelLayer, Builder> {
//
//        private Float[] textOffSetTuple;
//        public Builder(@NonNull FeatureCollection featureCollection) {
//            super(featureCollection);
//        }
//
//        /** The solution for the unchecked cast warning. */
//        public Builder getThis() {
//
//            return this;
//        }
//
//        public LabelLayer build() {
//            return new LabelLayer(this);
//        }
//
//        public String getLabelProperty(){
//            return this.labelProperty;
//        }
//
//        public float getLabelTextSize(){
//            return this.labelTextSize;
//        }
//
//        public int getLabelColorInt(){
//            return this.labelColorInt;
//        }
//
//        public Expression getLabelTextSizeExpression(){
//            return this.labelTextSizeExpression;
//        }
//
//        public void setFeatureCollection(FeatureCollection featureCollection){
//            this.featureCollection = featureCollection;
//        }
//        public String getBelowLayerId(){
//            return this.belowLayerId;
//        }
//        public Float[] getTextOffSetTuple(){
//            return this.textOffSetTuple;
//        }
//        public LabelLayer.Builder setTextOffSetTuple(Float[] textOffSetTuple){
//            this.textOffSetTuple = textOffSetTuple;
//            return getThis();
//        }
//    }
//}
