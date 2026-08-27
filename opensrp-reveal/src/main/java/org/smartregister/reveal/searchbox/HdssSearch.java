package org.smartregister.reveal.searchbox;
import org.smartregister.reveal.interfaces.SearchBoxInterface;

import java.util.List;


public class HdssSearch implements SearchBoxInterface {

    public static final String TEST = "test";
    public static final String TEST_2 = "test2";

    @Override
    public List<SearchItem> search(String text)  {


        SearchItem searchItem = new SearchItem();
        searchItem.setLabel1("Name");
        searchItem.setField1("ABC124");
        searchItem.setResult("ABC124");

        SearchItem searchItem1 = new SearchItem();
        searchItem1.setLabel1("Name");
        searchItem1.setField1("ABC125");
        searchItem1.setResult("ABC125");

        SearchItem searchItem2 = new SearchItem();
        searchItem2.setLabel1("Name");
        searchItem2.setField1("ABC126");
        searchItem2.setLabel2("Address");
        searchItem2.setField2("ABC1262223");
        searchItem2.setResult("ABC126");

        SearchItem searchItem3 = new SearchItem();
        searchItem3.setLabel1("Name");
        searchItem3.setField1("ABC127");
        searchItem3.setResult("ABC127");



        return List.of(searchItem,searchItem1,searchItem2,searchItem3);
    }
}
