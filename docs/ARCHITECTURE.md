# Architecture

This document provides a high‑level overview of the Clinic Appointment
System architecture.

The application follows a layered structure with controllers
exposing REST APIs, service classes encapsulating business logic,
repositories abstracting persistence and entity models representing
domain data. Configuration classes set up the datasource, JPA and
other infrastructure components. See `src/main/java/com/clinic` for
details.