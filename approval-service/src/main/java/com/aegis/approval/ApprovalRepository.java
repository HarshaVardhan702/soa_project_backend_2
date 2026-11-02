package com.aegis.approval;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApprovalRepository extends JpaRepository<Approval,Long> {
    Optional<Approval> findByClaimId(Long claimId);
}
