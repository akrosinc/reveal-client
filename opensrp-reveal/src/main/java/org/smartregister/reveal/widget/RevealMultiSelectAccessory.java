package org.smartregister.reveal.widget;

import androidx.appcompat.app.AlertDialog;

import com.vijay.jsonwizard.adapter.MultiSelectListSelectedAdapter;
import com.vijay.jsonwizard.domain.MultiSelectItem;

import org.json.JSONObject;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevealMultiSelectAccessory {
    private MultiSelectListSelectedAdapter selectedAdapter;
    private RevealMultiSelectListAdapter listAdapter;
    private AlertDialog alertDialog;
    private List<MultiSelectItem> selectedItemList;
    private List<RevealMultiSelectItem> itemList;
    private JSONObject formAttributes;

}
