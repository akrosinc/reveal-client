package org.smartregister.reveal.widget;

import com.vijay.jsonwizard.domain.MultiSelectItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevealMultiSelectItem extends MultiSelectItem {
    private String key;
    private String text;
    private String value;
    private String text2;
    private String text3;

    private String label1;
    private String label2;
    private String label3;
}
