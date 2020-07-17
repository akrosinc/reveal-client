package org.smartregister.reveal.contract;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Feature;

import org.json.JSONArray;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;

/**
 * Created by samuelgithengi on 3/25/19.
 */
public interface BaseContract {

    interface BasePresenter {

        void onFormSaved(@NonNull String structureId,
                         String taskID, @NonNull Task.TaskStatus taskStatus, @NonNull String businessStatus, String interventionType);

        void onStructureAdded(Feature feature, JSONArray featureCoordinates, double zoomlevel);

        void onFormSaveFailure(String eventType);

        void onFamilyFound(CommonPersonObjectClient finalFamily);
    }

    interface BaseInteractor {

        void saveJsonForm(String json);
    }
}
