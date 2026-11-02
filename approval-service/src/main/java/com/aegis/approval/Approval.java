package com.aegis.approval;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Approval {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true)
    private Long claimId;
    @Column(nullable=false)
    private String decision;
    private String notes;
    private Instant decidedAt;
    protected Approval(){}
    public Approval(Long claimId,String decision,String notes){this.claimId=claimId;this.decision=decision;this.notes=notes;this.decidedAt=Instant.now();}
    public Long getId(){return id;} public Long getClaimId(){return claimId;} public String getDecision(){return decision;}
    public String getNotes() {
    return notes;
}

public Instant getDecidedAt() {
    return decidedAt;
}

public void updateDecision(String decision, String notes) {
    this.decision = decision;
    this.notes = notes;
    this.decidedAt = Instant.now();
}
}
