
---

```markdown
# 🌲 Zionex Financial — Cloud-Native Mobile Banking Platform

[![Java 17+](https://img.shields.io/badge/Java-17%20%7C%2021-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/)
[![AWS S3](https://img.shields.io/badge/AWS-S3%20Object%20Storage-orange.svg)](https://aws.amazon.com/s3/)
[![Docker](https://img.shields.io/badge/Docker-Multi--Stage-blue.svg)](https://www.docker.com/)

---

## 📌 Executive Summary & Project Overview
**Zionex Financial** is a secure, cloud-native full-stack mobile banking simulator engineered to demonstrate enterprise-grade software architecture, database optimization, and cloud infrastructure patterns. 

Built as an elective project for WeThinkCode_, the application mimics a real-world commercial banking platform. It features multi-account ledger management (Current vs. Savings accounts), automated time-delayed compound interest engines, custom digital payment cards, secure authentication, and cloud-backed object storage—all structured inside a decoupled, stateless, containerized architecture.

---

## 📐 System Architecture & Data Flow

### 1. High-Level Architecture Diagram
The system follows a classic **3-Tier Cloud-Native Architecture**, ensuring total decoupling between the UI, the application logic, the database storage, and external object storage.

```text
  [ User Browser / Client UI ]
           │
           │  (HTTPS / REST API Requests via Fetch JSON)
           ▼
  [ Presentation & Application Tier ]
  (Spring Boot REST Controller - Stateless Instance)
           │
           ├───► [ Data Tier: AWS RDS PostgreSQL / H2 ] 
           │     (Managed Relational DB via Spring Data JPA / Hibernate)
           │
           └───► [ Object Storage: AWS Amazon S3 ]
                 (Decoupled Binary Storage for Profile Avatars via AWS SDK v2)
```

### 2. Component Breakdown & Responsibilities

* **Presentation Tier (`src/main/resources/static/`)**:
    * `index.html`: Simulated mobile banking wrapper providing responsive screen views (Auth, Overview, Investments, History).
    * `style.css`: Corporate green aesthetic (Nedbank-inspired) complete with a hyper-realistic multi-layered gradient digital debit card.
    * `script.js`: Asynchronous frontend state management, multi-part form serialization, and REST communication.
* **Application Tier (`src/main/java/transection/co.za/`)**:
    * `Main.java`: Spring Boot execution bootstrap.
    * `TransactionController.java`: Stateless REST controller mapping endpoints, capitalization formatting, and financial domain validation rules.
    * `S3Service.java` & `S3Config.java`: Cloud object management utilizing AWS SDK v2 to handle avatar files and issue clean HTTPS URL references.
* **Persistence & Storage Tier**:
    * `TransactionRepository.java`: Data access layer incorporating optimized JPQL aggregate queries (`SUM()`) to eliminate server memory bloat.
    * `Transaction.java`: Core JPA entity mapping ledger entries, timestamps, and account states.

---

## 🚀 Key Engineering & Performance Decisions

* **Elimination of Memory Bloat (`findAll()` Anti-Pattern Fixed):**
  Instead of pulling millions of rows into server RAM and iterating through them via Java loops, account balance calculations are offloaded entirely to the database engine using custom **JPQL Aggregation Queries (`@Query` with `SUM()`)**.
* **Decoupled Cloud Object Storage (AWS S3):**
  User profile avatars are uploaded via multipart form data and offloaded directly to an **Amazon S3 Bucket**. The relational database stores only lightweight HTTPS URL references, keeping tables lightweight and performant.
* **Environment-Driven Configuration (12-Factor App):**
  Database endpoints, credentials, and runtime parameters are fully decoupled from source code, injected dynamically via environment variables (`${DB_URL}`, etc.), ensuring secure portability between local H2 testing and production AWS RDS environments.
* **Multi-Stage Docker Build:**
  The project utilizes a production-grade multi-stage `Dockerfile`. Stage 1 compiles and builds the fat JAR using Maven inside an isolated container, while Stage 2 drops build tools entirely to deliver a lightweight, secure runtime image ready for AWS Elastic Beanstalk.

---

## 💼 Financial Business Logic & Features

* **Secure Authentication & Profile Creation:** Account registration requires a First Name, Surname, Password, Profile Picture, and an **Opening Deposit constraint (Minimum R150.00)**. Names/Surnames auto-normalize to proper Title Case.
* **The Zionex Digital Bank Card:** Renders a widescreen corporate payment card layout featuring an automated cardholder name, gold EMV chip, expiration details, and VISA branding.
* **Multi-Account Ledgers:**
    * *Current Account:* Day-to-day transaction hub for deposits and withdrawals with overdraft protection.
    * *Savings Account:* Automated **8.9% Bi-Monthly Interest Engine** governed by a 60-day maturity time lock.
* **Wealth Growth Hub (Investments):** Locks capital into a 7-day investment plan that automatically computes and triggers a **5% interest maturity payout**.
* **Precise Audit Trail:** Every transaction records an immutable log entry paired with exact timestamp logging.

---

## 📂 Project Structure

```text
BANK-Transactions/
├── src/
│   ├── main/
│   │   ├── java/za/co/zionex/banking/
│   │   │   ├── Main.java                 # Spring Boot Entry Point
│   │   │   ├── Transaction.java          # JPA Database Entity Model
│   │   │   ├── TransactionRepository.java# JPQL Aggregation Queries & Data Access
│   │   │   ├── TransactionController.java# REST API Endpoints & Business Logic
│   │   │   ├── S3Config.java             # AWS S3 Client Configuration Bean
│   │   │   └── S3Service.java            # AWS S3 Multipart Object Storage Service
│   │   │
│   │   └── resources/
│   │       ├── application.properties    # Environment-based configuration bindings
│   │       └── static/                   # Frontend assets
│   │           ├── index.html            # Mobile banking layout UI
│   │           ├── style.css             # Corporate styling & digital card styles
│   │           ├── script.js             # Asynchronous API integration logic
│   │           └── logo.png              # Zionex Brand Asset
│   │
├── Dockerfile                            # Multi-Stage Production Container Specification
├── pom.xml                               # Maven project descriptor & dependencies
└── README.md                             # Architecture & system documentation
```

---

## ⚙️ Local Setup & Execution Guide

### Option 1: Run Locally via Maven (Fastest Development Mode)
*Prerequisites: Java 17+ installed.*

1. Clone or open the repository in your terminal.
2. Run the application using Maven:
   ```bash
   mvn clean spring-boot:run
   ```
3. Open your browser and navigate to: **`http://localhost:8080`** *(Uses an embedded H2 memory database by default).*


