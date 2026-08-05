# Smart Retail Price Monitoring & Consumer Fairness Platform

A production-ready Spring Boot 3.3.5 backend system for crowdsourcing retail prices, AI-based forecasting, and algorithmic consumer fairness scoring.

## Tech Stack
- Java 17
- Spring Boot 3.x
- Spring Security (JWT)
- Spring Data JPA (Hibernate)
- MapStruct (DTO Mapping)
- MySQL
- Swagger/OpenAPI

## Modules Implemented
- **Authentication:** JWT, Role-based Access (Admin, Authority, User, Analyst)
- **Commodity/Market Catalog:** Hierarchical state/district/market tracking
- **Price Reports:** Community crowdsourced prices with admin/authority verification
- **Fairness Engine:** Algorithmic calculation of a Fairness Score (0-100) vs Market Averages
- **Alerting & Notifications:** Target price alerts with automated CRON processing
- **AI Forecasting:** Linear regression-based price prediction for tomorrow, next week, next month
- **Complaints:** Consumer grievance filing and resolution tracking
- **Analytics & Dashboards:** Regional comparisons, price history averages (mean, median, max, min)

## Quick Start
1. Configure MySQL credentials in `src/main/resources/application.properties`.
2. Initial data (Admin user, roles, basic markets, commodities) is automatically seeded from `data.sql`.
    - **Admin:** `admin` / `password`
    - **Authority:** `auth_officer` / `password`
    - **User:** `john_doe` / `password`
3. Run the application: `mvn spring-boot:run`
4. Access Swagger UI for full API documentation:
   `http://localhost:8080/swagger-ui.html`

## Architecture
- Layered Architecture: Controller -> Service -> Repository
- Global Exception Handling with custom Error Responses
- Stateless JWT Authorization

## Authors
- Smart Retail Development Team
