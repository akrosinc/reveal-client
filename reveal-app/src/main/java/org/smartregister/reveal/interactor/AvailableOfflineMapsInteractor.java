package com.revealprecision.reveal.interactor;

import org.smartregister.domain.Location;
import org.smartregister.repository.LocationRepository;
import com.revealprecision.reveal.application.RevealApplication;
import com.revealprecision.reveal.contract.AvailableOfflineMapsContract;
import com.revealprecision.reveal.model.OfflineMapModel;
import com.revealprecision.reveal.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

public class AvailableOfflineMapsInteractor implements AvailableOfflineMapsContract.Interactor {

    private AppExecutors appExecutors;

    private LocationRepository locationRepository;

    private AvailableOfflineMapsContract.Presenter presenter;

    public AvailableOfflineMapsInteractor(AvailableOfflineMapsContract.Presenter presenter) {
        this.presenter = presenter;
        appExecutors = RevealApplication.getInstance().getAppExecutors();
        locationRepository = RevealApplication.getInstance().getLocationRepository();
    }


    @Override
    public void fetchAvailableOAsForMapDownLoad(final List<String> locationIds) {

        Runnable runnable = new Runnable() {
            public void run() {
                List<Location> operationalAreas = locationRepository.getLocationsByIds(locationIds, false);

                appExecutors.mainThread().execute(() -> {
                    presenter.onFetchAvailableOAsForMapDownLoad(populateOfflineMapModelList(operationalAreas));
                });
            }
        };

        appExecutors.diskIO().execute(runnable);

    }

    public List<OfflineMapModel>  populateOfflineMapModelList(List<Location> locations) {
        List<OfflineMapModel> offlineMapModels = new ArrayList<>();
        for (Location location: locations) {
            OfflineMapModel offlineMapModel = new OfflineMapModel();
            offlineMapModel.setLocation(location);
            offlineMapModels.add(offlineMapModel);
        }

        return offlineMapModels;
    }

}
