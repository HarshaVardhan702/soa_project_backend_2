package com.aegis.claim;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Claim {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String policyNumber;
    @Column(nullable=false)
    private String claimantUsername;
    @Column(nullable=false, length=1000)
    private String description;
    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal amount;
    @Column(nullable=false)
    private String status;
    private Instant submittedAt;
    protected Claim(){}
    public Claim(String policyNumber,String claimantUsername,String description,BigDecimal amount){
        this.policyNumber=policyNumber;this.claimantUsername=claimantUsername;this.description=description;
        this.amount=amount;this.status="SUBMITTED";this.submittedAt=Instant.now();
    }
    public Long getId(){return id;} public String getPolicyNumber(){return policyNumber;}
    public String getClaimantUsername(){return claimantUsername;} public String getDescription(){return description;}
    public BigDecimal getAmount(){return amount;} public String getStatus(){return status;}
    public Instant getSubmittedAt(){return submittedAt;}
    public void setStatus(String status){this.status=status;}
}
