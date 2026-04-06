package org.smartregister.domain;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HttpResponseWrapper {
  private InputStream inputStream;
  private Map<String, List<String>> headers;
  // constructor, getters, etc.
}