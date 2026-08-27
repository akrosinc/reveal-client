package org.smartregister.reveal.hierarchydatareceiver;

import static org.smartregister.reveal.api.RevealService.HDSS_SEARCH_URL;
import static org.smartregister.reveal.api.RevealService.LOCATION_STRUCTURE_URL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.smartregister.CoreLibrary;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.reveal.util.FirebaseLogger;
import org.smartregister.service.HTTPAgent;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class LocationChildrenDataFetcher implements  HierarchyDataFetcher{
    @Override
    public Set<DataFetcherItem> getChildren(Map<String, String> requestDetails) throws NoHttpResponseException {

        HTTPAgent httpAgent = getHttpAgent();
        if (httpAgent == null) {
            throw new IllegalArgumentException(HDSS_SEARCH_URL + " http agent is null");
        }

        String baseUrl = getFormattedBaseUrl();

        String parentId = null;
        String hierarchyId = null;
        if (!requestDetails.isEmpty()){
            if (requestDetails.containsKey("parentId")){
                parentId = requestDetails.get("parentId");
            }
            if (requestDetails.containsKey("hierarchyId")){
                hierarchyId = requestDetails.get("hierarchyId");
            }
        }

        if (parentId!=null && hierarchyId!=null){
            String getChildrenUrl = baseUrl.concat("/api/v1/locationHierarchy/")
                    .concat(hierarchyId)
                    .concat("/")
                    .concat("location/")
                    .concat(parentId);

            Response<String> resp = httpAgent.fetch(getChildrenUrl);

            if (resp.isFailure()) {
                FirebaseLogger.logApiFailures(getChildrenUrl, resp);
                throw new NoHttpResponseException(getChildrenUrl + " not returned data");
            }
            Gson gson = new Gson();

            List<ChildLocation> childLocations = gson.fromJson(resp.payload(), new TypeToken<List<ChildLocation>>() {
            }.getType());

            Set<DataFetcherItem> dataFetcherItems = new HashSet<>();
            for (ChildLocation location: childLocations) {
                dataFetcherItems.add(new DataFetcherItem(location.identifier, location.properties.name));
            }

            return dataFetcherItems;
        }

        return Collections.emptySet();
    }
    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataFetcherItem{
        private String id;
        private String name;
    }

    @Getter @Setter
    public static class ChildLocation implements Serializable {
        private String identifier;
        private String type;
        private ChildLocationProperties properties;
    }
    @Getter @Setter
    public static class ChildLocationProperties implements Serializable{
        private String name;
        private String status;
        private String externalId;
        private String geographicLevel;
        private boolean assigned;
        private int childrenNumber;
        private boolean simulationSearchResult;
    }

    public HTTPAgent getHttpAgent() {
        return CoreLibrary.getInstance().context().getHttpAgent();
    }
    public String getFormattedBaseUrl() {
        String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
        String endString = "/";
        if (baseUrl.endsWith(endString)) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
        }
        return baseUrl;
    }
}
