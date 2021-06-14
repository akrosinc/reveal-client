package com.revealprecision.reveal.contract;

import com.revealprecision.reveal.model.OfflineMapModel;

public interface OfflineMapDownloadCallback {

    void onMapDownloaded(OfflineMapModel offlineMapModel);

    void onOfflineMapDeleted(OfflineMapModel offlineMapModel);
}
