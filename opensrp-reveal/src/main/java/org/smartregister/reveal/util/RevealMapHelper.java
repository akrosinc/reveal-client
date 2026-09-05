package org.smartregister.reveal.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.TileSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.reveal.R;
import org.smartregister.reveal.repository.RevealMappingHelper;
import org.smartregister.reveal.util.Constants.StructureType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static org.smartregister.reveal.util.Constants.CONFIGURATION.DEFAULT_GEO_JSON_CIRCLE_SIDES;
import static org.smartregister.reveal.util.Constants.CONFIGURATION.DEFAULT_INDEX_CASE_CIRCLE_RADIUS_IN_METRES;
import static org.smartregister.reveal.util.Constants.CONFIGURATION.INDEX_CASE_CIRCLE_RADIUS_IN_METRES;
import static org.smartregister.reveal.util.Constants.CONFIGURATION.OUTSIDE_OPERATIONAL_AREA_MASK_OPACITY;
import static org.smartregister.reveal.util.Constants.GeoJSON.IS_INDEX_CASE;
import static org.smartregister.reveal.util.Constants.GeoJSON.TYPE;
import static org.smartregister.reveal.util.Utils.createCircleFeature;
import static org.smartregister.reveal.util.Utils.getGlobalConfig;

/**
 * Created by samuelgithengi on 2/20/19.
 */
public class RevealMapHelper {

    private static final String LARVAL_BREEDING_ICON = "larval-breeding-icon";

    private static final String MOSQUITO_COLLECTION_ICON = "mosquito-collection-icon";

    private static final String INDEX_CASE_TARGET_ICON = "index-case-target-icon";

    private static final String POTENTIAL_AREA_OF_TRANSMISSION_ICON = "potential-area-of-transmission-icon";

    public static final String LARVAL_BREEDING_LAYER = "larval-breeding-layer";

    public static final String MOSQUITO_COLLECTION_LAYER = "mosquito-collection-layer";

    public static final String INDEX_CASE_SYMBOL_LAYER = "index-case-symbol-layer";

    public static final String INDEX_CASE_LINE_LAYER = "index-case-line-layer";

    public static final String POTENTIAL_AREA_OF_TRANSMISSION_LAYER = "potential-area-of-transmission-layer";

    public static final String OUT_OF_BOUNDARY_LAYER = "out-of-boundary-layer";

    public static final String OUT_OF_BOUNDARY_SOURCE = "out-of-boundary-source";

    private static final String INDEX_CASE_SOURCE = "index_case_source";

    private Location indexCaseLocation = null;

    private GeoJsonSource indexCaseSource;

    private float radius = Float.valueOf(getGlobalConfig(INDEX_CASE_CIRCLE_RADIUS_IN_METRES, DEFAULT_INDEX_CASE_CIRCLE_RADIUS_IN_METRES.toString()));

    private LineLayer indexCaseLineLayer;

    private Feature circleFeature;

    public static void addCustomLayers(@NonNull Style mMapboxMapStyle, Context context) {

        Expression dynamicIconSize = interpolate(linear(), zoom(),
                literal(10.5), literal(0),
                literal(13.98f), literal(0.3f),
                literal(17.79f), literal(1.5f),
                literal(18.8f), literal(2));

        // mosquito collection symbol layer
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_mosquito);
        mMapboxMapStyle.addImage(MOSQUITO_COLLECTION_ICON, icon);
        SymbolLayer symbolLayer = new SymbolLayer(MOSQUITO_COLLECTION_LAYER, context.getString(R.string.reveal_datasource_name));
        symbolLayer.setProperties(
                iconImage(MOSQUITO_COLLECTION_ICON),
                iconSize(dynamicIconSize),
                iconAllowOverlap(true));
        symbolLayer.setFilter(eq(get(TYPE), StructureType.MOSQUITO_COLLECTION_POINT));
        mMapboxMapStyle.addLayer(symbolLayer);

