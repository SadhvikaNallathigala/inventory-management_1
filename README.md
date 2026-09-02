# Inventory Console

A small Spring Boot + PostgreSQL app with two pages - **Products** and
**Orders** - covering all 9 required features, a standard
`{ success, data, error, meta }` JSON envelope on every endpoint, and
strict layering (`Controller -> Service -> Repository -> Entity`).

## 1. Prerequisites

- JDK 17+
- Maven (or just use IntelliJ, which bundles it)
- PostgreSQL running locally (default port `5432`)

## 2. One-time database setup

```bash
# in psql, or any Postgres GUI:
CREATE DATABASE inventory_db;
```

Update `src/main/resources/application.properties` if your Postgres
username/password aren't `postgres`/`postgres`:

```properties
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Tables are created automatically on first run (`ddl-auto=update`) -
nothing to run by hand.

## 3. Run it

In IntelliJ: open the folder with `pom.xml`, wait for Maven to sync,
then run `InventoryApplication.main()`.

From the terminal:
```bash
mvn spring-boot:run
```

Then open:
| What | URL |
|---|---|
| Products page | http://localhost:8080/index.html |
| Orders page | http://localhost:8080/orders.html |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Raw OpenAPI JSON | http://localhost:8080/api-docs |

## 4. The JSON contract (Swagger)

Every single endpoint returns exactly this shape, success or failure:

```json
{
  "success": true,
  "data": { "...": "..." },
  "error": null,
  "meta": { "totalProducts": 12, "lowStockCount": 2 }
}
```

On failure, `success` is `false`, `data` is `null`, and `error` holds
`{ "code": "...", "message": "..." }`.

## 5. Endpoints (kept deliberately few)

**Products**
| Method | Endpoint | Feature(s) covered |
|---|---|---|
| POST | `/api/products` | Add Product (includes code validation) |
| PUT | `/api/products/{code}` | Edit Product **and** Update Stock (pen icon - name/category/price and/or a signed stock delta) |
| DELETE | `/api/products/{code}` | Delete Product (soft delete - row + history stay in Postgres) |
| GET | `/api/products?keyword=&category=` | Search Product **and** Display Products |
| GET | `/api/products/{code}/history` | Stock History (eye icon) |

**Orders**
| Method | Endpoint | Feature(s) covered |
|---|---|---|
| POST | `/api/orders` | Customer Orders **and** Premium Orders (`premium: true/false`) |
| GET | `/api/orders?customer=&premium=` | Search/display orders; `meta` reports total/premium/regular counts |

## 6. UI notes

- **Products page**: stat cards, search + category filter, and a table
  where every row has three actions: ✏️ edit (also used to restock),
  👁 stock history, 🗑 delete.
- **Orders page**: stat cards (total / premium / regular / customers),
  a "Place order" form with a Premium toggle + priority level, search
  by customer, and a live queue position per order (premium orders are
  always served before regular ones).

## 7. File layout

```
pom.xml
src/main/java/com/inventory/
 ├── InventoryApplication.java
 ├── common/            ApiResponse, ApiError (the envelope), AppConstants (interface variables)
 ├── entity/             BaseEntity (abstract, inherited by all 3 tables), Product, StockHistory, CustomerOrder, enums
 ├── repository/         Spring Data JPA interfaces (3 files)
 ├── dto/                ProductDtos.java, OrderDtos.java - request/response shapes as nested classes
 ├── exception/          AppExceptions.java (all custom exceptions) + GlobalExceptionHandler
 ├── service/            ProductService/OrderService interfaces + their Impl classes - ALL business logic lives here
 └── controller/         ProductController, OrderController - thin HTTP layer, no business logic
src/main/resources/
 ├── application.properties
 └── static/             index.html (Products), orders.html (Orders), css/, js/
```

The controllers **never** contain business rules - they only convert
HTTP requests into service calls and service results into HTTP
responses. Every `if`, every validation, every collection lives in
`ProductServiceImpl` / `OrderServiceImpl`.

## 8. Where each required Java concept is used

| Concept | Where |
|---|---|
| `ArrayList` | `ProductServiceImpl`/`OrderServiceImpl` build response lists |
| `LinkedList` (as `Queue`) | `OrderServiceImpl.computeQueuePositions()` - regular orders, strict FIFO |
| `HashSet` | `ProductServiceImpl` low-stock/category scans; `OrderServiceImpl` unique customer count |
| `TreeSet` | `ProductServiceImpl.getCatalogMeta()` - alphabetically sorted category list |
| `HashMap` | `OrderServiceImpl` - orders-per-customer counts, queue position lookup |
| `TreeMap` | `ProductServiceImpl.getCatalogMeta()` - stock-by-category, sorted by category name |
| `Queue` | `OrderServiceImpl` - regular order FIFO line |
| `PriorityQueue` | `OrderServiceImpl` - premium orders served by priority level |
| Class / Object / Constructor | every entity and DTO |
| Encapsulation | private fields + getters/setters everywhere; `ApiResponse`'s private constructor |
| Inheritance | `Product`, `StockHistory`, `CustomerOrder` all extend `BaseEntity`; custom exceptions extend `ApiException` extends `RuntimeException` |
| Polymorphism | `ProductController`/`OrderController` depend on the `ProductService`/`OrderService` **interface**, not the impl |
| Abstraction | `BaseEntity` (abstract class), `ProductService`/`OrderService` (interfaces) |
| Interface Variables | `AppConstants` - regex, prefixes, thresholds, all implicitly `public static final` |
| Data Types, Operators | throughout the service layer (int/double/boolean, arithmetic on stock, comparisons for low-stock) |
| If/Else | validation everywhere in the service layer |
| Switch | `GlobalExceptionHandler` (error code → HTTP status), `ProductServiceImpl.updateProduct()` (history note) |
| Loops | `for`-each building response lists, `while` draining the priority/FIFO queues |
| Methods | every public/private method in the service layer |
| Arrays | `TreeSet`/collections serialized as JSON arrays; `String.format("%06d", ...)` for order codes |

## 9. Data persistence

Everything lives in PostgreSQL via Spring Data JPA - nothing is kept
in memory. Deleting a product only flips a `deleted` flag; the row and
its full `stock_history` log stay in the database permanently, exactly
as required.
