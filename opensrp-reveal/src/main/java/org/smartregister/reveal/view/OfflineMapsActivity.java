package org.smartregister.reveal.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.reveal.BuildConfig;
import org.smartregister.reveal.R;
import org.smartregister.reveal.contract.OfflineMapDownloadCallback;
import org.smartregister.reveal.fragment.AvailableOfflineMapsFragment;
import org.smartregister.reveal.fragment.DownloadedOfflineMapsFragment;
import org.smartregister.reveal.model.OfflineMapModel;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.downloaders.MapBoxOfflineResourcesDownloader;
import io.ona.kujaku.utils.LogUtil;

public class OfflineMapsActivity extends AppCompatActivity implements OfflineMapDownloadCallback {

    private static final String TAG = OfflineMapsActivity.class.getName();

    private ViewPagerAdapter adapter;

    private static  final int AVAILABLE_OFFLINE_MAPS_FRAGMENT_INDEX = 0;
    private static  final int DOWNLOADED_OFFLINE_MAPS_FRAGMENT_INDEX = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_offline_maps);

        setupViews();

        getOfflineDownloadedRegions();
    }

    protected void setupViews() {
        TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(setupViewPager(viewPager));
    }

    protected ViewPager setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        AvailableOfflineMapsFragment availableOfflineMapsFragment = AvailableOfflineMapsFragment.newInstance(this.getIntent().getExtras());
        availableOfflineMapsFragment.setOfflineMapDownloadCallback(this);
        adapter.addFragment(availableOfflineMapsFragment, this.getString(R.string.available).toUpperCase());

        DownloadedOfflineMapsFragment downloadedOfflineMapsFragment = DownloadedOfflineMapsFragment.newInstance(this.getIntent().getExtras());
        downloadedOfflineMapsFragment.setOfflineMapDownloadCallback(this);
        adapter.addFragment(downloadedOfflineMapsFragment, this.getString(R.string.downloaded).toUpperCase());

        viewPager.setAdapter(adapter);

        return viewPager;
    }

    @Override
    public void onMapDownloaded(OfflineMapModel offlineMapModel) {
        DownloadedOfflineMapsFragment downloadedOfflineMapsFragment = (DownloadedOfflineMapsFragment)  adapter.getItem(DOWNLOADED_OFFLINE_MAPS_FRAGMENT_INDEX);
        downloadedOfflineMapsFragment.updateDownloadedMapsList(offlineMapModel);
    }

    @Override
    public void onOfflineMapDeleted(OfflineMapModel offlineMapModel) {
        AvailableOfflineMapsFragment availableOfflineMapsFragment = (AvailableOfflineMapsFragment)  adapter.getItem(AVAILABLE_OFFLINE_MAPS_FRAGMENT_INDEX);
        availableOfflineMapsFragment.updateOperationalAreasToDownload(offlineMapModel);
    }

    private void getOfflineDownloadedRegions() {
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        OfflineManager offlineManager = OfflineManager.getInstance(this);
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                List<String> offlineRegionNames = new ArrayList<>();;
                if (offlineRegions != null && offlineRegions.length > 0) {
                    offlineRegionNames = getOfflineRegionNames(offlineRegions);
                }
                setOfflineDownloadedMapNames(offlineRegionNames);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "ERROR :: "  + error);
            }
        });
    }

    @NonNull
    private List<String> getOfflineRegionNames(final OfflineRegion[] offlineRegions) {
        List<String> offlineRegionNames = new ArrayList<>();

        for(int position = 0; position < offlineRegions.length; position++) {

            byte[] metadataBytes = offlineRegions[position].getMetadata();
            try {
                JSONObject jsonObject = new JSONObject(new String(metadataBytes));
                if (jsonObject.has(MapBoxOfflineResourcesDownloader.METADATA_JSON_FIELD_REGION_NAME)) {
                    offlineRegionNames.add(jsonObject.getString(MapBoxOfflineResourcesDownloader.METADATA_JSON_FIELD_REGION_NAME)) ;
                }

            } catch (JSONException e) {
                LogUtil.e(TAG, e);
            }

        }

        return offlineRegionNames;
    }

    private void setOfflineDownloadedMapNames(List<String> offlineRegionNames) {
        DownloadedOfflineMapsFragment downloadedOfflineMapsFragment = (DownloadedOfflineMapsFragment)  adapter.getItem(DOWNLOADED_OFFLINE_MAPS_FRAGMENT_INDEX);
        downloadedOfflineMapsFragment.setOfflineDownloadedMapNames(offlineRegionNames);

        AvailableOfflineMapsFragment availableOfflineMapsFragment = (AvailableOfflineMapsFragment)  adapter.getItem(AVAILABLE_OFFLINE_MAPS_FRAGMENT_INDEX);
        availableOfflineMapsFragment.setOfflineDownloadedMapNames(offlineRegionNames);

    }
}