---

### Option 1: Run Locally via Maven (Fastest Development Mode)
*Prerequisites: Java 17+ installed.*

1. Clone or open the repository in your terminal.
2. Run the application using Maven:
   ```bash
   mvn clean spring-boot:run
   ```

3. Open your browser and navigate to: **`http://127.0.0.1:8080`** *(Uses an embedded H2 memory database by default).*

---

### 🧪 Testing the Backend API via Terminal (cURL)
### Option 2:
While your application is running (`mvn spring-boot:run`), you can test the underlying REST API endpoints directly from a second terminal window using `curl`:

1. **Register a New Account via API:**
   ```bash
   curl -X POST "http://localhost:8080/api/transactions/register?username=Zanele&surname=Dlamini&password=secret123&initialDeposit=200"
   ```

2. **Log In via API:**
   ```bash
   curl -X POST "http://localhost:8080/api/transactions/login?username=Zanele&password=secret123"
   ```

3. **Check Current Account Balance:**
   ```bash
   curl -X GET "http://localhost:8080/api/transactions/balance/current?username=Zanele"
   ```

4. **Make a Current Account Deposit:**
   ```bash
   curl -X POST "http://localhost:8080/api/transactions/current/income?username=Zanele&amount=500"
   ```

5. **View Transaction History:**
   ```bash
   curl -X GET "http://localhost:8080/api/transactions/history?username=Zanele"
   ```

---

### Option 3: Run via Docker (Containerized Local Mode)
*Prerequisites: Docker Desktop installed and running.*

1. Build the multi-stage Docker container image:
   ```bash
   docker build -t zionex-finance-app .
   ```
2. Run the container and map port 8080:
   ```bash
   docker run -p 8080:8080 \
   -e AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY \
   -e AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_KEY \
   -e AWS_REGION=us-east-1 \
   -e BUCKET_NAME=your-bucket-name \
   zionex-finance-app
   ```
3. Access the app at **`http://127.0.0.1:8080`**.
4. Or find your WSL/PC IP address by running this quick command in your Ubuntu terminal:
   code
   ```Bash
   hostname -I
   ```
```

WTC-Z2R5GGJK