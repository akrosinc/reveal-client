package org.smartregister.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;

import java.io.Serializable;

import org.smartregister.domain.Action.SubjectConcept;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public class Expression implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String description;
	
	private String name;
	
	private String language;
	
	private String expression;
	
	private String reference;
	
	private SubjectConcept subjectCodableConcept;
}
