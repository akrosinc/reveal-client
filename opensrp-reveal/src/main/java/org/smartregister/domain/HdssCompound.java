package org.smartregister.domain;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HdssCompound implements Serializable {
    private String compoundId;
}