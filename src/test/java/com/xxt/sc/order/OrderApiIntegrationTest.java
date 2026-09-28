package com.xxt.sc.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsOrderWithServerSidePriceThenCancelsIt() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/ma/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"skuId\":10001,\"quantity\":2}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAY"))
                .andExpect(jsonPath("$.data.originalFen").value(398))
                .andExpect(jsonPath("$.data.payableFen").value(398))
                .andExpect(jsonPath("$.data.items[0].unitPriceFen").value(199))
                .andReturn();

        String body = created.getResponse().getContentAsString();
        String orderNo = com.jayway.jsonpath.JsonPath.read(body, "$.data.orderNo");

        mockMvc.perform(get("/api/v1/ma/orders/{orderNo}", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").value(orderNo));

        mockMvc.perform(post("/api/v1/ma/orders/{orderNo}/cancel", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void rejectsUnknownSku() throws Exception {
        mockMvc.perform(post("/api/v1/ma/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"skuId\":99999,\"quantity\":1}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40901"));
    }

    @Test
    void rejectsInvalidQuantity() throws Exception {
        mockMvc.perform(post("/api/v1/ma/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"skuId\":10001,\"quantity\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("40000"));
    }
}