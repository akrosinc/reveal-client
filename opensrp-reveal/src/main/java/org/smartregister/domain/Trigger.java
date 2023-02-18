package org.smartregister.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Trigger implements Serializable {

	private static final long serialVersionUID = 1L;
    private String type;
    private String name;
    private Expression expression;
    private Timing timingTiming;
}
