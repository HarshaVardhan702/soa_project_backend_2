package com.aegis.policy;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PolicyControllerTest {
    @Test
    void policyStoresItsCoreFields() {
        Policy policy = new Policy("POL-1001", "policyholder", "ACTIVE", LocalDate.of(2027, 12, 31));
        assertEquals("POL-1001", policy.getPolicyNumber());
        assertEquals("policyholder", policy.getHolderUsername());
        assertEquals("ACTIVE", policy.getStatus());
        assertEquals(LocalDate.of(2027, 12, 31), policy.getValidUntil());
    }
}
