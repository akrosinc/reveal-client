package org.smartregister.domain.form;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


public class FormLocation {
    public String name;
    public String key;
    public String level;
    public List<FormLocation> nodes = new LinkedList<>();

    public Stream<FormLocation> flattened() {
        return Stream.concat(
                Stream.of(this),
                nodes.stream().flatMap(FormLocation::flattened));
    }
}
