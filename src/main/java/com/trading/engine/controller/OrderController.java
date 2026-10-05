package com.trading.engine.controller;

import com.trading.engine.model.Order;
import com.trading.engine.model.TradeSettlement;
import com.trading.engine.service.OrderMatchingEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderMatchingEngine matchingEngine;

    public OrderController(OrderMatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitOrder(@RequestBody Order order) {
        Optional<TradeSettlement> match = matchingEngine.processOrder(order);

        if (match.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", "MATCHED_AND_SETTLED",
                    "settlement", match.get()
            ));
        }

        return ResponseEntity.accepted().body(Map.of(
                "status", "QUEUED_IN_ORDER_BOOK",
                "orderId", order.orderId()
        ));
    }

    @GetMapping("/settlements")
    public ResponseEntity<List<TradeSettlement>> getSettlements() {
        return ResponseEntity.ok(matchingEngine.getSettlements());
    }
}
