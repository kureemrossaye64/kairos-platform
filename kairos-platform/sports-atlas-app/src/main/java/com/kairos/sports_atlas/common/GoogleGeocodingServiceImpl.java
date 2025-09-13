package com.kairos.sports_atlas.common;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Production-grade implementation of the GeocodingService using the Google Maps API.
 * This service is only activated when an API key is provided in the configuration.
 */
@Service
@ConditionalOnProperty(name = "kairos.geocoding.google.api-key")
@Slf4j
public class GoogleGeocodingServiceImpl implements GeocodingService {

    private final GeoApiContext context;
    private final GeometryFactory geometryFactory;

    public GoogleGeocodingServiceImpl(@Value("${kairos.geocoding.google.api-key}") String apiKey) {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
        // GeometryFactory is used to create JTS Point objects
        this.geometryFactory = new GeometryFactory();
        log.info("Initialized GoogleGeocodingServiceImpl.");
    }

    @Override
    public Point geocode(String address) {
        try {
            log.debug("Geocoding address: {}", address);
            // We add ", Mauritius" to bias the search results heavily towards the country.
            GeocodingResult[] results = GeocodingApi.geocode(context, address + ", Mauritius").await();

            if (results == null || results.length == 0) {
                log.warn("No geocoding results found for address: {}", address);
                throw new GeocodingException("Could not find coordinates for the specified location.");
            }

            // Use the first, most relevant result
            LatLng location = results[0].geometry.location;
            log.info("Geocoded '{}' to Lat: {}, Lng: {}", address, location.lat, location.lng);

            // Convert Google's LatLng to a JTS Point that PostGIS understands
            Point point = geometryFactory.createPoint(new Coordinate(location.lng, location.lat));
            point.setSRID(4326); // Set the Spatial Reference System ID to WGS 84 (standard for GPS)
            return point;

        } catch (GeocodingException e) {
            throw e; // Re-throw our custom exception
        } catch (Exception e) {
            log.error("An error occurred during geocoding for address: {}", address, e);
            throw new GeocodingException("An external error occurred while trying to geocode the location.", e);
        }
    }
    
    // Custom exception for better error handling
    public static class GeocodingException extends RuntimeException {
        public GeocodingException(String message) {
            super(message);
        }
        public GeocodingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}