package com.aegis.policy;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/policies")
public class PolicyController {
    private final PolicyRepository repository;
    public PolicyController(PolicyRepository repository){this.repository=repository;}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Policy create(@Valid @RequestBody CreatePolicy request) {
        if (repository.findByPolicyNumber(request.policyNumber()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Policy number already exists");
        return repository.save(new Policy(request.policyNumber(), request.holderUsername(), request.status(), request.validUntil()));
    }

    @GetMapping
    public java.util.List<Policy> all(){return repository.findAll();}

    @GetMapping("/{policyNumber}")
public Policy get(@PathVariable("policyNumber") String policyNumber) {
        return repository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy not found"));
    }

    @GetMapping("/{policyNumber}/validate")
public Map<String,Object> validate(@PathVariable("policyNumber") String policyNumber) {
        Policy p=get(policyNumber);
        boolean valid="ACTIVE".equalsIgnoreCase(p.getStatus()) && p.getValidUntil()!=null && !p.getValidUntil().isBefore(LocalDate.now());
        return Map.of("policyNumber",p.getPolicyNumber(),"valid",valid,"status",p.getStatus());
    }

    public record CreatePolicy(@NotBlank String policyNumber, @NotBlank String holderUsername,
                               @NotBlank String status, @NotNull LocalDate validUntil) {}
}
