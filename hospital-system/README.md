# 🛡️ EverVault | Advanced Health Logistics

Welcome to **EverVault**, a premium Hospital Equipment Management System designed for **Evernorth Health Services**. This project features a modern React frontend and a multi-backend architecture (Java Spring Boot & Node.js).

## 🚀 How to Run (Choose your Method)

### Method 1: The "Everything in One" Java Demo (Recommended for School)
Use this if you have **Java 17+** and **Maven** installed. This runs the backend and the website together on Port 8080.

1.  Open a terminal in the `hospital-system/backend` folder.
2.  Run:
    ```bash
    mvn spring-boot:run
    ```
3.  Open: **[http://localhost:8080/](http://localhost:8080/)**

---

### Method 2: The Developer Setup (Node.js + React)
Use this on your own PC for live updates.

1.  **Terminal 1 (Backend):**
    ```bash
    cd hospital-system/backend-node
    node server.js
    ```
2.  **Terminal 2 (Frontend):**
    ```bash
    cd hospital-system/frontend
    npm run dev
    ```
3.  Open: **[http://localhost:5173/](http://localhost:5173/)** (or the link shown in the terminal)

---

## ✨ Features
- **EverVault Landing Page**: Premium entry point with Evernorth branding.
- **Priority Triage**: Smart queueing (Emergency > Urgent > Normal).
- **Live Tracking**: Automatic equipment status updates (available in demo).
- **Date Validation**: Prevents booking slots in the past.
- **Health Control Center**: Admin view for confirmed machine availability.

## 🧪 Running Tests (Demo / Showcase)

The backend has **unit tests** for the triage and queue logic (priority sorting, mark-as-served, next-slot calculation).

### How to run tests

1. Open a terminal and go to the backend folder:
   ```bash
   cd hospital-system/backend
   ```
2. Run the test suite:
   ```bash
   mvn test
   ```
3. You should see output like `Tests run: 3, Failures: 0, Errors: 0`. All green = all tests passed.

### What is tested
- **Priority sorting** – Emergency patients appear before Normal in the queue.
- **Mark as served** – Booking status becomes SERVED and equipment becomes AVAILABLE.
- **Next slot (empty queue)** – When no one is in queue, next slot shows "Now".

### How to showcase in a demo
1. Say: *"We have automated tests for the core triage logic."*
2. Run `mvn test` in the terminal and show the result.
3. Optionally open `backend/src/test/java/com/hospital/system/service/QueueServiceTest.java` and briefly show the test names (e.g. `testPrioritySorting`, `testMarkAsServed`).

**Requirements:** Java 17+ and Maven. No database or server needs to be running for tests.

---

## 🗄️ How the database is connected (Java backend)

The app uses **MySQL** and **Spring Boot JPA**. The connection is configured once; the rest of the code uses repositories and entities.

1. **Configuration** (`backend/src/main/resources/application.properties`)
   - `spring.datasource.url`, `username`, `password` → tell Spring how to connect to MySQL (`localhost:3306`, database `hospital_system`).
   - Spring creates a **DataSource** and a connection pool from this.

2. **Dependencies** (`backend/pom.xml`)
   - `spring-boot-starter-data-jpa` → enables JPA and repository support.
   - `mysql-connector-j` → JDBC driver so the app can talk to MySQL.

3. **Entities = tables**
   - `Equipment` (`model/Equipment.java`) → mapped to table `equipment` (id, name, type, status, buffer_time).
   - `Booking` (`model/Booking.java`) → mapped to table `booking` (id, patient_name, equipment_id, priority, slot_time, status, booking_time).
   - JPA uses the DataSource above to create/update these tables when the app starts (with `ddl-auto=create-drop` in this demo).

4. **Repositories = data access**
   - `EquipmentRepository` and `BookingRepository` extend `JpaRepository`. Spring implements them and runs SQL using the same DataSource.
   - Methods like `findAll()`, `findById()`, `save()` and custom ones like `findByStatus()` translate into SQL against the database.

5. **Service layer**
   - `QueueService` is injected with `BookingRepository` and `EquipmentRepository`. It never writes SQL; it calls `bookingRepository.save()`, `equipmentRepository.findById()`, etc., and JPA uses the configured connection to hit MySQL.

So: **application.properties** → DataSource → **JPA/Repositories** → **Entities** → MySQL. The connection itself is only defined in `application.properties`; the rest is standard Spring Data JPA usage.

---

## 🛠️ One-Time Setup (New Computer)
If you just downloaded this from GitHub, run these commands once:
- **Frontend**: `cd hospital-system/frontend && npm install`
- **Backend Node**: `cd hospital-system/backend-node && npm install` (if using node)

---
© 2026 Evernorth Health Logistics | Developed for Evernorth Portfolio Demo
