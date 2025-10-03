package com.kairos.sports_atlas.facility.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kairos.sports_atlas.entities.Facility;
import com.kairos.sports_atlas.entities.Partner;
import com.kairos.sports_atlas.entities.ReviewStatus;
import com.kairos.sports_atlas.entities.TrainingOpportunity;
import com.kairos.sports_atlas.repositories.FacilityRepository;
import com.kairos.sports_atlas.repositories.PartnerRepository;
import com.kairos.sports_atlas.repositories.TrainingOpportunityRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {
    private final PartnerRepository partnerRepository;
    private final ServiceEntityService serviceEntityService;
    private final TrainingOpportunityRepository trainingOpportunityRepository;
    private final FacilityRepository facilityRepository;
    
    // ... other repositories ...
    
    @Transactional
    public void approvePartner(UUID partnerId) {
        Partner partner = partnerRepository.findById(partnerId).orElseThrow();
        partner.setReviewStatus(ReviewStatus.APPROVED);
        partnerRepository.save(partner);
        
        // MANIFEST THE SERVICE
        serviceEntityService.manifestService(partner);
    }
    
    @Transactional
    public void approveTrainingOpportunity(UUID opportunityId) {
        TrainingOpportunity partner = trainingOpportunityRepository.findById(opportunityId).orElseThrow();
        partner.setReviewStatus(ReviewStatus.APPROVED);
        trainingOpportunityRepository.save(partner);
        serviceEntityService.manifestService(partner);
    }

    @Transactional
    public void approveFacility(UUID facilityId) {
        log.info("Approving facility with ID: {}", facilityId);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new RuntimeException("Facility not found: " + facilityId));

        facility.setReviewStatus(ReviewStatus.APPROVED);
        facilityRepository.save(facility);
        serviceEntityService.manifestService(facility);
    }
    
    public void approveItem(String entityType, UUID entityId) {
        switch (entityType.toLowerCase()) {
            case "partner":
                approvePartner(entityId);
                break;
            case "trainingopportunity":
                approveTrainingOpportunity(entityId);
                break;
            case "facility":
                approveFacility(entityId);
                break;
            default:
                throw new IllegalArgumentException("Unknown entity type for approval: " + entityType);
        }
    }
}