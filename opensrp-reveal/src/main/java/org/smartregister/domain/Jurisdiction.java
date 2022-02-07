package org.smartregister.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Vincent Karuri
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Jurisdiction implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;

}
