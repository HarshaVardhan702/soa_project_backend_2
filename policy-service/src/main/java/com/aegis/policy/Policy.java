package com.aegis.policy;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Policy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true)
    private String policyNumber;
    @Column(nullable=false)
    private String holderUsername;
    @Column(nullable=false)
    private String status;
    private LocalDate validUntil;

    protected Policy() {}
    public Policy(String policyNumber, String holderUsername, String status, LocalDate validUntil) {
        this.policyNumber=policyNumber; this.holderUsername=holderUsername; this.status=status; this.validUntil=validUntil;
    }
    public Long getId(){return id;}
    public String getPolicyNumber(){return policyNumber;}
    public String getHolderUsername(){return holderUsername;}
    public String getStatus(){return status;}
    public LocalDate getValidUntil(){return validUntil;}
}
