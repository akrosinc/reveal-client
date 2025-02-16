package org.smartregister.reveal.test;

import lombok.Getter;
import lombok.Setter;
import org.smartregister.domain.Task;

@Setter
@Getter
public  class HdssTask extends Task {

  private String individualId;
  private String householdId;
}
