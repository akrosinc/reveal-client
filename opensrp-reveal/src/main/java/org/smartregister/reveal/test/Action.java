package org.smartregister.reveal.test;

import lombok.Getter;

@Getter
public class Action {

  private final String individualId;
  private final String gender;
  private final String name;
  private final String dob;
  private final String createdDate;
  private final HdssTask task;
  private final String householdId;


  Action(
      String individualId,
      String gender,
      String dob,
      String createdDate,
      HdssTask task,
      String name,
      String householdId) {
    this.individualId = individualId;
    this.gender = gender;
    this.dob = dob;
    this.task = task;
    this.createdDate = createdDate;
    this.name = name;
    this.householdId = householdId;

  }
}
