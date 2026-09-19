package com.smartretail.pricemonitor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartretail.pricemonitor.constants.AlertType;
import com.smartretail.pricemonitor.dto.AlertRequest;
import com.smartretail.pricemonitor.dto.AlertResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;
import com.smartretail.pricemonitor.exception.UnauthorizedException;
import com.smartretail.pricemonitor.service.AlertService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlertService alertService;

    @Test
    @WithMockUser(username = "consumer1", roles = "USER")
    @DisplayName("Create Alert - Authenticated User")
    void testCreateAlert_Success() throws Exception {
        AlertRequest request = new AlertRequest();
        request.setCommodityId(1L);
        request.setMarketId(1L);
        request.setAlertType(AlertType.PRICE_DECREASE);
        request.setTargetPrice(BigDecimal.valueOf(35.00));

        AlertResponse response = AlertResponse.builder()
                .id(1L)
                .commodityId(1L)
                .commodityName("Tomato")
                .marketId(1L)
                .marketName("Azadpur Mandi")
                .alertType(AlertType.PRICE_DECREASE)
                .targetPrice(BigDecimal.valueOf(35.00))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(alertService.createAlert(eq("consumer1"), any(AlertRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targetPrice").value(35.00));
    }

    @Test
    @WithMockUser(username = "consumer1", roles = "USER")
    @DisplayName("Get My Alerts - Principal Enforced")
    void testGetMyAlerts() throws Exception {
        AlertResponse alertRes = AlertResponse.builder()
                .id(1L)
                .commodityId(1L)
                .commodityName("Tomato")
                .marketId(1L)
                .marketName("Azadpur Mandi")
                .alertType(AlertType.PRICE_DECREASE)
                .targetPrice(BigDecimal.valueOf(35.00))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        PagedResponse<AlertResponse> pagedResponse = PagedResponse.<AlertResponse>builder()
                .content(List.of(alertRes))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        Mockito.when(alertService.getUserAlerts(eq("consumer1"), anyInt(), anyInt()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].commodityName").value("Tomato"));
    }

    @Test
    @WithMockUser(username = "unauthorized_user", roles = "USER")
    @DisplayName("Toggle Alert - Ownership Enforcement Rejection")
    void testToggleAlert_UnauthorizedOwnership() throws Exception {
        Mockito.when(alertService.toggleAlert(eq("unauthorized_user"), eq(99L), eq(false)))
                .thenThrow(new UnauthorizedException("You are not authorized to modify this alert"));

        mockMvc.perform(patch("/api/v1/alerts/99/toggle")
                .param("active", "false"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "consumer1", roles = "USER")
    @DisplayName("Delete Alert - Success for Owner")
    void testDeleteAlert_Success() throws Exception {
        Mockito.doNothing().when(alertService).deleteAlert(eq("consumer1"), eq(1L));

        mockMvc.perform(delete("/api/v1/alerts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
