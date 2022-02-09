package org.smartregister.domain.jsonmapping;



import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeResponse {

    private OrganizationTypeEnum code;
    private String valueCodableConcept;

    enum OrganizationTypeEnum implements Serializable {
        CG("Community group"),
        TEAM("Team"),
        OTHER("Other");

        OrganizationTypeEnum(String organizationType) {
            this.organizationType = organizationType;
        }

        private String organizationType;


    }
}