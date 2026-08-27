package org.smartregister.reveal.hierarchydatareceiver;

import java.util.Map;
import java.util.Set;

public interface HierarchyDataFetcher {
    Set<LocationChildrenDataFetcher.DataFetcherItem> getChildren(Map<String,String> requestDetails) throws Exception;
}
