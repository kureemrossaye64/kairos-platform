package com.kairos.sports_atlas.partner.service;

import com.kairos.agentic.conversational.FormCompletionStrategy;
import com.kairos.agentic.form.Form;
import com.kairos.core.search.VdbDocument;
import com.kairos.core.search.VectorStoreService;
import com.kairos.sports_atlas.entities.Partner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PartnerCompletionStrategy implements FormCompletionStrategy {

    private final PartnerService partnerService;
    private final VectorStoreService vectorStoreService;

    @Override
    public String getEntityName() {
        return "Partner"; // Must match the name in the @ConversationalEntity annotation
    }

    @Override
    public void execute(Form completedForm) {
        log.info("Executing completion strategy for a new Partner form.");

        // 1. Extract all the gathered information from the form.
        String name = completedForm.getString("name");
        String activityName = completedForm.getString("activity"); // Field name in entity
        String location = completedForm.getString("location");
        String contactEmail = completedForm.getString("contactEmail");
        String contactPhone = completedForm.getString("contactPhone");
        String description = completedForm.getString("description");

        // 2. Use the PartnerService to handle the core logic of creating the entity.
        Partner savedPartner = partnerService.createPartner(name, activityName, location, contactEmail, contactPhone, description);
        log.info("Successfully saved Partner '{}' to PostgreSQL with ID: {}", savedPartner.getName(), savedPartner.getId());

        // 3. Trigger the ingestion of the unstructured description into the vector store (ChromaDB).
        VdbDocument partnerKnowledge = VdbDocument.builder()
                .id(UUID.randomUUID())
                .content(description)
                .metadata(Map.of(
                        "source", "partner_submission",
                        "partner_id", savedPartner.getId().toString(),
                        "partner_name", savedPartner.getName(),
                        "activity", savedPartner.getActivity().getName()
                ))
                .build();
        
        vectorStoreService.addDocument(partnerKnowledge);
        log.info("Successfully indexed partner description for '{}' in the vector store.", savedPartner.getName());
    }
    
}