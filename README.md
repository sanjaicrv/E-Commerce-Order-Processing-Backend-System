# E-Commerce Order Processing Backend System

This is a production-inspired E-Commerce Order Processing Backend System built using **Spring Boot 3.3.0**, **Java 17 (LTS)**, **Spring Security 6**, **JWT Authentication**, and **MySQL**. It features custom Data Structures & Algorithms (DSA) optimizations for core features like inventory lookups, shopping cart undo operations, sorting, and sales analytics.

---

## 🏗️ 6-Layer Module Architecture

Every functional module in the codebase adheres to a strict 6-layer structure:
1. **Entity**: Represents the database table schemas mapped via Hibernate JPA (`*Entity.java`).
2. **DTO**: Decouples API contract payloads from database schemas (`*DTO.java`).
3. **Controller**: Exposes REST endpoints (`*Controller.java`).
4. **ServiceInterface**: Establishes strict contracts for business processes (`*ServiceInterface.java`).
5. **Service**: Contains business logic executing the contracts (`*Service.java`).
6. **JPA Repository**: Handles direct database operations (`*Jpa.java`).

---

## ⚡ Custom DSA Implementations

To demonstrate advanced algorithm design patterns and maximize response throughput, the following custom DSA designs are implemented in the service layer:

### 1. Stock Lookup Cache (HashMap)
* **Location**: `InventoryService`
* **Purpose**: Performs high-speed $O(1)$ stock checks prior to starting transactional checkout flows.
* **Mechanism**: On startup, a `ConcurrentHashMap` maps `productId -> availableQuantity`. Updating inventory updates both MySQL and the local cache concurrently.

### 2. Cart Operation Undo (Stack)
* **Location**: `CartItemService`
* **Purpose**: Supports a multi-level transaction revert history for shopping carts.
* **Mechanism**: Pushes reverse states (`ADD`, `UPDATE`, `DELETE`) onto an in-memory `Stack` per user. Triggering `/api/cart/undo?userId=X` pops and executes the inverse operation.

### 3. Top-Selling Products (Priority Queue - Max Heap)
* **Location**: `OrderService`
* **Purpose**: Efficiently generates a sales leaderboard.
* **Mechanism**: Gathers aggregate sales from `OrderItem` entries and constructs a Max Heap (PriorityQueue). Fetches top $K$ products in $O(k \log n)$ time.

### 4. Custom Catalog Sorting (Merge Sort)
* **Location**: `ProductService`
* **Purpose**: Catalog sorting by price, rating, or popularity (review count).
* **Mechanism**: Implements a hand-written recursive Merge Sort algorithm operating in $O(n \log n)$ time complexity.

---

## 🔐 Security & Cross-Cutting Concerns

- **JWT Authentication**: Extracts the Bearer token from the HTTP request `Authorization` header to authenticate sessions.
- **Role-Based Access Control (RBAC)**: Exposes two roles: `ADMIN` and `CUSTOMER`. ADMIN privileges are required to modify the inventory, product catalog, and categories.
- **Global Exception Handling**: RestControllerAdvice captures runtime failures (e.g. `OutOfStockException`, `UserNotFoundException`) and returns standardized JSON error envelopes.
- **Swagger Documentation**: Configured OpenAPI interface supporting authorization schemes. Open locally at:
  👉 [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

## 📦 Database Modules & APIs

### 1. User Module
- `POST /api/auth/register` (Registers customer/admin; auto-creates cart for customers)
- `POST /api/auth/login` (Authenticates credentials, returns JWT)
- `GET /api/users` (List all users - Admin only)
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}` (Admin only)

### 2. Address Module
- `POST /api/addresses`
- `GET /api/addresses/user/{userId}`
- `PUT /api/addresses/{addressId}`
- `DELETE /api/addresses/{addressId}`

### 3. Category Module
- `POST /api/categories` (Admin only)
- `GET /api/categories`
- `GET /api/categories/{id}`
- `PUT /api/categories/{id}` (Admin only)
- `DELETE /api/categories/{id}` (Admin only)

### 4. Product Module
- `POST /api/products` (Admin only)
- `GET /api/products` (Paginated list)
- `GET /api/products/{id}`
- `PUT /api/products/{id}` (Admin only)
- `DELETE /api/products/{id}` (Admin only)
- `GET /api/products/category/{categoryId}`
- `GET /api/products/search?keyword=...`
- `GET /api/products/sort/price?direction=desc` (Custom Merge Sort)
- `GET /api/products/sort/rating` (Custom Merge Sort)
- `GET /api/products/sort/popularity` (Custom Merge Sort)

### 5. Inventory Module
- `POST /api/inventory` (Admin only)
- `GET /api/inventory` (Admin only)
- `GET /api/inventory/{productId}` (Admin only)
- `PUT /api/inventory/{productId}` (Admin only)
- `GET /api/inventory/low-stock` (Admin only)

### 6. Cart & CartItem Modules
- `GET /api/cart/{userId}`
- `DELETE /api/cart/{cartId}`
- `POST /api/cart/items`
- `PUT /api/cart/items/{id}`
- `DELETE /api/cart/items/{id}`
- `GET /api/cart/items/{cartId}`
- `POST /api/cart/undo?userId=...` (Stack-based Undo)

### 7. Order & OrderItem Modules
- `POST /api/orders` (Check out cart items - executes standard placeOrder sequence)
- `GET /api/orders`
- `GET /api/orders/{id}`
- `GET /api/orders/user/{userId}`
- `PUT /api/orders/status/{id}?status=CANCELLED` (Refunds inventory allocations)
- `DELETE /api/orders/{id}`
- `GET /api/orders/top-selling-products?k=5` (Max Heap query)

### 8. Payment Module
- `POST /api/payments`
- `GET /api/payments/{id}`
- `GET /api/payments/order/{orderId}`
- `PUT /api/payments/{id}`

### 9. Review Module
- `POST /api/reviews`
- `GET /api/reviews/product/{productId}`
- `PUT /api/reviews/{id}`
- `DELETE /api/reviews/{id}`

---

## 🚀 How to Run the Application

### Prerequisites
- Java 17 installed.
- Maven 3.9+ installed.
- MySQL instance running on localhost port `3306` with credentials `root` / `root`.

### Configuration
Update [application.properties](src/main/resources/application.properties) if you need to alter MySQL login credentials:
```properties
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Starting the Server
Run the application using Maven:
```bash
mvn spring-boot:run
```
*(Note: The server port is configured as `8081` to avoid local host port conflicts.)*

### Testing the API
Open [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) to interact with the endpoints. 

### Running Tests
Execute the isolated JUnit test suite (uses H2 in-memory DB configuration):
```bash
mvn clean test
```
