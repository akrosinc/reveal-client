package org.smartregister.reveal.layer;


import android.os.AsyncTask;

import android.util.Log;

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

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;

import androidx.annotation.NonNull;

import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.layers.KujakuLayer;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.tasks.GenericAsyncTask;

/**
 * This layer enables one to add labelled foci boundaries to the {@link io.ona.kujaku.views.KujakuMapView}
 * <p>
 * Sample usage:
 * <code>
 * <p>
 * BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
 * .setLabelProperty("name")
 * .setLabelTextSize(20f)
 * .setLabelColorInt(Color.RED)
 * .setBoundaryColor(Color.RED)
 * .setBoundaryWidth(6f);
 * <p>
 * kujakuMapView.addLayer(builder.build());
 * </code>
 * <p>
 * Created by Ephraim Kigamba - ekigamba@ona.io on 18/02/2019
 */
public class LabelLayer extends KujakuLayer {

    private static final String TAG = LabelLayer.class.getName();

    protected LabelLayer.Builder builder;

    protected String BOUNDARY_LABEL_SOURCE_ID = UUID.randomUUID().toString();
    protected String BOUNDARY_LINE_LAYER_ID = UUID.randomUUID().toString();
    protected String BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();

    private GeoJsonSource boundaryLabelsSource;

    private SymbolLayer boundaryLabelLayer;

    LabelLayer(@NonNull LabelLayer.Builder builder) {
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
                                Expression.zoom(),          // Use zoom level as the input
                                Expression.stop(14, 2f),     // At zoom level 5, text size will be 12
                                Expression.stop(17, 8f),    // At zoom level 10, text size will be 24
                                Expression.stop(19, 12f)     // At zoom level 15, text size will be 36
                        )),
                        PropertyFactory.textOpacity(Expression.interpolate(
                                Expression.linear(),
                                Expression.zoom(),
                                Expression.stop(17.5, 0f),  // When zoom < 17, opacity is 0 (invisible)
                                Expression.stop(18.5, 1f)   // When zoom >= 17, opacity is 1 (fully visible)
                        )),
                        PropertyFactory.textOffset(this.builder.getTextOffSetTuple())
                );

//        if (builder.getLabelTextSize() != 0f) {
//            boundaryLabelLayer.setProperties(PropertyFactory.textSize(builder.getLabelTextSize()));
//        }

