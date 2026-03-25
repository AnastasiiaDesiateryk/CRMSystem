package org.example.crm.adapter.out.persistence;

import org.example.crm.adapter.out.persistence.entity.ContactEntity;
import org.example.crm.adapter.out.persistence.entity.OrganizationEntity;
import org.example.crm.adapter.out.persistence.jpa.OrganizationJpaRepository;
import org.example.crm.application.dto.OrganizationDetails;
import org.example.crm.application.port.out.OrganizationReadPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter (READ):
 * JPA -> DTO (OrganizationDetails)
 *
 * Why this exists (hexagonal):
 * - application layer depends on OrganizationReadPort only
 * - JPA repository + entities stay inside adapter.out.persistence
 *
 * Related files:
 * - OrganizationQueryService (application): calls OrganizationReadPort.findAll()
 * - OrganizationJpaRepository (adapter.out.persistence.jpa): DB access
 * - OrganizationEntity (adapter.out.persistence.entity): DB mapping
 */
@Component
public class OrganizationReadAdapter implements OrganizationReadPort {

    private final OrganizationJpaRepository repo;

    public OrganizationReadAdapter(OrganizationJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrganizationDetails> findAll() {
        return repo.findAll().stream()
                .map(OrganizationReadAdapter::toDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<OrganizationDetails> findById(UUID id) {
        return repo.findById(id).map(OrganizationReadAdapter::toDetails);
    }

//    private static OrganizationDetails toDetails(OrganizationEntity e) {
//        // IMPORTANT:
//        // OrganizationDetails у тебя уже используется в OrganizationWriteAdapter.
//        // Держим mapping одинаковым, чтобы фронт получал одно и то же.
//        return new OrganizationDetails(
//                e.getId().toString(),
//                e.getName(),
//                e.getWebsite(),
//                e.getWebsiteStatus(),
//                e.getLinkedinUrl(),
//                e.getCountryRegion(),
//                e.getEmail(),
//                e.getCategory(),
//                e.getStatus(),
//                e.getNotes(),
//                e.getPreferredLanguage(),
//                e.getCreatedAt(),
//                e.getUpdatedAt(),
//                e.getVersion()
//        );
//    }
//}
private static OrganizationDetails toDetails(OrganizationEntity e) {
    return new OrganizationDetails(
            e.getId().toString(),
            e.getName(),
            e.getWebsite(),
            e.getWebsiteStatus(),
            e.getLinkedinUrl(),
            e.getCountryRegion(),
            e.getEmail(),
            e.getCategory(),
            e.getStatus(),
            e.getNotes(),
            e.getPreferredLanguage(),
            e.getCreatedAt(),
            e.getUpdatedAt(),
            e.getVersion(),
            toContactDetails(e.getContacts())
    );
}

    private static List<OrganizationDetails.ContactDetails> toContactDetails(List<ContactEntity> contacts) {
        if (contacts == null) return List.of();
        return contacts.stream().map(c ->
                new OrganizationDetails.ContactDetails(
                        c.getId().toString(),
                        c.getOrganization().getId().toString(),
                        c.getName(),
                        c.getRolePosition(),
                        c.getEmail(),
                        c.getPreferredLanguage(),
                        c.getNotes(),
                        c.getCreatedAt(),
                        c.getUpdatedAt(),
                        c.getVersion()
                )
        ).toList();
    }
}