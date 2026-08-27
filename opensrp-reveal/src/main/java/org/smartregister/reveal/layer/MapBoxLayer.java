package org.smartregister.reveal.layer;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.Source;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by samuelgithengi on 10/1/19.
 */
public class MapBoxLayer {

    protected static final String SATELLITE_LAYER_ID = "mapbox-satellite";
    protected static final String SATELLITE_SOURCE_ID = "mapbox-satellite-source";
    protected static final String SATELLITE_URI = "mapbox://mapbox.satellite";

    private final LinkedHashSet<Layer> layers = new LinkedHashSet<>();
    private final List<Source> sources = new ArrayList<>();

    public MapBoxLayer() {
        createLayersAndSources();
    }

    private void createLayersAndSources() {
        RasterSource rasterSource = new RasterSource(SATELLITE_SOURCE_ID, SATELLITE_URI, 256);
        RasterLayer rasterLayer = new RasterLayer(SATELLITE_LAYER_ID, SATELLITE_SOURCE_ID);

        layers.add(rasterLayer);
        sources.add(rasterSource);
    }

    @NonNull
    public String getDisplayName() {
        return "Mapbox Satellite";
    }

    @NonNull
    public String getId() {
        return "mapbox-satellite-base-layer";
    }

    @NonNull
    public String[] getSourceIds() {
        return new String[]{SATELLITE_SOURCE_ID};
    }

    @NonNull
    public String[] getLayerIds() {
        return new String[]{SATELLITE_LAYER_ID};
    }

    public LinkedHashSet<Layer> getLayers() {
        return layers;
    }

    public List<Source> getSources() {
        return sources;
    }
}
//package org.smartregister.reveal.layer;
//
//import androidx.annotation.NonNull;
//
//import com.mapbox.mapboxsdk.style.layers.Layer;
//import com.mapbox.mapboxsdk.style.layers.RasterLayer;
//import com.mapbox.mapboxsdk.style.sources.RasterSource;
//import com.mapbox.mapboxsdk.style.sources.Source;
//
//import java.util.ArrayList;
//import java.util.LinkedHashSet;
//import java.util.List;
//
//import io.ona.kujaku.plugin.switcher.layer.BaseLayer;
//
///**
// * Created by samuelgithengi on 10/1/19.
// */
//public class MapBoxLayer extends BaseLayer {
//    protected static final String satelliteLayerId = "mapbox-satellite";
//    protected static final String satelliteSourceId = "mapbox://mapbox.satellite";
//
//    private LinkedHashSet<Layer> layers = new LinkedHashSet<>();
//    private List<Source> sources = new ArrayList<>();
//
//    public MapBoxLayer() {
//        createLayersAndSources();
//    }
//
//    private void createLayersAndSources() {
//        RasterSource rasterSource = new RasterSource(satelliteSourceId, "mapbox://mapbox.satellite", 256);
//
//        RasterLayer rasterLayer = new RasterLayer(satelliteLayerId, satelliteSourceId);
//        rasterLayer.setSourceLayer("mapbox-satellite");
//
//        layers.add(rasterLayer);
//        sources.add(rasterSource);
//    }
//
//    @NonNull
//    @Override
//    public String getDisplayName() {
//        return "Mapbox Satellite";
//    }
//
//    @NonNull
//    @Override
//    public String[] getSourceIds() {
//        return new String[] {satelliteSourceId};
//    }
//
//    @Override
//    public LinkedHashSet<Layer> getLayers() {
//        return layers;
//    }
//
//    @Override
//    public List<Source> getSources() {
//        return sources;
//    }
//
//    @NonNull
//    @Override
//    public String getId() {
//        return "mapbox-satellite-base-layer";
//    }
//
//    @NonNull
//    @Override
//    public String[] getLayerIds() {
//        return new String[] {satelliteLayerId};
//    }
//}
