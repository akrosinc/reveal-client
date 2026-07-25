package org.smartregister.reveal.searchbox;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class SearchTaskResult {

  private boolean success;
  private String json;
  private String error;

  public static SearchTaskResult success(String json) {
    SearchTaskResult r = new SearchTaskResult();
    r.success = true;
    r.json = json;
    return r;
  }

  public static SearchTaskResult failure(String error) {
    SearchTaskResult r = new SearchTaskResult();
    r.success = false;
    r.error = error;
    return r;
  }
}
