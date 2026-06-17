# SkyBooker - Flight Booking System

SkyBooker is a comprehensive, console-based Java application that simulates a real-world airline reservation and management system. It is designed from the ground up to demonstrate advanced Object-Oriented Analysis and Design (OOAD) principles, strict adherence to SOLID principles, and practical implementations of Software Design Patterns.

## Key Features

- **Flight Search & Filtering**: Users can search for one-way and round-trip flights between destinations, viewing results sorted by price or schedule.
- **Dynamic Pricing Engine**: Applies age-based discounts (Child, Infant), dynamic baggage fees, premium seat surcharges, and express booking fees.
- **Interactive Seat Selection**: A visual console-based seat map that tracks availability and locks seats in real-time to prevent double-booking.
- **Payment Processing**: Integrates a flexible payment strategy supporting Credit/Debit Cards, UPI, and EMI transactions.
- **Check-In & Boarding Passes**: Manages check-in windows and generates formatted boarding passes with split date, time, and gate tracking.
- **Automated Notifications**: Simulated delivery of SMS and Email notifications for booking confirmations, flight delays, and refund lifecycles.
- **Admin Dashboard**: Specialized tools for system administrators to manage flights, dynamically update prices, assign crew members, and simulate flight statuses.

## Architectural Design Patterns

SkyBooker leverages multiple design patterns to ensure a scalable, maintainable, and loosely-coupled codebase:

1. **Singleton Pattern**: Utilized across all Manager classes (e.g., `BookingManager`, `FlightManager`, `NotificationManager`) to ensure centralized, thread-safe access to system resources and in-memory data stores.
2. **State Pattern**: Manages the lifecycle of a `Booking`. Transitions elegantly through `INITIATED`, `SEAT_SELECTED`, `PAYMENT_PENDING`, `CONFIRMED`, `CHECKED_IN`, and `CANCELLED` states without complex conditional logic.
3. **Strategy Pattern**: Powers the `Payable` interface. Allows the system to seamlessly switch between different payment algorithms (`CreditCardPayment`, `UPIPayment`, `EMIPayment`) at runtime.
4. **Builder Pattern**: Used in the `Flight.Builder` class to cleanly construct complex flight objects with optional parameters like baggage rules, amenities, and aircraft types.
5. **Filter/Criteria Pattern**: Implemented in the `FlightFilterService` to chain together search parameters (e.g., `PriceCriteria`, `AirlineCriteria`), allowing users to dynamically filter search results.
6. **Factory Method / Strategy**: Utilized for generating different types of dynamic discounts and tax applications within the `FareCalculatorService`.

## Project Structure

The codebase is organized into highly cohesive packages representing discrete layers of functionality:

```
skybooker-app/com/airline/skybooker/
├── Main.java                 # Application entry point
├── constants/                # Global configuration and string constants
├── enums/                    # System enumerations (Role, TripType, FlightStatus)
├── exception/                # Custom domain-specific exceptions
├── filters/                  # Logic for the Criteria Pattern (Search filters)
├── interfaces/               # Shared system contracts (e.g., Payable)
├── managers/                 # Singletons managing state and business aggregation
├── models/                   # Core domain entities (User, Flight, Booking)
├── notifications/            # Outbound communication channels (Email, SMS)
├── payments/                 # Strategy implementations for checkout
├── services/                 # Pure business logic (Fare calculations, Seat locking)
├── states/                   # Booking state implementations (State Pattern)
├── ui/                       # Console-based user interaction wizards
└── utils/                    # Shared helper methods (Input validation)
```

## Setup and Installation

### Prerequisites
- Java Development വ്യവഹാരങ്ങൾ Development Kit (JDK) 8 or higher
- A compatible IDE (IntelliJ IDEA, Eclipse, VS Code) or command-line terminal

### Execution
1. Clone the repository to your local machine.
2. Navigate to the root directory of the application:
   `cd Skybooker-OOAD/skybooker-app`
3. Compile the Java files:
   `javac -cp . com/airline/skybooker/Main.java`
4. Run the application:
   `java -cp . com.airline.skybooker.Main`

## Usage Instructions

Upon launching the application, you will be prompted to either log in or proceed as a guest. The system provides two primary user roles:

- **Admin (`admin@skybooker.com` / `admin123`)**: Access the administration dashboard to add flights, manage airports, trigger delays, and view system metrics.
- **Passenger (`testuser@example.com` / `password123`)**: Access the customer dashboard to search for flights, complete bookings, select seats, check in, and view travel itineraries.

## Technology Stack

- **Language**: Java
- **UI**: Console / Standard I/O
- **Architecture**: In-Memory Data Storage (No external database required)
- **Design Philosophy**: Object-Oriented Analysis and Design (OOAD), SOLID

---
*Disclaimer: SkyBooker is an mock flight project. It does not interface with actual flight databases or process real financial transactions.*
