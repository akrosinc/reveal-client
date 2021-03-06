package org.smartregister.domain;


import androidx.annotation.NonNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class ClientRelationship {

    @NonNull
    private String baseEntityId;

    @NonNull
    private String relationship;

    @NonNull
    private String relationalId;

}
