package com.aegis.approval;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/approvals")
public class ApprovalController {
    private final ApprovalRepository repository;
    public ApprovalController(ApprovalRepository repository){this.repository=repository;}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Approval decide(@Valid @RequestBody DecisionRequest request) {
        if(repository.findByClaimId(request.claimId()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Claim already adjudicated");
        return repository.save(new Approval(request.claimId(),request.decision().toUpperCase(),request.notes()));
    }

@PutMapping("/{claimId}")
public Approval update(
        @PathVariable("claimId") Long claimId,
        @Valid @RequestBody DecisionRequest request) {

    Approval approval = repository.findByClaimId(claimId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Approval not found"));

    approval.updateDecision(
            request.decision().toUpperCase(),
            request.notes());

    return repository.save(approval);
}

@GetMapping("/{claimId}")
public Approval get(@PathVariable("claimId") Long claimId) {
    return repository.findByClaimId(claimId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Approval not found"));
}

    public record DecisionRequest(@NotNull Long claimId,@NotBlank String decision,String notes){}
}
