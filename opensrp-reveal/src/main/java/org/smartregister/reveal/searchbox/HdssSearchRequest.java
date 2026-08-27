package org.smartregister.reveal.searchbox;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
public class HdssSearchRequest extends SearchRequest{
    private String searchString;

    private String nameString;

    private String gender;

    private String dob;

    private boolean searchOnline;

    private String cluster;

    private String startAge;

    private String endAge;

    private boolean useAgeRange;

    private boolean useExactDate;

    private int batchSize;

    private int batchNumber;

}
