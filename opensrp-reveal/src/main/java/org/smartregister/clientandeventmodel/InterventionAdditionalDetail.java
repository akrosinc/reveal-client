package org.smartregister.clientandeventmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InterventionAdditionalDetail {
    private String planIdentifier;
    private String eventDateTime;
    private String taskKeyId;
    private String taskId;
    private String eventType;
    private String key;
    private String value;
    private String valueType;
}
