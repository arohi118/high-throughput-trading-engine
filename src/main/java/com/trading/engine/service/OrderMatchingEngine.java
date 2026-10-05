package com.trading.engine.service;

import com.trading.engine.model.Order;
import com.trading.engine.model.OrderSide;
import com.trading.engine.model.TradeSettlement;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory, non-blocking order book implementing price-time priority matching.
 */
@Service
public class OrderMatchingEngine {

    private final AtomicLong settlementSequence = new AtomicLong(1);

    // Buy Book: Highest price first (descending)
    private final ConcurrentSkipListMap<Double, Queue<Order>> buyBook = 
            new ConcurrentSkipListMap<>(Comparator.reverseOrder());

    // Sell Book: Lowest price first (ascending)
    private final ConcurrentSkipListMap<Double, Queue<Order>> sellBook = 
            new ConcurrentSkipListMap<>();

    private final List<TradeSettlement> settlementLedger = Collections.synchronizedList(new ArrayList<>());

    public synchronized Optional<TradeSettlement> processOrder(Order incomingOrder) {
        double priceKey = incomingOrder.price().doubleValue();

        if (incomingOrder.side() == OrderSide.BUY) {
            // Match against existing lowest SELLs
            if (!sellBook.isEmpty() && sellBook.firstKey() <= priceKey) {
                Double bestSellPrice = sellBook.firstKey();
                Queue<Order> ordersAtPrice = sellBook.get(bestSellPrice);
                Order matchedSell = ordersAtPrice.poll();

                if (ordersAtPrice.isEmpty()) {
                    sellBook.remove(bestSellPrice);
                }

                TradeSettlement settlement = new TradeSettlement(
                        "SETTLE-" + settlementSequence.getAndIncrement(),
                        incomingOrder.orderId(),
                        matchedSell.orderId(),
                        incomingOrder.symbol(),
                        matchedSell.price(),
                        Math.min(incomingOrder.quantity(), matchedSell.quantity()),
                        Instant.now()
                );
                settlementLedger.add(settlement);
                return Optional.of(settlement);
            } else {
                buyBook.computeIfAbsent(priceKey, k -> new LinkedList<>()).add(incomingOrder);
                return Optional.empty();
            }
        } else {
            // Match against existing highest BUYs
            if (!buyBook.isEmpty() && buyBook.firstKey() >= priceKey) {
                Double bestBuyPrice = buyBook.firstKey();
                Queue<Order> ordersAtPrice = buyBook.get(bestBuyPrice);
                Order matchedBuy = ordersAtPrice.poll();

                if (ordersAtPrice.isEmpty()) {
                    buyBook.remove(bestBuyPrice);
                }

                TradeSettlement settlement = new TradeSettlement(
                        "SETTLE-" + settlementSequence.getAndIncrement(),
                        matchedBuy.orderId(),
                        incomingOrder.orderId(),
                        incomingOrder.symbol(),
                        matchedBuy.price(),
                        Math.min(incomingOrder.quantity(), matchedBuy.quantity()),
                        Instant.now()
                );
                settlementLedger.add(settlement);
                return Optional.of(settlement);
            } else {
                sellBook.computeIfAbsent(priceKey, k -> new LinkedList<>()).add(incomingOrder);
                return Optional.empty();
            }
        }
    }

    public List<TradeSettlement> getSettlements() {
        return Collections.unmodifiableList(settlementLedger);
    }
}
