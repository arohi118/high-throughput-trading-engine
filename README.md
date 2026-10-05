# High-Throughput Trading & Settlement Engine

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A high-performance, low-latency in-memory trade matching and settlement engine built with **Java 21** and **Spring Boot 3**.

Engineered around high-concurrency principles from tier-1 investment banking infrastructure (handling 10,000+ TPS patterns), this project leverages **Virtual Threads (Project Loom)**, lock-free non-blocking data structures (`ConcurrentSkipListMap`), and price-time priority order matching to achieve sub-millisecond execution profiles.

---

## 🏗️ Architecture & Concurrency Model
Incoming Order Request
│
▼
[Spring Boot 3 REST Endpoint] ── (Powered by Java 21 Virtual Threads)
│
▼
[OrderMatchingEngine]
│
┌───────┴───────┐
▼               ▼
[BUY Book]     [SELL Book]  ── (Price-Time Priority via ConcurrentSkipListMap)
│               │
└───────┬───────┘
│ Match Detected?
┌─────┴─────┐
YES          NO
│           │
▼           ▼
[Immediate   [Enqueued in
Settlement]   Order Book]


---

## ✨ Features

- **Java 21 Virtual Threads:** Non-blocking I/O handling that maximizes thread density without standard platform thread pool exhaustion.
- **Price-Time Priority In-Memory Book:** Implements fast limit order book execution with $O(\log N)$ inserts and removals using `ConcurrentSkipListMap`.
- **Zero-Allocation Data Records:** Employs Java Records for immutable, low-overhead transaction payloads.
- **Production Observability:** Built-in Spring Boot Actuator and Prometheus metric endpoints for real-time throughput and latency tracking.

---

## 🚀 Getting Started

### Prerequisites
- JDK 21 installed
- Apache Maven 3.8+

### Build and Run

```bash
git clone [https://github.com/arohi118/high-throughput-trading-engine.git](https://github.com/arohi118/high-throughput-trading-engine.git)
cd high-throughput-trading-engine

# Build with Maven
mvn clean package

# Run the engine
java -jar target/high-throughput-trading-engine-1.0.0.jar
🔌 API Testing
1. Place a SELL Order into the Book
Bash
curl -X POST http://localhost:8080/api/v1/orders/submit \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-101",
    "portfolioId": "PF-SWISS-01",
    "symbol": "AAPL",
    "side": "SELL",
    "price": 185.50,
    "quantity": 100,
    "timestamp": "2026-10-05T08:00:00Z"
  }'
Response:

JSON
{
  "status": "QUEUED_IN_ORDER_BOOK",
  "orderId": "ORD-101"
}
2. Place a Matching BUY Order
Bash
curl -X POST http://localhost:8080/api/v1/orders/submit \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-102",
    "portfolioId": "PF-US-02",
    "symbol": "AAPL",
    "side": "BUY",
    "price": 185.50,
    "quantity": 100,
    "timestamp": "2026-10-05T08:00:01Z"
  }'
Response (Instant Settlement):

JSON
{
  "status": "MATCHED_AND_SETTLED",
  "settlement": {
    "settlementId": "SETTLE-1",
    "buyOrderId": "ORD-102",
    "sellOrderId": "ORD-101",
    "symbol": "AAPL",
    "executionPrice": 185.50,
    "executedQuantity": 100,
    "settledAt": "2026-10-05T08:00:01.120Z"
  }
}
👤 Author
Arohi Rup

LinkedIn: linkedin.com/in/arohirup

GitHub: @arohi118
