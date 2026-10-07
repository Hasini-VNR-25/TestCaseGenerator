package com.cbp.testgen;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WebEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testDashboardEndpoint() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    void testGenerateEndpoint() throws Exception {
        mockMvc.perform(get("/generate"))
                .andExpect(status().isOk());
    }

    @Test
    void testHistoryEndpoint() throws Exception {
        mockMvc.perform(get("/history"))
                .andExpect(status().isOk());
    }

    @Test
    void testMutantSerializationAndDeserialization() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        com.cbp.testgen.mutation.Mutant mutant = new com.cbp.testgen.mutation.Mutant(
                "MUT_1",
                "deposit",
                15,
                com.cbp.testgen.mutation.MutationOperator.RELATIONAL_OPERATOR_SWAP,
                "amount <= 0",
                "amount < 0",
                "public void deposit(double amount) { if (amount < 0) ... }"
        );
        mutant.setStatus(com.cbp.testgen.mutation.Mutant.Status.KILLED);
        mutant.setKillingTestName("testDeposit_NegativeAmount");
        mutant.setExecutionDetails("Killed by testDeposit_NegativeAmount");

        String json = mapper.writeValueAsString(java.util.List.of(mutant));
        org.junit.jupiter.api.Assertions.assertNotNull(json);

        java.util.List<com.cbp.testgen.mutation.Mutant> deserialized = mapper.readValue(
                json,
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<com.cbp.testgen.mutation.Mutant>>() {}
        );

        org.junit.jupiter.api.Assertions.assertEquals(1, deserialized.size());
        com.cbp.testgen.mutation.Mutant m = deserialized.get(0);
        org.junit.jupiter.api.Assertions.assertEquals("MUT_1", m.getId());
        org.junit.jupiter.api.Assertions.assertEquals("deposit", m.getMethodName());
        org.junit.jupiter.api.Assertions.assertEquals(15, m.getLineNumber());
        org.junit.jupiter.api.Assertions.assertEquals(com.cbp.testgen.mutation.MutationOperator.RELATIONAL_OPERATOR_SWAP, m.getOperator());
        org.junit.jupiter.api.Assertions.assertEquals(com.cbp.testgen.mutation.Mutant.Status.KILLED, m.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("testDeposit_NegativeAmount", m.getKillingTestName());
    }
}
