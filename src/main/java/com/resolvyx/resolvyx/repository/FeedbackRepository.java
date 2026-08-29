package com.resolvyx.resolvyx.repository;

import com.resolvyx.resolvyx.entity.Feedback;
import com.resolvyx.resolvyx.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByTicket(Ticket ticket);
    boolean existsByTicket(Ticket ticket);
}