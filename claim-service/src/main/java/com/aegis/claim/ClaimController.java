package com.aegis.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/claims")
public class ClaimController {
    private final ClaimRepository repository;
    private final WebClient.Builder webClient;

    public ClaimController(ClaimRepository repository,WebClient.Builder webClient){
        this.repository=repository;this.webClient=webClient;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Claim submit(@Valid @RequestBody SubmitClaim request,
                        @RequestHeader(value="X-Authenticated-User",defaultValue="demo-user") String username) {
        Map policy;
        try {
            policy=webClient.build().get().uri("http://policy-service/policies/{number}/validate",request.policyNumber())
                    .retrieve().bodyToMono(Map.class).block();
        } catch(Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Policy validation service unavailable or policy not found");
        }
        if(policy==null || !Boolean.TRUE.equals(policy.get("valid")))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Policy is not active or has expired");

        Claim claim=repository.save(new Claim(request.policyNumber(),username,request.description(),request.amount()));
        try {
            Map approval=webClient.build().post().uri("http://approval-service/approvals")
                    .bodyValue(Map.of("claimId",claim.getId(),"decision","PENDING","notes","Awaiting adjuster review"))
                    .retrieve().bodyToMono(Map.class).block();
            if(approval!=null) claim.setStatus(String.valueOf(approval.get("decision")));
            return repository.save(claim);
        } catch(Exception ex) {
            claim.setStatus("SUBMITTED");
            repository.save(claim);
            return claim;
        }
    }

    @GetMapping
    public List<Claim> all(){return repository.findAll();}

    @GetMapping("/{id}")
    public Claim get(@PathVariable Long id){
        return repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Claim not found"));
    }

    @GetMapping("/mine")
    public List<Claim> mine(@RequestHeader(value="X-Authenticated-User",defaultValue="demo-user") String username){
        return repository.findByClaimantUsername(username);
    }

    @PutMapping("/{id}/status")
    public Claim updateStatus(@PathVariable("id") Long id,@Valid @RequestBody StatusRequest request){
        Claim claim=get(id); claim.setStatus(request.status().toUpperCase()); return repository.save(claim);
    }

    public record SubmitClaim(@NotBlank String policyNumber,@NotBlank @Size(max=1000) String description,
                              @NotNull @DecimalMin("0.01") BigDecimal amount){}
    public record StatusRequest(@NotBlank String status){}
}
