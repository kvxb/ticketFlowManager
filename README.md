# Ticket Management System (JIRA)

## Overview

This Ticket Management System is a comprehensive Java application that simulates the lifecycle of software development tasks. It manages users (Developers, Managers, Reporters), tickets (Bugs, Feature Requests, UI Feedback), and milestones through a sophisticated workflow. The system handles complex operations including ticket assignment, status changes, commenting, analytics generation, and advanced search functionality.

## Features

    User Management: Role-based access control for Managers, Developers, and Reporters, each with distinct permissions and capabilities.

    Ticket Lifecycle: Support for creating, assigning, resolving, and closing tickets with proper state management.

    Milestone System: A milestone system that groups tickets into time-bound milestones with dependency management (blocking/unblocking logic).

    Advanced Search: A flexible search system that filters tickets and developers based on complex criteria including priority, date ranges, keywords, and expertise areas.

    Analytics Engine: A comprehensive analytics module that generates reports for App Stability, Customer Impact, Resolution Efficiency, and Developer Performance.

## Design Patterns Implemented

The application employs several Design Patterns to maintain modularity and manage complexity effectively:

### Singleton Pattern

Used in the Database class to ensure a single, centralized source of truth for the application's state (users, tickets, milestones) throughout the runtime, preventing data inconsistency and simplifying state management.

### Factory Pattern

Implemented in TicketFactory to encapsulate the logic for instantiating different types of tickets (Bug, FeatureRequest, UIFeedback). This decouples client code from specific class implementations and makes adding new ticket types straightforward.

### Chain of Responsibility

Extensively used in the validation modules (DeveloperValidationHandler, CommentValidationHandler). This allows assignment or commenting requests to pass through a chain of validators (checking seniority, status, milestone locking). If any validator fails, the request is rejected immediately, creating a clean and extensible validation pipeline.

### Strategy Pattern

Applied in the SearchService. Each individual filter (like CreatedAfterFilter, ExpertiseAreaFilter) implements a common interface, allowing the search engine to dynamically apply different filtering strategies at runtime without massive conditional blocks, making new search criteria easy to add.

### Observer Pattern

Implemented between Milestone (Subject) and Developer (Observer). Developers are automatically notified when milestones are created, become due, or are unblocked, ensuring timely awareness of important events.

### Builder Pattern

Used for constructing complex Ticket objects and their subclasses, providing a clear and fluent API for setting numerous attributes while maintaining immutability where appropriate.

## Development Decisions

The development process carefully balanced modularity with development speed. While initial attempts focused on refactoring large core classes (Database and IOUtil) into smaller specialized components, it became apparent that excessive fragmentation decreased speed of navigation.

Key Decision: Related logic was kept localized within larger, cohesive files. This structure improved development speed by enabling rapid keyword searching and method navigation within single contexts, prioritizing maintainability through searchability over strict class-size limits while maintaining clean separation of concerns through design patterns.

## Project Structure

src/main/java
├── database/       # Singleton database instance
├── io/             # Input/Output handling and JSON parsing
├── main/           # Application entry point and simulation loops
├── mathutils/      # Mathematical utility functions
├── milestones/     # Milestone logic
├── notifications/  # Observer pattern interfaces
├── search/         # Search service and filtering strategies
├── services/       # Analytics service
├── tickets/        # Ticket entities and Factory pattern
├── users/          # User entities
└── validation/     # Chain of Responsibility validators

## Data Formats

The system expects JSON input containing:

    Users: A list of developers, managers, and reporters with their attributes

    Commands: A chronological list of actions to perform (e.g., reportTicket, assignTicket, createMilestone)

The system generates a JSON output file containing the results of commands, reports, and error messages, following the exact specifications provided in the assignment requirements.
