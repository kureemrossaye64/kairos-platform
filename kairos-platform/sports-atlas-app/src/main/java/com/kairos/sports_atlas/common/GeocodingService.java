package com.kairos.sports_atlas.common;
import org.locationtech.jts.geom.Point;
public interface GeocodingService {
    Point geocode(String address);
}