# EverVault | Project Presentation (PPT Outline)

Use this outline to build your PowerPoint. Add your own screenshots where indicated.

---

## Slide 1: Title
- **Title:** EverVault | Advanced Health Logistics
- **Subtitle:** Hospital Equipment Management System
- **By:** Evernorth Health Services
- **Screenshot:** [Add screenshot of Landing page – hero + “Go to Patient Portal” / “Admin Access”]

---

## Slide 2: Agenda
- Project overview
- High-level design
- Data flow
- Tech stack
- Key features & screenshots
- Demo / Testing
- Conclusion

---

## Slide 3: Project Overview
- **What:** Web app for managing hospital equipment (MRI, CT, Ventilator) and patient booking queues.
- **Who:** Designed for Evernorth Health Services.
- **Goals:**
  - Priority triage (Emergency > Urgent > Normal)
  - Live equipment status (Available / In Use / Maintenance)
  - Admin control to confirm bookings and call next patient
- **Screenshot:** [Add screenshot of Patient Portal – equipment cards + booking form]

---

## Slide 4: High-Level Design

**Copy the diagram from `diagrams/high-level-design.png` (generate from Mermaid below) or draw in PPT:**

```
┌─────────────────────────────────────────────────────────────────┐
│                        BROWSER (User)                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  React Frontend (Vite)                                     │   │
│  │  • LandingView  • PatientView  • AdminView  • NavBar      │   │
│  └────────────────────────────┬───────────────────────────────┘   │
└───────────────────────────────┼───────────────────────────────────┘
                                │ HTTP (REST API)
                                ▼
┌───────────────────────────────────────────────────────────────────┐
│  Spring Boot Backend (Port 8080)                                    │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────────────────────┐ │
│  │ Controller  │  │ QueueService│  │ Repositories (JPA)          │ │
│  │ /api/*      │──│ (business   │──│ EquipmentRepository         │ │
│  │             │  │  logic)     │  │ BookingRepository           │ │
│  └─────────────┘  └──────┬─────┘  └──────────────┬─────────────┘ │
└──────────────────────────┼────────────────────────┼───────────────┘
                           │                        │
                           ▼                        ▼
                    ┌──────────────┐         ┌──────────────┐
                    │   MySQL      │         │   Static     │
                    │ hospital_    │         │   (built     │
                    │ system DB    │         │   React UI)  │
                    └──────────────┘         └──────────────┘
```

**Mermaid code (paste at https://mermaid.live to export as image):**

```mermaid
flowchart TB
    subgraph Browser["Browser"]
        UI[React Frontend - Landing, Patient, Admin]
    end
    subgraph Backend["Spring Boot :8080"]
        Controller[HospitalController /api/*]
        Service[QueueService]
        Repo[EquipmentRepository, BookingRepository]
        Controller --> Service --> Repo
    end
    DB[(MySQL hospital_system)]
    UI -->|REST| Controller
    Repo --> DB
```

---

## Slide 5: Data Flow Diagram

**Screenshot:** [Use diagram from `diagrams/data-flow.png` or Mermaid below]

**Mermaid code (paste at https://mermaid.live to export as image):**

```mermaid
sequenceDiagram
    participant U as User (Browser)
    participant F as React Frontend
    participant C as HospitalController
    participant Q as QueueService
    participant R as Repository
    participant DB as MySQL

    U->>F: Open Patient Portal / Select equipment
    F->>C: GET /api/equipment
    C->>Q: get queue, next slot
    Q->>R: findAll(), findByEquipmentIdAndStatus()
    R->>DB: SELECT
    DB-->>R: rows
    R-->>Q: list
    Q-->>C: enriched equipment
    C-->>F: JSON
    F-->>U: Show equipment + queue

    U->>F: Submit booking
    F->>C: POST /api/bookings
    C->>Q: createBookingRequest()
    Q->>R: save(booking)
    R->>DB: INSERT
    DB-->>R: ok
    R-->>Q: booking
    Q-->>C: booking
    C-->>F: 201 + booking
    F-->>U: Success message

    U->>F: Admin: Confirm / Call Next
    F->>C: POST /api/bookings/:id/confirm or POST /api/queue/:id/next
    C->>Q: confirmBooking() or callNext()
    Q->>R: save()
    R->>DB: UPDATE
    DB-->>R: ok
    R-->>Q: entity
    Q-->>C: result
    C-->>F: JSON
    F-->>U: UI updates
```

---

## Slide 6: Tech Stack
| Layer      | Technology              |
|-----------|--------------------------|
| Frontend  | React, Vite, CSS        |
| Backend   | Java 17, Spring Boot 3.2|
| API       | REST (JSON)             |
| Database  | MySQL, JPA / Hibernate  |
| Build     | Maven, npm              |

- **Run:** Single command – `mvn spring-boot:run` (frontend built into backend static).

---

## Slide 7: Key Features – Screenshots
- **Landing:** [Screenshot – hero + two buttons]
- **Patient Portal:** [Screenshot – equipment cards + booking form]
- **Admin Control:** [Screenshot – machine cards + Triage Queue + Live Operations + “Call Next Patient”]
- **Priority Triage:** Emergency > Urgent > Normal; equipment status (Available / In Use / Maintenance).

---

## Slide 8: Testing
- **Unit tests:** QueueService (priority sort, mark as served, next slot).
- **Controller tests:** GET /api/equipment, POST /api/bookings.
- **Run:** `mvn test`
- **Screenshot:** [Terminal showing “Tests run: 5, Failures: 0” or similar]

---

## Slide 9: Conclusion
- EverVault provides a simple, end-to-end flow: patient booking → admin triage → queue and equipment status.
- Single deploy: Spring Boot on port 8080 (React built into static).
- Database: MySQL; optional unit and controller tests for reliability.

---

## How to Get Diagram Images for PPT

1. **Mermaid diagrams:** Go to https://mermaid.live and paste the Mermaid code from Slide 4 or 5. Export as PNG and insert into PowerPoint.
2. **Screenshots:** Run the app (`mvn spring-boot:run` in `hospital-system/backend`), open http://localhost:8080, and capture:
   - Landing page
   - Patient Portal (with equipment and form)
   - Admin Control (with machines and queues)
