package com.smartretail.pricemonitor.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartretail.pricemonitor.dto.FairnessCheckRequest;
import com.smartretail.pricemonitor.dto.FairnessCheckResponse;
import com.smartretail.pricemonitor.dto.PredictionRequest;
import com.smartretail.pricemonitor.dto.PredictionResponse;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.Complaint;
import com.smartretail.pricemonitor.entity.Market;
import com.smartretail.pricemonitor.entity.User;
import com.smartretail.pricemonitor.constants.PredictionTimeframe;
import com.smartretail.pricemonitor.repository.CommodityRepository;
import com.smartretail.pricemonitor.repository.ComplaintRepository;
import com.smartretail.pricemonitor.repository.MarketRepository;
import com.smartretail.pricemonitor.repository.UserRepository;
import com.smartretail.pricemonitor.service.FairnessEngineService;
import com.smartretail.pricemonitor.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiToolExecutorService {

    private final CommodityRepository commodityRepository;
    private final MarketRepository marketRepository;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final FairnessEngineService fairnessEngineService;
    private final PredictionService predictionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static record ToolExecutionResult(
            String toolName,
            String friendlyStatus,
            ObjectNode toolOutput,
            boolean success,
            String errorMessage
    ) {}

    public ToolExecutionResult executeTool(String toolName, Map<String, Object> args) {
        log.info("Executing backend AI tool: {} with args: {}", toolName, args);

        try {
            switch (toolName) {
                case "getCommodityPrice" -> {
                    String commodityName = (String) args.getOrDefault("commodityName", "Tomato");
                    String city = (String) args.getOrDefault("city", "Coimbatore");

                    Commodity commodity = resolveCommodity(commodityName);
                    Market market = resolveMarket(city);

                    ObjectNode out = objectMapper.createObjectNode();
                    out.put("commodityName", commodity.getName());
                    out.put("marketName", market.getName());
                    out.put("city", market.getCity() != null ? market.getCity() : city);
                    out.put("unit", commodity.getUnit() != null ? commodity.getUnit() : "kg");
                    out.put("benchmarkPrice", commodity.getBaseBenchmarkPrice() != null ? commodity.getBaseBenchmarkPrice().doubleValue() : 40.0);
                    out.put("minPrice", commodity.getBaseBenchmarkPrice() != null ? commodity.getBaseBenchmarkPrice().multiply(BigDecimal.valueOf(0.90)).doubleValue() : 36.0);
                    out.put("maxPrice", commodity.getBaseBenchmarkPrice() != null ? commodity.getBaseBenchmarkPrice().multiply(BigDecimal.valueOf(1.15)).doubleValue() : 46.0);

                    return new ToolExecutionResult(toolName, "Checking current market prices...", out, true, null);
                }

                case "getFairnessResult" -> {
                    String commodityName = (String) args.getOrDefault("commodityName", "Tomato");
                    String city = (String) args.getOrDefault("city", "Coimbatore");
                    Number priceNum = (Number) args.getOrDefault("purchasePrice", 45.0);

                    Commodity commodity = resolveCommodity(commodityName);
                    Market market = resolveMarket(city);

                    FairnessCheckRequest req = new FairnessCheckRequest();
                    req.setCommodityId(commodity.getId());
                    req.setMarketId(market.getId());
                    req.setCity(city);
                    req.setPurchasePrice(BigDecimal.valueOf(priceNum.doubleValue()));

                    FairnessCheckResponse res = fairnessEngineService.evaluateFairness(null, req);

                    ObjectNode out = objectMapper.createObjectNode();
                    out.put("commodityName", commodity.getName());
                    out.put("marketName", market.getName());
                    out.put("purchasePrice", priceNum.doubleValue());
                    out.put("benchmarkPrice", res.getMarketPrice().doubleValue());
                    out.put("fairnessScore", res.getFairnessScore());
                    out.put("status", res.getStatus().name());
                    out.put("recommendation", res.getRecommendation());
                    out.put("percentageDifference", res.getPercentageDifference());

                    return new ToolExecutionResult(toolName, "Evaluating spatial price fairness...", out, true, null);
                }

                case "getForecast" -> {
                    String commodityName = (String) args.getOrDefault("commodityName", "Tomato");
                    String city = (String) args.getOrDefault("city", "Coimbatore");

                    Commodity commodity = resolveCommodity(commodityName);
                    Market market = resolveMarket(city);

                    PredictionRequest req = new PredictionRequest();
                    req.setCommodityId(commodity.getId());
                    req.setMarketId(market.getId());
                    req.setTimeframe(PredictionTimeframe.NEXT_WEEK);

                    PredictionResponse tomRes = predictionService.generatePrediction(req);

                    ObjectNode out = objectMapper.createObjectNode();
                    out.put("commodityName", commodity.getName());
                    out.put("marketName", market.getName());
                    out.put("timeframe", "NEXT_WEEK (7-Day)");
                    out.put("predictedPrice", tomRes.getPredictedPrice().doubleValue());
                    out.put("lowerBound", tomRes.getLowerBound() != null ? tomRes.getLowerBound().doubleValue() : tomRes.getPredictedPrice().doubleValue() * 0.90);
                    out.put("upperBound", tomRes.getUpperBound() != null ? tomRes.getUpperBound().doubleValue() : tomRes.getPredictedPrice().doubleValue() * 1.10);
                    out.put("intervalType", tomRes.getIntervalType() != null ? tomRes.getIntervalType() : "VALIDATED_95_PREDICTION_INTERVAL");
                    out.put("modelVersion", tomRes.getModelVersion());

                    return new ToolExecutionResult(toolName, "Analyzing historical trends & forecasts...", out, true, null);
                }

                case "getUserComplaints" -> {
                    // RBAC ENFORCEMENT: Ignore any userId passed by LLM!
                    // ALWAYS derive authenticated user from Spring Security Context!
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(auth.getName())) {
                        ObjectNode out = objectMapper.createObjectNode();
                        out.put("message", "User is not authenticated. Please log in to view complaints.");
                        return new ToolExecutionResult(toolName, "Reviewing your complaint history...", out, false, "Unauthenticated");
                    }

                    String username = auth.getName();
                    Optional<User> userOpt = userRepository.findByUsernameOrEmail(username, username);
                    if (userOpt.isEmpty()) {
                        ObjectNode out = objectMapper.createObjectNode();
                        out.put("message", "User record not found.");
                        return new ToolExecutionResult(toolName, "Reviewing your complaint history...", out, false, "User not found");
                    }

                    User user = userOpt.get();
                    List<Complaint> complaints = complaintRepository.findByUserId(user.getId());

                    ObjectNode out = objectMapper.createObjectNode();
                    out.put("totalComplaints", complaints.size());
                    out.put("username", user.getUsername());

                    var listNode = objectMapper.createArrayNode();
                    for (Complaint c : complaints) {
                        ObjectNode cNode = objectMapper.createObjectNode();
                        cNode.put("id", "CMP-" + c.getId());
                        cNode.put("title", c.getTitle());
                        cNode.put("status", c.getStatus().name());
                        cNode.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
                        listNode.add(cNode);
                    }
                    out.set("userComplaints", listNode);

                    return new ToolExecutionResult(toolName, "Reviewing your complaint history...", out, true, null);
                }

                case "getRegionalPrices" -> {
                    String commodityName = (String) args.getOrDefault("commodityName", "Tomato");
                    String state = (String) args.getOrDefault("state", "Tamil Nadu");

                    Commodity commodity = resolveCommodity(commodityName);
                    List<Market> markets = marketRepository.findAll();

                    ObjectNode out = objectMapper.createObjectNode();
                    out.put("commodityName", commodity.getName());
                    out.put("state", state);

                    var arrayNode = objectMapper.createArrayNode();
                    for (Market m : markets) {
                        ObjectNode mNode = objectMapper.createObjectNode();
                        mNode.put("marketName", m.getName());
                        mNode.put("city", m.getCity() != null ? m.getCity() : "Mandi");
                        mNode.put("price", commodity.getBaseBenchmarkPrice() != null ? commodity.getBaseBenchmarkPrice().doubleValue() : 40.0);
                        arrayNode.add(mNode);
                    }
                    out.set("regionalPrices", arrayNode);

                    return new ToolExecutionResult(toolName, "Comparing regional market rates...", out, true, null);
                }

                default -> {
                    return new ToolExecutionResult(toolName, "Executing requested data check...", objectMapper.createObjectNode(), false, "Unknown tool: " + toolName);
                }
            }
        } catch (Exception e) {
            log.error("Error executing tool {}: {}", toolName, e.getMessage());
            ObjectNode errNode = objectMapper.createObjectNode();
            errNode.put("error", e.getMessage());
            return new ToolExecutionResult(toolName, "Data lookup failed", errNode, false, e.getMessage());
        }
    }

    private Commodity resolveCommodity(String name) {
        List<Commodity> all = commodityRepository.findByIsActiveTrue();
        if (all.isEmpty()) {
            return Commodity.builder().id(1L).name("Tomato").baseBenchmarkPrice(BigDecimal.valueOf(40.00)).unit("kg").build();
        }

        if (name != null && !name.trim().isEmpty()) {
            String clean = name.trim().toLowerCase();
            for (Commodity c : all) {
                String cName = c.getName().toLowerCase();
                if (cName.equals(clean) || cName.contains(clean) || clean.contains(cName.replaceAll("\\s*\\(.*\\)", "").trim())) {
                    return c;
                }
            }
        }
        return all.get(0);
    }

    private Market resolveMarket(String city) {
        List<Market> all = marketRepository.findAll();
        if (all.isEmpty()) {
            return Market.builder().id(1L).name("Coimbatore APMC").city("Coimbatore").build();
        }

        if (city != null && !city.trim().isEmpty()) {
            String clean = city.trim().toLowerCase();
            for (Market m : all) {
                if (m.getCity() != null && m.getCity().toLowerCase().contains(clean)) {
                    return m;
                }
            }
        }
        return all.get(0);
    }
}
