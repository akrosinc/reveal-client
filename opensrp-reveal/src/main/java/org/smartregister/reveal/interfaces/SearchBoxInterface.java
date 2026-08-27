package org.smartregister.reveal.interfaces;

import org.smartregister.reveal.searchbox.SearchItem;

import java.util.List;

public interface SearchBoxInterface {

    List<SearchItem> search(String text);
}
