package com.kairos.sports_atlas.repositories;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kairos.sports_atlas.entities.ServiceEntity;

public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, UUID> {
    // This will be useful for updates: find the manifest by its source.
    Optional<ServiceEntity> findByOriginEntityId(UUID originEntityId);
    
    @Query(
            value = """
                SELECT s.*, 
                       ST_Distance(s.coordinates, :locationPoint) as distance
                FROM services s
                WHERE to_tsvector('english', 
                                  coalesce(s.name, '') || ' ' || 
                                  coalesce(s.description, '') || ' ' || 
                                  coalesce(s.location, '') || ' ' || 
                                  coalesce(s.service_type, '')) 
                      @@ plainto_tsquery('english', :term)
                ORDER BY distance ASC
                """,
            nativeQuery = true
        )
        List<ServiceEntity> textSearch(
                @Param("term") String term,
                @Param("locationPoint") Point locationPoint
        );

        // The vectorSearch method remains the same as before
        @Query(
            value = """
                SELECT s.*,
                       ST_Distance(s.coordinates, :locationPoint) as distance
                FROM services s
                ORDER BY s.text_embedding <=> CAST(:embedding AS vector), distance ASC
                LIMIT :limit
                """,
            nativeQuery = true
        )
        List<ServiceEntity> vectorSearch(
                @Param("embedding") float[] embedding,
                @Param("locationPoint") Point locationPoint,
                @Param("limit") int limit
        );
}