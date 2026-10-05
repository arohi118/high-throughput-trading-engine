package com.trading.engine.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(
        String orderId,
        String portfolioId,
        String symbol,
        OrderSide side,
        BigDecimal price,
        long quantity,
        Instant timestamp
) {}