        // larval breeding symbol layer
        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_breeding);
        mMapboxMapStyle.addImage(LARVAL_BREEDING_ICON, icon);
        symbolLayer = new SymbolLayer(LARVAL_BREEDING_LAYER, context.getString(R.string.reveal_datasource_name));
        symbolLayer.setProperties(
                iconImage(LARVAL_BREEDING_ICON),
                iconSize(dynamicIconSize),
                iconAllowOverlap(true));
        symbolLayer.setFilter(eq(get(TYPE), StructureType.LARVAL_BREEDING_SITE));
        mMapboxMapStyle.addLayer(symbolLayer);

        // Potential Area Of Transmission symbol layer
        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_paot);
        mMapboxMapStyle.addImage(POTENTIAL_AREA_OF_TRANSMISSION_ICON, icon);
        symbolLayer = new SymbolLayer(POTENTIAL_AREA_OF_TRANSMISSION_LAYER, context.getString(R.string.reveal_datasource_name));
        symbolLayer.setProperties(
                iconImage(POTENTIAL_AREA_OF_TRANSMISSION_ICON),
                iconSize(dynamicIconSize),
                iconAllowOverlap(true));
        symbolLayer.setFilter(eq(get(TYPE), StructureType.POTENTIAL_AREA_OF_TRANSMISSION));
        mMapboxMapStyle.addLayer(symbolLayer);
    }

    public void addIndexCaseLayers(MapboxMap mapboxMap, Context context, FeatureCollection featureCollection) {
        Feature indexCase = getIndexCase(featureCollection);
        if (indexCase == null) {
            return; // no need to continue if index case does not exist
        }

        Style mMapboxMapStyle = mapboxMap.getStyle();

        // index case symbol layer
        Expression dynamicIconSize = interpolate(linear(), zoom(),
                literal(11.98f), literal(1),
                literal(17.79f), literal(3f),
                literal(18.8f), literal(4));

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_index_case_target_icon);
        mMapboxMapStyle.addImage(INDEX_CASE_TARGET_ICON, icon);
        SymbolLayer symbolLayer = new SymbolLayer(INDEX_CASE_SYMBOL_LAYER, context.getString(R.string.reveal_datasource_name));
        symbolLayer.setProperties(iconImage(INDEX_CASE_TARGET_ICON), iconSize(dynamicIconSize),
                iconIgnorePlacement(true), iconAllowOverlap(true));
        symbolLayer.setFilter(eq(get(IS_INDEX_CASE), Boolean.TRUE.toString()));
        mMapboxMapStyle.addLayer(symbolLayer);

        // index case circle layer
        indexCaseLocation = (new RevealMappingHelper()).getCenter(indexCase.geometry().toJson());

        try {
            circleFeature = createCircleFeature(new LatLng(indexCaseLocation.getLatitude(), indexCaseLocation.getLongitude()), radius, DEFAULT_GEO_JSON_CIRCLE_SIDES);
            indexCaseSource = new GeoJsonSource(INDEX_CASE_SOURCE, circleFeature);
            mapboxMap.getStyle().addSource(indexCaseSource);
        } catch (JSONException e) {
            Timber.tag("Reveal Exception").w(e);
        }

        indexCaseLineLayer = new LineLayer(INDEX_CASE_LINE_LAYER, indexCaseSource.getId());
        indexCaseLineLayer.withProperties(
                lineWidth(2f),
                lineColor("#ffffff"),
                lineJoin(Property.LINE_JOIN_ROUND)
        );
        mMapboxMapStyle.addLayer(indexCaseLineLayer);


        updateIndexCaseLayers(mapboxMap, featureCollection, context);
    }

    public void updateIndexCaseLayers(MapboxMap mapboxMap, FeatureCollection featureCollection, Context context) {
        try {
            if (featureCollection != null) {
                Feature indexCase = getIndexCase(featureCollection);
                if (indexCase != null) {
                    // create index case point
                    indexCaseLocation = (new RevealMappingHelper()).getCenter(indexCase.geometry().toJson());
                    JSONObject feature = new JSONObject(indexCase.toJson());
                    JSONObject geometry = new JSONObject();
                    geometry.put("type", "Point");
                    geometry.put("coordinates", new JSONArray(new Double[]{indexCaseLocation.getLongitude(), indexCaseLocation.getLatitude()}));
                    feature.put("geometry", geometry);
                    circleFeature = createCircleFeature(new LatLng(indexCaseLocation.getLatitude(), indexCaseLocation.getLongitude()), radius, DEFAULT_GEO_JSON_CIRCLE_SIDES);
                    indexCaseSource.setGeoJson(circleFeature);
                } else { // Clear outer circle if there is no index case
                    if (indexCaseSource != null)
                        indexCaseSource.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<>()));
                }
            }
        } catch (JSONException e) {
            Timber.tag("Reveal Exception").w(e);
        }
    }

    public Feature getIndexCase(FeatureCollection featureCollection) {
        Feature indexCase = null;
        for (Feature feature : featureCollection.features()) {
            if (feature.hasProperty(IS_INDEX_CASE) && feature.getBooleanProperty(IS_INDEX_CASE)) {
                indexCase = feature;
                break; // index case already found, no need to proceed
            }
        }
        return indexCase;
    }

    public LineLayer getIndexCaseLineLayer() {
        return indexCaseLineLayer;
    }

    public static void addOutOfBoundaryMask(@NonNull Style mMapboxMapStyle, Feature operationalArea, Feature boundingBoxPolygon, Context context) {

        // create multi polygon
        List<Polygon> polygonList = new ArrayList<>();
        polygonList.add((Polygon) boundingBoxPolygon.geometry());
        if (operationalArea.geometry() instanceof MultiPolygon) {
            polygonList.addAll(((MultiPolygon) operationalArea.geometry()).polygons());
        } else {
            polygonList.add((Polygon) operationalArea.geometry());
        }

        MultiPolygon opAreaMultiPolygon = MultiPolygon.fromPolygons(polygonList);

        // create mask source
        GeoJsonSource outOfBoundarySource = new GeoJsonSource(OUT_OF_BOUNDARY_SOURCE, opAreaMultiPolygon);
        mMapboxMapStyle.addSource(outOfBoundarySource);

        // add mask
        FillLayer maskLayer = new FillLayer(OUT_OF_BOUNDARY_LAYER, outOfBoundarySource.getId());
        maskLayer.withProperties(fillColor(context.getResources().getColor(R.color.outside_area_mask)),
                fillOpacity(OUTSIDE_OPERATIONAL_AREA_MASK_OPACITY));
        mMapboxMapStyle.addLayer(maskLayer);

    }

