package com.smartretail.pricemonitor.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartretail.pricemonitor.constants.FairnessStatus;
import com.smartretail.pricemonitor.dto.AiChatRequest;
import com.smartretail.pricemonitor.dto.AiChatResponse;
import com.smartretail.pricemonitor.dto.FairnessCheckRequest;
import com.smartretail.pricemonitor.dto.FairnessCheckResponse;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.Market;
import com.smartretail.pricemonitor.repository.CommodityRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.service.FairnessEngineService;
import com.smartretail.pricemonitor.service.ai.AiToolExecutorService;
import com.smartretail.pricemonitor.service.ai.GeminiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiConversationalEngineService {

    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;
    private final FairnessEngineService fairnessEngineService;
    private final GeminiClientService geminiClientService;
    private final AiToolExecutorService aiToolExecutorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiChatResponse processQuery(AiChatRequest request) {
        String query = request.getQuery() != null ? request.getQuery().trim() : "";
        String mode = request.getMode() != null ? request.getMode().trim().toUpperCase() : "AI";

        String activeLocation = request.getActiveLocation() != null && !request.getActiveLocation().isEmpty()
                ? request.getActiveLocation() : "Coimbatore";
        String activeState = request.getActiveState() != null && !request.getActiveState().isEmpty()
                ? request.getActiveState() : "Tamil Nadu";

        // ─── PRIMARY AI ENGINE: GEMINI 3.6 FLASH INTEGRATION ──────────────────────
        String systemPrompt = "You are FairPrice AI Assistant, an intelligent government price monitoring and spatial fairness assistant for India. " +
                "Your identity: FairPrice AI Assistant. " +
                "Rules: " +
                "1. Never invent numerical prices, forecasts, complaints, or fairness scores. " +
                "2. Use available application tools to answer user questions about prices, fairness, forecasts, and user complaints. " +
                "3. If tool data is unavailable, state clearly that market data is unavailable. " +
                "4. Give concise, data-grounded, respectful answers. " +
                "Active Location Context: " + activeLocation + ", " + activeState;

        List<Map<String, String>> historyList = request.getHistory();

        // 1. Initial LLM Call
        GeminiClientService.GeminiAiResponse aiRes = geminiClientService.queryGemini(query, historyList, systemPrompt, null);

        // 2. Check if AI is unavailable (Rate limit 429, missing key, etc.)
        if (!aiRes.isAvailable()) {
            return AiChatResponse.builder()
                    .reply("AI assistance is temporarily unavailable. Please try again later.")
                    .detectedLocation(activeLocation)
                    .groundingStatus("AI_UNAVAILABLE")
                    .source("UNAVAILABLE")
                    .build();
        }

        // 3. Handle Tool Request from Gemini
        if ("TOOL_REQUESTED".equalsIgnoreCase(aiRes.groundingStatus()) && aiRes.toolRequested() != null) {
            String toolName = aiRes.toolRequested();
            String callId = aiRes.callId();
            Map<String, Object> toolArgs = aiRes.toolArgs() != null ? aiRes.toolArgs() : Map.of();

            // Execute backend tool with strict Spring Security RBAC authorization
            AiToolExecutorService.ToolExecutionResult toolExec = aiToolExecutorService.executeTool(toolName, toolArgs);

            // Format tool result back into Gemini part node conforming strictly to Gemini 3.6 Tool Protocol
            ObjectNode toolPartNode = objectMapper.createObjectNode();
            ObjectNode functionRespNode = objectMapper.createObjectNode();
            functionRespNode.put("name", toolName);
            if (callId != null && !callId.isEmpty()) {
                functionRespNode.put("id", callId);
            }
            functionRespNode.set("response", toolExec.toolOutput());
            toolPartNode.set("functionResponse", functionRespNode);

            // 4. Second LLM Call to synthesize final answer from tool result
            GeminiClientService.GeminiAiResponse finalAiRes = geminiClientService.queryGemini(query, historyList, systemPrompt, List.of(toolPartNode));

            String finalReply = (finalAiRes.isAvailable() && finalAiRes.replyText() != null)
                    ? finalAiRes.replyText()
                    : ("ℹ️ " + toolExec.friendlyStatus() + "\n\n" + toolExec.toolOutput().toPrettyString());

            return AiChatResponse.builder()
                    .reply(finalReply)
                    .detectedLocation(activeLocation)
                    .friendlyToolStatus(toolExec.friendlyStatus())
                    .groundingStatus("GROUNDED_IN_DB")
                    .source("AI")
                    .build();
        }

        // 5. Direct Text Response from Gemini
        return AiChatResponse.builder()
                .reply(aiRes.replyText() != null ? aiRes.replyText() : "I have evaluated your market query against live APMC rates.")
                .detectedLocation(activeLocation)
                .groundingStatus(aiRes.groundingStatus())
                .source("AI")
                .build();
    }
}
