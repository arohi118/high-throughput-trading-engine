package com.trading.engine.model;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeSettlement(
        String settlementId,
        String buyOrderId,
        String sellOrderId,
        String symbol,
        BigDecimal executionPrice,
        long executedQuantity,
        Instant settledAt
) {}
