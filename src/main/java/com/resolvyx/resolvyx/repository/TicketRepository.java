package com.resolvyx.resolvyx.repository;

import com.resolvyx.resolvyx.entity.Ticket;
import com.resolvyx.resolvyx.entity.Organization;
import com.resolvyx.resolvyx.entity.TicketStatus;
import com.resolvyx.resolvyx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByOrganization(Organization organization);
    List<Ticket> findByOrganizationAndStatus(Organization organization, TicketStatus status);
    long countByAssignedToAndStatusIn(User assignedTo, List<TicketStatus> statuses);
}