//    public boolean isMyLocationComponentActive(Context context, ImageButton myLocationButton) {
//        return context.getResources().getDrawable(R.drawable.ic_cross_hair_blue).getConstantState().equals(myLocationButton.getDrawable().getConstantState());
//    }
public static boolean isMyLocationComponentActive(Context context, ImageButton myLocationButton) {
    if (myLocationButton == null || myLocationButton.getDrawable() == null) {
        return false;
    }

    try {
        Drawable expectedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_cross_hair_blue);
        if (expectedDrawable == null || expectedDrawable.getConstantState() == null) {
            return false;
        }

        Drawable currentDrawable = myLocationButton.getDrawable();
        if (currentDrawable.getConstantState() == null) {
            return false;
        }

        return expectedDrawable.getConstantState().equals(currentDrawable.getConstantState());
    } catch (Exception e) {
        return false;
    }
}
    /**
     * Programmatically injects the raster satellite source and layer into Mapbox
     * without Kujaku dependencies.
     */
    public static void addBaseLayers(MapView mapView, Style style, Context context) {
        String sourceId = "mapbox-satellite-raster-source";
        String layerId = "mapbox-satellite-raster-layer";

        if (style.getSource(sourceId) == null) {
            // Online Mapbox Satellite Source
            String mapboxToken = context.getString(R.string.mapbox_access_token);
            String tileUrl = "https://api.mapbox.com/v4/mapbox.satellite/{z}/{x}/{y}.png?access_token=" + mapboxToken;

            TileSet tileSet = new TileSet("2.1.0", tileUrl);
            tileSet.setMinZoom(0);
            tileSet.setMaxZoom(20);

            RasterSource rasterSource = new RasterSource(sourceId, tileSet, 256);
            style.addSource(rasterSource);

            RasterLayer rasterLayer = new RasterLayer(layerId, sourceId);

            // Add layer at the very bottom (index 0) so feature geometries render on top
            if (!style.getLayers().isEmpty()) {
                style.addLayerAt(rasterLayer, 0);
            } else {
                style.addLayer(rasterLayer);
            }
        }
    }

    public List<FeatureCollection> splitMultiPolygons(FeatureCollection multiPolygonFeatureCollection) {
        List<FeatureCollection> polygonFeatureCollections = new ArrayList<>();

        // Iterate through each feature in the FeatureCollection
        for (Feature feature : multiPolygonFeatureCollection.features()) {
            Geometry geometry = feature.geometry();
            if (geometry instanceof MultiPolygon) {
                MultiPolygon multiPolygon = (MultiPolygon) geometry;

                // Iterate through each Polygon in the MultiPolygon
                for (List<List<Point>> polygonCoordinates : multiPolygon.coordinates()) {
                    // The list of rings in the current polygon
                    List<List<Point>> rings = new ArrayList<>(polygonCoordinates);

                    // Create a Polygon from the coordinates
                    Polygon polygon = Polygon.fromLngLats(rings);

                    // Create a new Feature for this Polygon
                    Feature polygonFeature = Feature.fromGeometry(polygon);

                    // Create a new FeatureCollection for this individual Polygon
                    FeatureCollection polygonFeatureCollection = FeatureCollection.fromFeatures(new Feature[]{polygonFeature});

                    // Add to result list
                    polygonFeatureCollections.add(polygonFeatureCollection);
                }
            }
        }

        return polygonFeatureCollections;
    }

    public FeatureCollection splitMultiPolygonsToSingleFeatureCollection(Feature multiPolygonFeatureCollection) {
        List<Feature> polygonFeatures = getFeaturesFromMultiPolygonFeature(multiPolygonFeatureCollection);
        // Create a FeatureCollection containing all the individual Polygon features
        return FeatureCollection.fromFeatures(polygonFeatures);
    }

    public FeatureCollection getFeatureCollectionFromMultiPolygonList(List<Feature> features) {

        List<Feature> featureListOutput = new ArrayList<>();
        for (Feature feature : features) {
            Geometry geometry = feature.geometry();
            if (geometry instanceof MultiPolygon) {
                List<Feature> featuresFromMultiPolygonFeature = getFeaturesFromMultiPolygonFeature(feature);
                featureListOutput.addAll(featuresFromMultiPolygonFeature);
            } else {
                featureListOutput.add(feature);
            }
        }
        return FeatureCollection.fromFeatures(featureListOutput);
    }

    public Map<String, List<Feature>> getFeatureCollectionMapFromMultiPolygonList(List<Feature> features) {

        Map<String, List<Feature>> featureMap = new HashMap<>();
        for (Feature feature : features) {

            if (feature.properties() != null) {
                JsonObject properties = feature.properties();
                if (properties.has("geographicLevel")) {

                    String geographicLevel = properties.get("geographicLevel").getAsString();

                    if (featureMap.containsKey(geographicLevel)) {
                        List<Feature> features1 = featureMap.get(geographicLevel);

                        if (features1 == null) {
                            features1 = new ArrayList<>();
                        }

                        List<Feature> featuresFromMultiPolygonFeature = getFeaturesFromMultiPolygonFeature(feature);

                        features1.addAll(featuresFromMultiPolygonFeature);
                        featureMap.put(geographicLevel, features1);

                    } else {

                        List<Feature> featuresFromMultiPolygonFeature = getFeaturesFromMultiPolygonFeature(feature);

                        List<Feature> features1 = new ArrayList<>(featuresFromMultiPolygonFeature);
                        featureMap.put(geographicLevel, features1);
                    }
                }
            }

        }
        return featureMap;
    }

    private static @NonNull List<Feature> getFeaturesFromMultiPolygonFeature(Feature multiPolygonFeatureCollection) {
        List<Feature> polygonFeatures = new ArrayList<>();

        // Iterate through each feature in the FeatureCollection

        String name = null;
        String geographicLevel = null;
        if (multiPolygonFeatureCollection.properties() != null) {
            JsonObject properties = multiPolygonFeatureCollection.properties();

            if (properties.has("name")) {
                name = properties.get("name").getAsString();
            }
            if (properties.has("geographicLevel")){
                geographicLevel = properties.get("geographicLevel").getAsString();
            }
        }

        Geometry geometry = multiPolygonFeatureCollection.geometry();
        if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;

            int count = 0;
            // Iterate through each Polygon in the MultiPolygon
            for (List<List<Point>> polygonCoordinates : multiPolygon.coordinates()) {
                // Create a Polygon from the coordinates
                Polygon polygon = Polygon.fromLngLats(polygonCoordinates);

                // Create a new Feature for this Polygon
                Feature polygonFeature = Feature.fromGeometry(polygon);

                if (name != null) {
                    polygonFeature.addStringProperty("name", name.concat("_").concat(String.valueOf(count)));
                }

                if (geographicLevel!=null){
                    polygonFeature.addStringProperty("geographicLevel", geographicLevel);
                }
                // Add the polygon feature to the list
                polygonFeatures.add(polygonFeature);
                count++;
            }
        }
        return polygonFeatures;
    }

    public FeatureCollection getLabels(FeatureCollection multiPolygonFeatureCollection) {
        List<Feature> polygonFeatures = new ArrayList<>();

        List<Feature> features = multiPolygonFeatureCollection.features();
        for (Feature feature : features) {
            Geometry geometry = feature.geometry();
            if (geometry instanceof Polygon) {

                Polygon polygon = (Polygon) geometry;
                Point point = calculateCentroid(polygon);
                Feature pointFeature = Feature.fromGeometry(point);
                if (feature.properties() != null && feature.properties().has("added_label")) {
                    pointFeature.addStringProperty("added_label", feature.properties().get("added_label").getAsString());
                    polygonFeatures.add(pointFeature);
                }
            }
        }
        // Create a FeatureCollection containing all the individual Polygon features
        return FeatureCollection.fromFeatures(polygonFeatures);
    }

    public Point calculateCentroid(Polygon polygon) {
        double area = 0.0;
        double C_x = 0.0;
        double C_y = 0.0;
        int n = polygon.coordinates().get(0).size(); // Assuming it's a single polygon

        List<Point> points = polygon.coordinates().get(0); // Get the outer ring coordinates

        for (int i = 0; i < n; i++) {
            Point current = points.get(i);
            Point next = points.get((i + 1) % n); // Wrap around to the first point

            double x0 = current.longitude();
            double y0 = current.latitude();
            double x1 = next.longitude();
            double y1 = next.latitude();

            double a = x0 * y1 - x1 * y0;
            area += a;
            C_x += (x0 + x1) * a;
            C_y += (y0 + y1) * a;
        }

        area *= 0.5;
        C_x /= (6.0 * area);
        C_y /= (6.0 * area);

        return Point.fromLngLat(C_x, C_y); // Return the centroid as a Point
    }
}
//package org.smartregister.reveal.util;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.location.Location;
//import android.widget.ImageButton;
//
//import androidx.annotation.NonNull;
//
//import com.google.gson.JsonObject;
//import com.mapbox.geojson.Feature;
//import com.mapbox.geojson.FeatureCollection;
//import com.mapbox.geojson.Geometry;
//import com.mapbox.geojson.MultiPolygon;
//import com.mapbox.geojson.Point;
//import com.mapbox.geojson.Polygon;
//import com.mapbox.mapboxsdk.geometry.LatLng;
//import com.mapbox.mapboxsdk.maps.MapView;
//import com.mapbox.mapboxsdk.maps.MapboxMap;
//import com.mapbox.mapboxsdk.maps.Style;
//import com.mapbox.mapboxsdk.style.expressions.Expression;
//import com.mapbox.mapboxsdk.style.layers.FillLayer;
//import com.mapbox.mapboxsdk.style.layers.LineLayer;
//import com.mapbox.mapboxsdk.style.layers.Property;
//import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
//import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.smartregister.reveal.R;
//import org.smartregister.reveal.repository.RevealMappingHelper;
//import org.smartregister.reveal.util.Constants.StructureType;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//import timber.log.Timber;
//
//import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
//import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
//import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
//import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
//import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
//import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
//import static org.smartregister.reveal.util.Constants.CONFIGURATION.DEFAULT_GEO_JSON_CIRCLE_SIDES;
//import static org.smartregister.reveal.util.Constants.CONFIGURATION.DEFAULT_INDEX_CASE_CIRCLE_RADIUS_IN_METRES;
//import static org.smartregister.reveal.util.Constants.CONFIGURATION.INDEX_CASE_CIRCLE_RADIUS_IN_METRES;
//import static org.smartregister.reveal.util.Constants.CONFIGURATION.OUTSIDE_OPERATIONAL_AREA_MASK_OPACITY;
//import static org.smartregister.reveal.util.Constants.GeoJSON.IS_INDEX_CASE;
//import static org.smartregister.reveal.util.Constants.GeoJSON.TYPE;
//import static org.smartregister.reveal.util.Utils.createCircleFeature;
//import static org.smartregister.reveal.util.Utils.getGlobalConfig;
//
///**
// * Created by samuelgithengi on 2/20/19.
// */
//public class RevealMapHelper
//{
//
//    private static final String LARVAL_BREEDING_ICON = "larval-breeding-icon";
//
//    private static final String MOSQUITO_COLLECTION_ICON = "mosquito-collection-icon";
//
//    private static final String INDEX_CASE_TARGET_ICON = "index-case-target-icon";
//
//    private static final String POTENTIAL_AREA_OF_TRANSMISSION_ICON = "potential-area-of-transmission-icon";
//
//    public static final String LARVAL_BREEDING_LAYER = "larval-breeding-layer";
//
//    public static final String MOSQUITO_COLLECTION_LAYER = "mosquito-collection-layer";
//
//    public static final String INDEX_CASE_SYMBOL_LAYER = "index-case-symbol-layer";
//
//    public static final String INDEX_CASE_LINE_LAYER = "index-case-line-layer";
//
//    public static final String POTENTIAL_AREA_OF_TRANSMISSION_LAYER = "potential-area-of-transmission-layer";
//
//    public static final String OUT_OF_BOUNDARY_LAYER = "out-of-boundary-layer";
//
//    public static final String OUT_OF_BOUNDARY_SOURCE = "out-of-boundary-source";
//
//    private static final String INDEX_CASE_SOURCE = "index_case_source";
//
//    private Location indexCaseLocation = null;
//
//    private GeoJsonSource indexCaseSource;
//
//    private float radius = Float.valueOf(getGlobalConfig(INDEX_CASE_CIRCLE_RADIUS_IN_METRES, DEFAULT_INDEX_CASE_CIRCLE_RADIUS_IN_METRES.toString()));
//
//    private LineLayer indexCaseLineLayer;
//
//    private Feature circleFeature;
//
//    public static void addCustomLayers(@NonNull Style mMapboxMapStyle, Context context) {
//
//        Expression dynamicIconSize = interpolate(linear(), zoom(),
//                literal(10.5), literal(0),
//                literal(13.98f), literal(0.3f),
//                literal(17.79f), literal(1.5f),
//                literal(18.8f), literal(2));
//
//        // mosquito collection symbol layer
//        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_mosquito);
//        mMapboxMapStyle.addImage(MOSQUITO_COLLECTION_ICON, icon);
//        SymbolLayer symbolLayer = new SymbolLayer(MOSQUITO_COLLECTION_LAYER, context.getString(R.string.reveal_datasource_name));
//        symbolLayer.setProperties(
//                iconImage(MOSQUITO_COLLECTION_ICON),
//                iconSize(dynamicIconSize),
//                iconAllowOverlap(true));
//        symbolLayer.setFilter(eq(get(TYPE), StructureType.MOSQUITO_COLLECTION_POINT));
//        mMapboxMapStyle.addLayer(symbolLayer);
//
//        // larval breeding symbol layer
//        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_breeding);
//        mMapboxMapStyle.addImage(LARVAL_BREEDING_ICON, icon);
//        symbolLayer = new SymbolLayer(LARVAL_BREEDING_LAYER, context.getString(R.string.reveal_datasource_name));
//        symbolLayer.setProperties(
//                iconImage(LARVAL_BREEDING_ICON),
//                iconSize(dynamicIconSize),
//                iconAllowOverlap(true));
//        symbolLayer.setFilter(eq(get(TYPE), StructureType.LARVAL_BREEDING_SITE));
//        mMapboxMapStyle.addLayer(symbolLayer);
//
//        // Potential Area Of Transmission symbol layer
//        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_paot);
//        mMapboxMapStyle.addImage(POTENTIAL_AREA_OF_TRANSMISSION_ICON, icon);
//        symbolLayer = new SymbolLayer(POTENTIAL_AREA_OF_TRANSMISSION_LAYER, context.getString(R.string.reveal_datasource_name));
//        symbolLayer.setProperties(
//                iconImage(POTENTIAL_AREA_OF_TRANSMISSION_ICON),
//                iconSize(dynamicIconSize),
//                iconAllowOverlap(true));
//        symbolLayer.setFilter(eq(get(TYPE), StructureType.POTENTIAL_AREA_OF_TRANSMISSION));
//        mMapboxMapStyle.addLayer(symbolLayer);
//    }
//
//    public void addIndexCaseLayers(MapboxMap mapboxMap, Context context, FeatureCollection featureCollection) {
//        Feature indexCase = getIndexCase(featureCollection);
//        if (indexCase == null) {
//            return; // no need to continue if index case does not exist
//        }
//
//        Style mMapboxMapStyle = mapboxMap.getStyle();
//
//        // index case symbol layer
//        Expression dynamicIconSize = interpolate(linear(), zoom(),
//                literal(11.98f), literal(1),
//                literal(17.79f), literal(3f),
//                literal(18.8f), literal(4));
//
//        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_index_case_target_icon);
//        mMapboxMapStyle.addImage(INDEX_CASE_TARGET_ICON, icon);
//        SymbolLayer symbolLayer = new SymbolLayer(INDEX_CASE_SYMBOL_LAYER, context.getString(R.string.reveal_datasource_name));
//        symbolLayer.setProperties(iconImage(INDEX_CASE_TARGET_ICON), iconSize(dynamicIconSize),
//                iconIgnorePlacement(true), iconAllowOverlap(true));
//        symbolLayer.setFilter(eq(get(IS_INDEX_CASE), Boolean.TRUE.toString()));
//        mMapboxMapStyle.addLayer(symbolLayer);
//
//        // index case circle layer
//        indexCaseLocation = (new RevealMappingHelper()).getCenter(indexCase.geometry().toJson());
//
//        try {
//            circleFeature = createCircleFeature(new LatLng(indexCaseLocation.getLatitude(), indexCaseLocation.getLongitude()), radius, DEFAULT_GEO_JSON_CIRCLE_SIDES);
//            indexCaseSource = new GeoJsonSource(INDEX_CASE_SOURCE, circleFeature);
//            mapboxMap.getStyle().addSource(indexCaseSource);
//        } catch (JSONException e) {
//            Timber.tag("Reveal Exception").w(e);
//        }
//
//        indexCaseLineLayer = new LineLayer(INDEX_CASE_LINE_LAYER, indexCaseSource.getId());
//        indexCaseLineLayer.withProperties(
//                lineWidth(2f),
//                lineColor("#ffffff"),
//                lineJoin(Property.LINE_JOIN_ROUND)
//        );
//        mMapboxMapStyle.addLayer(indexCaseLineLayer);
//
//
//        updateIndexCaseLayers(mapboxMap, featureCollection, context);
//    }
//
//    public void updateIndexCaseLayers(MapboxMap mapboxMap, FeatureCollection featureCollection, Context context) {
//        try {
//            if (featureCollection != null) {
//                Feature indexCase = getIndexCase(featureCollection);
//                if (indexCase != null) {
//                    // create index case point
//                    indexCaseLocation = (new RevealMappingHelper()).getCenter(indexCase.geometry().toJson());
//                    JSONObject feature = new JSONObject(indexCase.toJson());
//                    JSONObject geometry = new JSONObject();
//                    geometry.put("type", "Point");
//                    geometry.put("coordinates", new JSONArray(new Double[]{indexCaseLocation.getLongitude(), indexCaseLocation.getLatitude()}));
//                    feature.put("geometry", geometry);
//                    circleFeature = createCircleFeature(new LatLng(indexCaseLocation.getLatitude(), indexCaseLocation.getLongitude()), radius, DEFAULT_GEO_JSON_CIRCLE_SIDES);
//                    indexCaseSource.setGeoJson(circleFeature);
//                } else { // Clear outer circle if there is no index case
//                    if (indexCaseSource != null)
//                        indexCaseSource.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<>()));
//                }
//            }
//        } catch (JSONException e) {
//            Timber.tag("Reveal Exception").w(e);
//        }
//    }
//
//    public Feature getIndexCase(FeatureCollection featureCollection) {
//        Feature indexCase = null;
//        for (Feature feature : featureCollection.features()) {
//            if (feature.hasProperty(IS_INDEX_CASE) && feature.getBooleanProperty(IS_INDEX_CASE)) {
//                indexCase = feature;
//                break; // index case already found, no need to proceed
//            }
//        }
//        return indexCase;
//    }
//
//    public LineLayer getIndexCaseLineLayer() {
//        return indexCaseLineLayer;
//    }
//
//    public static void addOutOfBoundaryMask(@NonNull Style mMapboxMapStyle, Feature operationalArea, Feature boundingBoxPolygon, Context context) {
//
//
//        // create multi polygon
//        List<Polygon> polygonList = new ArrayList<>();
//        polygonList.add((Polygon) boundingBoxPolygon.geometry());
//        if (operationalArea.geometry() instanceof MultiPolygon) {
//            polygonList.addAll(((MultiPolygon) operationalArea.geometry()).polygons());
//        } else {
//            polygonList.add((Polygon) operationalArea.geometry());
//        }
//
//        MultiPolygon opAreaMultiPolygon = MultiPolygon.fromPolygons(polygonList);
//
//        // create mask source
//        GeoJsonSource outOfBoundarySource = new GeoJsonSource(OUT_OF_BOUNDARY_SOURCE, opAreaMultiPolygon);
//        mMapboxMapStyle.addSource(outOfBoundarySource);
//
//        // add mask
//        FillLayer maskLayer = new FillLayer(OUT_OF_BOUNDARY_LAYER, outOfBoundarySource.getId());
//        maskLayer.withProperties(fillColor(context.getResources().getColor(R.color.outside_area_mask)),
//                fillOpacity(OUTSIDE_OPERATIONAL_AREA_MASK_OPACITY));
//        mMapboxMapStyle.addLayer(maskLayer);
//
//    }
//
//    public boolean isMyLocationComponentActive(Context context, ImageButton myLocationButton) {
//        return context.getResources().getDrawable(R.drawable.ic_cross_hair_blue).getConstantState().equals(myLocationButton.getDrawable().getConstantState());
//    }
//
////    public static void addBaseLayers(KujakuMapView kujakuMapView, Style style, Context context) {
////        BaseLayerSwitcherPlugin baseLayerSwitcherPlugin = new BaseLayerSwitcherPlugin(kujakuMapView, style);
////        MapBoxLayer mapBoxLayer = new MapBoxLayer();
////        baseLayerSwitcherPlugin.addBaseLayer(mapBoxLayer, true);
////        kujakuMapView.getMbTilesHelper().setMBTileLayers(context, baseLayerSwitcherPlugin);
////
////        baseLayerSwitcherPlugin.show();
////    }
//public static void addBaseLayers(MapView mapView, Style style, Context context) {
//    // 1. Get the path to your offline .mbtiles file
//    String mbtilesPath = context.getDatabasePath("offline_map.mbtiles").getAbsolutePath();
//
//    // 2. Format as sqlite URL scheme for Mapbox
//    String tileUrl = "sqlite://" + mbtilesPath;
//
//    // 3. Create a TileSet and RasterSource
//    com.mapbox.mapboxsdk.style.sources.TileSet tileSet =
//            new com.mapbox.mapboxsdk.style.sources.TileSet("2.1.0", tileUrl);
//
//    com.mapbox.mapboxsdk.style.sources.RasterSource rasterSource =
//            new com.mapbox.mapboxsdk.style.sources.RasterSource("mbtiles-source", tileSet, 256);
//
//    // 4. Add Source and Layer to Mapbox Style
//    if (style.getSource("mbtiles-source") == null) {
//        style.addSource(rasterSource);
//
//        com.mapbox.mapboxsdk.style.layers.RasterLayer rasterLayer =
//                new com.mapbox.mapboxsdk.style.layers.RasterLayer("mbtiles-layer", "mbtiles-source");
//
//        // Add below custom operational area layers so overlays render on top
//        style.addLayerAt(rasterLayer, 0);
//    }
//}
//    public List<FeatureCollection> splitMultiPolygons(FeatureCollection multiPolygonFeatureCollection) {
//        List<FeatureCollection> polygonFeatureCollections = new ArrayList<>();
//
//        // Iterate through each feature in the FeatureCollection
//        for (Feature feature : multiPolygonFeatureCollection.features()) {
//            Geometry geometry = feature.geometry();
//            if (geometry instanceof MultiPolygon) {
//                MultiPolygon multiPolygon = (MultiPolygon) geometry;
//
//                // Iterate through each Polygon in the MultiPolygon
//                for (List<List<Point>> polygonCoordinates : multiPolygon.coordinates()) {
//                    // The list of rings in the current polygon
//                    List<List<Point>> rings = new ArrayList<>(polygonCoordinates);
//
//                    // Create a Polygon from the coordinates
//                    Polygon polygon = Polygon.fromLngLats(rings);
//
//                    // Create a new Feature for this Polygon
//                    Feature polygonFeature = Feature.fromGeometry(polygon);
//
//                    // Create a new FeatureCollection for this individual Polygon
//                    FeatureCollection polygonFeatureCollection = FeatureCollection.fromFeatures(new Feature[]{polygonFeature});
//
//                    // Add to result list
//                    polygonFeatureCollections.add(polygonFeatureCollection);
//                }
//            }
//        }
//
//        return polygonFeatureCollections;
//    }
//
//    public FeatureCollection splitMultiPolygonsToSingleFeatureCollection(Feature multiPolygonFeatureCollection) {
//        List<Feature> polygonFeatures = getFeaturesFromMultiPolygonFeature(multiPolygonFeatureCollection);
//        // Create a FeatureCollection containing all the individual Polygon features
//        return FeatureCollection.fromFeatures(polygonFeatures);
//    }
//
//    public FeatureCollection getFeatureCollectionFromMultiPolygonList(List<Feature> features) {
//
//        List<Feature> featureListOutput = new ArrayList<>();
//        for (Feature feature : features) {
//            Geometry geometry = feature.geometry();
//            if (geometry instanceof MultiPolygon) {
//                List<Feature> featuresFromMultiPolygonFeature = getFeaturesFromMultiPolygonFeature(feature);
//                featureListOutput.addAll(featuresFromMultiPolygonFeature);
//            } else {
//                featureListOutput.add(feature);
//            }
//        }
//        return FeatureCollection.fromFeatures(featureListOutput);
//    }
//
//    public Map<String, List<Feature>> getFeatureCollectionMapFromMultiPolygonList(List<Feature> features) {
//
//        Map<String, List<Feature>> featureMap = new HashMap<>();
//        for (Feature feature : features) {
//
//            if (feature.properties() != null) {
//                JsonObject properties = feature.properties();
//                if (properties.has("geographicLevel")) {
//
//                    String geographicLevel = properties.get("geographicLevel").getAsString();
//
//                    if (featureMap.containsKey(geographicLevel)) {
//                        List<Feature> features1 = featureMap.get(geographicLevel);
//
//                        if (features1 == null) {
//                            features1 = new ArrayList<>();
//                        }
//
//                        List<Feature> featuresFromMultiPolygonFeature = getFeaturesFromMultiPolygonFeature(feature);
//
//                        features1.addAll(featuresFromMultiPolygonFeature);
//                        featureMap.put(geographicLevel, features1);
//
//                    } else {
//
//                        List<Feature> featuresFromMultiPolygonFeature = getFeaturesFromMultiPolygonFeature(feature);
//
//                        List<Feature> features1 = new ArrayList<>(featuresFromMultiPolygonFeature);
//                        featureMap.put(geographicLevel, features1);
//                    }
//                }
//            }
//
//        }
//        return featureMap;
//    }
//
//    private static @NonNull List<Feature> getFeaturesFromMultiPolygonFeature(Feature multiPolygonFeatureCollection) {
//        List<Feature> polygonFeatures = new ArrayList<>();
//
//        // Iterate through each feature in the FeatureCollection
//
//        String name = null;
//        String geographicLevel = null;
//        if (multiPolygonFeatureCollection.properties() != null) {
//            JsonObject properties = multiPolygonFeatureCollection.properties();
//
//            if (properties.has("name")) {
//                name = properties.get("name").getAsString();
//            }
//            if (properties.has("geographicLevel")){
//                geographicLevel = properties.get("geographicLevel").getAsString();
//            }
//        }
//
//        Geometry geometry = multiPolygonFeatureCollection.geometry();
//        if (geometry instanceof MultiPolygon) {
//            MultiPolygon multiPolygon = (MultiPolygon) geometry;
//
//            int count = 0;
//            // Iterate through each Polygon in the MultiPolygon
//            for (List<List<Point>> polygonCoordinates : multiPolygon.coordinates()) {
//                // Create a Polygon from the coordinates
//                Polygon polygon = Polygon.fromLngLats(polygonCoordinates);
//
//                // Create a new Feature for this Polygon
//                Feature polygonFeature = Feature.fromGeometry(polygon);
//
//                if (name != null) {
//                    polygonFeature.addStringProperty("name", name.concat("_").concat(String.valueOf(count)));
//                }
//
//                if (geographicLevel!=null){
//                    polygonFeature.addStringProperty("geographicLevel", geographicLevel);
//                }
//                // Add the polygon feature to the list
//                polygonFeatures.add(polygonFeature);
//                count++;
//            }
//        }
//        return polygonFeatures;
//    }
//
//    public FeatureCollection getLabels(FeatureCollection multiPolygonFeatureCollection) {
//        List<Feature> polygonFeatures = new ArrayList<>();
//
//        // Iterate through each feature in the FeatureCollection
//
//
//        List<Feature> features = multiPolygonFeatureCollection.features();
//        List<Point> points = new ArrayList<>();
//        for (Feature feature : features) {
//            Geometry geometry = feature.geometry();
//            if (geometry instanceof Polygon) {
//
//                Polygon polygon = (Polygon) geometry;
//                Point point = calculateCentroid(polygon);
//                Feature pointFeature = Feature.fromGeometry(point);
//                if (feature.properties() != null && feature.properties().has("added_label")) {
//                    pointFeature.addStringProperty("added_label", feature.properties().get("added_label").getAsString());
//                    polygonFeatures.add(pointFeature);
//                }
//            }
//        }
//        // Create a FeatureCollection containing all the individual Polygon features
//        return FeatureCollection.fromFeatures(polygonFeatures);
//    }
//
//    public Point calculateCentroid(Polygon polygon) {
//        double area = 0.0;
//        double C_x = 0.0;
//        double C_y = 0.0;
//        int n = polygon.coordinates().get(0).size(); // Assuming it's a single polygon
//
//        List<Point> points = polygon.coordinates().get(0); // Get the outer ring coordinates
//
//        for (int i = 0; i < n; i++) {
//            Point current = points.get(i);
//            Point next = points.get((i + 1) % n); // Wrap around to the first point
//
//            double x0 = current.longitude();
//            double y0 = current.latitude();
//            double x1 = next.longitude();
//            double y1 = next.latitude();
//
//            double a = x0 * y1 - x1 * y0;
//            area += a;
//            C_x += (x0 + x1) * a;
//            C_y += (y0 + y1) * a;
//        }
//
//        area *= 0.5;
//        C_x /= (6.0 * area);
//        C_y /= (6.0 * area);
//
//        return Point.fromLngLat(C_x, C_y); // Return the centroid as a Point
//    }
//}
