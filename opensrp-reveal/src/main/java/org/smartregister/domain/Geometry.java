package org.smartregister.domain;


import java.io.Serializable;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

public class Geometry implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum GeometryType {
        @SerializedName("Point")
        POINT,
        @SerializedName("Polygon")
        POLYGON,
        @SerializedName("MultiPolygon")
        MULTI_POLYGON
    };

    private GeometryType type;

    private JsonArray coordinates;

    public GeometryType getType() {
        return type;
    }

    public void setType(GeometryType type) {
        this.type = type;
    }

    public JsonArray getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(JsonArray coordinates) {
        this.coordinates = coordinates;
    }

}
