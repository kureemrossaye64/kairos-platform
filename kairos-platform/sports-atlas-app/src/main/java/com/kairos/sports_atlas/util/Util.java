package com.kairos.sports_atlas.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.kairos.sports_atlas.entities.User;

public class Util {
	
	
	public static String getConversationId() {
        // This method provides a unique identifier for the current user's conversation.
        // For our POC, we'll use the authenticated user's name.
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return "poc-user"; // Fallback for unauthenticated or system contexts
    }
	
	public static User getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return ((User) principal);
        }
        
        throw new RuntimeException("cannot find logged user");
	}

}
