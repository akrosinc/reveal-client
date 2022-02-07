package org.smartregister.domain;

import org.joda.time.DateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Period implements Serializable {

    private static final long serialVersionUID = 1L;

    private DateTime start;

    private DateTime end;

}
