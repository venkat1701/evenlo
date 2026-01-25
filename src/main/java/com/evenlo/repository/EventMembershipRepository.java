package com.evenlo.repository;

import com.evenlo.model.EventMembership;
import com.evenlo.model.EventMembershipRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventMembershipRepository extends JpaRepository<EventMembership, UUID> {
	Optional<EventMembership> findByEventIdAndUserId(UUID eventId, UUID userId);
	boolean existsByEventIdAndUserIdAndRoleIn(UUID eventId, UUID userId, Iterable<EventMembershipRole> roles);
}
