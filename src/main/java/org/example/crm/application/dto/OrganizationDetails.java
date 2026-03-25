//package org.example.crm.application.dto;
//
//import java.time.Instant;
//
//public record OrganizationDetails(
//        String id,
//        String name,
//        String website,
//        String websiteStatus,
//        String linkedinUrl,
//        String countryRegion,
//        String email,
//        String category,
//        String status,
//        String notes,
//        String preferredLanguage,
//        Instant createdAt,
//        Instant updatedAt,
//        long version
//) {
//    public String etag() { return "W/\"" + version + "\""; }
//}
package org.example.crm.application.dto;

import java.time.Instant;
import java.util.List;

public record OrganizationDetails(
        String id,
        String name,
        String website,
        String websiteStatus,
        String linkedinUrl,
        String countryRegion,
        String email,
        String category,
        String status,
        String notes,
        String preferredLanguage,
        Instant createdAt,
        Instant updatedAt,
        long version,
        List<ContactDetails> contacts
) {
    public String etag() { return "W/\"" + version + "\""; }

    public record ContactDetails(
            String id,
            String organizationId,
            String name,
            String rolePosition,
            String email,
            String preferredLanguage,
            String notes,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        public String etag() { return "W/\"" + version + "\""; }
    }
}