//        if (builder.getLabelTextSizeExpression() != null) {
//            boundaryLabelLayer.setProperties(PropertyFactory.textSize(builder.getLabelTextSizeExpression()));
//        }
    }



    private void createBoundaryLabelSource() {
        boundaryLabelsSource = new GeoJsonSource(BOUNDARY_LABEL_SOURCE_ID);
    }


    @Override
    public void addLayerToMap(@NonNull MapboxMap mapboxMap) {

        createLayers(mapboxMap);

        GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
            @Override
            public Object[] call() throws Exception {
                return new Object[]{calculateCenterPoints(builder.getFeatureCollection())};
            }
        });

        genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onSuccess(Object[] objects) {
                FeatureCollection boundaryCenterFeatures = (FeatureCollection) objects[0];

                boundaryLabelsSource.setGeoJson(boundaryCenterFeatures);

                mapboxMap.getStyle().addSource(boundaryLabelsSource);

                if (builder.getBelowLayerId() != null) {
                    addLayersBelow(mapboxMap);
                } else {
                    addLayers(mapboxMap);
                }

                visible = true;
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        });

        genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void createLayers(@NonNull MapboxMap mapboxMap) {
        // Create the sources
        if (mapboxMap.getStyle().getSource(BOUNDARY_LABEL_SOURCE_ID) != null) {
            BOUNDARY_LABEL_SOURCE_ID = UUID.randomUUID().toString();
        }
        createBoundaryLabelSource();

        // Create the layers
        if (mapboxMap.getStyle().getLayer(BOUNDARY_LABEL_LAYER_ID) != null) {
            BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();
        }
        createBoundaryLabelLayer(builder);

    }

    protected void addLayersBelow(@NonNull MapboxMap mapboxMap) {
        mapboxMap.getStyle().addLayerBelow(boundaryLabelLayer, builder.getBelowLayerId());
    }

    protected void addLayers(@NonNull MapboxMap mapboxMap) {
        mapboxMap.getStyle().addLayer(boundaryLabelLayer);
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

    /**
     * Generates the center from the {@link Geometry} of a given {@link Feature} for {@link Geometry}
     * of types {@link com.mapbox.geojson.MultiPolygon}, {@link com.mapbox.geojson.Polygon} and
     * {@link com.mapbox.geojson.MultiPoint}
     *
     * @param featureGeometry
     * @return
     */
    private Point getCenter(@NonNull Geometry featureGeometry) {
        double[] bbox = TurfMeasurement.bbox(featureGeometry);

        LatLng centerLatLng  = LatLngBounds.from(bbox[3], bbox[2], bbox[1], bbox[0]).getCenter();
        return Point.fromLngLat(centerLatLng.getLongitude(), centerLatLng.getLatitude());
    }

    /**
     * Return a list of Layers
     * @param mapboxMap
     * @return
     */
    protected ArrayList<Layer> getLayers(@NonNull MapboxMap mapboxMap) {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        layers.add(mapboxMap.getStyle().getLayerAs(BOUNDARY_LABEL_LAYER_ID));

        return layers;
    }

    @Override
    public void enableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        for (Layer layer: getLayers(mapboxMap)) {
            if (layer != null && NONE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(PropertyFactory.visibility(VISIBLE));
                visible = true;
            }
        }
    }

    @Override
    public void disableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        for (Layer layer: getLayers(mapboxMap)) {
            if (layer != null && VISIBLE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(PropertyFactory.visibility(NONE));
                visible = false;
            }
        }
    }

    @Override @NonNull
    public String[] getLayerIds() {
        return new String[] {BOUNDARY_LABEL_LAYER_ID, BOUNDARY_LINE_LAYER_ID};
    }

    @Override
    public boolean removeLayerOnMap(@NonNull MapboxMap mapboxMap) {
        setRemoved(true);

        // Remove the layers & sources
        Style style = mapboxMap.getStyle();
        if (style != null && style.isFullyLoaded()) {
            removeLayers(style) ;
            removeSources(style);

            return true;
        } else {
            Log.e(TAG, "Could not remove the layers & source because the the style is null or not fully loaded");
            return false;
        }
    }

    protected void removeLayers(@NonNull Style style) {
        style.removeLayer(boundaryLabelLayer);
    }

    protected void removeSources(@NonNull Style style) {
        style.removeSource(boundaryLabelsSource);
    }

    @Override
    public void updateFeatures(@NonNull FeatureCollection featureCollection) {
        this.builder.setFeatureCollection(featureCollection);

        if (boundaryLabelLayer != null) {
            GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
                @Override
                public Object[] call() throws Exception {
                    return new Object[]{calculateCenterPoints(builder.getFeatureCollection())};
                }
            });

            genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
                @Override
                public void onSuccess(Object[] objects) {
                    FeatureCollection boundaryCenterFeatures = (FeatureCollection) objects[0];

                    boundaryLabelsSource.setGeoJson(boundaryCenterFeatures);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            });

            genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public FeatureCollection getFeatureCollection() {
        return this.builder.getFeatureCollection();
    }

    public void setFeatureCollection(FeatureCollection featureCollection){
        this.builder.setFeatureCollection(featureCollection);
    }

    public static class Builder extends KujakuLayer.Builder<LabelLayer, Builder> {

        private Float[] textOffSetTuple;
        public Builder(@NonNull FeatureCollection featureCollection) {
            super(featureCollection);
        }

        /** The solution for the unchecked cast warning. */
        public Builder getThis() {

            return this;
        }

        public LabelLayer build() {
            return new LabelLayer(this);
        }

        public String getLabelProperty(){
            return this.labelProperty;
        }

        public float getLabelTextSize(){
            return this.labelTextSize;
        }

        public int getLabelColorInt(){
            return this.labelColorInt;
        }

        public Expression getLabelTextSizeExpression(){
            return this.labelTextSizeExpression;
        }

        public void setFeatureCollection(FeatureCollection featureCollection){
            this.featureCollection = featureCollection;
        }
        public String getBelowLayerId(){
            return this.belowLayerId;
        }
        public Float[] getTextOffSetTuple(){
            return this.textOffSetTuple;
        }
        public LabelLayer.Builder setTextOffSetTuple(Float[] textOffSetTuple){
            this.textOffSetTuple = textOffSetTuple;
            return getThis();
        }
    }
}
