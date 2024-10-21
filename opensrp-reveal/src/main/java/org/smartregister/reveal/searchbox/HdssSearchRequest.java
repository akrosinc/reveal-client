package org.smartregister.reveal.searchbox;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HdssSearchRequest extends SearchRequest{
    private String searchString;

    private String gender;

    private String dob;
}
