package com.smartretail.pricemonitor.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartretail.pricemonitor.config.GeminiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiClientService {

    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public static record GeminiAiResponse(
            boolean isAvailable,
            String replyText,
            String toolRequested,
            String callId,
            Map<String, Object> toolArgs,
            String groundingStatus,
            String errorMessage
    ) {}

    public GeminiAiResponse queryGemini(String userQuery, List<Map<String, String>> history, String contextPrompt, List<ObjectNode> toolResults) {
        String apiKey = geminiProperties.getApiKey();
        String modelName = geminiProperties.getModel();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.info("Gemini API key is not configured. Falling back gracefully.");
            return new GeminiAiResponse(false, "AI assistance is temporarily unavailable. Please try again later.", null, null, null, "AI_UNAVAILABLE", null);
        }

        try {
            // NEVER LOG THE FULL URL AS IT CONTAINS THE SECRET API KEY!
            String endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

            ObjectNode requestBody = objectMapper.createObjectNode();

            // 1. System Instruction / Context
            ObjectNode sysInstruction = objectMapper.createObjectNode();
            ObjectNode sysParts = objectMapper.createObjectNode();
            sysParts.put("text", contextPrompt);
            sysInstruction.set("parts", objectMapper.createArrayNode().add(sysParts));
            requestBody.set("systemInstruction", sysInstruction);

            // 2. Contents (History + User Message + Tool Results)
            ArrayNode contentsArray = objectMapper.createArrayNode();

            if (history != null) {
                for (Map<String, String> msg : history) {
                    String role = "user".equalsIgnoreCase(msg.get("sender")) ? "user" : "model";
                    ObjectNode contentNode = objectMapper.createObjectNode();
                    contentNode.put("role", role);
                    ArrayNode parts = objectMapper.createArrayNode();
                    ObjectNode part = objectMapper.createObjectNode();
                    part.put("text", msg.get("text"));
                    parts.add(part);
                    contentNode.set("parts", parts);
                    contentsArray.add(contentNode);
                }
            }

            // Current Query
            ObjectNode userContent = objectMapper.createObjectNode();
            userContent.put("role", "user");
            ArrayNode userParts = objectMapper.createArrayNode();
            ObjectNode userPart = objectMapper.createObjectNode();
            userPart.put("text", userQuery);
            userParts.add(userPart);

            // If we have tool results from previous iteration, add them in strict Gemini 3.6 tool protocol format
            if (toolResults != null && !toolResults.isEmpty()) {
                for (ObjectNode toolRes : toolResults) {
                    userParts.add(toolRes);
                }
            }

            userContent.set("parts", userParts);
            contentsArray.add(userContent);
            requestBody.set("contents", contentsArray);

            // 3. Tools Declaration
            ArrayNode toolsArray = objectMapper.createArrayNode();
            ObjectNode functionDeclarationsNode = objectMapper.createObjectNode();
            ArrayNode functionList = objectMapper.createArrayNode();

            functionList.add(createToolDeclaration("getCommodityPrice", "Fetch live APMC mandi wholesale price for a commodity in a city or market",
                    Map.of("commodityName", "string", "city", "string")));

            functionList.add(createToolDeclaration("getFairnessResult", "Evaluate spatial price fairness index for a retail purchase",
                    Map.of("commodityName", "string", "city", "string", "purchasePrice", "number")));

            functionList.add(createToolDeclaration("getForecast", "Retrieve ML time-series price forecast for tomorrow, next week, or next month",
                    Map.of("commodityName", "string", "city", "string")));

            functionList.add(createToolDeclaration("getUserComplaints", "Retrieve official price dispute complaints submitted by the authenticated user",
                    Map.of()));

            functionList.add(createToolDeclaration("getRegionalPrices", "Compare mandi rates across districts in a state",
                    Map.of("commodityName", "string", "state", "string")));

            functionDeclarationsNode.set("functionDeclarations", functionList);
            toolsArray.add(functionDeclarationsNode);
            requestBody.set("tools", toolsArray);

            // NOTE: Do NOT send deprecated parameters (temperature, top_p, top_k, candidate_count) for Gemini 3.6 Flash.

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            log.info("Sending request to Gemini API (model={})", modelName);
            ResponseEntity<String> response = restTemplate.exchange(endpointUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode partsNode = candidates.get(0).path("content").path("parts");
                    if (partsNode.isArray() && partsNode.size() > 0) {
                        JsonNode firstPart = partsNode.get(0);

                        // Check if model requested a Function Call
                        if (firstPart.has("functionCall")) {
                            JsonNode funcCall = firstPart.get("functionCall");
                            String functionName = funcCall.path("name").asText();
                            String callId = funcCall.has("id") ? funcCall.get("id").asText() : null;
                            JsonNode argsNode = funcCall.path("args");
                            Map<String, Object> argsMap = objectMapper.convertValue(argsNode, Map.class);

                            return new GeminiAiResponse(true, null, functionName, callId, argsMap, "TOOL_REQUESTED", null);
                        }

                        // Text response
                        if (firstPart.has("text")) {
                            String text = firstPart.get("text").asText();
                            return new GeminiAiResponse(true, text, null, null, null, "GROUNDED_IN_DB", null);
                        }
                    }
                }
            }

            return new GeminiAiResponse(true, "I am analyzing live market data for your request.", null, null, null, "GENERAL_GUIDANCE", null);

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Gemini API rate limit exceeded (HTTP 429).");
            return new GeminiAiResponse(false, "AI assistance is temporarily unavailable. Please try again later.", null, null, null, "AI_UNAVAILABLE", "429_RATE_LIMIT");
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Gemini API key is invalid or unauthorized.");
            return new GeminiAiResponse(false, "AI assistance is temporarily unavailable. Please try again later.", null, null, null, "AI_UNAVAILABLE", "401_UNAUTHORIZED");
        } catch (Exception e) {
            // NEVER LOG EXCEPTION MESSAGE AS IT MIGHT CONTAIN URL QUERY PARAMETERS
            log.error("Error communicating with Gemini API ({})", e.getClass().getSimpleName());
            return new GeminiAiResponse(false, "AI assistance is temporarily unavailable. Please try again later.", null, null, null, "AI_UNAVAILABLE", e.getClass().getSimpleName());
        }
    }

    private ObjectNode createToolDeclaration(String name, String description, Map<String, String> parameters) {
        ObjectNode func = objectMapper.createObjectNode();
        func.put("name", name);
        func.put("description", description);

        ObjectNode paramsNode = objectMapper.createObjectNode();
        paramsNode.put("type", "OBJECT");
        ObjectNode propsNode = objectMapper.createObjectNode();

        parameters.forEach((paramName, paramType) -> {
            ObjectNode prop = objectMapper.createObjectNode();
            prop.put("type", paramType.equalsIgnoreCase("number") ? "NUMBER" : "STRING");
            propsNode.set(paramName, prop);
        });

        paramsNode.set("properties", propsNode);
        func.set("parameters", paramsNode);
        return func;
    }
}
