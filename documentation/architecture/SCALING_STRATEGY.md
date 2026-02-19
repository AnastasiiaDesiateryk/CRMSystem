# Scaling Strategy

## Current Scaling Posture

The backend is designed to scale horizontally:

* Stateless JWT authentication
* No server-side sessions
* External database dependency (PostgreSQL)

## Horizontal Scaling

Multiple backend instances can run behind a load balancer:

* Requests authenticated via JWT on every instance
* No sticky sessions required
* Key material shared via mounted keys or secret manager

## Database Considerations

* PostgreSQL is the single stateful component
* Connection pooling via HikariCP
* Scaling options:

  * Vertical scaling
  * Read replicas (future)
  * Query optimization and indexing

## Concurrency and Consistency

* Optimistic locking prevents lost updates
* ETag/If-Match avoids centralized lock services

## Recommended Production Add-ons (Future)

* Reverse proxy with TLS termination
* Rate limiting at the edge
* Centralized logging and metrics

---
