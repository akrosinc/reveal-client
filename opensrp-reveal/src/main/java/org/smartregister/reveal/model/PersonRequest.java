package org.smartregister.reveal.model;

import java.time.LocalDate;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PersonRequest {
    private UUID identifier;
    private boolean active;
    private PersonName name;
    private String gender;
    private LocalDate birthDate;
    private String[] groups;
}
